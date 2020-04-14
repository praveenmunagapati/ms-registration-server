/*
 * Copyright (c) 2008-2019 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.labkey.vcslicense;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevCommitList;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.labkey.vcslicense.renderers.*;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: adam
 * Date: May 10, 2008
 * Time: 5:55:19 PM
 */

/*

    A not-entirely-automated utility for updating/inserting copyright notices into files in a Subversion or Git
    repository.

    What it does:

    1. Recursively crawls one or more Subversion/Git repositories, interrogating each file to determine its creation
       date, author, and most recent modification date. Based on creation date & author, determines company that should
       own copyright.
    2. For files of interest (e.g., all .java and .js files) reads the first 16 KB of text (from the local file system)
       looking for an existing copyright notice of the form:

            Copyright (c) <year1>(-<year2>) <some company>

    3. Compares existing copyright notice (if present) with copyright notice generated from step #1.
    4. Generates a report and a list of SED commands that, when run, should update the copyright (if dates or company
       are incorrect) or insert a new license (if missing) in each file that requires changes.

    Ideally, this would be a command line utility that uses a config file to determine its behavior, but that would
    be a bunch of work. Right now, the tool reads one parameter from the command line: a path to a batch file that will
    be cleared and filled with commands that update the files. All other configuration parameters are changed directly
    in the code. The tool will use your saved Subversion credentials to access the repository.

    Make sure you have no outstanding commits and your local directory is synced with the repository. (For git 
    repositories, make sure your local branch is the same as HEAD, i.e. the default branch.) Run VCSLicense and review
    the report to get a sense for how many files are out-of-date, whether you need to add more directories or companies
    so the tool ignores third-party sources, etc. Once the report looks correct, run the generated batch file (or
    change the .bat to .command for OS X, or copy the list of SED commands from the console window and paste them into
    a command window).

    I usually create a patch file before committing, doing a sanity check in a text editor to ensure the changes are
    limited strictly to copyright notices. (In an early version of this utility, SED was changing all line endings in
    every file, which was a problem.)

    Note that all inserted & updated copyright notices will end with the current year, even if the file hasn't been
    touched for a long time. This is because when you commit these updated files Subversion will now consider them
    modified this year. If we didn't use the current year then running the utility again after committing would result
    in a raft of further updates to make.

    This utility generates a lot of network traffic; on a fast network (e.g., FHCRC corporate network) it can crawl the
    entire /labkey/server directory in about 15 minutes. On a slow network (e.g., DSL from home) it will take longer.

    Note: Your local copy of sed must support the -b option (for "binary", which keeps sed from changing all your line
    endings to unix style). Cygwin's does, but GnuWin32's does not. For OSX the built in sed doesn't support the syntax
    we used, but gnu-sed works ok. On mac you can install using "brew install gnu-sed --with-default-names"

    The console output of this tool is getting larger and larger, and seems to overflow IntelliJ's buffer lately. You
    may want to solve this by editing the vcslicense configuration to save the console output to a file (look under the
    "Logs" tab) so that you can view the entire output properly.

    Tips for things to fix:

     -- Take a good look at anything marked UnrecognizedCopyright or UnknownCompany in the console output, because there
    won't generally be many of them and they often signal something that needs to be fixed. (A notable exception is
    entries from Fred Hutchinson Cancer Research Center, since they're mixed in liberally with LabKey code and cannot be
    easily separated out.)

     -- When committing these changes, you may get an "inconsistent line endings" error from Subversion on a few files.
    I noticed that all the files causing problems had their "svn:eolstyle" property set to "native". They are easily
    fixed; just open the file in IntelliJ, delete the line break immediately after the copyright, add it back, and save.

     -- You also might see an error about "Byte Order Mark (BOM) detected" while parsing a file. The tool is acting
    correctly in these cases, and those files must be fixed, probably by re-saving not in that Unicode format.
    Helpfully, IntelliJ can fix this problem: right-click in the source file, select "File Encoding", select "UTF-8",
    and then click "Convert". If it's already in UTF-8, just right-click in the source file and select "Remove BOM"
    instead. (IntelliJ, or a tool like Beyond Compare, can show you that the files are in fact different afterwards.)
    If you see a number of these errors from the same group of developers, you may want to tell them about the issue to
    prevent future issues with files of this type being committed in future.

     -- Watch out for files where a copyright notice already exists but is not the first lines in the file (typically,
    when the package, imports, or taglib tag are placed above the copyright comments). The tool WILL produce the wrong
    output in those cases if the entire license is overwritten, and these errors must be fixed manually.

     -- See a "Malformed URL ''" error in your console output? You probably haven't cloned a Git repo you need. Make
    sure it's in the correct branch too (trunk vs. release).

    -- Is the tool failing on the SVN repository talking about String index is -1? Delete the log files the tool makes
    ("previous_*.txt") and try again.

     -- Most cases where the tool overwrites a license incorrectly these days are MIT-licensed. There's no good way to
    detect this in the current copyright detection scheme though.

     -- Getting a "Authentication required" error for the SVN repo? Try these steps:
         -- Go to <home>/.subversion
         -- Delete the "auth" folder there
         -- In the "config" file, add a line under the "[auth]" section that says "password-stores ="
         -- Re-authenticate SVN via command line, with a command such as "svn log -l 5 --username your_username_here"
         -- When it asks you if it's ok to save your password in plaintext, say "yes"
         -- Try running VCSLicense again
    Note that this will mean storing your SVN password in plaintext on your computer, so you may want to revert this
    change when you are done running this tool.

     -- Getting a FileNotFoundException when running the tool? Your source isn't up to date anymore, so you'll need to
    sync with source control again if you want to get rid of the error.

     -- Warnings about "module.properties" files not being found are only a problem if the repository is actually a
    LabKey module. In this case, a LICENSE or License.txt file should be present in the root, though. If it isn't, one
    should be added.

     -- labkey-api-js, labkey-api-python, and labkey-api-r are special snowflakes to test and must be handled
    individually if changes are made to files in them. For significant changes to labkey-api-js or labkey-api-python,
    talk to the maintainer about building them. labkey-api-r should be a sibling directory to /server and must be built
    with its own gradle.
*/
public class VCSLicense
{
    private static final String currentYear = new SimpleDateFormat("yyyy").format(new Date());
    private static final ConcurrentMap<String, AtomicInteger> _checkedFileCounts = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, AtomicInteger> _skippedFileCounts = new ConcurrentHashMap<>();
    private static final Map<String, Report> _reports = new ConcurrentHashMap<>(1000);

    // Standard LabKey copyright
    static final String labkeyName = "LabKey Corporation";

    // Pattern explanation:                                                                 Optional Month    Start Year          Optional End Year                    Company
    private static final Pattern copyright = Pattern.compile("Copyright(?: ?(?:\\(c\\)|©)) (([a-zA-Z]{3,8} )?((19|20)\\d\\d)\\s*((-|,)\\s*((19|20)\\d\\d))?(,| ))?( )*(.*)", Pattern.CASE_INSENSITIVE);
    private static final Pattern lenientCopyright = Pattern.compile("Copyright (([a-zA-Z]{3,8} )?((19|20)\\d\\d)\\s*((-|,)\\s*((19|20)\\d\\d))?(,| ))?( )*(.*)", Pattern.CASE_INSENSITIVE);
    private static final Pattern basicCopyright = Pattern.compile("Copyright", Pattern.CASE_INSENSITIVE);

    private static final Pattern apacheLongLicenseFinder =  Pattern.compile("Licensed under the Apache License, Version 2.0 ", Pattern.CASE_INSENSITIVE);  // that space after 2.0 is important to distinguish between short and long Apache license
    private static final Pattern apacheShortLicenseFinder = Pattern.compile("Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0", Pattern.CASE_INSENSITIVE);
    private static final Pattern labkeyLicenseFinder = Pattern.compile("All rights reserved. No portion of this work may be reproduced in", Pattern.CASE_INSENSITIVE);
    //private static final Pattern argosLicenseFinder = Pattern.compile("This file is part of the Argos application, which cannot be copied or distributed", Pattern.CASE_INSENSITIVE);

