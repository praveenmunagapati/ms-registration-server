package org.labkey.flow.flowjo;

import com.treestar.flowjo.application.engine.FlowJoServerInterface;
import com.treestar.flowjo.engine.FEML;
import com.treestar.lib.Debug;
import com.treestar.lib.auth.AuthCallback;
import com.treestar.lib.auth.AuthRequest;
import com.treestar.lib.file.uri.UriUtil;
import com.treestar.lib.xml.SElement;
import org.labkey.remoteapi.Command;
import org.labkey.remoteapi.CommandException;
import org.labkey.remoteapi.Connection;
import org.labkey.remoteapi.query.ContainerFilter;
import org.labkey.remoteapi.query.ExecuteSqlCommand;
import org.labkey.remoteapi.query.Filter;
import org.labkey.remoteapi.query.Row;
import org.labkey.remoteapi.query.SelectRowsCommand;
import org.labkey.remoteapi.query.SelectRowsResponse;

import javax.swing.*;
import java.io.Console;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * LabKey Server integration for FlowJo.
 *
 */
public class LabKeyServer implements FlowJoServerInterface
{
    private static final String SERVER_NAME = "LabKey";


    private Connection _conn;
    private String _serverURL;
    private String _baseURL;
    private String _contextPath;
    private SElement _serverElt, _protocolElt;

    public LabKeyServer()
    {
        Debug.println("+LabKeyServer.<init>");
    }

    @Override
    public String getServerUrl()
    {
        Debug.println("+LabKeyServer.getServerUrl: " + _serverURL);
        return _serverURL.toString();
    }

    @Override
    public SElement getElement()
    {
        Debug.println("+LabKeyServer.getElement");
        return _serverElt;
    }

    @Override
    public boolean openWorkspace(SElement workspaceElt)
    {
        Debug.println("+LabKeyServer.openWorkspace");

        // find <Server> element in workspace xml
        SElement serversElt = workspaceElt.getChild(FEML.Servers);
        if (serversElt == null) {
            Debug.println("-LabKeyServer.openWorkspace: <Server> element not found");
            return false;
        }

        for (SElement serverElt : serversElt.getChildren(FEML.Server))
        {
            if (!SERVER_NAME.equals(serverElt.getString(FEML.name)))
                continue;

            // found <Server name="LabKey" />
            String serverURL = serverElt.getString(FEML.serverURL);
            if (!setServerURL(serverURL))
            {
                Debug.println("-LabKeyServer.openWorkspace: failed to parse URL: " + serverURL);
                return false;
            }
            _serverElt = new SElement(serverElt);
            try
            {
                processLabKeyRequest(workspaceElt);
                break;
            }
            catch (Exception e)
            {
                Debug.println("-LabKeyServer.openWorkspace: exception thrown");
                Debug.printStackTrace(e);
            }
        }

        Debug.println("-LabKeyServer.openWorkspace: success");
        return true;
    }

    private boolean setServerURL(String serverURL)
    {
        if (serverURL == null || serverURL.length() == 0)
            return false;

        _serverURL = serverURL;
        _baseURL = serverURL;
        _contextPath = null;
        try
        {
            URI uri = new URI(serverURL);
            String path = uri.getPath();
            if (path == null || path.length() == 0 || "/".equals(path))
                _contextPath = "/";
            else
                _contextPath = path;
            _baseURL = serverURL.substring(0, serverURL.length() - _contextPath.length());
            return true;
        }
        catch (URISyntaxException e)
        {
            Debug.printStackTrace(e);
            return false;
        }
    }

