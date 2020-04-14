<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%@ page import="org.labkey.api.view.HttpView"%>
<%@ page import="org.labkey.api.view.JspView"%>

<%@ page import="org.scharp.atlas.elispot.ElispotBaseController.*"%>
<%@ page import="org.scharp.atlas.elispot.model.BatchType" %>
<%@ page import="org.scharp.atlas.elispot.model.StudyLab"%>
<%@ page import="org.scharp.atlas.elispot.EliSpotManager"%>

<%@ page import="java.util.HashMap" %>
<%@ page import="org.scharp.atlas.elispot.EliSpotSchema" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<labkey:errors/>
<%
    try{
        JspView<BatchForm> me = (JspView<BatchForm>) HttpView.currentView();
        BatchForm bean = me.getModelBean();
        BatchType[] batchTypes = EliSpotManager.getBatchTypes();
        StudyLab studyLab = EliSpotManager.getStudyLab(HttpView.currentContext().getContainer(),bean.getLab_study_seq_id());
        HashMap studyMap = EliSpotManager.getStudyDescs(HttpView.currentContext().getContainer());
        HashMap labMap = EliSpotManager.getLabMap(HttpView.currentContext().getContainer());
        if(studyLab != null){

%>
<labkey:form action="insertToBatch.post" method="post">
    <table>
        <h6>Add a Batch to the lab study combo : <%=bean.getLab_study_seq_id()%> : <%=studyMap.get(studyLab.getStudy_seq_id())%> - <%=labMap.get(studyLab.getLab_seq_id())%></h6>
        <tr>
            <td><th>Batch Type : </th></td>
            <td>
                <select name="batch_type">
                    <% for(BatchType batchType : batchTypes){%>
                    <option value="<%=batchType.getBatch_type()%>"><%=batchType.getBatch_type_desc()%></option>
                    <%}%>
                </select>
            </td>
        </tr>
        <tr>
            <td><th>Batch Description : </th></td>
            <td><input type="text" name="batch_description"/> </td>
        </tr>
        <tr><td><th>Lab Study Combination : </th></td>
            <td><input type="hidden" name="lab_study_seq_id" value="<%=bean.getLab_study_seq_id()%>"/>
                <%=studyMap.get(studyLab.getStudy_seq_id())%> - <%=labMap.get(studyLab.getLab_seq_id())%>
            </td>
        </tr>

    </table>
    <%= button("Insert This").submit(true) %>&nbsp;<%= button("Back").href("showData.view?actionType=GRID&&tableName=" + EliSpotSchema.TABLE_BATCH+"&&queryString="+EliSpotSchema.COLUMN_LABSTUDY_SEQ_ID+"&&queryValue="+bean.getLab_study_seq_id()) %>

</labkey:form>
<%}
}
catch(Exception e)
{%>
There is something wrong in the file.
<%=e.getMessage()%>
<%} %>