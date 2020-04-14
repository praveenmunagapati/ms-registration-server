<%@ page import="org.labkey.security.RemoteLogin" %>
<%@ page import="org.labkey.security.RemoteLoginHelper" %>
<html>
<head><title>Reset</title></head>
<%
    //We happen to stash this here for this example. Not part of the API.
    String remoteServer = (String) request.getSession(true).getAttribute("labkey.remoteServer");
    if (null == remoteServer)
        remoteServer = "http://localhost:8080/labkey";

    RemoteLoginHelper helper = RemoteLogin.getHelper(request, remoteServer);
    if (helper.isLoginComplete())
        helper.reset();
%>
<body>
    Login info for remote server has been reset. <br>
    Note that this does <i>not</i> log the user
    out of the labkey server. To log out of the remote server click <a href="<%=remoteServer%>/login/logout.view">here.</a><br><br>

    <a href="index.jsp">Home</a><br>
    <a href="permissions.jsp">Check permissions</a>
</body>
</html>