    private void processLabKeyRequest(SElement workspaceElt)
    {
        Debug.println("+LabKeyServer.processLabKeyRequest");

        // Get a list of all FCSFile keyword runs that have been previously imported into the server.
        List<Row> runs = getKeywordRuns(null);
        if (runs == null)
        {
            Debug.println("-LabKeyServer.processLabKeyRequest: no flow runs");
            return;
        }

        // CONSIDER: Prompt for Flow protocol folder first, before selecting a Keyword run.
        String folderPath = null;
        int keywordRunId = 0;
        SElement keywordRunElt = null;
        _protocolElt = _serverElt.getChild("Protocol");
        if (_protocolElt == null)
        {
            // Let the user select a keyword run
            Row run = chooseKeywordRun(runs);
            if (run == null)
                return;

            folderPath = (String)run.getValue("Folder/Path");
            keywordRunId = (Integer)run.getValue("RowId");

            _protocolElt = new SElement("Protocol");
            _protocolElt.setAttribute("folderPath", folderPath);
            _serverElt.addContent(_protocolElt);

            keywordRunElt = new SElement("KeywordRun");
            keywordRunElt.setInt("runid", keywordRunId);
            _protocolElt.addContent(keywordRunElt);
        }
        else
        {
            folderPath = _protocolElt.getAttribute("folderPath");
            keywordRunElt = _protocolElt.getChild("KeywordRun");
            keywordRunId = keywordRunElt.getInt("runid");
        }

        if (folderPath == null || keywordRunId == 0)
        {
            Debug.println("-LabKeyServer.processLabKeyRequest: no folderPath or keyword runId");
            return;
        }

        // Fetch list of FCS files
        List<String> fcsURLs = new ArrayList<String>();
        List<SElement> fcsFileElts = keywordRunElt.getChildren("FCSFile");
        if (fcsFileElts == null || fcsFileElts.size() == 0)
        {
            List<Row> fcsFiles = getFCSFiles(folderPath, keywordRunId);
            for (Row fcsFile : fcsFiles)
            {
                Map<String, String> attrs = new HashMap<String, String>();
                attrs.put("name", (String)fcsFile.getValue("Name"));
                attrs.put("rowid", String.valueOf(fcsFile.getValue("RowId")));
                String url = absoluteUrl((String)fcsFile.getValue("DownloadLink"));
                attrs.put("url", url);
                fcsURLs.add(url);
                keywordRunElt.addContent(new SElement("FCSFile", attrs));
            }
        }
        else
        {
            for (SElement fcsFileElt : fcsFileElts)
            {
                fcsURLs.add(fcsFileElt.getString("url"));
            }
        }

        // Determine if workspace element contains samples
        boolean hasSamples = workspaceElt.getChild(FEML.SampleList) != null;
        if (!hasSamples)
            constructMinimalWorkspace(workspaceElt, fcsURLs);

        Debug.println("-LabKeyServer.processLabKeyRequest");
    }

    // absolutize the URL
    private String absoluteUrl(String url)
    {
        if (!url.startsWith("http"))
        {
            if (url.startsWith(_contextPath))
                url = url.substring(_contextPath.length());

            url = _baseURL + _contextPath + url;
        }
        return url;
    }

    private Row chooseKeywordRun(List<Row> runs)
    {
        Debug.println("+LabKeyServer.chooseKeywordRun");

        if (runs == null || runs.isEmpty())
        {
            Debug.println("-LabKeyServer.chooseKeywordRun: no keyword runs");
            return null;
        }

        Map<String, Row> runsByName = new TreeMap<String, Row>();
        ArrayList<String> runNames = new ArrayList<String>();
        for (Row run : runs)
        {
            String runName = (String)run.getValue("Name");
            Integer fcsFileCount = (Integer)run.getValue("FCSFileCount");
            String folderPath = (String)run.getValue("Folder/Path");

            String display = String.format("%s (%s - %d files)", runName, folderPath, fcsFileCount);
            runNames.add(display);
            runsByName.put(display, run);
        }

        // construct a JList for JOptionPane
        JList jlist = new JList();
        String[] runArray = runNames.toArray(new String[runNames.size()]);
        String selectedRunName = (String) JOptionPane.showInputDialog(jlist, "Please select a set of FCS files", "FCS Files", JOptionPane.QUESTION_MESSAGE, null, runArray, runArray[0]);
        if (selectedRunName == null || selectedRunName.isEmpty())
        {
            Debug.println("-LabKeyServer.chooseKeywordRun: no selected run");
            return null;
        }

        Row run = runsByName.get(selectedRunName);
        if (run == null)
        {
            Debug.println("-LabKeyServer.chooseKeywordRun: run not found '%s'", selectedRunName);
            return null;
        }

        Debug.println("-LabKeyServer.chooseKeywordRun: selected run '%s'", selectedRunName);
        return run;
    }