    private final Collection<Repository> repositories;

    // =========== START OF PARAMETERS YOU MAY WANT TO TWEAK ===========

    // OS SPECIFIC CONFIGS FOR SYSTEM RUNNING THE SED FILE
    // the batchEscape() is used in the windows environment to replace the % symbol with %% (windows uses % as a sys var delimiter) in the sed batch file. % is used in jsp comments)
    // on mac batchEscape() results in having %% appear in the sed file and when the sed is applied the %% appears in the modified jsp file
    private static final boolean applyBatchEscape = false;

    private static final RendererMap apacheLicenseRenderers = new RendererMap(); // Renderers used for Apache licenses (full version in SQL and R files)
    private static final RendererMap customModuleRenderers = new RendererMap();  // Custom modules -- use short Apache licenses for SQL and R files
    private static final RendererMap labkeyLicenseRenderers = new RendererMap();  // Renderers for modules that require LabKey license -- full LabKey licenses in SQL and R files

    // LOCATIONS OF REPOSITORIES
    //    SRC_DIR = location of repository (manual create and/or synch it before each run). This is where the tool will attempt to examine each file to determine
    //              if copyright and license is ok. Uses repo info to get first and last commit date (and possibly committer name).
    //    DEST_DIR = for each repository the script containing the sed statements will cd to this root directory where the sed changes for that repository is applied.
    //              For example by specifying these roots in the IntelliJ labkey project after applying the sed those files will then be detected as modified by
    //              whatever version control tool is responsible for that directory (svn or git) and IntelliJ's update-project will apply the changes using svn or git as appropriate.

    // The following defines all assume that your trunk/release/sprint branches are in the same base directory.
    // This can be easily changed below if desired.
    private static final String LABKEY_REPOSITORY_BASE_DIR = "/Users/ankurjuneja";

    // Source/destination dirs for trunk/develop (or trunk/master)
    private static final String LABKEY_TRUNK_BASE_DIR = LABKEY_REPOSITORY_BASE_DIR + "/LabKey/trunk";
    private static final String LABKEY_TRUNK_SRC_DIR = LABKEY_REPOSITORY_BASE_DIR + "/LabKey/trunk/server";
    private static final String LABKEY_TRUNK_DEST_DIR = LABKEY_REPOSITORY_BASE_DIR + "/LabKey/trunk/server";

    // Source/destination dirs for releaseXX.X/releaseXX.X
    //private static final String LABKEY_RELEASE_SRC_DIR = LABKEY_REPOSITORY_BASE_DIR + "/LabKey_18_3/release18.3/server";
    //private static final String LABKEY_RELEASE_DEST_DIR = LABKEY_REPOSITORY_BASE_DIR + "/LabKey_18_3/release18.3/server";
    // Use trunk directory for this, it's easier
    private static final String LABKEY_RELEASE_SRC_DIR = LABKEY_REPOSITORY_BASE_DIR + "/LabKey/trunk/server";
    private static final String LABKEY_RELEASE_DEST_DIR = LABKEY_REPOSITORY_BASE_DIR + "/LabKey/trunk/server";

    // Source/destination dirs for sprintXX.X_X/sprintXX.X_X (not used currently)
    private static final String LABKEY_SPRINT_SRC_DIR = LABKEY_REPOSITORY_BASE_DIR + "/LabKey_sprint_16.2_3/sprint_16.2_3/server";
    private static final String LABKEY_SPRINT_DEST_DIR = LABKEY_REPOSITORY_BASE_DIR + "/LabKey_sprint_16.2_3/sprint_16.2_3/server";

    private static Set<String> companiesToIgnore = new HashSet<>(Arrays.asList(
            "MuleSource, Inc",
            "Ext JS, LLC.",
            "Ext JS, Inc.",
            "John Wilkins",
            "Google Inc.",
            "Sun Microsystems, Inc",
            "Hannes Wallnoefer <hannes@helma.at>",
            "Fred Sauer",
            "Ing. Jozef Sak\u00e1lo\u0161",
            "by Ing. Jozef Sak\u00e1lo\u0161",
            "Damien Miller <djm@mindrot.org>",
            "opcthree",
            "Unimod",
            "University of Washington - Seattle, WA"  // SkylinePort files in targetedms
    ));


    private enum LicenseType
    {
        APACHE_LICENSE
        {
            public RendererMap getRenderers()
            {
                return apacheLicenseRenderers;
            }
        },
        LABKEY_LICENSE
        {
            public RendererMap getRenderers()
            {
                return labkeyLicenseRenderers;
            }
        };

        public abstract RendererMap getRenderers();
    }

    private enum BranchType
    {
        TRUNK
        {
            public String getSourceDirectory(String parentDirectoryName, String repoName)
            {
                return LABKEY_TRUNK_SRC_DIR + "/" + parentDirectoryName + "/" + repoName;
            }
            public String getDestDirectory(String parentDirectoryName, String repoName)
            {
                return LABKEY_TRUNK_DEST_DIR + "/" + parentDirectoryName + "/" + repoName;
            }
        },
        TRUNK_BUILDSRC
        {
            public String getSourceDirectory(String parentDirectoryName, String repoName)
            {
                return LABKEY_TRUNK_BASE_DIR + "/buildSrc/" + repoName;
            }
            public String getDestDirectory(String parentDirectoryName, String repoName)
            {
                return LABKEY_TRUNK_BASE_DIR + "/buildSrc/" + repoName;
            }
        },
        RELEASE
        {
            public String getSourceDirectory(String parentDirectoryName, String repoName)
            {
                return LABKEY_RELEASE_SRC_DIR + "/" + parentDirectoryName + "/" + repoName;
            }
            public String getDestDirectory(String parentDirectoryName, String repoName)
            {
                return LABKEY_RELEASE_DEST_DIR + "/" + parentDirectoryName + "/" + repoName;
            }
        },
        SPRINT
        {
            public String getSourceDirectory(String parentDirectoryName, String repoName)
            {
                return LABKEY_SPRINT_SRC_DIR + "/" + parentDirectoryName + "/" + repoName;
            }
            public String getDestDirectory(String parentDirectoryName, String repoName)
            {
                return LABKEY_SPRINT_DEST_DIR + "/" + parentDirectoryName + "/" + repoName;
            }
        };

        public abstract String getSourceDirectory(String parentDirectoryName, String repoName);
        public abstract String getDestDirectory(String parentDirectoryName, String repoName);
    }

