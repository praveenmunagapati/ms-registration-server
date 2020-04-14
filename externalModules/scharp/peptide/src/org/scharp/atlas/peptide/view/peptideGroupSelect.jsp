<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%@ page import="org.labkey.api.view.HttpView"%>
<%@ page import="org.labkey.api.view.JspView" %>
<%@ page import="org.scharp.atlas.peptide.PeptideBaseController.PeptideQueryForm" %>
<%@ page import="org.scharp.atlas.peptide.PeptideController" %>
<%@ page import="org.scharp.atlas.peptide.PeptideManager" %>
<%@ page import="org.scharp.atlas.peptide.PeptideSchema" %>
<%@ page import="org.scharp.atlas.peptide.model.ManuFactureStatus" %>
<%@ page import="org.scharp.atlas.peptide.model.PeptideGroup" %>
<%@ page import="org.scharp.atlas.peptide.model.PeptidePool" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%
    JspView<PeptideQueryForm> me = (JspView<PeptideQueryForm>) HttpView.currentView();
    PeptideQueryForm bean = me.getModelBean();
    if(bean.getMessage() != null){
%>
<%=bean.getMessage()%><%}%>
<script type="text/javascript">
    function selectOptions()
    {
        document.PeptideQueryForm.submit();
    }
</script>
<labkey:errors/>
<labkey:form name="PeptideQueryForm" action="searchForPeptides.post" method="post">
    <h4 align="center" style="color:blue">Search for Peptides using different criteria : </h4><br><br>
    <table>
        <tr>
            <td><th>Search Where : </th></td>
            <td>
                <select id="queryKey" name="queryKey" onchange="selectOptions()">
                    <option value="" <%=bean.getQueryKey() == null?" selected" :""%>></option>
                    <option value="<%=PeptideSchema.COLUMN_PEPTIDE_GROUP_ID%>" <%=bean.getQueryKey() != null && bean.getQueryKey().equals(PeptideSchema.COLUMN_PEPTIDE_GROUP_ID)?" selected":""%>>Peptide Group</option>
                    <option value="<%=PeptideSchema.COLUMN_PEPTIDE_POOL_ID%>" <%=bean.getQueryKey() != null && bean.getQueryKey().equals(PeptideSchema.COLUMN_PEPTIDE_POOL_ID)?" selected":""%>>Peptide Pool</option>
                    <option value="<%=PeptideSchema.COLUMN_PEPTIDE_SEQUENCE%>" <%=bean.getQueryKey() != null && bean.getQueryKey().equals(PeptideSchema.COLUMN_PEPTIDE_SEQUENCE)?" selected":""%>>Peptide Sequence</option>
                    <option value="<%=PeptideSchema.COLUMN_QC_PASSED%>" <%=bean.getQueryKey() != null && bean.getQueryKey().equals(PeptideSchema.COLUMN_QC_PASSED)?" selected":""%>>Manufacture Status</option>
                    <option value="<%=PeptideSchema.COLUMN_PEPTIDE_ID%>" <%=bean.getQueryKey() != null && bean.getQueryKey().equals(PeptideSchema.COLUMN_PEPTIDE_ID)?" selected":""%>>Peptide Range</option>
                </select>
            </td>
            <td>
                <th><%=(bean.getQueryKey() != null && bean.getQueryKey().equals(PeptideSchema.COLUMN_PEPTIDE_SEQUENCE))?" Equals / Contains : ":" Equals : "%>
               </th>
            </td>
            <td>

                <%if(bean.getQueryKey() == null || bean.getQueryKey().length() == 0){  %>


                <select id="queryValue" name="queryValue"><option value=""></option></select>

                <%}
                else if(bean.getQueryKey().equals(PeptideSchema.COLUMN_PEPTIDE_GROUP_ID)){
                    PeptideGroup[] peptideGroups = PeptideManager.getPeptideGroups();
                %>

                <select id="queryValue" name="queryValue">
                    <% for(PeptideGroup pepGroup : peptideGroups) { %>

                    <option value="<%=pepGroup.getPeptide_group_id()%>"><%=pepGroup.getPeptide_group_id()%></option>
                    <% } %> </select>

                <% } else if (bean.getQueryKey().equals(PeptideSchema.COLUMN_PEPTIDE_POOL_ID)) {
                    PeptidePool[] peptidePools = PeptideManager.getPeptidePools();
                %>
                <select id="queryValue" name="queryValue">
                    <%for (PeptidePool pepPool : peptidePools) { %>
                    <option value="<%=pepPool.getPeptide_pool_id().toString()%>">PP<%=PeptideController.toLZ(pepPool.getPeptide_pool_id())%> - <%=pepPool.getPool_type() == null ? "":pepPool.getPool_type()%> - <%=pepPool.getDescription() == null ?(pepPool.getMatrix_id() == null ?"":pepPool.getMatrix_id()+"|")+(pepPool.getPeptide_group_id() == null?"":pepPool.getPeptide_group_id()):pepPool.getDescription()%></option>
                    <% }%>
                </select>
                <%}
                else if(bean.getQueryKey().equals(PeptideSchema.COLUMN_PEPTIDE_SEQUENCE)){%>
                <input type="text" name="queryValue" size="20" /> &nbsp;
                <%}
                else if(bean.getQueryKey().equals(PeptideSchema.COLUMN_QC_PASSED)){
                    ManuFactureStatus[] status = PeptideManager.getManufactureStatus();
                %>
                <select id="queryValue" name="queryValue">
                    <option value=""></option>
                    <%for (ManuFactureStatus s : status) { %>
                    <option value="<%=s.getQc_passed()%>"><%=s.getDescription()%></option>
                    <%}%>
                </select>
                <%}
                else if(bean.getQueryKey().equals(PeptideSchema.COLUMN_PEPTIDE_ID)){%>
                <input type="text" name="queryValue" size="20"/> &nbsp;
                <%}%>
            </td>
            <td>
                <input type="submit" name="action_type" value="Get Peptides"/>
            </td>
        </tr>
    </table>
</labkey:form>
