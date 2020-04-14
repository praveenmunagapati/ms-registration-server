<%@ page import="org.labkey.api.view.HttpView"%>
<%@ page import="org.labkey.api.view.ViewContext"%>
<%@ page import="org.labkey.api.view.ActionURL"%>
<%@ page import="org.scharp.atlas.specimentracking.model.Manifests"%>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%
    ViewContext context = HttpView.currentContext();
    Manifests[] manifests = (Manifests[]) context.get("manifests");
%>
This container contains <%= manifests.length %> manifests.<br>
<%= button("View Grid").href(new ActionURL("Specimen_tracking", "begin.view", context.getContainer())) %>