    private VCSLicense()
    {
        repositories = Arrays.asList(
                // Require Apache License
                // TODO: Change module.properties links to Apache license to https
                getAccountsRepository(),
                getAdjudicationRepository(),
                getAlderBioRepository(),
//                getBuildReportsRepository(), //not buidling anymore
                getCdiscOdmRepository(),
                getCdsRepository(),
                getCommonAssaysRepository(),
                getCustomModuleRepository(),
                getEhrModulesRepository(),
                getDockerRepository(),
                getDockerRStudioRepository(),
                getEnlistRepository(),
                getEvaluationContentRepository(),
                getFileTransferRepository(),
                getGlassComponentsRepository(),
                getGlassDesignRepository(),
                getGradlePluginRepository(),
                getHostingDocumentationRepository(),
//                getHplcRepository(), //deprecated and archived
                getIdriRepository(),
                getInventoryRepository(),
                getJanssenRepository(),
                getLabBookRepository(),
                getLabKeyApiJsRepository(),
                getLabKeyApiPythonRepository(),
                getLabKeyApiRRepository(),
                getLabKeySvnRepository(),
                getMedimmuneRepository(),
                getMedimmuneFlowRepository(),
                getMobileAppStudyRepository(),
                getNciRepository(),
                getNihsRepository(),
                getNlpRepository(),
                getOConnorRepository(),
                getPlatformRepository(),
                getRnaSeqMatrixDemoRepository(),
                getSamplesRepository(),
                getServiceToolsRepository(),
                getSignalDataRepository(),
                getSndRepository(),
                getSnprcEhrModulesRepository(),  // TODO: make this read from externalModules directory instead, where it should be downloaded to
                getTargetedMSRepository(),      // PS: do this for CNPRC too when it gets added
                getTestAutomationRepository(),
                getTnprcBillingRepository(),
                getTnprcEhrRepository(),
                getTrialServicesRepository(),
                getTrialShareRepository(),
                getWorkflowRepository(),

                // Require LabKey License
                getAssayReportRepository(),
                getAssayRequestRepository(),
                getBiologicsRepository(),
                getCasRepository(),
                getCloudRepository(),
                getComplianceRepository(),
                getComplianceActivitiesRepository(),
                getConnectorsRepository(),
                getDataDefinitionsRepository(),
                getDataIntegrationRepository(),
                getDuoRepository(),
                getFreezerProRepository(),
                getGelRepository(),
                getGelReportsRepository(),
                getGelTestRepository(),
                getHarvestRepository(),
                getHutchAbstractionRepository(),
                getLabkeyApiJdbcRepository(),  // TODO: make this read from remoteapi directory instead, where it should be downloaded to
                getNhsDigitalRepository(),
                getNlpPremiumRepository(),
                getOAuthRepository(),
                getOpenEmpiRepository(),
                getPanoramaPremiumModulesRepository(),
                getPatientTestDataRepository(),
                getPremiumRepository(),
                getRecipeRepository(),
                getRedcapRepository(),
                getRedshiftRepository(),
                getRStudioRepository(),
                getSamlRepository(),
                getScrumtimeRepository(),
                getServerProvisioningRepository(),
                getSynonymRepository(),
                getSysengLkRepository()
                );
    }

    static
    {
        // Extensions are always lowercase
        apacheLicenseRenderers.addRenderer(new JavaLicenseRenderer(License.Apache), ".java", ".sql", ".sas", ".jsx", ".ts", ".tsx", ".groovy");
        apacheLicenseRenderers.addRenderer(new JspLicenseRenderer(License.Apache), ".jsp");
        apacheLicenseRenderers.addRenderer(new JavaLicenseRenderer(License.Apache).setShort(), ".js");
        apacheLicenseRenderers.addRenderer(new RLicenseRenderer(License.Apache), ".r", ".tf", ".tfvars");
        apacheLicenseRenderers.addRenderer(new PythonLicenseRenderer(License.Apache), ".py", ".sh");
        apacheLicenseRenderers.addRenderer(new UpdateOnlyLicenseRenderer(License.Apache), ".xml");     // Corrects an existing license, but won't insert a new one

        customModuleRenderers.addRenderer(new JavaLicenseRenderer(License.Apache), ".java", ".sas", ".jsx", ".ts", ".tsx", ".groovy");
        customModuleRenderers.addRenderer(new JavaLicenseRenderer(License.Apache).setShort(), ".sql");
        customModuleRenderers.addRenderer(new JspLicenseRenderer(License.Apache), ".jsp");
        customModuleRenderers.addRenderer(new JavaLicenseRenderer(License.Apache).setShort(), ".js");
        customModuleRenderers.addRenderer(new RLicenseRenderer(License.Apache).setShort(), ".r", ".tf", ".tfvars");
        customModuleRenderers.addRenderer(new UpdateOnlyLicenseRenderer(License.Apache), ".xml");           // Corrects an existing license, but won't insert a new one

        labkeyLicenseRenderers.addRenderer(new JavaLicenseRenderer(License.LabKey), ".java", ".sql", ".sas", ".jsx", ".ts", ".tsx", ".groovy");
        labkeyLicenseRenderers.addRenderer(new JspLicenseRenderer(License.LabKey), ".jsp");
        labkeyLicenseRenderers.addRenderer(new JavaLicenseRenderer(License.LabKey).setShort(), ".js");
        labkeyLicenseRenderers.addRenderer(new RLicenseRenderer(License.LabKey), ".r", ".tf", ".tfvars");
        labkeyLicenseRenderers.addRenderer(new PythonLicenseRenderer(License.LabKey), ".py", ".sh");
        labkeyLicenseRenderers.addRenderer(new UpdateOnlyLicenseRenderer(License.LabKey), ".xml");     // Corrects an existing license, but won't insert a new one
    }

    private static String getLogFilename(final String repoName)
    {
        return "previous_" + repoName + "_git.txt";
    }

    private class RepositoryBuilder
    {
        private Set<String> companiesToIgnore = Collections.emptySet();
        private Set<String> directoriesToIgnore = Collections.emptySet();
        private Set<String> filesToIgnore = Collections.emptySet();
        private String parentDirectoryName;
        private String repoName;
        private LicenseType licenseType;
        private BranchType branchType;
        private String branchToCompare = "";  // use to override default repo branch to compare changes with
        private RendererMap renderers;
        private String sourceDirectory;

        RepositoryBuilder(String parentDirectoryName, String repoName, LicenseType licenseType, BranchType branchType)
        {
            this(parentDirectoryName, repoName, licenseType, branchType, licenseType.getRenderers());
        }

        RepositoryBuilder(String parentDirectoryName, String repoName, LicenseType licenseType, BranchType branchType, RendererMap renderers)
        {
            this(parentDirectoryName, repoName, licenseType, branchType, renderers, branchType.getSourceDirectory(parentDirectoryName, repoName));
        }

        RepositoryBuilder(String parentDirectoryName, String repoName, LicenseType licenseType, BranchType branchType, RendererMap renderers, String sourceDirectory)
        {
            this.parentDirectoryName = parentDirectoryName;
            this.repoName = repoName;
            this.licenseType = licenseType;
            this.branchType = branchType;
            this.renderers = renderers;
            this.sourceDirectory = sourceDirectory;
        }

        private Repository buildRepository()
        {

            List<? extends Configuration> configurations = Collections.singletonList(new DirectoryConfiguration(list(""), directoriesToIgnore, filesToIgnore, renderers));

            return new Repository(repoName, new File(branchType.getSourceDirectory(parentDirectoryName, repoName)),
                    new File(branchType.getDestDirectory(parentDirectoryName, repoName)), configurations, companiesToIgnore,
                    getLogFilename(repoName), new LicenseSelector()
            {
                @Override
                public boolean isValidCompany(String fileCompany)
                {
                    return labkeyName.equals(fileCompany);
                }

                @Override
                public String getCompany(Date createDate, String fileAuthor)
                {
                    return labkeyName;
                }
            }, branchToCompare);
        }

        void setCompaniesToIgnore(Set<String> companiesToIgnore) {
            this.companiesToIgnore = companiesToIgnore;
        }

        void setDirectoriesToIgnore(Set<String> directoriesToIgnore) {
            this.directoriesToIgnore = directoriesToIgnore;
        }

        void setFilesToIgnore(Set<String> filesToIgnore) {
            this.filesToIgnore = filesToIgnore;
        }

        void setRepoName(String repoName) {
            this.repoName = repoName;
        }

        void setLicenseType(LicenseType licenseType) {
            this.licenseType = licenseType;
        }

        void setBranchType(BranchType branchType) {
            this.branchType = branchType;
        }

        void setBranchToCompare(String branchToCompare)
        {
            this.branchToCompare = branchToCompare;
        }
    }

    // REPOSITORIES THAT NEED APACHE LICENSE

