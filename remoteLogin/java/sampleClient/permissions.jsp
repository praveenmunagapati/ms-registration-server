<%@ page import="org.labkey.security.RemoteLogin" %>
<%@ page import="org.labkey.security.RemoteLoginException" %>
<%@ page import="org.labkey.security.RemoteLoginHelper" %>
<%@ page import="java.util.Set" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<%
    //We happen to stash this here for this example. Not part of the API.
    String remoteServer = (String) request.getSession(true).getAttribute("labkey.remoteServer");
    if (null == remoteServer)
        remoteServer = "http://localhost:8080/labkey";

    RemoteLoginHelper rlogin = RemoteLogin.getHelper(request, remoteServer);
    if (!rlogin.isLoginComplete())
    {
        response.sendRedirect(rlogin.getLoginRedirect());
        return;
    }

    String folderPath = request.getParameter("folderPath");
    if (null == folderPath)
        folderPath = "/home";

%>
  <head><title>Simple jsp page</title></head>
  <body>
  Labkey Server Address: <%=remoteServer%><br>
  Email: <%=rlogin.getUserEmail()%><br><br>

  <form action="permissions.jsp" method="get">
      Labkey Folder Path <input name="folderPath" type="text" value="<%=folderPath%>"><br><br>
      <input type="Submit">
  </form>

  <b>Permissions for folder</b><br>
  <%
      Set<RemoteLogin.Permission> permissions = null;
      try
      {
          permissions = rlogin.getPermissions(folderPath);
      } catch (RemoteLoginException e)
      {
          response.getWriter().print("<b>Error getting permissions: </b>" + e.getMessage() + "<br>");
          response.getWriter().print("<pre>");
          e.printStackTrace(response.getWriter());
          response.getWriter().print("</pre>");
      }
      if (null != permissions)
      {
          for (RemoteLogin.Permission perm : RemoteLogin.Permission.values())
          {
  %>
        <%=perm.toString()%>: + <%=permissions.contains(perm)%><br>
      <%
        }
    }
  %>
  </body>
</html>