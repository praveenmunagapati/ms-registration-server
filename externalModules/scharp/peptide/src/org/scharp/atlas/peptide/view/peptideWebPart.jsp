<%@ page import="org.labkey.api.view.HttpView"%>
<%@ page import="org.labkey.api.view.ViewContext"%>
<%@ page import="org.labkey.api.view.ActionURL"%>
<%@ page import="org.scharp.atlas.peptide.model.PeptideGroup" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%
    ViewContext context = HttpView.currentContext();
    PeptideGroup[] peptides = (PeptideGroup[]) context.get("peptides");
%>
This container contains <%= peptides.length %> peptide groups.<br>
<%= button("View Grid").href(new ActionURL("Peptide", "showAllPeptideGroups", context.getContainer())) %>