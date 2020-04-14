import java.io.*;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Date;
import java.util.Properties;

/**
 * User: adam
 * Date: Sep 17, 2010
 * Time: 4:03:01 PM
 */
public class WorkflowComplete
{
    private static File _logFile;

    public static void main(String[] args)
    {
        try
        {
            // Inputs to galaxy (such properties.xml) have been imported as linked files, so properties.xml's parent
            // is the pipeline directory for this run.  Write the log and copy reads file to this directory.
            File properties = new File(args[0]);
            File pipelineDir = properties.getParentFile();
            _logFile = new File(pipelineDir, "workflow_complete.log");

            log("Working Directory = " + System.getProperty("user.dir"));
            log("WorkflowComplete task started at " + new Date());
            log("Properties file: " + properties.getPath());
            log("Pipeline directory: " + pipelineDir.getPath());

            // Grab matches file from command line
            File matches = new File(args[1]);
            log("Matches file: " + matches.getPath());

            if (args.length > 2)
            {
                log("Random arg: " + args[2]);
            }

            // Copy matches file to pipeline directory
            copyFile(matches, new File(pipelineDir, "matches.txt"));

            // Load properties from properties.xml
            Properties props = new Properties();
            InputStream is = new FileInputStream(properties);
            props.loadFromXML(is);
            is.close();

            // If a "completionFilename" property exists then create an empty file with that name. Signalling LabKey Server
            // via HTTP is the primary, preferred mechanism, however, signalling via a file is an option for development
            // configurations where the Galaxy Server can't communicate back to LabKey Server via HTTP.
            String completionFilename = (String)props.get("completionFilename");

            if (null != completionFilename)
            {
                File completionFile = new File(pipelineDir, completionFilename);
                log("Attempting to write completion file: " + completionFile.getPath());
                boolean success = completionFile.createNewFile();

                if (!success)
                    log("Warning: completion file already exists!");
            }

            // POST to the provided URL to signal LabKey Server that the workflow is complete
            // TODO: Code below has not been tested!! Updated to POST via HttpClient on 2019-02-13. See WorkflowCompletionMonitor.java.
            String url = (String)props.get("url");
            log("Attempting to signal LabKey server at " + url);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
            BodyHandler<String> bodyHandler = HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
            HttpResponse<String> response = client.send(request, bodyHandler);
            String message = response.body();
            log("LabKey response: \"" + message + "\"");
        }
        catch (Throwable t)
        {
            log(t);
        }
        finally
        {
            log("WorkflowComplete task ended at " + new Date());
        }
    }


    // Primary log method.  All other log methods call this to route their output to the specified places.
    private static void log(String message)
    {
        appendToLogFile(message);
        System.out.println(message);
    }


    // Run the specified command and direct standard out and standard err output to the log.  This helped debug some
    // permissions issues; not currently used.
    private static void run(String command) throws IOException
    {
        Process process = Runtime.getRuntime().exec(command);
        InputStream standardOut = process.getInputStream();
        log(standardOut);
        InputStream standardErr = process.getErrorStream();
        log(standardErr);
    }


    private static void log(InputStream is) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        byte[] buffer = new byte[10000];
        
        while ((is.read(buffer)) > 0)
            sb.append(new String(buffer));

        log(sb.toString());
    }


    private static void appendToLogFile(String message)
    {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(_logFile, true)))
        {
            bw.write(message);
            bw.newLine();
            bw.flush();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }


    private static void log(Throwable t)
    {
        log(t.getMessage());

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);

        log(sw.toString());
    }


    // From org.labkey.api.util.FileUtil
    private static void copyFile(File src, File dst) throws IOException
    {
        dst.createNewFile();
        FileInputStream is = null;
        FileOutputStream os = null;
        FileChannel in = null;
        FileLock lockIn = null;
        FileChannel out = null;
        FileLock lockOut = null;

        try
        {
            is = new FileInputStream(src);
            in = is.getChannel();
            lockIn = in.lock(0L, Long.MAX_VALUE, true);
            os = new FileOutputStream(dst);
            out = os.getChannel();
            lockOut = out.lock();
            in.transferTo(0, in.size(), out);
            os.getFD().sync();
            dst.setLastModified(src.lastModified());
        }
        finally
        {
            if (null != lockIn)
                lockIn.release();
            if (null != lockOut)
                lockOut.release();
            if (null != in)
                in.close();
            if (null != out)
                out.close();
            if (null != os)
                os.close();
            if (null != is)
                is.close();
        }
    }
}
