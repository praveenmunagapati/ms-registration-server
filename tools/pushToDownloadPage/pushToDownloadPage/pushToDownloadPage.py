#
# Copyright (c) 2011-2019 LabKey Corporation
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#!/usr/bin/python

"""
pushToDownloadPage.py

NOTE: This script now requires Python 3.

An application which will push new binary distribution files to the Download pages for each customer
and the general public.

The application will be used by LabKey staff to push binary distribution files to each customer's download
page and to the download locations on labkey.org and labkey.com. The application can push 3 types of download files
    - monthly: Distribution files created at the end of each monthly build (formerly sprint/alpha).  These bits will be used by our customers
        to test new features currently under development
    - snapshot: Distribution files created during stabilization of the release (formerly beta). Newer updates to the release build will
        be checked into this branch before eventually being merged into the Release branch.
    - release: Distribution files from current stable branch

Each customer who either has support contract or has customer installers created for them on TeamCity, will be given
a Customer Download Page on labkey.org.  This page will live in the Customer's Project on labkey.org. When this
script executed, it will push the distribution files for each customer to S3, update the text on the Customer's
download page and post a message to the Customer's download page message board.

If you do not specify a Customer on the command line, then the script will update the Customer Download pages for all
Customer's and the General Public's download pages on labkey.org and labkey.com


How to use the script:
=========================
pushToDownloadPage.py [-c customer name] [-b build id] [-m "build message"] update-type

    where
        -b, --buildid: Build Id from TeamCity. If not specified we will push the lastSuccessful installers
        -d, --do-not-download: Skip the download of the client API artifacts
            from teamcity and use the previously downloaded artifacts.
        -c, --customer: Customer name. If not specified we will push to all customers
        update-type: This can be one of 4 options.
            - monthly: Distribution files created at the end of each monthly build (formerly sprint/alpha). These bits will be used
                by our customers to test new features currently under development
            - snapshot: Distribution files created during stabilization of the release and features not merged into the Release
                branch yet (formerly beta).
            - release: Distribution files from current stable branch
            - trunk: Distribution files from the trunk
        -e, --environment: Environment to use for configuration (see .cfg file for settings). Default is prod
        -m, --message: Message to add to wikis and customer message board when pushing this build
                (This message will appear above the downloadable files in bold text and be preceded with 'Note:')
                Actual message should be in quotes.
        -x, --safe-mode: Don't talk to S3, or write to message/wiki for a client, or write to previous build list
        -n, --no-aws: Don't talk to S3


The list of customers with their own download page is located in the "Customers" list in https://www.labkey.org/list/_lkops/begin.view
folder. The version numbers for release and snapshot is located in the "Releases" list in https://www.labkey.org/list/_lkops/begin.view
folder. This information is read to build the TeamCity URL for downloading the installers/dist files.


A spec for this application is available at https://docs.google.com/a/labkey.com/document/d/1uX09sh4tbCjOBZcuEaNNfWy8h90a7mvSSA-eBbnRk1E/edit?hl=en_US

Written by B.Connolly
LabKey
Last Modified: Feb 2020 by J.Yoon

"""


import os
import glob
import shutil
import datetime
import time
import sys
import optparse
import urllib.request, urllib.error, urllib.parse
import requests
import boto3, botocore  # must be installed
import labkey  # must be installed, obviously
from labkey.utils import create_server_context
import labkey.unsupported.wiki, labkey.unsupported.messageboard
from xml.etree import ElementTree
import base64
import configparser
import re

"""
######################################################################
######################################################################
Functions
######################################################################
"""
def _create_content_from_template(file_name, mdict):
    """Function will take in the path to a template file and a dictionary.
        It will read the file into a variable and then process the template
        using the provided dictionary.

    Variables:
        file_name = path to template file to be processed
        mdict = dictionary of variables
    """
    try:
        f = open(file_name, 'r')
    except IOError as e:
        print("({0})".format(e))
        print("Template file, " + file_name + ", is not available. Error message is below")
        #_print_log(logObject,"Template file, " + file_name + ", is not available. The error message is ({0})".format(e))
        return(0)
    mbody = f.read()
    f.close()

    # Process the template dictionary passed to function
    mbody = mbody % mdict
    return(mbody)


def _print_log(fo,string):
    """Function will write the 'string' to the 'fo' file object.
        It will prepend the string with the date/time string similar to
        that seen in apache access logs. It will then write the string
        to the file object specified by 'fo'

    Variables:
        fo = file object to output the log to
        string = string to write to the file object
    """
    # Create date string
    dStr=str(datetime.datetime.now())
    logStr=dStr + " " + string +"\n"
    fo.write(logStr)
    return None


def _checkUrl(myurl):
    """This function will connect to url to check that it exists

    Variables:
        myurl = the url to be tested
    """
    # print myurl
    try:
        f = urllib.request.urlopen(myurl)
    except urllib.error.HTTPError as e:
        print(e.getcode())
        if e.getcode() == 404:
            return(0)

    f.close()
    return(1)


def _check_file_existence_s3(s3_connection, bucket_name, s3_key_name):
    if (not options.safemode) and (not options.noaws):
        try:
            s3_connection.Object(bucket_name, s3_key_name).load()
        except botocore.exceptions.ClientError as e:
            if e.response['Error']['Code'] == "404":
                return(False)
            else:
                raise
        return(True)


def _download_from_teamcity(teamcity_path, download_directory, use_cred):
    # Determine if teamcity_path contains any "/". If so, then we will need to create
    # subdirectories in download_directory
    if len(teamcity_path.split("/")) > 1:
        # Create required directories
        teamcity_path_directory = teamcity_path.rpartition("/")[0]
        try:
            if not os.path.exists(download_directory + "/" + teamcity_path_directory):
                os.makedirs(download_directory + "/" + teamcity_path_directory)
            elif not os.path.isdir(download_directory + "/" + teamcity_path_directory):
                print("Attempting to create directory " + download_directory + "/" + \
                      teamcity_path_directory + " but a file with the same name already exists")
                print("Program is exiting.")
                sys.exit(1)
        except IOError as e:
            print("There was an error creating the directory," + download_directory + "/" + \
                  teamcity_path_directory + ".")
            print("({0})".format(e))
            sys.exit(1)
    # Check for credential file (which contains login and password for accessing
    # the TeamCity server) in either "TEAMCITY_CREDENTIALS" environment variable
    # or in the file .teamcitycredentials.txt in your home directory
    #
    # If the credential file does not exist, then try and use the guest auth.
    #
    if use_cred:
        tc_url = 'https://teamcity.labkey.org/httpAuth/repository/download/' + \
                 buildType + '/' + buildId + '/' + teamcity_path
    else:
        tc_url = 'https://teamcity.labkey.org/guestAuth/repository/download/' + \
                 buildType + '/' + buildId + '/' + teamcity_path

    print("Download " + artifact.attrib['name'] + '.' + artifact.attrib['ext'] + \
          " from " + tc_url)
    print(" -- Start-time: " + str(datetime.datetime.now()))
    chunk = 4096
    try:
        # Changed to use Requests here because our old handwritten Authorization header + urlopen was making S3 cranky
        resp = requests.get(tc_url, auth=requests.auth.HTTPBasicAuth(tc_username, tc_password))
        # Write the contents of the downloaded file to a local file
        local_file = open(download_directory + "/" + teamcity_path, "wb")
        local_file.write(resp.content)  # file writing is different with Requests
        lmsg = " -- Completed at: " + str(datetime.datetime.now()) + "\n"
        print(lmsg)
        local_file.close()
    except urllib.error.HTTPError as e:
        print("There was problem downloading the files from TeamCity. " \
              "The HTTP response code was " + str(e.getcode()))
        print("The error message is " + format(e))
        sys.exit(1)
    except urllib.error.URLError as e:
        print("There was problem connecting to the TeamCity Servers. ")
        print("The error message is " + e.reason)
        sys.exit(1)
    return(download_directory + "/" + teamcity_path)

