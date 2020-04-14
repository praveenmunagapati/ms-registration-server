<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%@ page import="org.labkey.api.view.HttpView"%>
<%@ page import="org.labkey.api.view.ViewContext"%>
<%@ page import="org.scharp.atlas.elispot.EliSpotSchema" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%
    ViewContext context = HttpView.currentContext();
%>
Hello, and welcome to the ELISpot module.
<p />
<labkey:form action="" method="GET">
Your Network organization: <%= context.getContainer().getProject().getName()%>
<input type="hidden" name="networkId" value="<%= context.getContainer().getProject().getName()%>" />
<p />
 <h6>List of tables : </h6>
<ul>

    <li >tblstudy : <a href="showData.view?tableName=<%=EliSpotSchema.TABLE_STUDIES%>">View Data</a> ||
                    <a href="insertToStudy.view">Insert Data</a></li>
    <li >tbllabs : <a href="showData.view?tableName=<%=EliSpotSchema.TABLE_LABS%>">View Data</a> ||
                    <a href="insertToLabs.view">Insert Data</a></li>
    <li >tblstudylabs : <a href="showData.view?tableName=<%=EliSpotSchema.TABLE_STUDY_LABS%>">View Data</a> ||
                    <a href="insertToStudyLabs.view">Insert Data</a></li>
    <li>tblplatetemplate : <a href="showData.view?tableName=<%=EliSpotSchema.TABLE_PLATE_TEMPLATE%>">View Data</a> ||
                            <a href="insertToPlateTemplate.view">Insert Data</a></li>
    <li>tblspecimen : <a href="showData.view?tableName=<%=EliSpotSchema.TABLE_SPECIMEN%>">View Data</a> ||
                        <a href="insertToSpecimen.view">Insert Data</a></li>
</ul>
<p />
    <br><br>
    <p>
        <li>tblplatetemplatedetails : <a href="showData.view?tableName=<%=EliSpotSchema.TABLE_PLATETEMPLATE_DETAILS%>">View Data</a> ||
        <a href="importPTDetails.view">Import Data</a> </li>

</labkey:form>
