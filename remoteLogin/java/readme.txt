A Java library and simple sample application that uses the LabKey Server remote login API.

To deploy and test the library & sample app:

1. Type "ant build" in this directory to build the library and sample application into the standard labkey build directory.
2. Deploy the webapp on Tomcat
   - Copy the sampleClient/remoteLogin.xml file to the appropriate directory (e.g., /tomcat/conf/Catalina/localhost)
   - Change the docBase path to point to the sampleClient you just build
3. Make sure your LabKey Server is running
4. Point your browser at the remoteLogin webapp (e.g., http://localhost:8080/remoteLogin) and follow the instructions