"""
######################################################################
End Functions
######################################################################
"""


"""
######################################################################
Variables
######################################################################
"""
#
# Define Variables
#

exePath = os.path.abspath( __file__ )
exeDirectory = os.path.dirname(os.path.abspath( __file__ ))
cfgFile = exeDirectory + "/pushToDownloadPage.cfg"
logFile = exeDirectory + "/pushToDownloadPage.log"
currDate = str(datetime.datetime.now())
startDirectory = os.getcwd()
#TODO get rid of labkey.credentials file, probably no longer needed

"""
######################################################################
End Variables
######################################################################
"""
#
# Open Log File
#
#try:
#    logObject = open(logFile, mode='a')
#except IOError as e:
#    print "Unable to write to log file, " + logFile + ". The error message is " + "({0})".format(e))
#    print sys.argv[0] + "is stopping. Fix the problem of writing to the log file, " + logFile + " and run the program again"
#    sys.exit(1)

#
# Read command line options
#
usage = "usage: %prog [options] update-type"
cl=optparse.OptionParser(usage=usage)
cl.add_option('--customer','-c',action='store',
              help="Customer's download page to updated. If not specified build will be pushed" + \
                   "to all customers.", type="string", dest="customer")
cl.add_option('--buildid','-b',action='store',
              help="The build id from TeamCity for the release that you want to publish" + \
                   "if not specified will download lastSuccessful installer build.",
              type="string", dest="buildid")
cl.add_option('--do-not-download','-d',action='store_false', dest="download",
              help="Do not download client API from teamcity. Use previously downloaded bits")
cl.add_option('--safe-mode','-x', action='store_true',
              help="Do not talk to S3, or write messages to wiki/message, or write to previous build list. " +
                   "The build content link that would have been produced will also be written to the console.", dest="safemode")
cl.add_option('--no-aws','-n', action='store_true',
              help="Do not talk to S3.", dest="noaws")
cl.add_option('--environment','-e', action='store',
              help="Environment to use for configuration (see .cfg file for settings). Default is prod", dest="env")
cl.add_option('--message','-m', action='store',
              help="Message to add to wikis and customer message board when pushing this build. " +
                   "(This message will appear above the downloadable files in bold text and be preceded with 'Note:'). " +
                   "Actual message should be in quotes.", dest="buildmessage")

(options, args) = cl.parse_args()
if options.customer:
    customerName = options.customer
else:
    customerName = 'all'
if options.buildid:
    buildId = options.buildid + ":id"
else:
    buildId = '.lastSuccessful'
if len(args) != 1:
    cl.error("You must specify the type of update to be made. The supported options are " + \
             "trunk, monthly, snapshot, or release \n")
else:
    updateType = args[0].lower()
if not options.env:
    options.env = 'prod'

config = configparser.ConfigParser()
config.read(cfgFile)
configEnv = config[options.env]
if not configEnv:
    cl.error("Specified environment '" + options.env + "' not found in config file. \n")
labkeyOrgServer = configEnv['LabkeyServerUrl']
contextPath = configEnv['ContextPath']
if contextPath:
    slashContextPath = '/' + contextPath
else:
    slashContextPath = ''
buildInfoRoot = configEnv['BuildInfoRoot']
if buildInfoRoot == '/':
    buildInfoRoot = ''
opsDirPath = configEnv['OpsDirPath']
issuesListPath = configEnv['IssuesListPath']
cacheDirectory = exeDirectory + configEnv['CacheDirectoryPath']
useSsl = (configEnv['UseSsl'] == "True")
if useSsl:
    labkeyOrgUrl = 'https://' + labkeyOrgServer
else:
    labkeyOrgUrl = 'http://' + labkeyOrgServer
s3BucketName = configEnv['S3Bucket']
s3Url = 'http://' + s3BucketName + '.s3.amazonaws.com'
s3CredentialFile = exeDirectory + configEnv['S3CredentialFilePath']

#
# Print Start up message
#
print("")
print(sys.argv[0] + " is starting (" + currDate +")")
#_print_log(logObject, sys.argv[0] + " is starting")

#
# Determine the type of update to be made.  This will be specified as
# an argument on the command line
#
updateTypes = ['monthly', 'snapshot', 'release', 'trunk']
if updateType not in updateTypes:
    print("You must specify a supported type of update to be made. \n\
          The supported options are trunk, monthly, snapshot, or release")
    sys.exit(1)

#
# Open and read the S3 credential file. Load the credentials into ENV
# variables for use later
#

if (not options.safemode) and (not options.noaws):
    try:
        f = open(s3CredentialFile, mode='r')
        creds = f.read().splitlines()
        # Remove any items from the list start with #
        f.close()
        for e in creds:
            if 'AWSAccessKeyId' in e:
                AWSAccessKeyId = e.split('=')[1]
            elif 'AWSSecretKey' in e:
                AWSSecretKey = e.split('=')[1]
    except IOError as e:
        print("({0})".format(e))
        print("S3 Credential file, " + s3CredentialFile + ", is not available. ")

    # Set env variables for Access and Secret Keys
    os.environ['AWS_ACCESS_KEY_ID'] = AWSAccessKeyId
    os.environ['AWS_SECRET_ACCESS_KEY'] = AWSSecretKey
    #print(os.getenv('AWS_ACCESS_KEY_ID'), os.getenv('AWS_SECRET_ACCESS_KEY'))
#
# Open and read the TeamCity credential file (which contains the login and
# password for accessing the TeamCity Server).
# Check for credential file in either "TEAMCITY_CREDENTIALS" environment
# variable or in the file .teamcitycredentials.txt in your home directory
#
# If the credential file does not exist, then try and use the guest auth.
#
try:
    tc_cred_file_name = os.environ["TEAMCITY_CREDENTIALS"]
except KeyError:
    tc_cred_file_name = os.environ["HOME"] + '/.teamcitycredentials.txt'
if os.path.exists(tc_cred_file_name):
    try:
        f = open(tc_cred_file_name, 'r')
        tc_machine = f.readline().strip().split(' ')[1]
        tc_username = f.readline().strip().split(' ')[1]
        tc_password = f.readline().strip().split(' ')[1]
        f.close()
    except IOError as e:
        print("({0})".format(e))
        print("There was a problem reading the TeamCity credential file, " + tc_cred_file_name)
        print("Fix the problem above and try the program again. ")
        print("Program is exiting.")
        sys.exit(1)
    tc_use_cred = True
else:
    tc_use_cred = False


#
# Read list of available customer pages from a list in labkey.org.
# If a customer name was specified on the command line then only
# update the download page for that customer. If no customer name is
# specified, then update all customer pages
#
# Get the list of customers
containerPath = buildInfoRoot + opsDirPath
server_context = create_server_context(labkeyOrgServer, containerPath, context_path=contextPath, use_ssl=useSsl)
schema = 'lists'
table = 'Customers'
ct = labkey.query.select_rows(server_context, schema, table)
customerList = ct['rows']
# Get the list of distributions
schema = 'lists'
table = 'distributions'
ct = labkey.query.select_rows(server_context, schema, table)
distributions = ct['rows']

