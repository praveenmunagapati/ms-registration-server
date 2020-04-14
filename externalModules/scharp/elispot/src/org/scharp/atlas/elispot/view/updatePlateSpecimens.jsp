<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%@ page import="org.labkey.api.view.HttpView"%>
<%@ page import="org.labkey.api.view.JspView"%>

<%@ page import="org.scharp.atlas.elispot.EliSpotManager"%>

<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="java.util.HashMap" %>
<%@ page import="org.scharp.atlas.elispot.ElispotBaseController.*" %>
<%@ page import="org.scharp.atlas.elispot.model.*" %>
<%@ page import="org.labkey.api.data.Container" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<labkey:errors/>
<%
    try {
        JspView<UpdatePSpecimensForm> me = (JspView<UpdatePSpecimensForm>) HttpView.currentView();
        UpdatePSpecimensForm bean = me.getModelBean();
        Container c = HttpView.currentContext().getContainer();
        Plate plate = EliSpotManager.getPlate(c, bean.getPlate_seq_id());
        PlateSpecimens [] plateSpecimens = EliSpotManager.getPlateSpecimens(bean.getPlate_seq_id());
        BatchInformation batchInfo = EliSpotManager.getBatchInfo(bean.getBatch_seq_id());
        Specimen[] specimens = EliSpotManager.getSpecimens(c,batchInfo.getStudy_seq_id());
        HashMap<Integer,String> specMap = new HashMap<Integer,String>();
        for(Specimen spec : specimens)
        {
            specMap.put(spec.getSpecimen_seq_id(),spec.getPtid());
        }
        CellCounter[] cellCounters = EliSpotManager.getCellCounters();
        if(plateSpecimens.length > 0){

%>

<labkey:form action="editCellCounts.post" method="post">
    <input type="hidden" name="plate_seq_id" value="<%=plate.getPlate_seq_id()%>"/>
    <input type="hidden" name="batch_seq_id" value="<%=bean.getBatch_seq_id()%>"/>
    <input type="hidden" name="lab_study_seq_id" value="<%=bean.getLab_study_seq_id()%>"/>
    <table><tr><td>
    <th>The specimen Information for Plate : <%=plate.getPlate_name()%> in Batch : <%=batchInfo.getBatch_description()%></th>
    </td></tr></table>
    <table>
        <td><th>Date Plated : </th></td>
        <td>
            <input type="text" name="test_date" value="<%=(plate.getTest_date()== null||plate.getTest_date().toString().length() ==0)?"":new SimpleDateFormat("dd-MMM-yyyy").format(plate.getTest_date())%>" <%=plate.isBool_report_plate()?"disabled":""%>/>
            <span style="display:inline;padding-left:2px;color:blue;font:bold">(dd-MMM-YYYY) Enter Date Plated.
                    The Date Plated must be before or same as current Date.</span>
        </td>
    </table>
    <br><br><br>
    <table border="2">
        <tr>
            <th>Specimen Position</th>
            <th>Specimen</th>
            <th>Cell Counter</th>
            <th>d1_CellCount <br>(10^6)</th>
            <th>d1_Viability <br>(%)</th>
            <th>d2_CellCount <br>(10^6)</th>
            <th>d2_Viability <br>(%)</th>
        </tr>
        <% for (int i = 0;i<plateSpecimens.length;i++) {
                PlateSpecimens pSpecimen = plateSpecimens[i];
                String counter1 = ((pSpecimen.getCounter_seq_id() != null && StringUtils.trimToNull(pSpecimen.getCounter_seq_id().toString()) != null) ? pSpecimen.getCounter_seq_id().toString() : "");
                String counterseqId = (bean.getCounterseqIds() != null && bean.getCounterseqIds().length>i && StringUtils.trimToNull(bean.getCounterseqIds()[i]) != null ? bean.getCounterseqIds()[i] : counter1);
                String d1Cellcount1 = (StringUtils.trimToNull(pSpecimen.getD1_cellcount())) == null ? "": pSpecimen.getD1_cellcount();
                String d1Cellcount = (bean.getD1_cellcounts() != null &&bean.getD1_cellcounts().length > i&&bean.getD1_cellcounts()[i] != null)?bean.getD1_cellcounts()[i]:d1Cellcount1;
                String d1Viability1 = (StringUtils.trimToNull(pSpecimen.getD1_viability()) != null?pSpecimen.getD1_viability():"");
                String d1Viability = (bean.getD1_viabilities() != null &&bean.getD1_viabilities().length > i&&bean.getD1_viabilities()[i] != null)?bean.getD1_viabilities()[i]:d1Viability1;
                String d2Cellcount1 = (StringUtils.trimToNull(pSpecimen.getD2_cellcount()) != null?pSpecimen.getD2_cellcount():"");
                String d2Cellcount = (bean.getD2_cellcounts() != null &&bean.getD2_cellcounts().length > i&&bean.getD2_cellcounts()[i] != null)?bean.getD2_cellcounts()[i]:d2Cellcount1;
                String d2Viability1 = (StringUtils.trimToNull(pSpecimen.getD2_viability()) != null?pSpecimen.getD2_viability():"");
                String d2Viability = (bean.getD2_viabilities() != null &&bean.getD2_viabilities().length > i&&bean.getD2_viabilities()[i] != null)?bean.getD2_viabilities()[i]:d2Viability1; 
        %>
        <tr>
            <td align="1">
                <input type="hidden" name="specwellgroups" value="<%=pSpecimen.getSpec_well_group()%>"/>
                <%=pSpecimen.getSpec_well_group()%>
            </td>

            <%if(!(pSpecimen.getSpecimen_seq_id()==null)){%>
             <td align="1">
                <input type="hidden" name="specimenseqIds" value="<%=pSpecimen.getSpecimen_seq_id()%>" />
                <%=pSpecimen.getSpecimen_seq_id() == null?"":specMap.get(pSpecimen.getSpecimen_seq_id())%>
            </td>
            <td align="1">
                <select name="counterseqIds"  <%=plate.isBool_report_plate()?"disabled":""%> >
                    <option value=""></option>
                    <%for(CellCounter cc : cellCounters){%>
                    <option value="<%=cc.getCounter_seq_id()%>" <%=cc.getCounter_seq_id().toString().equals(counterseqId)?"selected":""%>><%=cc.getCounter_desc()%></option>
                    <%}%>
                </select>
            </td>
            <td align="1">
                <input type="text" size="10" name="d1_cellcounts" value="<%=d1Cellcount%>" <%=plate.isBool_report_plate()?"disabled":""%> />
            </td>
            <td align="1">
                <input type="text" size="10" name="d1_viabilities" value="<%=d1Viability%>" <%=plate.isBool_report_plate()?"disabled":""%> />
            </td>
            <td align="1">
                <input type="text" size="10" name="d2_cellcounts" value="<%=d2Cellcount%>" <%=plate.isBool_report_plate()?"disabled":""%> />
            </td>
            <td align="1">
                <input type="text" size="10" name="d2_viabilities" value="<%=d2Viability%>" <%=plate.isBool_report_plate()?"disabled":""%>/>
            </td>
            <%}%>
        </tr>
        <%}%>
    </table>
    <br/><br/>
    <%if(!plate.isBool_report_plate()){%>
    <%= button("Update Specimens").submit(true) %> &nbsp;&nbsp;&nbsp;
    <%}%>
</labkey:form>
<%}
else{ %>
There are no specimens associated with this plate <%=plate.getPlate_name()%> .
<%}%>
<br/><br/>
<%= button("Go Back").href("displayBatchSummary.view?batchId=" + bean.getBatch_seq_id()+"&&labstudyseqId="+batchInfo.getLab_study_seq_id()) %>&nbsp;
<%}
catch(Exception e)
{%>
There is something wrong on this page.<%=e.getMessage()%>
<%}%>