    private void constructMinimalWorkspace(SElement workspaceElt, List<String> fcsURLs)
    {
        System.out.println("+LabKeyServer.constructMinimalWorkspace");
        if (fcsURLs == null || fcsURLs.size() == 0)
        {
            System.out.println("-LabKeyServer.constructMinimalWorkspace: no fcs file URLs");
            return;
        }

        // remove all current children of <Workspace>
        List<SElement> childrenCopy = new ArrayList<SElement>(workspaceElt.getChildren());
        for (SElement e : childrenCopy)
            workspaceElt.remove(e);

        // construct <Servers>, with child <Server>
        SElement serversElem = new SElement(FEML.Servers);
        serversElem.addContent(new SElement(_serverElt));
        workspaceElt.addContent(serversElem);

        // construct <SampleList>, with children <Sample>
        SElement sampleListElem = new SElement(FEML.SampleList);
        workspaceElt.addContent(sampleListElem);
        for (String uri : fcsURLs)
        {
            SElement sampleElem = new SElement(FEML.Sample);
            SElement dataSetElem = new SElement(FEML.DataSet);
            sampleElem.addContent(dataSetElem);
            dataSetElem.setString(FEML.uri, uri);
            sampleListElem.addContent(sampleElem);
        }

        System.out.println("-LabKeyServer.constructMinimalWorkspace");
    }

    @Override
    public void save(SElement sElement)
    {
        Debug.println("+LabKeyServer.save");
    }

    @Override
    public void endSession()
    {
        Debug.println("+LabKeyServer.endSession");
        _conn = null;
    }

    private Connection initConnection(String serverURL)
    {
        if (_conn == null || !_conn.getBaseUrl().equals(serverURL))
        {
            // TODO: Integrate with FlowJo authentication
            //_conn = new Connection(serverURL, username, password);
            _conn = new Connection(serverURL);
        }
        return _conn;
    }

    private List<Row> getRows(Command<? extends SelectRowsResponse> cmd, String folderPath)
    {
        List<Row> ret = null;
        try
        {
            Connection conn = initConnection(_serverURL);
            SelectRowsResponse response = cmd.execute(conn, folderPath);
            ret = new ArrayList<Row>(response.getRowCount().intValue());
            for (Row row : response.getRowset())
                ret.add(row);
        }
        catch (IOException e)
        {
            Debug.printStackTrace(e);
        }
        catch (CommandException e)
        {
            Debug.printStackTrace(e);
        }

        return ret;
    }

    /**
     * Get a list of imported FCSFile runs either within the given folder or across the whole site.
     * @param folderPath
     * @return
     */
    private List<Row> getKeywordRuns(String folderPath)
    {
        SelectRowsCommand cmd = new SelectRowsCommand("flow", "Runs");
        cmd.setColumns(Arrays.asList("RowId", "Name", "FCSFileCount", "Folder", "Folder/Path"));
        cmd.setFilters(Arrays.asList(new Filter("ProtocolStep", "Keywords"), new Filter("FCSFileCount", 0, Filter.Operator.GT)));
        cmd.setExtendedFormat(true);

        // If no folder path, get all flow Keyword runs on the server
        if (folderPath == null) {
            folderPath = "/Home";
            cmd.setContainerFilter(ContainerFilter.AllFolders);
        }

        return getRows(cmd, folderPath);
    }

