<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%@ page import="org.labkey.api.view.HttpView"%>
<%@ page import="org.labkey.api.view.ViewContext"%>

<%@ page import="org.scharp.atlas.elispot.model.Lab" %>
<%@ page import="org.scharp.atlas.elispot.model.Study" %>
<%@ page import="org.scharp.atlas.elispot.EliSpotManager" %>

<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%
    try{
    ViewContext context = HttpView.currentContext();
    Study[] studies = EliSpotManager.getStudies(context.getContainer(),true);
    Lab[] labs = EliSpotManager.getLabs(context.getContainer());
%>
Hello, and welcome to the ELISpot module.
<p />
<labkey:form action="./batchSelect.view" method="GET">
<labkey:errors/> 
Your Network organization: <%= context.getContainer().getProject().getName()%>
<input type="hidden" name="networkId" value="<%= context.getContainer().getProject().getName()%>" />
<p />
    <%if(studies != null && studies.length > 0){%>
Select Study: &nbsp;
<select name="studyId">
    <option value=""></option>
    <%for (Study study : studies) {
    //if(study.getStatus().equalsIgnoreCase("ACTIVE")){%>
		<option value="<%= study.getStudy_seq_id() %>"><%=study.getStudy_identifier()%> - <%= study.getStudy_description() %></option>
	<% }//}%>
</select>

<p />
Select Lab: &nbsp;
<select name="labId">
    <option value=""></option>
    <% for (Lab lab : labs) {%>
		<option value="<%=lab.getLab_seq_id()%>"><%=lab.getLab_desc()%></option>
	<% } %>
</select>
<p />
<%= button("Select").submit(true) %>
<%}else{%>
    <h4 style="color:blue;">There are no active studies in this Network.<br> Please Contact the administrator and make sure that they activated the study you are working on.</h4>
    <%}%>
</labkey:form>
<%}
catch(Exception e){
%>
There is some thing wrong on this page.
<%=e.getMessage()%>
<%}%>