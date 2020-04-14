<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%@ page import="org.labkey.api.view.HttpView"%>
<%@ page import="org.labkey.api.view.ViewContext"%>

<%@ page import="org.scharp.atlas.elispot.model.Batch" %>
<%@ page import="org.scharp.atlas.elispot.EliSpotManager" %>
<%@ page import="org.scharp.atlas.elispot.ElispotBaseController.*" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<labkey:errors/>
<%
    try{
    ViewContext context = HttpView.currentContext();
    StudyLabBatchForm form = (StudyLabBatchForm) (HttpView.currentModel());
    Batch[] batches = EliSpotManager.getBatchInformation(HttpView.currentContext().getContainer(), form.getLabstudyseqId());
     if(batches != null && batches.length >0){
%>

<labkey:form action="./batchMenu.view" method="GET">

<p />
Select Batch: &nbsp;
<select name="batchId">
	<% for (Batch batch : batches) { %>
		<option value="<%= batch.getBatch_seq_id() %>"><%= batch.getBatch_description() %></option>
	<% } %>
</select>
<input type="hidden" name="networkId" value="<%= context.getContainer().getProject().getName()%>" />
<input type="hidden" name="labstudyseqId" value="<%=form.getLabstudyseqId()%>"/>
<p />
<%= button("Select").submit(true) %>

</labkey:form>
<%}else%>
<h4 style="color:blue;">There are no batches entered in this lab study combination. Please contact the administrator for the details.</h4>
<%}
catch(Exception e){%>
<%}%>