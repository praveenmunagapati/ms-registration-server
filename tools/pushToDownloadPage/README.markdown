## pushToDownloadPage.py
An application which will push new installers and binary distribution files to the Download pages for each customer and the general public.

The application will be used by LabKey staff to push installers/binary distrbution files to each customer's download page and to the download locations on labkey.org and labkey.com. The application can push 5 types of download files

- sprint: Installers/Dist files created at the end of sprint.  These bits will be used by our customers to test new features currently under development 
- beta: Installers/Dist files created during stabilization of the release (usually are produced after the release branch has been created)
- release: Installers/Dist files from current stable branch
- release-modules: Installers/Dist file from current stable modules branch
- trunk: Installer/Dist files from the trunk

Each customer who either has support contract or has customer installers created for them on TeamCity, will be given a Customer Download Page on labkey.org.  The download wiki pages will live in the Customer's Project on labkey.org. When this script executed, it will push the installers/dist files for each customer to S3, update the text on the Customer's download pages and post a message to the Customer's download page message board.

The text and formatting of the wiki and messageboards is handled both in the script and in template files located in the templates directory. By default, there is a single template for all customers: 

- releaseWikiContent.html: Template used to create content for the release wiki
- devWikiContent.html: Template used to create content for the development wiki
- message.html: Template used to create content for the message board message

If you would like to have a custom message for a customer, all you need to do is 

- For each template you want to customize 
-- Copy original template to a new file named CUSTOMER-TEMPLATENAME.html 
--- where CUSTOMER is the value in the Customer Name column for the Customers List (see below for list location)
--- where TEMPLATENAME is the name of the template you want to customize.
--- For example, if you want to create a custom version of the release wiki for the customer named "brian", the file would be named brian-releaseWikiContent.html
-- Edit the new file.


If you do not specify a Customer on the command line, then the script will update the Customer Download pages for all Customer's and the General Public's download pages on labkey.org and labkey.com


## How to use the script: 

	pushToDownloadPage.py [-s sprint] [-c customer name] [-b build id] update-type 
	
	where 
		-s, --sprint: Sprint Number. Must be used if you using a update-type = sprint 
		-b, --buildid: Build Id from TeamCity. If not specified we will push the lastSuccessful installers 
		-d, --do-not-download: Skip the download from teamcity and use the previously downloaded build
		-c, --customer: Customer name. If not specified we will push to all Customer's
		update-type: This can be one of 4 options. 
			- sprint: Installers/Dist files created at the end of sprint. These bits will be used by 
				our customers to test new features currently under development 
			- beta: Installers/Dist files created during stabilization of the release 
				(usually are produced after the release branch has been created)
			- release: Installers/Dist files from current stable branch
			- release-modules: Installers/Dist file from current stable modules branch
			- trunk: Installer/Dist files from the trunk


The list of customers with their own download page is located in the "Customers" list in https://www.labkey.org/_lkops/ folder. The version numbers for release and beta is located in the "Releases" list in https://www.labkey.org/_lkops/ folder.  This information is read to build the TeamCity URL for downloading the installers/dist files. 

A spec for this application is available at [https://docs.google.com/a/labkey.com/document/d/1uX09sh4tbCjOBZcuEaNNfWy8h90a7mvSSA-eBbnRk1E/edit?hl=en_US](https://docs.google.com/a/labkey.com/document/d/1uX09sh4tbCjOBZcuEaNNfWy8h90a7mvSSA-eBbnRk1E/edit?hl=en_US)

Written by B.Connolly
LabKey