    private List<Row> getFCSFileURLs(String folderPath, int runId)
    {
        if (folderPath == null || runId <= 0)
            return null;

        SelectRowsCommand cmd = new SelectRowsCommand("exp", "DataInputs");
        cmd.setColumns(Arrays.asList("Data", "Data/DownloadLink"));
        cmd.setFilters(Arrays.asList(
                new Filter("Role", "Data"),
                new Filter("TargetProtocolApplication/Run", runId)));
        cmd.setExtendedFormat(true);

        return getRows(cmd, folderPath);
    }

    private List<Row> getFCSFiles(String folderPath, int runId)
    {
        if (folderPath == null || runId <= 0)
            return null;

        String sql =
                "SELECT\n" +
                "  FCSFiles.RowId,\n" +
                "  FCSFiles.Name,\n" +
                "  FCSFiles.Run,\n" +
                "  DI.Data,\n" +
                "  DI.Data.DownloadLink\n" +
                "FROM\n" +
                "  flow.FCSFiles, exp.ProtocolApplications PA, exp.DataInputs DI\n" +
                "WHERE\n" +
                "  FCSFiles.SourceProtocolApplication.Name = 'Keywords'\n" +
                "  AND FCSFiles.SourceProtocolApplication.RowId = PA.RowId\n" +
                "  AND PA.RowId = DI.TargetProtocolApplication.RowId\n" +
                "  AND FCSFiles.Run.RowId = " + runId;
        ExecuteSqlCommand cmd = new ExecuteSqlCommand("flow", sql);
        cmd.setExtendedFormat(true);
        // Workaround for Issue 16961: ExecuteSql API without maxRows parameter defaults to returning 100 rows
        cmd.setMaxRows(10000);

        return getRows(cmd, folderPath);
    }

    public static void main(String[] args) throws Exception
    {
        String serverURL = args[0];

        LabKeyServer server = new LabKeyServer();
        server.setServerURL(serverURL);

        System.out.printf("Fetching list of runs on server %s...\n", serverURL);
        List<Row> runs = server.getKeywordRuns(null);
        if (runs == null || runs.size() == 0)
        {
            System.out.printf("No runs found on server\n");
            return;
        }

        Map<Integer, Row> runsByRowId = new HashMap<Integer, Row>();
        System.out.printf("Runs:\n");
        for (Row run : runs)
        {
            String runName = (String)run.getValue("Name");
            Integer runId = (Integer)run.getValue("RowId");
            Integer fcsFileCount = (Integer)run.getValue("FCSFileCount");
            String folderPath = (String)run.getValue("Folder/Path");
            System.out.printf("  %d: %s - %s (files %d)\n", runId, runName, folderPath, fcsFileCount);
            runsByRowId.put(runId, run);
        }

        Integer runId;
        Console console = System.console();
        if (console != null)
        {
            String line = console.readLine("Select run id: ");
            runId = Integer.parseInt(line);
        }
        else
        {
            // non-interactive, just choose the first
            runId = runsByRowId.keySet().iterator().next();
        }

        Row run = runsByRowId.get(runId);
        if (run == null)
        {
            System.out.printf("Run '%d' not found\n", runId);
            return;
        }

        String folderPath = (String)run.getValue("Folder/Path");
        System.out.printf("Fetching urls for run %d...\n", runId);
        List<Row> fcsFiles = server.getFCSFiles(folderPath, runId);

        System.out.printf("Files:\n");
        for (Row fcsFile : fcsFiles)
        {
            String name = (String)fcsFile.getValue("Name");
            Integer rowId = (Integer)fcsFile.getValue("RowId");
            String url = (String)fcsFile.getValue("DownloadLink");
            System.out.printf("  %d: %s - %s\n", rowId, name, server.absoluteUrl(url));
        }
    }
}
