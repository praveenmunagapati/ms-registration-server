<html>
<head>
    <title>Configure Remote Server</title>
</head>
<body>
<h1>Configure Remote Server</h1>
Use this page to specify remote labkey server. Path should be full path including protocol, port (if not default) and context path.
<%
    String remoteServer = request.getParameter("remoteServer");
    boolean changed = null != remoteServer;
    //NOTE: We stash the remote server in session. NOT part of the API, just convenient
    if (changed)
    {
        request.getSession(true).setAttribute("labkey.remoteServer", remoteServer);
        out.println("Remote server changed<br>");
    }
    else
        remoteServer = (String) request.getSession(true).getAttribute("labkey.remoteServer");
    
    if (null == remoteServer)
        remoteServer = "http://localhost:8080/labkey";
%>
<form action="configure.jsp" method="get">
    <input type="text" name="remoteServer" size="80" value="<%=remoteServer%>">
    <input type="submit">
</form>
<br>
<a href="index.jsp">Home</a><br>
<a href="permissions.jsp">Check permissions</a>
</body>
</html>