#
# If a customer has been specified on the command line, check if the
# customer exists. If the customer does not exist, print a list of
# available customers and exit.
#
# If the customerName = 'all' then the user did not specify a customer
# on the command line. In that case, warn the user that they will be
# uploading builds to all customers.
#
if customerName != 'all':
    found = 0
    for c in range(len(customerList)):
        cust = customerList[c]['name']
        #custCheckList.append(cust.rstrip().lstrip().lower())
        if customerName.rstrip().lstrip().lower() == str(cust).rstrip().lstrip().lower():
            customerList = [customerList[c]]
            found = 1
            break
    if not found:
        print("The customer specified on the command line, " + customerName + \
              ", was not found in the list of available customer pages.")
        print("Current customers with Download Pages are: ")
        for c in range(len(customerList)):
            print(" -- " + customerList[c]['name'])
        sys.exit(1)
else:
    print("You have not specified a customer name on the command line.")
    print("This means you will push the build to these customers: ")
    for c in range(len(customerList)):
        print(" -- " + customerList[c]['name'])
    print("")
    try:
        s = input('--> Hit [Enter] key to continue or CTRL+C to quit')
    except KeyboardInterrupt as e:
        # Catch CTRL+C and do not print any error message, but cancel program.
        print("n")
        sys.exit(0)
#
# Read the list of available versions that can be posted
# to the download pages. This list contains information required
# to download the installer/binary bits and create the wiki
# and message board messages.
#
#server_context should already be defined earlier
schema = 'lists'
table = 'Releases'
rl = labkey.query.select_rows(server_context, schema, table)
releaseList = rl['rows']

# Store required information about the release to be pushed
found = 0
for i in releaseList:
    if updateType == str(i['version']):
        version = i['version']
        buildType =  i['buildType']
        versionNum = i['versionNum']
        found = 1
        break
if not found:
    print("The update type specified on the command line, " + updateType + \
          ", was not found in the list of available releases. ")
    print("See https://www.labkey.org/list/_lkops/grid.view?listId=538.")
    print("Current releases in list are: ")
    for c in range(len(releaseList)):
        print(" -- " + releaseList[c]['version'])
    sys.exit(1)


#
# Clean up any downloads from previous runs
#
if not os.path.exists(cacheDirectory):
    try:
        os.makedirs(cacheDirectory)
        os.chdir(cacheDirectory)
    except IOError as e:
        print("({0})".format(e))
        print("Unable to create the directory, " + cacheDirectory + ".")
else:
    if options.download == None:
        lmsg = "Clean up all files in " + cacheDirectory + " from old runs"
        print("\n" + lmsg) #; _print_log(logObject, lmsg)
        # Delete any files from the last run.
        os.chdir(cacheDirectory)
        fList = glob.glob('*')
        if len(fList) > 0:
            for l in fList:
                if os.path.isfile(l):
                    try:
                        os.remove(l)
                    except IOError as e:
                        print("({0})".format(e))
                    x=1
                else:
                    try:
                        shutil.rmtree(l)
                    except IOError as e:
                        print("({0})".format(e))

#
# Prior to the 12.3 release, we were able to download all the
# artifacts from the installer build as a single zip file. This zip file is
# now greater than 4GB and cannot be unzipped by Python.
#
# Now we will find the list of artifacts for the installer build and then
# just download the artifacts that we need.
#

if tc_use_cred:
    tc_artifact_list_url = 'https://teamcity.labkey.org/httpAuth/repository/download/' + \
                           buildType + '/' + buildId + '/teamcity-ivy.xml'
    req = urllib.request.Request(tc_artifact_list_url)
    req.add_header('Authorization', b'Basic ' + base64.b64encode(bytes(tc_username, "utf-8") + b':' + bytes(tc_password, "utf-8")))
else:
    tc_artifact_list_url = 'https://teamcity.labkey.org/guestAuth/repository/download/' + \
                           buildType + '/' + buildId + '/teamcity-ivy.xml'
    req = urllib.request.Request(tc_artifact_list_url)

lmsg = "Download the TeamCity build artifacts list from " + tc_artifact_list_url + "\n"
print(lmsg) #; _print_log(logObject, lmsg)
try:
    resp = urllib.request.urlopen(req)
    tc_artifact_list_string = resp.read()
except urllib.error.HTTPError as e:
    print("There was problem downloading the list of artifacts from TeamCity " \
          "The HTTP response code was " + str(e.getcode()))
    print("The error message is " + format(e))
except urllib.error.URLError as e:
    print("There was problem connecting to the TeamCity Servers. ")
    print("The error message is " + e.reason)

tc_artifact_list = ElementTree.fromstring(tc_artifact_list_string)
#print tc_artifact_list_string
#print tc_artifact_list.tag
#for artifact in tc_artifact_list.iter('artifact'):
#    print artifact.attrib, artifact.attrib['name']


#
# Find the date and time which the build was completed.
#
if tc_use_cred:
    tc_stats_url = 'https://teamcity.labkey.org/httpAuth/app/rest/builds/buildType:' + \
                   buildType +',status:Success'
    req = urllib.request.Request(tc_stats_url)
    req.add_header('Authorization', b'Basic ' + base64.b64encode(bytes(tc_username, "utf-8") + b':' + bytes(tc_password, "utf-8")))
else:
    tc_stats_url = 'https://teamcity.labkey.org/guestAuth/app/rest/builds/buildType:' + \
                   buildType +',status:Success'
    req = urllib.request.Request(tc_stats_url)

lmsg = "Determine the date/time when this build was completed \n"
print(lmsg) #; _print_log(logObject, lmsg)
try:
    resp = urllib.request.urlopen(req)
    tc_stats_list_string = resp.read()
except urllib.error.HTTPError as e:
    print("There was problem downloading the list of artifacts from TeamCity " \
          "The HTTP response code was " + str(e.getcode()))
    print("The error message is " + format(e))
except urllib.error.URLError as e:
    print("There was problem connecting to the TeamCity Servers. ")
    print("The error message is " + e.reason)

tc_stats_list = ElementTree.fromstring(tc_stats_list_string)
build_start_date = tc_stats_list.find("startDate").text
build_date = time.strptime(build_start_date[:8], "%Y%m%d")

#
# Download client API artifacts from TeamCity.
#
if options.download == None:
    lmsg = "Download the API, etc artifacts from TeamCity \n"
    print(lmsg)
    # Download the Extra Modules distribution files
    #for artifact in tc_artifact_list.iter('artifact'):
    #    if 'extra_modules' in artifact.attrib['name']:
    #        _download_from_teamcity(artifact.attrib['name'] + "." + artifact.attrib['ext'], cacheDirectory, tc_use_cred)

    # Download the Client APIs
    for artifact in tc_artifact_list.getiterator('artifact'):
        if 'client-api' in artifact.attrib['name']:
            _download_from_teamcity(artifact.attrib['name'] + "." + artifact.attrib['ext'], cacheDirectory, tc_use_cred)

    # 03/18/2019 - Pipeline Config no longer needing to be distributed.
    #              See ticket #37055

    # Download the Pipeline Configuration files
    #for artifact in tc_artifact_list.getiterator('artifact'):
    #    if 'PipelineConfig' in artifact.attrib['name']:
    #        print(artifact.attrib['name'])
    #        _download_from_teamcity(artifact.attrib['name'] + "." + artifact.attrib['ext'], cacheDirectory, tc_use_cred)

#
# Find the SVN revision number. This information will be extracted from the
# file names of the files in the zip file.  Then build the svn URL.
# - In most cases the SVN revision number will be found by reading
#   the filename for the PipelineConfig artifacts.

# 03/18/2019 - Changed from using PipelineConfig to ClientAPI-Java due to
#              Pipeline Config no longer needing to be distributed.
#              See ticket #37055

f = glob.glob('*-ClientAPI-Java.zip')
if len(f) != 0:
    svnRev = f[0].split("-")[1].split(".")[0]
