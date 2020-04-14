<%@ page import="org.labkey.api.view.HttpView"%>
<%@ page import="org.labkey.api.view.ViewContext"%>
<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%@ page import="org.scharp.atlas.elispot.EliSpotManager" %>
<%@ page import="org.scharp.atlas.elispot.model.*" %>
<%@ page import="org.scharp.atlas.elispot.ElispotBaseController.*" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<% try {
    ViewContext context = HttpView.currentContext();
    StudyLabBatchForm form = (StudyLabBatchForm) (HttpView.currentModel());
    BatchInformation batchInfo = EliSpotManager.getBatchInfo(form.getBatchId());
%>
<div>
    ELISpot module:
    <table>
        <tr><td><th>Network Name: </th></td>
            <td><%=context.getContainer().getProject().getName()%></td>
        </tr>
        <tr><td><th>Study Name: </th></td>
            <td><%=batchInfo.getStudy_identifier()%>--<%=batchInfo.getStudy_description()%></td>
        </tr>
        <tr><td><th>Lab Name: </th></td>
            <td><%=batchInfo.getLab_desc()%></td>
        </tr>
        <tr><td><th>Batch Name: </th></td>
            <td><%=batchInfo.getBatch_description()%></td>
        </tr>

    </table>
</div>
<div>
    <p/>
    <%=link("View Batch Summary").href("displayBatchSummary.view?batchId=" + form.getBatchId()+"&&labstudyseqId="+form.getLabstudyseqId()) %>

    <br>

    <labkey:errors/>

    <p/>
</div>
<labkey:form name="BatchFileForm" action="batchMenu.post " method="POST" enctype="multipart/form-data">
    <input type="hidden" name="batchId" value="<%=form.getBatchId()%>" />
    <input type="hidden" name="labstudyseqId" value="<%=form.getLabstudyseqId()%>"/>
    <input type="hidden" name="networkId" value="<%=context.getContainer().getProject().getName()%>" />
    <input type="hidden" name="studyId" value="<%= form.getStudyId()%>" />
    <input type="hidden" name="labId" value="<%= form.getLabId()%>" />
    <table>
        <tr>  <td><th> Select Reader Type: </th></td>
            <%if(batchInfo.getReader_seq_id()==null){
                Reader[] readerObjs = EliSpotManager.getReaderInformation();
            %>
            <td><select name="reader_id">
                <option value=""></option>
                <% for (Reader reader:  readerObjs) { %>
                <option value="<%=reader.getReader_seq_id()%>"><%=reader.getReader_type() %>
                </option>
                <% } %>
            </select> </td>
            <%}else{
                Reader reader = EliSpotManager.getReaderInformation(batchInfo.getReader_seq_id());%>
            <td><input type="hidden" name="reader_id" value="<%=batchInfo.getReader_seq_id()%>"/>
                <%=reader.getReader_type()%>
            </td>
            <%}%>
        </tr>
        <tr>
            <td><th>File Upload: </th></td>
            <td><input type="file" name="batchFile"/></td></tr>
        <br />
    </table>
    <%= button("Import").submit(true) %> &nbsp;
    <%= button("Go Back").href("batchSelect.view?studyId="+form.getStudyId()+"&&labId="+form.getLabId())%>
</labkey:form>
<%}
catch(Exception e){%>
There is some thing wrong in the JSP Page
<%=e.getMessage()%>
<%}%>