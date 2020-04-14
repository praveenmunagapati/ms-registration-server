<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%@ page import="org.labkey.api.security.User"%>
<%@ page import="org.labkey.api.security.permissions.UpdatePermission" %>
<%@ page import="org.labkey.api.view.HttpView" %>
<%@ page import="org.labkey.api.view.ViewContext" %>
<%@ page import="org.scharp.atlas.peptide.PeptideController" %>
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
    <li><a href="showPeptidesToBeMade.view">List Peptides to be Made</a></li>
    <%if(ctx.getContainer().hasPermission(user, UpdatePermission.class)){%>
    <li><a href="importPeptides.view">Import Peptides/Manufacture Status</a></li>
    <%}else{%>
    <li>Import Peptides/Manufacture Status</li>
    <%}%>
</ul>
<labkey:errors/>
<%
    PeptideController.DisplayPeptideForm form = (PeptideController.DisplayPeptideForm) (HttpView.currentModel());
%>
<labkey:form action="displayPeptide.view" method="get">
Lookup Peptide by Id: <input type="text" name="peptideId" size="10" value="<%=form.getPeptideId()== null ?"":form.getPeptideId()%>"/> &nbsp; <%= button("Find").submit(true) %>
</labkey:form>
<p>
<h3 style="color:blue;font:italic">Peptide Pools :</h3>
<ul>
	<li><a href="poolManufactureStatus.view">Manufacture Status</a></li>
    <%if(ctx.getContainer().hasPermission(user, UpdatePermission.class)){%>
    <li><a href="importPeptidePools.view">Import New/Existing Pools</a> </li>
    <li><!-- href="createPeptidePool.view"-->Create Matrix Peptide Pools</li>
    <%}else{%>
    <li>Import New/Existing Pools</li>
    <%}%>

</ul>
