<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%@ page import="org.labkey.api.security.User"%>
<%@ page import="org.labkey.api.security.permissions.UpdatePermission" %>
<%@ page import="org.labkey.api.view.HttpView" %>
<%@ page import="org.labkey.api.view.ViewContext" %>
<%@ page import="org.scharp.atlas.pepdb.PepDBController" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>

<%  ViewContext ctx = getViewContext();
    User user = ctx.getUser(); %>
<h4>If you see some of the links disabled then you don't have permission to enter data.
    If you need to enter any data contact Atlas Administrator.</h4>
<h3 style="color:blue;font:italic">Peptide Groups : </h3>
<ul>
	<li><a href="showAllPeptideGroups.view">List Peptide Groups</a></li>
    <%if(ctx.getContainer().hasPermission(user, UpdatePermission.class)){%>
    <li><a href="insertPeptideGroup.view">Insert a New Group</a></li>
    <%}else{%>
    <li>Insert a New Group</li>
    <%}%>
</ul>
<h3 style="color:blue;font:italic">Peptides : </h3>
<ul>
	<li><a href="searchForPeptides.view">Search for Peptides by Criteria</a></li>
    <%if(ctx.getContainer().hasPermission(user, UpdatePermission.class)){%>
    <li><a href="importPeptides.view">Import Peptides</a></li>
    <%}else{%>
    <li>Import Peptides</li>
    <%}%>
    <li><a href="displayResult.view">Peptides From Last Import</a></li>
</ul>
<labkey:errors/>
<%
    PepDBController.DisplayPeptideForm form = (PepDBController.DisplayPeptideForm) (HttpView.currentModel());
%>
<labkey:form action="displayPeptide.view" method="get">
Lookup Peptide by Id: <input type="text" name="peptide_id" size="10" value="<%=form.getPeptide_id()== null ?"":form.getPeptide_id()%>"/> &nbsp; <%= button("Find").submit(true) %>
</labkey:form>
<p>
<h3 style="color:blue;font:italic">Peptide Pools :</h3>
<ul>
    <%if(ctx.getContainer().hasPermission(user, UpdatePermission.class)){%>
    <li><a href="importPeptidePools.view">Import Peptide Pools</a> </li>
    <%}else{%>
    <li>Import Peptide Pools</li>
    <%}%>
    <li><a href="showAllPeptidePools.view">List All Peptide Pools</a></li>
</ul>