    private Repository getAccountsRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "accounts", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getAdjudicationRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "adjudication", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getAlderBioRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "alderBio", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getBuildReportsRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "buildReports", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getCdiscOdmRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "cdisc_ODM", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getCdsRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "cds", LicenseType.APACHE_LICENSE, BranchType.RELEASE);
        repoBuilder.setFilesToIgnore(
                set(
                        "webapp/Connector/src/ext-patches.js"
                )
        );
        repoBuilder.setDirectoriesToIgnore(
                set(
                        "webapp/production",
                        "webapp/frontPage/components",
                        "app/Connector"
                )
        );
        //repoBuilder.setBranchToCompare("develop");  // override current repo default, which is release16.2 branch

        return repoBuilder.buildRepository();
    }

    private Repository getCommonAssaysRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("modules", "commonAssays", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        repoBuilder.setDirectoriesToIgnore(
                set(
                        "ms2/webapp/MS2/lorikeet_0.3"
                )
        );
        repoBuilder.setFilesToIgnore(
                set(
                        "flow/resources/flowReports/positivity.R",
                        "luminex/resources/transformscripts/youtil.R"
                )
        );
        return repoBuilder.buildRepository();
    }

    private Repository getCustomModuleRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("modules", "customModules", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        repoBuilder.setFilesToIgnore(
                set(
                        "hdrl/resources/schemas/dbscripts/oracle/labware.sql"
                )
        );
        repoBuilder.setCompaniesToIgnore(companiesToIgnore);
        return repoBuilder.buildRepository();
    }

    private Repository getDockerRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "docker", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getDockerRStudioRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "docker-rstudio", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getEhrModulesRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("modules", "ehrModules", LicenseType.APACHE_LICENSE, BranchType.TRUNK, customModuleRenderers);
        repoBuilder.setCompaniesToIgnore(companiesToIgnore);
        repoBuilder.setDirectoriesToIgnore(
                set(
                        "ehr/resources/web/ehr/lib"
                )
        );
        repoBuilder.setFilesToIgnore(
                set(
                        "ehr/resources/web/ehr/ext3/ext.ux.datetimefield.js"
                )
        );
        return repoBuilder.buildRepository();
    }

    private Repository getEnlistRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "enlist", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getEvaluationContentRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "evaluationContent", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getFileTransferRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "fileTransfer", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getGlassComponentsRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "glass-components", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getGlassDesignRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "glass-design", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getGradlePluginRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "gradlePlugin", LicenseType.APACHE_LICENSE, BranchType.TRUNK_BUILDSRC);
        repoBuilder.setDirectoriesToIgnore(
                set(
                        "moduleTemplate"
                )
        );
        return repoBuilder.buildRepository();
    }

    private Repository getHostingDocumentationRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "hostingDocumentation", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getHplcRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "HPLC", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        repoBuilder.setCompaniesToIgnore(
                set(
                        "Tom Alexander"  // regression.js
                )
        );

        return repoBuilder.buildRepository();
    }

    private Repository getIdriRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "idri", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        repoBuilder.setCompaniesToIgnore(
                set(
                        "Tom Alexander"  // regression.js
                )
        );
        return repoBuilder.buildRepository();
    }

    private Repository getInventoryRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "inventory", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getJanssenRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "janssen", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getLabBookRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "labbook", LicenseType.APACHE_LICENSE, BranchType.RELEASE);
        return repoBuilder.buildRepository();
    }

    private Repository getLabKeyApiJsRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "labkey-api-js", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getLabKeyApiPythonRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "labkey-api-python", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getLabKeyApiRRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "labkey-api-r", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private static Repository getLabKeySvnRepository()
    {

        Set<String> directoriesToIgnore = set(
                "server/customModules/ehr",
                "server/customModules/EHR_ComplianceDB",
                "server/customModules/OConnor",
                "server/customModules/WNPRC_EHR/tools/nightwatch-tests/node_modules",
                "server/customModules/WNPRC_EHR/webapp/wnprc_ehr/c3-0.4.10",
                "server/installer/nsis2.46",
                "server/installer/3rdparty",
                "server/modules/dumbster/src/com/dumbster/smtp" //in test automation
                // also check configurations.add() calls below, which have their own different ignored directories!
            );

        Set<String> filesToIgnore = set(
                "server/test/src/org/labkey/junit/rules/TestWatcher.java",
                "server/customModules/lincs/resources/reports/schemas/targetedms/GCT_input_peptidearearatio/p100_processing.r"
            );

        List<Configuration> configurations = new LinkedList<>();

        configurations.add(new DirectoryConfiguration(list("server"), directoriesToIgnore, filesToIgnore, apacheLicenseRenderers));
        configurations.add(new DirectoryConfiguration(list("remoteapi"), null, null, apacheLicenseRenderers));
        configurations.add(new DirectoryConfiguration(list("tools/vcslicense", "tools/pushToDownloadPage"),
                set("tools/pushToDownloadPage/.idea"),
                set("tools/vcslicense/src/org/labkey/vcslicense/VCSLicense.java", "tools/vcslicense/src/org/labkey/vcslicense/renderers/License.java"),
                apacheLicenseRenderers));

        return new Repository("core labkey", "https://svn.mgt.labkey.host/stedi/trunk", new File(LABKEY_TRUNK_BASE_DIR), configurations, companiesToIgnore, "previous_labkey_svn.txt", new LabKeyLicenseSelector());
    }

    private Repository getMedimmuneRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "medImmune", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getMedimmuneFlowRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "medImmuneFlow", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getMobileAppStudyRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "mobileAppStudy", LicenseType.APACHE_LICENSE, BranchType.RELEASE);
        return repoBuilder.buildRepository();
    }

    private Repository getNciRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "nci", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getNihsRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "nihs", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        repoBuilder.setDirectoriesToIgnore(
                set(
                        "src/org/nihs/messages"
                )
        );
        return repoBuilder.buildRepository();
    }

    private Repository getNlpRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "nlp", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private static Repository getOConnorRepository()
    {
        final Date startDate;
        final String OCONNOR_DIR = "/customModules/OConnor";

        try
        {
            startDate = DateFormat.getDateInstance(DateFormat.SHORT).parse("7/25/14");
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }

        Set<String> companiesToIgnore = Collections.emptySet();
        Set<String> directoriesToIgnore = Collections.emptySet();
        Set<String> filesToIgnore = Collections.emptySet();

        List<? extends Configuration> configurations = Collections.singletonList(new DirectoryConfiguration(list(""), directoriesToIgnore, filesToIgnore, customModuleRenderers));

        return new Repository("oconnor", "https://svn.mgt.labkey.host/stedi/trunk/server/customModules/OConnor", new File(LABKEY_TRUNK_SRC_DIR + OCONNOR_DIR), configurations,
                companiesToIgnore, "previous_oconnor_svn.txt", new LicenseSelector()
        {
            private final String _dave = "David O'Connor";

            @Override
            public boolean isValidCompany(String fileCompany)
            {
                return labkeyName.equals(fileCompany) || _dave.equals(fileCompany);
            }

            @Override
            public String getCompany(Date createDate, String fileAuthor)
            {
                return createDate.before(startDate) ? _dave : labkeyName;
            }
        });
    }

    private Repository getPlatformRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("modules", "platform", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        repoBuilder.setDirectoriesToIgnore(
                set(
                        "api/src/org/json",
                        "api/src/org/systemsbiology/jrap",
                        "api/src/proteowizard/pwiz/RAMPAdapter",
                        "api/webapp/ext-3.4.1",
                        "api/webapp/ext-4.2.1",
                        "api/src/org/mitre",
                        "internal/webapp/internal/clipboard",
                        "internal/webapp/codemirror-4.2",
                        "internal/webapp/dropzone",
                        "internal/webapp/hopscotch",
                        "internal/webapp/internal/jQuery",
                        "internal/webapp/slider",
                        "internal/webapp/tiny_mce",
                        "internal/webapp/ux",
                        "internal/webapp/vis/lib",
                        "timeline/webapp/similetimeline",
                        "timeline/webapp/timeline",
                        "wiki/radeox",
                        "wiki/resources/scripts"
                )
        );
        repoBuilder.setFilesToIgnore(
                set(
                        "api/src/org/labkey/api/collections/SparseBitSet.java",
                        "api/src/org/labkey/api/resource/ChildFirstClassLoader.java",
                        "internal/webapp/Ext.ux.MultiSelectTreePanel.js",
                        "internal/webapp/stacktrace-1.3.0.min.js",
                        "survey/webapp/survey/DateTimeField.js"
                )
        );
        repoBuilder.setCompaniesToIgnore(companiesToIgnore);
        return repoBuilder.buildRepository();
    }

    private Repository getRnaSeqMatrixDemoRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "RNASeqMatrixDemo", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getSamplesRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "samples", LicenseType.APACHE_LICENSE, BranchType.TRUNK);

        repoBuilder.setDirectoriesToIgnore(
                set(
                        "docker/LabKeyServer/16.3/tomcat",
                        "docker/labkey-standalone/tomcat"
                )
        );

        return repoBuilder.buildRepository();
    }

    private Repository getServiceToolsRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "serviceTools", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getSignalDataRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "signalData", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        repoBuilder.setCompaniesToIgnore(
                set(
                        "Tom Alexander"  // regression.js
                )
        );
        return repoBuilder.buildRepository();
    }

    private Repository getSndRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "snd", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    // TODO: make a BranchType.TRUNK_EXTERNAL that points to externalModules directory and config this repo to use it
    // PS: do this for CNPRC too when it gets added
    private Repository getSnprcEhrModulesRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "snprcEHRModules", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        repoBuilder.setDirectoriesToIgnore(
                set(
                        "snprc_scheduler"
                )
        );
        return repoBuilder.buildRepository();
    }

    private Repository getTargetedMSRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("modules", "targetedms", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        repoBuilder.setDirectoriesToIgnore(
                set(
                        "webapp/TargetedMS/jquery",
                        "webapp/TargetedMS/DataTables"
                )
        );
        repoBuilder.setFilesToIgnore(
                set(
                        "webapp/TargetedMS/js/bootstrap-tagsinput.min.js",
                        "webapp/TargetedMS/js/typeahead.bundle.min.js",
                        "webapp/TargetedMS/js/clipboard.min.js"
                )
        );
        repoBuilder.setCompaniesToIgnore(companiesToIgnore);
        return repoBuilder.buildRepository();
    }

    private Repository getTestAutomationRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("", "testAutomation", LicenseType.APACHE_LICENSE, BranchType.TRUNK, apacheLicenseRenderers);
        repoBuilder.setFilesToIgnore(
                set(
                        "src/org/labkey/junit/rules/TestWatcher.java"
                )
        );
        repoBuilder.setCompaniesToIgnore(companiesToIgnore);
        return repoBuilder.buildRepository();
    }

    private Repository getTnprcBillingRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "tnprc_billing", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getTnprcEhrRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "tnprc_ehr", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getTrialServicesRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "trialServices", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getTrialShareRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "trialShare", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getWorkflowRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "workflow", LicenseType.APACHE_LICENSE, BranchType.TRUNK);
        repoBuilder.setCompaniesToIgnore(
                set(
                        "Alfresco Software, Ltd."
                )
        );

        return repoBuilder.buildRepository();
    }

    // REPOSITORIES THAT NEED LABKEY LICENSE

    private Repository getAssayReportRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "assayreport", LicenseType.LABKEY_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getAssayRequestRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "assayRequest", LicenseType.LABKEY_LICENSE, BranchType.RELEASE);
        return repoBuilder.buildRepository();
    }

    private Repository getBiologicsRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "biologics", LicenseType.LABKEY_LICENSE, BranchType.RELEASE);
        repoBuilder.setDirectoriesToIgnore(
                set(
                        "src/client/typings"
                )
        );

        return repoBuilder.buildRepository();
    }

    private Repository getCasRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "cas", LicenseType.LABKEY_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getCloudRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "cloud", LicenseType.LABKEY_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getConnectorsRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "connectors", LicenseType.LABKEY_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getComplianceRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "compliance", LicenseType.LABKEY_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getComplianceActivitiesRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "complianceActivities", LicenseType.LABKEY_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getDataDefinitionsRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "datadefinitions", LicenseType.LABKEY_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getDataIntegrationRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "dataintegration", LicenseType.LABKEY_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getDuoRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "duo", LicenseType.LABKEY_LICENSE, BranchType.TRUNK);
        repoBuilder.setDirectoriesToIgnore(
                set(
                        "resources/web/duo",
                        "src/com/duosecurity/duoweb"
                )
        );

        return repoBuilder.buildRepository();
    }

    private Repository getFreezerProRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "freezerpro", LicenseType.LABKEY_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getGelRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "gel", LicenseType.LABKEY_LICENSE, BranchType.RELEASE);
        return repoBuilder.buildRepository();
    }

    private Repository getGelReportsRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "gel_reports", LicenseType.LABKEY_LICENSE, BranchType.RELEASE);
        return repoBuilder.buildRepository();
    }

    private Repository getGelTestRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "gelTest", LicenseType.LABKEY_LICENSE, BranchType.RELEASE);
        return repoBuilder.buildRepository();
    }

    private Repository getHarvestRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "harvest", LicenseType.LABKEY_LICENSE, BranchType.RELEASE);
        return repoBuilder.buildRepository();
    }

    private Repository getHutchAbstractionRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "hutch_abstraction", LicenseType.LABKEY_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getLabkeyApiJdbcRepository()
    {
        // TODO: make a BranchType.TRUNK_REMOTEAPI that points to remoteapi directory and config this repo to use it
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "labkey-api-jdbc", LicenseType.LABKEY_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getNhsDigitalRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "nhs_digital", LicenseType.LABKEY_LICENSE, BranchType.RELEASE);
        return repoBuilder.buildRepository();
    }

    private Repository getNlpPremiumRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "nlp_premium", LicenseType.LABKEY_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getOAuthRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "oauth", LicenseType.LABKEY_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getOpenEmpiRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "openEMPI", LicenseType.LABKEY_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getPanoramaPremiumModulesRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "PanoramaPremiumModules", LicenseType.LABKEY_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getPatientTestDataRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "patient_test_data", LicenseType.LABKEY_LICENSE, BranchType.RELEASE);
        return repoBuilder.buildRepository();
    }

    private Repository getPremiumRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "premium", LicenseType.LABKEY_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getRecipeRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "recipe", LicenseType.LABKEY_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getRedcapRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "redcap", LicenseType.LABKEY_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getRedshiftRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "redshift", LicenseType.LABKEY_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getRStudioRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "rstudio", LicenseType.LABKEY_LICENSE, BranchType.TRUNK);
        repoBuilder.setFilesToIgnore(
                set(
                        "resources/web/rstudio_hook.js"
                )
        );

        return repoBuilder.buildRepository();
    }

    private Repository getSamlRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "saml", LicenseType.LABKEY_LICENSE, BranchType.TRUNK);
        repoBuilder.setDirectoriesToIgnore(
                set(
                        "src/com/onelogin"
                )
        );

        return repoBuilder.buildRepository();
    }

    private Repository getScrumtimeRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "scrumtime", LicenseType.LABKEY_LICENSE, BranchType.RELEASE);
        return repoBuilder.buildRepository();
    }

    private Repository getServerProvisioningRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "serverProvisioning", LicenseType.LABKEY_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getSynonymRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "synonym", LicenseType.LABKEY_LICENSE, BranchType.TRUNK);
        return repoBuilder.buildRepository();
    }

    private Repository getSysengLkRepository()
    {
        RepositoryBuilder repoBuilder = new RepositoryBuilder("optionalModules", "syseng-lk", LicenseType.LABKEY_LICENSE, BranchType.TRUNK);
        repoBuilder.setFilesToIgnore(
                set(
                    "aws/render/react-modal.min.js"
                )
        );
        return repoBuilder.buildRepository();
    }

    // =========== END OF PARAMETERS YOU MAY WANT TO TWEAK ===========

    private static HashSet<String> set(String... s)
    {
        return new HashSet<>(Arrays.asList(s));
    }

    private static List<String> list(String... s)
    {
        return Arrays.asList(s);
    }

    private static VCSLicenseExecutor exec;

    // TODO: Move these to the Repository?
    private static long previousRevision;
    private static long latestRevision;

    public static void main(String[] args) throws IOException, AmbiguousObjectException, GitAPIException
    {
        VCSLicense vcsLicense = new VCSLicense();
        System.exit(vcsLicense.run(args));
    }

    private int run(String[] args) throws IOException
    {
        long t0 = System.currentTimeMillis();

        /*
         * initialize the library
         */
        setupLibrary();
        Writer batchWriter = null;

        try
        {
            if (args.length > 0)
            {
                File batchFile = new File(args[0]);
                batchFile.delete();
                batchWriter = new OutputStreamWriter(new FileOutputStream(batchFile), StandardCharsets.UTF_8);
            }

            for (final Repository repository : repositories)
            {
                _reports.clear();
                latestRevision = 0;

                Repository.RepositoryType repositoryType;
                File gitDirFile = repository.getSourceDir();
                repositoryType = Repository.determineRepositoryType(gitDirFile);
                gitDirFile = new File(repository.getSourceDir(), "/.git");  // add for getGitRepository()
                switch (repositoryType)
                {
                    case GIT_REPOSITORY:
                        org.eclipse.jgit.lib.Repository gitRepository = getGitRepository(repository, gitDirFile);
                        if(repository.getBranchToCompare().equals(""))
                            System.out.println("Repository Root: " + gitRepository.exactRef("HEAD").getName());
                        else  // branch to compare with is overridden
                            System.out.println("Repository Root: " + gitRepository.exactRef("refs/heads/" + repository.getBranchToCompare()).getName());
                        System.out.println("Repository Source Directory: " + repository.getSourceDir());
                        System.out.println("Repository Destination Directory: " + repository.getDestinationDir());

                        // Read license information from module.properties files

                        File modulePropsFile = new File(gitRepository.getDirectory().getParent(), "module.properties");
                        Properties props = new Properties();
                        if (modulePropsFile.exists())
                        {
                            try (FileInputStream in = new FileInputStream(modulePropsFile))
                            {
                                props.load(in);
                            }
                            catch (IOException e)
                            {
                                System.out.println("Error reading module properties file '" + modulePropsFile.getAbsolutePath() + "'.");
                            }

                            String licenseString = props.getProperty("License");
                            String licenseUrlString = props.getProperty("LicenseURL");

                            if(licenseString != null)
                                System.out.println("License: " + licenseString);
                            else
                                System.out.println("WARNING: No License found in module.properties file for repository '" + repository.getName() + "'.");
                            if(licenseUrlString != null)
                                System.out.println("LicenseURL: " + licenseUrlString);
                            else
                                System.out.println("WARNING: No LicenseURL found in module.properties file for repository '" + repository.getName() + "'.");
                        }
                        else
                        {
                            System.out.println("WARNING: No module.properties file for repository '" + repository.getName() + "'.");
                        }

                        exec = new VCSLicenseExecutor(10);

                        for (Configuration config : repository.getConfigurations())
                        {
                            for (String path : config.getIncludedDirectories())
                            {
                                exec.execute(new GitLicenseRunnable(repository, config, path));
                            }
                        }
                        break;  // end case GIT_REPOSITORY
                    case SVN_REPOSITORY:
                        SVNRepository svnRepository = getSVNRepository(repository);
                        SVNNodeKind nodeKind = svnRepository.checkPath("", -1);

                        if (nodeKind == SVNNodeKind.NONE)
                        {
                            throw new Exception("There is no entry at '" + repository.getSvnServer() + "'.");
                        }
                        else if (nodeKind == SVNNodeKind.FILE)
                        {
                            throw new Exception("The entry at '" + repository.getSvnServer() + "' is a file but a directory was expected.");
                        }

                        /*
                         * getRepositoryRoot() returns the actual root directory where the repository was created. 'true' forces
                         * to connect to the repository if the root url is not cached yet.
                         */
                        System.out.println("Repository Root: " + svnRepository.getRepositoryRoot(true));

                        /*
                         * getRepositoryUUID() returns Universal Unique IDentifier (UUID) of the repository. 'true' forces to
                         * connect to the repository if the UUID is not cached yet.
                         */
                        System.out.println("Repository UUID: " + svnRepository.getRepositoryUUID(true));
                        System.out.println("Repository Source Directory: " + repository.getSourceDir());
                        System.out.println("Repository Destination Directory: " + repository.getDestinationDir());

                        File previousSVN = new File(repository.getPreviousRevisionFilename());
                        previousRevision = 0;
                        latestRevision = -1;

                        if (previousSVN.exists())
                        {
                            char[] buffer = new char[100];

                            try (Reader reader = new InputStreamReader(new FileInputStream(previousSVN), StandardCharsets.UTF_8))
                            {
                                int ret = reader.read(buffer);
                                assert -1 == ret;
                                previousRevision = Integer.valueOf(new String(buffer, 0, ret).trim());
                            }
                        }

                        System.out.println("Previous revision: " + previousRevision);
                        latestRevision = svnRepository.getLatestRevision();

                        Set<String> changedPaths = new TreeSet<>();

                        if (previousRevision > 0)
                        {
                            @SuppressWarnings("unchecked")
                            Collection<SVNLogEntry> col = (Collection<SVNLogEntry>) svnRepository.log(new String[]{""}, null, previousRevision, latestRevision, true, false);

                            for (SVNLogEntry logEntry : col)
                            {
                                System.out.println(logEntry.getDate() + " " + logEntry.getRevision());
                                Map<String, SVNLogEntryPath> pathMap = logEntry.getChangedPaths();

                                for (SVNLogEntryPath path : pathMap.values()) {
                                    System.out.print("  ");
                                    System.out.println(path.getType() + " " + path.getPath());
                                }

                                changedPaths.addAll(pathMap.keySet());
                            }
                        }

                        // TODO: Not ready for prime time... non-0 previous revision currently means botched starting dates, etc.
                        previousRevision = 0;

                        System.out.println("Repository latest revision: " + latestRevision);
                        System.out.println();

                        exec = new VCSLicenseExecutor(10);

                        for (Configuration config : repository.getConfigurations())
                        {
                            for (String path : config.getIncludedDirectories())
                            {
                                exec.execute(new SVNLicenseRunnable(repository, config, path, previousRevision, latestRevision));
                            }
                        }
                        break;  // end case SVN_REPOSITORY
                    default:   // should never happen
                        throw new Exception("Unexpected repository type for repo '" + repository.getName() + "'.");
                }   // end RepositoryType switch()

                do
                {
                    Thread.sleep(1000);

                    int total = 0;

                    for (AtomicInteger count : _checkedFileCounts.values())
                        total += count.get();

                    System.out.println("Processed files: " + total);
                }
                while (exec.getActiveCount() > 0 || exec.getQueue().size() > 0);

                System.out.println();
                System.out.println("REPORT");
                System.out.println();

                if (_reports.isEmpty())
                {
                    System.out.println("All " + repository.getName() + " licenses appear to be up-to-date.");
                    System.out.println();
                }
                else
                {
                    System.out.println("File Path\tCurrent Copyright\tStatus\tSuggested Copyright");
                    boolean corrections = false;

                    List<Report> orderedReports = new ArrayList<>(_reports.values());
                    Collections.sort(orderedReports, (r1, r2) -> r1.getPath().compareTo(r2.getPath()));

                    for (Report report : orderedReports)
                    {
                        System.out.println(report.getReport());

                        if (null != report.getCorrection())
                            corrections = true;
                    }

                    System.out.println();
                    System.out.println("CORRECTIONS");
                    System.out.println();

                    if (!corrections)
                    {
                        System.out.println("No corrections to make.");
                    }
                    else
                    {
                        out(batchWriter, "cd " + (repository.getDestinationDir()).getCanonicalPath());

                        for (Report report : orderedReports)
                            if (null != report.getCorrection())
                                out(batchWriter, report.getCorrection());
                    }
                }

                // Use obscure redirection syntax to avoid spaces and special characters
                /* TODO: latestRevision is always set to 0 for git, since it's not really a good concept to talk about
                 *       with a distributed VCS anyway. It should not even really be output for git repos.
                 */
                out(batchWriter, ">" + System.getProperty("user.dir") + "/" + repository.getPreviousRevisionFilename() + " latest rev " + latestRevision);

                System.out.println();
            }   // end Repository loop
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return 1;
        }
        finally
        {
            if (null != batchWriter)
                batchWriter.close();
        }

        summarize(_checkedFileCounts, "Checked");
        summarize(_skippedFileCounts, "Skipped");

        logMissed("directories",  DirectoryConfiguration.getMissedDirectories());
        logMissed("files", DirectoryConfiguration.getMissedFiles());
        logMissed("companies", Repository.getMissedCompanies());

        System.out.println((float) (System.currentTimeMillis() - t0) / 60000 + " minutes");
        return 0;
    }

    private static void logMissed(String noun, Collection<String> missed)
    {
        if (!missed.isEmpty())
        {
            System.out.println("Warning: the following " + noun + " were not seen:");

            for (String item : missed)
                System.out.println(item);

            System.out.println();
        }
    }

    private static void out(Writer batchWriter, String text) throws IOException
    {
        System.out.println(text);

        if (null != batchWriter)
        {
            if (applyBatchEscape)
            {
                batchWriter.write(batchEscape(text) + System.getProperty("line.separator"));
            }
            else
            {
                batchWriter.write(text + System.getProperty("line.separator"));
            }
        }
    }

    private static String batchEscape(String text)
    {
        return text.replace("%", "%%");
    }

    private static void summarize(Map<String, AtomicInteger> fileCounts, String s)
    {
        int sum = 0;
        System.out.println("Summary of " + s + " Files:");
        for (String extension : fileCounts.keySet())
        {
            int fileCount = fileCounts.get(extension).get();
            System.out.println("   " + extension + " " + fileCount);
            sum += fileCount;
        }
        System.out.println("Total " + sum);
        System.out.println();
    }

    /*
     * Initializes the library to work with a repository via
     * different protocols.
     */
    private static void setupLibrary() {
        /*
         * For using over http:// and https://
         */
        DAVRepositoryFactory.setup();
        /*
         * For using over svn:// and svn+xxx://
         */
        SVNRepositoryFactoryImpl.setup();

        /*
         * For using over file:///
         */
        FSRepositoryFactory.setup();
    }


    static void processGitFiles(final ThreadContext context, final Repository repository, final Configuration config, final String path) throws IOException, SVNException
    {
        org.eclipse.jgit.lib.Repository jgitRepository = context.getGitRepository();
        final RendererMap renderers = config.getRenderers();

        String branchHeadString;
        if (repository.getBranchToCompare().equals(""))
            branchHeadString = "HEAD";
        else  // branch to compare with is overridden
            branchHeadString = "refs/heads/" + repository.getBranchToCompare();
        Ref branchHead = jgitRepository.exactRef(branchHeadString);
        if(branchHead == null )
        {
            throw new IOException("The reference '" + branchHeadString + "' for the repo at '" + repository.getSourceDir() + "' was not found.");
        }

        RevWalk repoWalk = new RevWalk(jgitRepository);
        RevCommit headCommit = repoWalk.parseCommit(branchHead.getObjectId());
        RevTree headRevTree = headCommit.getTree();
        TreeWalk treeWalk = new TreeWalk(jgitRepository);
        treeWalk.addTree(headRevTree);
        treeWalk.setRecursive(false);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");

        // Look at all files

        while (treeWalk.next())
        {
            if(treeWalk.isSubtree())  // directory, not file
            {
                if(config.isAllowedDirectory(treeWalk.getPathString()))
                {
                    treeWalk.enterSubtree();
                }
                else
                {
                    // do nothing, not an allowed directory
                }

            }
            else  // file
            {
                repoWalk = new RevWalk(jgitRepository);
                headCommit = repoWalk.parseCommit(branchHead.getObjectId());
                repoWalk.markStart(headCommit);
                // Only look at commits that changed this file
                repoWalk.setTreeFilter(AndTreeFilter.create(PathFilter.create(treeWalk.getPathString()), TreeFilter.ANY_DIFF));

                /*
                 * This tool intentionally does not track renames of any kind, in the interest of being conservative.
                 * Tracking renames in git is challenging because they are not recorded explicitly, and must be
                 * deduced using similarity metrics, leading to potential false positives.
                 * Someday, if we'd like to track renames, the following code will do this, using a filter that mimics
                 * "--follow" in git. It should replace the line immediately previous to this comment.
                 *
                 * Config repoConfig = new Config();
                 * repoConfig.setBoolean("diff", null, "renames", true);
                 * DiffConfig repoDiffConfig = repoConfig.get(DiffConfig.KEY);
                 * repoWalk.setTreeFilter(FollowFilter.create(treeWalk.getPathString(), repoDiffConfig));
                 */

                RevCommitList<RevCommit> revCommitList = new RevCommitList<>();
                revCommitList.source(repoWalk);
                revCommitList.fillTo(Integer.MAX_VALUE);  // gets all commits for this file

                String name = treeWalk.getNameString();
                int index = name.lastIndexOf('.');

                final String extension = index != -1 ? name.substring(index).toLowerCase() : ""; // No extension case
                final String filePath = createFilePath(path, treeWalk.getPathString());

                if (!renderers.containsKey(extension) || !config.isAllowedFile(filePath))
                {
                    addFile(_skippedFileCounts, extension);
                    continue;
                }
                else
                {
                    addFile(_checkedFileCounts, extension);
                }

                LicenseRenderer renderer = renderers.get(extension);

                try
                {
                    File file = new File(repository.getSourceDir(), filePath);
                    RevCommit firstCommit = revCommitList.get(revCommitList.size() - 1);
                    Date createDate = new Date(firstCommit.getCommitTime() * 1000L);
                    RevCommit lastCommit = revCommitList.get(0);
                    String year2 = dateFormat.format(new Date(lastCommit.getCommitTime() * 1000L));
                    String author = lastCommit.getCommitterIdent().getName();

                    readRepoFile(file, filePath, renderer, createDate, year2, author, context, repository);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     * Called recursively to obtain all entries that make up the repository tree
     * repository - an SVNRepository which interface is used to carry out the
     * request, in this case it's a request to get all entries in the directory
     * located at the path parameter;
     *
     * path is a directory path relative to the repository location path (that
     * is a part of the URL used to create an SVNRepository instance);
     *
     */
    static void processSVNFiles(final ThreadContext context, final Repository repository, final Configuration config, final String path, final long previousRevision, long latestRevision) throws SVNException
    {
        /*
         * Gets the contents of the directory specified by path at the latest
         * revision (for this purpose -1 is used here as the revision number to
         * mean HEAD-revision) getDir returns a Collection of SVNDirEntry
         * elements. SVNDirEntry represents information about the directory
         * entry. Here this information is used to get the entry name, the name
         * of the person who last changed this entry, the number of the revision
         * when it was last changed and the entry type to determine whether it's
         * a directory or a file. If it's a directory listEntries steps into a
         * next recursion to display the contents of this directory. The third
         * parameter of getDir is null and means that a user is not interested
         * in directory properties. The fourth one is null, too - the user
         * doesn't provide its own Collection instance and uses the one returned
         * by getDir.
         */

        SVNRepository svnRepository = context.getSVNRepository();
        final RendererMap renderers = config.getRenderers();

        //noinspection unchecked
        List<SVNDirEntry> entries = new ArrayList<>(svnRepository.getDir(path, -1, null, (Collection) null));
        Collections.sort(entries);
        List<String> directories = new ArrayList<>(entries.size());

        for (final SVNDirEntry entry : entries)
        {
            if (entry.getKind() == SVNNodeKind.FILE)
            {
                String name = entry.getName();
                int index = name.lastIndexOf('.');

                final String extension = index != -1 ? name.substring(index).toLowerCase() : ""; // No extension case
                final String filePath = createFilePath(path, name);

                if (!renderers.containsKey(extension) || !config.isAllowedFile(filePath))
                {
                    addFile(_skippedFileCounts, extension);
                    continue;
                }
                else
                {
                    addFile(_checkedFileCounts, extension);
                }

                LicenseRenderer renderer = renderers.get(extension);

                try
                {
                    svnRepository.log(new String[]{filePath}, previousRevision, latestRevision, false, false, 1, svnLogEntry -> {
                        File file = new File(repository.getSourceDir(), filePath);
                        Extractor extractor = config.getExtractor(context);
                        Date createDate = extractor.getYear1(filePath, svnLogEntry);
                        String year2 = context.getYear(extractor.getYear2(filePath, entry));
                        String author = extractor.getAuthor(filePath, svnLogEntry);

                        readRepoFile(file, filePath, renderer, createDate, year2, author, context, repository);
                    });
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
            else if (entry.getKind() == SVNNodeKind.DIR)
            {
                String filePath = createFilePath(path, entry.getName());

                if (config.isAllowedDirectory(filePath))
                    directories.add(filePath);
            }
        }

        for (String directory : directories)
            exec.execute(new SVNLicenseRunnable(repository, config, directory, previousRevision, latestRevision));
    }


    private static void readRepoFile(File repoFile, String filePath, LicenseRenderer renderer, Date createDate, String year2, String author, ThreadContext context, Repository repository)
    {
        String fileYear1 = null;
        String fileYear2 = null;
        String fileCopyright = null;
        String fileCompany = null;
        boolean hasBasicCopyright = false;
        String beginning;

        String year1 = context.getYear(createDate);

        try
        {
            beginning = context.getBeginning(repoFile);

            // Skip empty files
            if (null == beginning)
                return;

            // Look for a copyright notice that includes (c) first
            Matcher m = copyright.matcher(beginning);

            boolean found = m.find();

            if (!found)
            {
                // Give LicenseSelector a chance to ignore files with non-standard copyright patterns
                if (repository.getLicenseSelector().shouldIgnoreFile(beginning))
                    return;

                // How about without (c)?
                m = lenientCopyright.matcher(beginning);
                found = m.find();
            }

            if (found)
            {
                fileCopyright = m.group(0);
                fileYear1 = m.group(3);
                fileYear2 = null != m.group(7) ? m.group(7) : fileYear1;
                fileCompany = m.group(11);

                // "Company" is over once we see three or more consecutive spaces (helps with raphael-min.js, etc.)
                fileCompany = truncateAfter(fileCompany, "   ");

                // Or a semicolon
                fileCompany = truncateAfter(fileCompany, ";");

                // Or the rest of the license
                fileCompany = truncateAfter(fileCompany, ". All rights reserved.");
            }
            else
            {
                // One last attempt... just look for the word copyright
                Matcher m2 = basicCopyright.matcher(beginning);
                hasBasicCopyright = m2.find();
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Problem with " + repoFile, e);
        }

        // If we're looking at a subset of revisions then we can't determine FHCRC vs. LabKey copyright, so just assume the file company is correct
        String company = previousRevision > 0 ? fileCompany : repository.getLicenseSelector().getCompany(createDate, author);

        // If we change a file (correct the dates or add a copyright) then end date is always the current year
        String recommendedCopyright = "Copyright (c) " + year1 + (year1.equals(currentYear) ? "" : "-" + currentYear) + " " + company;
        Status status;

        if (null == fileCompany)
        {
            if (hasBasicCopyright)
            {
                status = Status.UnrecognizedCopyright;
                recommendedCopyright = "";                  // Leave it alone
            }
            else
                status = Status.NoCopyright;
        }
        else if (!company.equals(fileCompany))
        {
            if (repository.getLicenseSelector().isValidCompany(fileCompany))
            {
                status = Status.WrongCompany;
            }
            else
            {
                if (repository.shouldIgnoreCompany(fileCompany))
                    status = Status.IgnoreCompany;
                else
                    status = Status.UnknownCompany;

                recommendedCopyright = fileCopyright;      // Leave existing copyright
            }
        }
        else if (year1.equals(fileYear1) && (year2.equals(fileYear2)
                || currentYear.equals(fileYear2)))   // Last condition addresses files that have been fixed locally but not yet checked in
        {
            status = Status.Okay;
        }
        else
        {
            status = Status.BadDates;
        }

        // determine if the file has the appropriate license for the repository it is in
        License fileLicense = null;
        boolean fileLongLicense = true;
        // dont attempt to replace license of files that have copyrights held by other companies or have been ignored by config
        if (status != Status.IgnoreCompany && status != Status.UnknownCompany && status != Status.UnrecognizedCopyright)
        {
            Matcher labkeyLicenseMatcher = labkeyLicenseFinder.matcher(beginning);
            if (labkeyLicenseFinder.matcher(beginning).find())
            {
                fileLicense = License.LabKey;
            }
            else if (apacheShortLicenseFinder.matcher(beginning).find())
            {
                fileLicense = License.Apache;
                fileLongLicense = false;
            }
            else if (apacheLongLicenseFinder.matcher(beginning).find())
            {
                fileLicense = License.Apache;
            }
            // if this file has a license see if it matches the license of the repository it is in
            if (null != fileLicense)
            {
                if (renderer.isWrongLicense(fileLicense))
                {
                    status = Status.WrongLicense;
                }
            }
        }

        if (renderer.shouldReport(status))
        {
            StringBuilder correction = new StringBuilder();
            StringBuilder report = new StringBuilder();
            report.append(filePath);
            report.append("\t").append(null != fileCopyright ? fileCopyright : "");
            report.append("\t").append(status);
            report.append("\t").append(recommendedCopyright);

            if (status == Status.WrongLicense)
            {
                // Replace license at the beginning of the file with a different license
                String license = renderer.getLicense(recommendedCopyright);
                if (null != license)
                    renderer.replaceLicense(correction, filePath, fileLicense, fileLongLicense, license, beginning);
            }
            else if (status == Status.BadDates || status == Status.WrongCompany)
            {
                // Update the existing copyright line to correct company and date(s)
                correction.append("sed -i -b \"s/");       // -i means change the file in place, -b means leave the line endings alone
                correction.append(fileCopyright);
                correction.append("/").append(recommendedCopyright);
                if(fileLicense.equals(License.LabKey))
                {
                    // need to add back more text for LabKey license
                    correction.append(". All rights reserved. No portion of this work may be reproduced in");
                }
                correction.append("/\" ");
                correction.append('"').append(filePath).append('"');
            }
            else if (status == Status.NoCopyright)
            {
                // Insert our standard copyright notice at the beginning of the file
                String license = renderer.getLicense(recommendedCopyright);

                if (null != license)
                    renderer.insertNewLicense(correction, filePath, license, beginning);
            }

            _reports.put(filePath, new Report(filePath, report.toString(), correction.length() > 0 ? correction.toString() : null));
        }
    }

    // Discard all characters from separator to the end of the string
    private static String truncateAfter(String s, String separator)
    {
        int idx = s.toLowerCase().indexOf(separator.toLowerCase());
        return (idx > -1 ? s.substring(0, idx) : s);
    }


    static org.eclipse.jgit.lib.Repository getGitRepository(Repository repository, File gitDirFile) throws IOException
    {
        FileRepositoryBuilder fileRepositoryBuilder = new FileRepositoryBuilder();
        return fileRepositoryBuilder.setGitDir(gitDirFile).readEnvironment().build();
    }


    static SVNRepository getSVNRepository(Repository repository) throws SVNException
    {
        SVNRepository svnRepository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(repository.getSvnServer()));
        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager();
        svnRepository.setAuthenticationManager(authManager);

        return svnRepository;
    }


    private static void addFile(ConcurrentMap<String, AtomicInteger> map, String extension)
    {
        if (null != map.putIfAbsent(extension, new AtomicInteger(1)))
            map.get(extension).incrementAndGet();
    }


    private static String createFilePath(String path, String name) throws SVNException
    {
         return path.equals("") ? name : path + "/" + name;
    }
}
