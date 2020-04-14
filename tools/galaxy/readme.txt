This directory contains the Java code and XML configuration files for a WorkflowComplete Galaxy task.
The task is driven by a properties file that specifies a directory path and a URL. The task copies the
other input files to the path and invokes the URL to signal LabKey Server that the workflow is complete.

Configuration instructions:

- Create a new tools/labkey directory in your Galaxy installation
- Compile WorkflowComplete.java to WorkflowComplete.class
- Copy WorkflowComplete.class and workflow_complete.xml to your tools/labkey directory
- Modify your tools_conf.xml file in the root of your Galaxy installation, adding the new <section>
  in tools_conf_labkey.xml
- Restart your Galaxy server

A new LabKey section should appear, providing access to the WorkflowComplete task.