else:
    for artifact in tc_artifact_list.getiterator('artifact'):
        if 'LabKey' in artifact.attrib['name']:
            svnRev = artifact.attrib['name'].split("-")[1].split(".")[0]
            break

#
# Begin processing the push to the download pages. This will loop though all
# customers in the customerList
#
prefixPattern = re.compile("[^\/]*")  # get all characters up to (but not including) first '/'
for c in range(len(customerList)):
    # Download the distribution files for this customer
    print("Download installer artifacts for the customer, " + customerList[c]['buildTarget'].lstrip().rstrip() + " \n")
    for artifact in tc_artifact_list.iter('artifact'):
        possibleMatch = prefixPattern.match(artifact.attrib['name']).group()
        if customerList[c]['buildTarget'].lstrip().rstrip() == possibleMatch:  # artifact path starts exactly with customer buildTarget
            _download_from_teamcity(artifact.attrib['name'] + "." + artifact.attrib['ext'], cacheDirectory, tc_use_cred)

    lmsg = "\n\nStart publishing for Customer, " + customerList[c]['name'] + "......"
    print(lmsg) #; _print_log(logObject, lmsg)

    # Using information downloaded into customerList above, create the
    # required variables and dictionary.
    cust = customerList[c]['name'].lstrip().rstrip().lower()
    project = customerList[c]['projectName'].lstrip().rstrip()
    folder = customerList[c]['folder'].lstrip().rstrip()
    buildTarget = customerList[c]['buildTarget'].lstrip().rstrip()
    custOrgUrl = labkeyOrgUrl + slashContextPath + '/project/' + buildInfoRoot + urllib.parse.quote(project + '/' + folder)
    custOrgProjUrl = labkeyOrgUrl + slashContextPath + '/project/' + buildInfoRoot + urllib.parse.quote(project)
    if updateType == 'monthly':
        s3KeyPath = "downloads/" + cust + '/d/monthly/'
    elif updateType == 'snapshot':
        s3KeyPath = "downloads/" + cust + '/d/snapshot'
    elif updateType == 'trunk':
        s3KeyPath = "downloads/" + cust + '/d/trunk'
    else:
        s3KeyPath = "downloads/" + cust + '/r/' + versionNum
    s3KeyUrl = s3Url + "/" + s3KeyPath

    # Determine direct base distribution for each customer (i.e. the next level down, not the most basic)
    baseDistribution = None
    for dist in distributions:
        if dist['name'] == buildTarget:
            baseDistribution = dist['baseDistribution']

    isProfessional = False
    if buildTarget == 'professional' or buildTarget == 'biologics_pro' or buildTarget == 'pro_plus' or baseDistribution == 'pro_plus' or baseDistribution == 'professional':  # assume for now that all professional client distributions or are directly derived from professional (except for biologics)
        isProfessional = True

    #
    # Download the installer artifacts from the teamcity server
    #

    #
    # Create the dictionary for use in processing the wiki and message
    # templates
    #
    custDict = {}
    custDict['version'] = versionNum
    custDict['slashContextPath'] = slashContextPath
    custDict['project'] = project
    custDict['buildInfoRoot'] = buildInfoRoot
    custDict['folder'] = folder
    custDict['buildType'] = buildType
    custDict['svnRev'] = svnRev
    custDict['whatsNew'] = customerList[c]['whatsNew']
    custDict['releaseNotes'] = customerList[c]['releaseNotes']
    custDict['buildDate'] = time.strftime("%A %b %d %Y" ,build_date)
    if (options.buildmessage is not None):
        custDict['buildMessage'] = 'NOTE: ' + options.buildmessage
    else:
        custDict['buildMessage'] = ''

    #
    # Create message board subject text and description text for the message
    #

    #
    # 03/02/2019 - Updated text to accommodate newer build versioning
    #

    if (updateType == 'release'):
        custDict['messageTitle'] = '(' + project + ') An official release of LabKey Server ' + custDict['version'] + \
                                   ' is now available'
        custDict['messageDesc'] = ''
    elif (updateType == 'snapshot'):
        custDict['wikiTitle'] = 'Latest Snapshot build of LabKey Server ' + custDict['version']
        custDict['releaseOrBuild'] = 'build'
        custDict['releaseOrBuild2'] = 'Build'
        custDict['messageTitle'] = '(' + project + ') Snapshot build of LabKey Server ' + custDict['version'] + \
                                   ' is now available.'
        custDict['messageDesc'] = 'The latest snapshot build of LabKey Server ' + \
                                  custDict['version'] + ' has been created.'
    elif (updateType == 'monthly'):
        custDict['wikiTitle'] = 'LabKey Server ' + custDict['version'] + ' Monthly Release is now available.'
        custDict['releaseOrBuild'] = 'release'
        custDict['releaseOrBuild2'] = 'Release'
        custDict['messageTitle'] = '(' + project + ') The LabKey Server ' + \
                                   custDict['version'] + ' Monthly Release is now available'
        custDict['messageDesc'] = "The LabKey Server " + custDict['version'] + " Monthly Release" + \
                                  " build is now available. See the <a href='https://www.labkey.org/Documentation/wiki-page.view?name=releaseNotes" + \
                                  "".join(custDict['version'].split('.')) + "'>release notes</a> for a list of completed features."
    elif (updateType == 'trunk'):
        custDict['wikiTitle'] = 'Latest Nightly Development Build of LabKey Server ' + custDict['version']
        custDict['releaseOrBuild'] = 'build'
        custDict['releaseOrBuild2'] = 'Build'
        custDict['messageTitle'] = '(' + project + ') The latest development build for LabKey Server ' + \
                                   custDict['version'] + ' is now available'
        custDict['messageDesc'] = 'A new nightly development build for LabKey Server ' + \
                                  custDict['version'] + ' has been created.'


    #
    # Create the listings of build artifacts available for the customer for
    # both the messages and wiki content.
    # This will listing will get created by checking for the existence of
    # the artifact files in customers build directory.
    #
    # Generate listing to be used in the Binaries and Installers table in the Wiki content
    # binaryDict will contain the text strings used to describe binaries/installers
    message_list = []
    binaryDict = {}
    binaryDict['windowsBinary'] = 'Binaries for Manual Installation (zip)'
    binaryDict['unixBinary'] = 'Binaries for Manual Installation (tar.gz)'

    # Read the customer's buildTarget directory and see which installer artifacts are available.
    binary_list = []
    f = glob.glob(cacheDirectory + '/' + buildTarget + '/*bin.zip')
    if len(f) != 0:
        binary_list.append('windowsBinary')
        custDict['windowsBinary'] = os.path.basename(f[0])
        custDict['windowsBinarySize'] = "%0.1f MB" % (os.path.getsize(f[0])/(1024*1024.0))
        custDict['windowsBinaryLink'] = s3KeyUrl + '/' + custDict['windowsBinary']

    f = glob.glob(cacheDirectory + '/' + buildTarget + '/*bin.tar.gz')
    if len(f) != 0:
        binary_list.append('unixBinary')
        custDict['unixBinary'] = os.path.basename(f[0])
        custDict['unixBinarySize'] = "%0.1f MB" % (os.path.getsize(f[0])/(1024*1024.0))
        custDict['unixBinaryLink'] = s3KeyUrl + '/' + custDict['unixBinary']

    # Generate content to be used in the Wiki
    c = 0
    content = ''
    for tr in binary_list:
        if c == 0:
            content = content + '    <tr class="labkey-row">\n'
            c = c + 1
        else:
            content = content + '    <tr class="labkey-alternate-row">\n'
            c = 0

        content = content + '        <td style="white-space: nowrap;">' + binaryDict[tr] + '</td>\n'
        content = content + '        <td style="white-space: nowrap;"><a href="' + custDict[tr + "Link"] + '">' + custDict[tr] + '</a></td>\n'
        content = content + '        <td style="white-space: nowrap;">' + custDict[tr + "Size"] + '</td>\n'
        content = content + '        </tr>\n'

    custDict['wikiBinaryTableRows'] = content

    #
    # Generate listing to be used in the Related Products and Previous Releases content
    # relatedDict will contain the text strings used to describe binaries/installers

    # 03/18/2019 - Pipeline Config no longer needing to be distributed.
    #              See ticket #37055

    relatedDict = {}
    #relatedDict['pipelineConfig'] = 'Pipeline Configuration'
    relatedDict['javaClientSrc'] = 'Java Client API Library - Source Code (zip)'
    relatedDict['javaClient'] = 'Java Client API Library with Docs (zip)'
    relatedDict['javaScriptClient'] = 'JavaScript Client API Library Docs (zip)'
    relatedDict['sasClient'] = 'SAS Client API Library (zip)'
    relatedDict['pythonClient'] = 'Python Client API Library (zip)'


    if isProfessional:  # JDBC driver is currently a Professional feature
        relatedDict['jdbcClient'] = 'JDBC Driver (jar)'
    if cust == 'general':
        relatedDict['extraModules'] = 'Additional LabKey Modules'

    # Read the customer's buildTarget directory and see which installer artifacts are available.
    related_list = []
    #f = glob.glob(cacheDirectory + '/*PipelineConfig.tar.gz')
    #if len(f) != 0:
    #    related_list.append('pipelineConfigUnix')
    #    custDict['pipelineConfigUnix'] = os.path.basename(f[0])
    #    custDict['pipelineConfigUnixSize'] = "%0.1f MB" % (os.path.getsize(f[0])/(1024*1024.0))
    #    custDict['pipelineConfigUnixLink'] = s3KeyUrl + '/' + custDict['pipelineConfigUnix']

    #f = glob.glob(cacheDirectory + '/*PipelineConfig.zip')
    #if len(f) != 0:
    #    related_list.append('pipelineConfigWindows')
    #    custDict['pipelineConfigWindows'] = os.path.basename(f[0])
    #    custDict['pipelineConfigWindowsSize'] = "%0.1f MB" % (os.path.getsize(f[0])/(1024*1024.0))
    #    custDict['pipelineConfigWindowsLink'] = s3KeyUrl + '/' + custDict['pipelineConfigWindows']

    f = glob.glob(cacheDirectory + '/client-api/java/*ClientAPI-Java-src.zip')
    if len(f) != 0:
        related_list.append('javaClientSrc')
        custDict['javaClientSrc'] = os.path.basename(f[0])
        custDict['javaClientSrcSize'] = "%0.1f MB" % (os.path.getsize(f[0])/(1024*1024.0))
        custDict['javaClientSrcLink'] = s3KeyUrl + '/' + custDict['javaClientSrc']

    f = glob.glob(cacheDirectory + '/client-api/java/*ClientAPI-Java.zip')
    if len(f) != 0:
        related_list.append('javaClient')
        custDict['javaClient'] = os.path.basename(f[0])
        custDict['javaClientSize'] = "%0.1f MB" % (os.path.getsize(f[0])/(1024*1024.0))
        custDict['javaClientLink'] = s3KeyUrl + '/' + custDict['javaClient']

    f = glob.glob(cacheDirectory + '/client-api/javascript/*ClientAPI-JavaScript-Docs.zip')
    if len(f) != 0:
        related_list.append('javaScriptClient')
        custDict['javaScriptClient'] = os.path.basename(f[0])
        custDict['javaScriptClientSize'] = "%0.1f MB" % (os.path.getsize(f[0])/(1024*1024.0))
        custDict['javaScriptClientLink'] = s3KeyUrl + '/' + custDict['javaScriptClient']

    f = glob.glob(cacheDirectory + '/client-api/Python/LabKey*.zip')
    if len(f) != 0:
        related_list.append('pythonClient')
        custDict['pythonClient'] = os.path.basename(f[0])
        custDict['pythonClientSize'] = "%0.1f MB" % (os.path.getsize(f[0])/(1024*1024.0))
        custDict['pythonClientLink'] = s3KeyUrl + '/' + custDict['pythonClient']

    f = glob.glob(cacheDirectory + '/client-api/sas/*ClientAPI-SAS.zip')
    if len(f) != 0:
        related_list.append('sasClient')
        custDict['sasClient'] = os.path.basename(f[0])
        custDict['sasClientSize'] = "%0.1f MB" % (os.path.getsize(f[0])/(1024*1024.0))
        custDict['sasClientLink'] = s3KeyUrl + '/' + custDict['sasClient']

    if isProfessional:  # JDBC driver is currently a Professional feature
        f = glob.glob(cacheDirectory + '/client-api/jdbc/labkey-api-jdbc*-all.jar')
        if len(f) != 0:
            related_list.append('jdbcClient')
            custDict['jdbcClient'] = os.path.basename(f[0])
            custDict['jdbcClientSize'] = "%0.1f MB" % (os.path.getsize(f[0])/(1024*1024.0))
            custDict['jdbcClientLink'] = s3KeyUrl + '/' + custDict['jdbcClient']


    #
    # Generate content to be used in the Wiki
    #
    c = 0
    content = ''

    #
    # Check for Pipeline Configs and create table row if one or both exist. This is here because if both exist
    # then they will be placed on 1 table row instead of 2

    # 03/18/2019 - Pipeline Config no longer needing to be distributed.
    #              See ticket #37055

    #if 'pipelineConfigUnix' in related_list:
    #    if 'pipelineConfigWindows' in related_list:
    #        content = content + '    <tr class="labkey-row"> \n'
    #        content = content + '        <td style="white-space: nowrap;">' + relatedDict['pipelineConfig'] + '</td>\n'
    #        content = content + '        <td style="white-space: nowrap;"><a href="' + custDict["pipelineConfigUnixLink"] + '">' + \
    #                  custDict["pipelineConfigUnix"] + '</a> <a href="' + custDict["pipelineConfigWindowsLink"] + '">(zip)</a></td>\n'
    #        content = content + '        <td style="white-space: nowrap;">' + custDict["pipelineConfigUnixSize"] + '</td>\n'
    #        content = content + '        </tr>\n'
    #        c = c + 1
    #        related_list.remove('pipelineConfigWindows')
    #        message_list.append('pipelineConfig')
    #        pipelineConfigMessageText = '<li> ' + relatedDict['pipelineConfig'] + ': (<a href="' + custDict["pipelineConfigWindowsLink"] + \
    #                                    '">' + custDict["pipelineConfigWindows"] + '</a> | <a href="' + custDict["pipelineConfigUnixLink"] + \
    #                                    '">'+ custDict["pipelineConfigUnix"] + '</a>) </li>'
    #    else:
    #        content = content + '    <tr class="labkey-row">\n'
    #        content = content + '        <td style="white-space: nowrap;">' + relatedDict['pipelineConfig'] + '</td>\n'
    #        content = content + '        <td style="white-space: nowrap;"><a href="' + custDict["pipelineConfigUnixLink"] + '">' + \
    #                  custDict["pipelineConfigUnix"] + '</a></td>\n'
    #        content = content + '        <td style="white-space: nowrap;">' + custDict["pipelineConfigUnixSize"] + '</td>\n'
    #        content = content + '        </tr>\n'
    #        c = c + 1
    #        message_list.append('pipelineConfig')
    #        pipelineConfigMessageText = '<li> ' + relatedDict['pipelineConfig'] + ': (<a href="' + custDict["pipelineConfigUnixLink"] + \
    #                                    '">'+ custDict["pipelineConfigUnix"] + '</a>) </li>'
    #    related_list.remove('pipelineConfigUnix')
    #else:
    #    if 'pipelineConfigWindows' in related_list:
    #        content = content + '    <tr class="labkey-row">\n'
    #        content = content + '        <td style="white-space: nowrap;">' + relatedDict['pipelineConfig'] + '</td>\n'
    #        content = content + '        <td style="white-space: nowrap;"><a href="' + custDict["pipelineConfigWindowsLink"] + '">' + \
    #                  custDict["pipelineConfigWindows"] + '</a></td>\n'
    #        content = content + '        <td style="white-space: nowrap;">' + custDict["pipelineConfigWindowsSize"] + '</td>\n'
    #        content = content + '        </tr>\n'
    #        c = c + 1
    #        related_list.remove('pipelineConfigWindows')
    #        message_list.append('pipelineConfig')
    #        pipelineConfigMessageText = '<li> ' + relatedDict['pipelineConfig'] + ': (<a href="' + custDict["pipelineConfigWindowsLink"] + \
    #                                    '">'+ custDict["pipelineConfigWindows"] + '</a>) </li>'
    #
    # Create the rest of the rows in the table
    for tr in related_list:
        if c == 0:
            content = content + '    <tr class="labkey-row">\n'
            c = c + 1
        else:
            content = content + '    <tr class="labkey-alternate-row">\n'
            c = 0

        content = content + '        <td style="white-space: nowrap;">' + relatedDict[tr] + '</td>\n'
        content = content + '        <td style="white-space: nowrap;"><a href="' + custDict[tr + "Link"] + '">' + custDict[tr] + '</a></td>\n'
        content = content + '        <td style="white-space: nowrap;">' + custDict[tr + "Size"] + '</td>\n'
        content = content + '        </tr>\n'
    custDict['wikiRelatedTableRows'] = content
    #
    # Generate content to be used in the Message
    # Create the un-ordered list contents for message HTML text. This is a hideous block of if statements
    # as we want to control the order of the list in the message and unlike the Wiki text where we have
    # one artifact per row, in the message we double up artifacts on a single line to make the message
    # feel shorter.
    #
    # Check for Windows and Unix Manual Install Binaries and create row if one or both exist. This is here because
    # if both existthen they will be placed on 1 row instead of 2


    message_content = ''
    message_list = message_list + binary_list + related_list
    messageDict = dict(list(binaryDict.items()) + list(relatedDict.items()))
    if 'unixBinary' in message_list:
        if 'windowsBinary' in message_list:
            message_list.remove('windowsBinary')
            message_list.remove('unixBinary')
            message_content = message_content + '<li> Binaries for Manual Install: (<a href="' + custDict["windowsBinaryLink"] + \
                              '">' + custDict["windowsBinary"] + '</a> | <a href="' + custDict["unixBinaryLink"] + '">' + \
                              custDict["unixBinary"] + '</a>)</li>\n'
        else:
            message_list.remove('unixBinary')
            message_content = message_content + '<li> Binaries for Manual Install: (<a href="' + custDict["unixBinaryLink"] + '">' + \
                              custDict["unixBinary"] + '</a>)</li>\n'
    else:
        if 'windowsBinary' in message_list:
            message_list.remove('windowsBinary')
            message_content = message_content + '<li> Binaries for Manual Install: (<a href="' + custDict["windowsBinaryLink"] + '">' + \
                              custDict["windowsBinary"] + '</a>)</li>\n'


    # 03/18/2019 - Pipeline Config no longer needing to be distributed.
    #              See ticket #37055

    #if 'pipelineConfig' in message_list:
    #    tr = 'pipelineConfig'
    #    message_content = message_content + pipelineConfigMessageText + '\n'
    #    message_list.remove(tr)
    if 'javaClientSrc' in message_list:
        tr = 'javaClientSrc'
        message_content = message_content + '<li> ' + messageDict[tr] + ': (<a href="' + custDict[tr + "Link"] + \
                          '">' + custDict[tr] + '</a>)</li>\n'
        message_list.remove(tr)
    if 'javaClient' in message_list:
        tr = 'javaClient'
        message_content = message_content + '<li> ' + messageDict[tr] + ': (<a href="' + custDict[tr + "Link"] + \
                          '">' + custDict[tr] + '</a>)</li>\n'
        message_list.remove(tr)
    if 'javaScriptClient' in message_list:
        tr = 'javaScriptClient'
        message_content = message_content + '<li> ' + messageDict[tr] + ': (<a href="' + custDict[tr + "Link"] + \
                          '">' + custDict[tr] + '</a>)</li>\n'
        message_list.remove(tr)
    if 'pythonClient' in message_list:
        tr = 'pythonClient'
        message_content = message_content + '<li> ' + messageDict[tr] + ': (<a href="' + custDict[tr + "Link"] + \
                          '">' + custDict[tr] + '</a>)</li>\n'
        message_list.remove(tr)
    if 'sasClient' in message_list:
        tr = 'sasClient'
        message_content = message_content + '<li> ' + messageDict[tr] + ': (<a href="' + custDict[tr + "Link"] + \
                          '">' + custDict[tr] + '</a>)</li>\n'
        message_list.remove(tr)

    if isProfessional:  # JDBC driver is currently a Professional feature
        if 'jdbcClient' in message_list:
            tr = 'jdbcClient'
            message_content = message_content + '<li> ' + messageDict[tr] + ': (<a href="' + custDict[tr + "Link"] + \
                              '">' + custDict[tr] + '</a>)</li>\n'
            message_list.remove(tr)

    # Create entries for any artifacts that have been missed above
    for tr in message_list:
        message_content = message_content + '<li> ' + messageDict[tr] + ': (<a href="' + custDict[tr + "Link"] + \
                          '">' + custDict[tr] + '</a>)</li>\n'

    custDict['messageListItems'] = message_content

    #
    # Check if Customer's Project exists
    #

    if not _checkUrl(custOrgProjUrl + '/begin.view?'):
        lmsg = "The Customer's Project,  " + project + ", on " + labkeyOrgUrl + \
               " does not exist. The HTTP response code was 404. We Will not publish downloads for this customer =" + \
               cust + ". Please create the customer's project and try again.  The project URL is " + \
               custOrgProjUrl + '/begin.view?'
        print(lmsg) ; #; _print_log(logObject, lmsg)
        break

    #
    # Check if Customer's Project download folder exists
    #
    if not _checkUrl(custOrgUrl + '/begin.view?'):
        lmsg = "The Customer's Project,  " + project + ", on " + \
               labkeyOrgUrl + " does not exist. The HTTP response code was 404. We will not publish downloads for this customer =" + \
               cust + ". Please create the customer's project and try again.  The project URL is " + \
               custOrgUrl  + '/begin.view?'
        print(lmsg)  #; _print_log(logObject, lmsg)
        break

    #
    # Find the customer's directory, find all files to be uploaded
    #
    art_dir = cacheDirectory + "/" + buildTarget
    if os.path.exists(art_dir):
        art_file_list = [o for o in os.listdir(art_dir) if os.path.isfile(art_dir + "/" + o)]
    else:
        lmsg = "\nThe artifact download directory, " + art_dir + \
               ", does not exist. There was a problem downloading the artifacts. " + \
               "Please check that the distribution named, " + buildTarget + ", exists " + \
               "on the TeamCity Server and try again.\n"
        print(lmsg) #; _print_log(logObject, lmsg)
        sys.exit(1)

    #
    # Print out information about what is getting pushed to S3 and what
    # will be published to customers download site (wiki and message board).
    # In addition, pause the program to allow the user to review this
    # information. The user will be asked to hit the "enter" key to
    # complete the push.
    #
    print("\n\n -- Review the information for this push: ")
    print('{0:<15}{1}'.format("Customer Name:", buildTarget ))
    print('{0:<15}{1}'.format("Release Name:", updateType ))
    print('{0:<15}{1}'.format("Message Title:", custDict['messageTitle'] ))
    print('{0:<15}{1:<12}{2:<8}{3:<10}{4:<8}'.format("TeamCity Info:", "Build Type: ", buildType, "Build ID:", buildId))
    print("\nDistribution files to be pushed: ")
    for f in art_file_list:
        print('    - {0}'.format(f))
    print("\n--> Review the information above. If it looks correct, then")
    try:
        s = input('--> Hit [Enter] key to continue or CTRL+C to quit')
    except KeyboardInterrupt as e:
        # Catch CTRL+C and do not print any error message, but cancel program.
        print("n")
        sys.exit(0)

    #
    # Push build artifacts to S3
    # Used logic from http://bcbio.wordpress.com/2011/04/10/parallel-upload-to-amazon-s3-with-python-boto-and-multiprocessing/
    # to write this code originally, but much has changed since the code was switched to boto3's upload_file().
    #

    if (not options.safemode) and (not options.noaws):
        lmsg = "Begin pushing the Customer's build artifacts to S3 path " + s3KeyUrl
        print("\n\n -- " + lmsg) #; _print_log(logObject, lmsg)

        for f in art_file_list:
            # Path to file on local disk
            full_path = art_dir + '/' + f
            # Path to file at S3
            s3_key_name = s3KeyPath + '/' + f

            # Start the upload.
            lmsg = "Start the upload of " + full_path + " to " + s3_key_name
            print("\n -- " + lmsg) #; _print_log(logObject, lmsg)
            # Create the connection
            s3 = boto3.resource('s3')
            # Test if the S3 bucket exists. If it does not exist create a new bucket.
            # If any other error occurs print the the error to the screen and quit
            try:
                s3.meta.client.head_bucket(Bucket=s3BucketName)
            except botocore.exceptions.ClientError as e:
                # If a client error is thrown, then check that it was a 404 error.
                # If it was a 404 error, then the bucket does not exist.
                error_code = int(e.response['Error']['Code'])
                if error_code == 404:
                    # Bucket does not exist, so try to create it
                    try:
                        s3.create_bucket(Bucket=s3BucketName)
                    except e:
                        lmsg = ("There was an error while attempting to create the bucket named " + \
                                s3BucketName + ". The error message is " + format(e))
                        print(" -- " + lmsg) #; _print_log(logObject, lmsg)
                        sys.exit(1)
                else:
                    lmsg = "There was an error while connecting to S3. The error message is " + format(e)
                    print(" -- " + lmsg) #; _print_log(logObject, lmsg)
                    sys.exit(1)

            # Check if the file currently exists at S3
            if _check_file_existence_s3(s3, s3BucketName, s3_key_name):
                lmsg =  full_path + " already exists in S3 bucket. Skipping the upload"
                print(" -- " + lmsg) #; _print_log(logObject, lmsg)
            else:
                # Copy the file to the S3 bucket specified in s3BucketName
                start_time = time.time()
                # Determine the size of the file to be uploaded
                mb_size = os.path.getsize(full_path) / (1024.0 * 1024.0)
                # Upload file using boto3 upload_file(). This will do multipart transfers
                # with multiple threads above a certain threshold (currently 8 MB). The files
                # are being uploaded using S3 Reduced Redundancy Storage
                # (see https://aws.amazon.com/s3/reduced-redundancy/) and public reading.
                s3.meta.client.upload_file(full_path, s3BucketName, s3_key_name, ExtraArgs={'ACL': "public-read", 'StorageClass': "REDUCED_REDUNDANCY"})
                print(' -- %s (%sMB): Successfully transferred in %s minutes' % (str(os.path.basename(full_path)), mb_size, str((time.time() - start_time)/60)))

        # Upload pipeline configs, docs and client apis
        # Build the list of files to be uploaded
        #
        # (Todo: Make the upload to S3 a function.  I am duplicating the code from
        #   above section here.)

    # 03/18/2019 - Pipeline Config no longer needing to be distributed.
    #              See ticket #37055

        other_file_list = []
        #f = glob.glob(cacheDirectory + '/*PipelineConfig.tar.gz')
        #if len(f) != 0:
        #    other_file_list.append(f[0])
        #f = glob.glob(cacheDirectory + '/*PipelineConfig.zip')
        #if len(f) != 0:
        #    other_file_list.append(f[0])
        f = glob.glob(cacheDirectory + '/client-api/Python/LabKey*.zip')
        if len(f) != 0:
            other_file_list.append(f[0])
        f = glob.glob(cacheDirectory + '/client-api/javascript/*ClientAPI-JavaScript-Docs.zip')
        if len(f) != 0:
            other_file_list.append(f[0])
        f = glob.glob(cacheDirectory + '/client-api/java/*ClientAPI-Java.zip')
        if len(f) != 0:
            other_file_list.append(f[0])
        f = glob.glob(cacheDirectory + '/client-api/java/*ClientAPI-Java-src.zip')
        if len(f) != 0:
            other_file_list.append(f[0])
        f = glob.glob(cacheDirectory + '/client-api/sas/*ClientAPI-SAS.zip')
        if len(f) != 0:
            other_file_list.append(f[0])

        if isProfessional:  # JDBC driver is currently a Professional feature
            f = glob.glob(cacheDirectory + '/client-api/jdbc/labkey-api-jdbc*-all.jar')
            if len(f) != 0:
                other_file_list.append(f[0])


        # Loop through all fields in the other_file_list and push them to S3
        for f in other_file_list:
            # Path to file on local disk
            full_path = f
            # Path to file at S3
            # print f
            s3_key_name = s3KeyPath + '/' + os.path.basename(f)
            # Start the upload.
            lmsg = "Start the upload of " + full_path + " to " + s3_key_name
            print("\n -- " + lmsg) #; _print_log(logObject, lmsg)
            # Create the connection
            s3 = boto3.resource('s3')
            # Test if the S3 bucket exists. If it does not exist create a new bucket.
            # If any other error occurs print the the error to the screen and quit
            try:
                s3.meta.client.head_bucket(Bucket=s3BucketName)
            except botocore.exceptions.ClientError as e:
                # If a client error is thrown, then check that it was a 404 error.
                # If it was a 404 error, then the bucket does not exist.
                error_code = int(e.response['Error']['Code'])
                if error_code == 404:
                    # Bucket does not exist, so try to create it
                    try:
                        s3.create_bucket(Bucket=s3BucketName)
                    except e:
                        lmsg = ("There was an error while attempting to create the bucket named " + \
                                s3BucketName + ". The error message is " + format(e))
                        print(" -- " + lmsg) #; _print_log(logObject, lmsg)
                        sys.exit(1)
                else:
                    lmsg = "There was an error while connecting to S3. The error message is " + format(e)
                    print(" -- " + lmsg) #; _print_log(logObject, lmsg)
                    sys.exit(1)

            # Check if the file currently exists at S3
            if _check_file_existence_s3(s3, s3BucketName, s3_key_name):
                lmsg =  full_path + " already exists in S3 bucket. Skipping the upload"
                print(" -- " + lmsg) #; _print_log(logObject, lmsg)
            else:
                # Copy the file to the S3 bucket specified in s3BucketName
                start_time = time.time()
                # Determine the size of the file to be uploaded
                mb_size = os.path.getsize(full_path) / (1024.0 * 1024.0)
                # Upload file using boto3 upload_file(). This will do multipart transfers
                # with multiple threads above a certain threshold (currently 8 MB). The files
                # are being uploaded using S3 Reduced Redundancy Storage
                # (see https://aws.amazon.com/s3/reduced-redundancy/) and public reading.
                s3.meta.client.upload_file(full_path, s3BucketName, s3_key_name, ExtraArgs={'ACL': "public-read", 'StorageClass': "REDUCED_REDUNDANCY"})
                print(' -- %s (%sMB): Successfully transferred in %s minutes'
                      % (str(os.path.basename(full_path)), mb_size, str((time.time() - start_time)/60)))

    #
    # Now that all files have been pushed to S3. The next step is update the Download site wiki
    # and post a message to the Download site's message board.
    #
    # If a Release, update the Release wiki for the customer
    if (updateType == 'release'):
        lmsg = "Update the Release wiki for the customer"
        print("\n\n -- " + lmsg) #; _print_log(logObject, lmsg)
        if cust == 'general':
            print(" -- The code to update labkey.com website is not yet complete")
            # This is currently done manually due to the use of JoomLa on the corporate website
        else:
            # Read wiki template for this customer
            tName = exeDirectory + "/templates/" + cust + "-releaseWikiContent.html"
            if os.path.isfile(tName):
                wikiBody = _create_content_from_template(tName, custDict)
            else:
                wikiBody = _create_content_from_template(exeDirectory + "/templates/releaseWikiContent.html", custDict)
            #print "\n"
            #print wikiBody
            #print "\n"

            if not options.safemode:
                if (wikiBody != 0):
                    # Post updated wiki content to the Customers Download page
                    containerPath = buildInfoRoot + project + '/' + folder
                    server_context = create_server_context(labkeyOrgServer, containerPath, context_path=contextPath, use_ssl=useSsl)
                    wikiName = 'release'
                    wikiBody = wikiBody
                    response = labkey.unsupported.wiki.update_wiki(
                        server_context,
                        wikiName,
                        wikiBody)

                    sc = response.status_code
                    if (200 <= sc < 300) or sc == 304:
                        lmsg = "Update of the Development wiki has been successfully completed."
                        print(" -- " + lmsg) #; _print_log(logObject, lmsg)
                    else:
                        lmsg = "Update of the Development wiki has failed. You may need to update \
                                the customer's wiki by hand. (Note error message from labkeyApi is \
                                printed to the screen)"
                        lmsg = lmsg + "\nThe response from the server is: " + str(response)
                        print(" -- " + lmsg) #; _print_log(logObject, lmsg)
                else:
                    lmsg = "There was a problem creating the Release Wiki content from template. \
                            Release wiki will not be updated for customer. You may need to update \
                            the customer's wiki by hand"
                    print(" -- " + lmsg) #; _print_log(logObject, lmsg)
    else:
        #
        # If a development release (monthly, snapshot or trunk), update the Development Wiki
        #
        lmsg = "Update the Development wiki for the customer"
        print("\n\n -- " + lmsg) #; _print_log(logObject, lmsg)

        # Read wiki template for this customer
        tName = exeDirectory + "/templates/" + cust + "-devWikiContent.html"
        if os.path.isfile(tName):
            wikiBody = _create_content_from_template(tName, custDict)
        else:
            wikiBody = _create_content_from_template(exeDirectory + "/templates/devWikiContent.html", custDict)
        #print "\n"
        #print wikiBody
        #print "\n"

        if not options.safemode:
            if wikiBody != 0:
                # Post updated wiki content to the Customers Download page
                containerPath = buildInfoRoot + project + '/' + folder
                server_context = create_server_context(labkeyOrgServer, containerPath, context_path=contextPath, use_ssl=useSsl)
                wikiName = 'development'
                wikiBody = wikiBody
                response = labkey.unsupported.wiki.update_wiki(
                    server_context,
                    wikiName,
                    wikiBody)

                sc = response.status_code
                if (200 <= sc < 300) or sc == 304:
                    lmsg = "Update of the Development wiki has been successfully completed."
                    print(" -- " + lmsg) #; _print_log(logObject, lmsg)
                else:
                    lmsg = "Update of the Development wiki has failed. You may need to update \
                            the customer's wiki by hand. (Note error message from labkeyApi is \
                            printed to the screen)"
                    lmsg = lmsg + "\nThe response from the server is: " + str(response)
                    print(" -- " + lmsg) #; _print_log(logObject, lmsg)
            else:
                lmsg = "There was a problem creating the Development Wiki content from template. \
                        Release wiki will not be updated for customer. You may need to update the \
                        customer's wiki by hand"
                print(" -- " + lmsg) #; _print_log(logObject, lmsg)

    #
    # Post a Message to the customer's message board.
    #
    lmsg = "Post message to the customer's message board on their download page"
    print("\n\n -- " + lmsg) #; _print_log(logObject, lmsg)

    tName = exeDirectory + "/templates/" + cust + "-message.html"
    if os.path.isfile(tName):
        emailBody = _create_content_from_template(tName, custDict)
    else:
        emailBody = _create_content_from_template(exeDirectory + "/templates/message.html", custDict)

    #print "\n"
    #print emailBody
    #print "\n"

    if not options.safemode:
        if emailBody != 0:
            # Post updated email message content to the Customer's Message Board
            containerPath = buildInfoRoot + project + '/' + folder
            server_context = create_server_context(labkeyOrgServer, containerPath, context_path=contextPath, use_ssl=useSsl)
            messageTitle = custDict['messageTitle']
            messageBody = emailBody
            renderAs = 'HTML'
            results = labkey.unsupported.messageboard.post_message(
                server_context,
                messageTitle,
                messageBody,
                renderAs)

            if results:
                lmsg = "Post of message to Customer's message board was completed "
                print(" -- " + lmsg) #; _print_log(logObject, lmsg)
            else:
                lmsg = "Post of message to Customer's message board has failed. (Note error message \
                        from labkeyApi is printed to the screen)"
                lmsg = lmsg + "\nThe response from the server is: " + str(results)
                print(" -- " + lmsg) #; _print_log(logObject, lmsg)
        else:
            lmsg = "There was a problem creating the message content from template. A message will \
                    not be posted to the customer's message board."
            print(" -- " + lmsg) #; _print_log(logObject, lmsg)

    #
    # Clean up files downloaded from TeamCity
    #
    # This is being done to preserve disk space, as > 5GB of files are downloaded
    # when builds are pushed to all customers.
    lmsg = "Clean up all files in " + cacheDirectory + "/" + buildTarget + " from this run"
    print("\n" + lmsg) #; _print_log(logObject, lmsg)
    if not os.path.exists(cacheDirectory + "/" + buildTarget):
        lmsg = cacheDirectory + "/" + buildTarget + " directory does not exist. Nothing to clean up."
        print("\n" + lmsg) #; _print_log(logObject, lmsg)
    else:
        # Delete any files from the this run.
        fList = glob.glob(buildTarget + '/*')
        if len(fList) > 0:
            for l in fList:
                if os.path.isfile(l):
                    try:
                        os.remove(l)
                    except IOError as e:
                        print("({0})".format(e))
                    x=1
                else:
                    try:
                        shutil.rmtree(l)
                    except IOError as e:
                        print("({0})".format(e))



#
# End of script
# Print some messages and clean up.
#
lmsg = "\n\nThe push of installers to our Customer download pages is complete. (" + \
       str(datetime.datetime.now()) +")"
print(lmsg) #; _print_log(logObject, lmsg)
os.chdir(startDirectory)
