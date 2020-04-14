<%--
  User: Mark Igra
  Date: Sep 12, 2007
  Time: 2:19:30 PM
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head><title>Remote Login Sample Client</title></head>
  <body>
    <b>Labkey Remote Login Sample Client</b>
  This web application contains a sample client for using the simple remote login service. This application
  is not intended to be an example of how to build a web application, but the simplest possible example
  of how to use the Java APIs.<br><br>

  The sample includes the following jsp pages
    <ul>
  <li><a href="configure.jsp">configure.jsp</a> allows you to type in a labkey server path that will be used for login & permissions</li>
  <li><a href="permissions.jsp">permissions.jsp</a> forces the user to log onto the configured remote server (if not already logged on) and shows that users permissions for a designated container on that server</li>
  <li><a href="reset.jsp">reset.jsp</a> resets state for this user from this client. Does NOT log user out of remote server</li>

  </ul>
  </body>
</html>