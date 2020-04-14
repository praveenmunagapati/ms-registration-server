<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%@ page import="org.labkey.api.view.HttpView"%>
<%@ page import="org.labkey.api.view.JspView"%>

<%@ page import="org.scharp.atlas.elispot.ElispotBaseController.*"%>
<%@ page import="org.scharp.atlas.elispot.EliSpotManager"%>

<%@ page import="java.util.ArrayList"%>
<%@ page import="org.scharp.atlas.elispot.model.*" %>
<%@ page import="org.labkey.api.data.Container" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<script type="text/javascript">
    function selectAll(txt,name)
    {
        var x = document.getElementsByName(name);
        for(i =0;i<x.length;i++)
        {
            document.getElementsByName(name)[i].value = txt.value;
        }
    }
</script>
<labkey:errors/>
<%
    try {
        JspView<UpdatePInfoForm> me = (JspView<UpdatePInfoForm>) HttpView.currentView();
        UpdatePInfoForm bean = me.getModelBean();
        Container c = HttpView.currentContext().getContainer();
        BatchInformation batchInfo = EliSpotManager.getBatchInfo(bean.getBatch_seq_id());
        Plate[] plates = EliSpotManager.getPlatesInfo(c,bean.getBatch_seq_id());
        Substrate[] substrates = EliSpotManager.getSubstrates();
        PlateType[] platetypes = EliSpotManager.getPlateTypes();
        if(plates.length > 0){

%>

<labkey:form action="updatePlateInfo.post" method="post">
<input type="hidden" name="batch_seq_id" value="<%=bean.getBatch_seq_id()%>"/>
<input type="hidden" name="lab_study_seq_id" value="<%=bean.getLab_study_seq_id()%>"/>
<table><tr><td>
    <th>The Plates Information for Batch : <%=batchInfo.getBatch_seq_id()%> - <%=batchInfo.getBatch_description()%></th>
</td></tr></table>
<br><br><br>
<table border="2">
    <tr><th colspan="5">If you choose Apply to all plates options that selection will be applied to all the plates in the batch.</th></tr>
    <tr>
        <th>Plate Id</th>
        <th>Plate Name</th>
        <th>Is Precoated</th>
        <th>Plate Manufacturer/Type</th>
        <th>Substrate</th>

    </tr>
    <%
        ArrayList<Plate> approvedPlates = new ArrayList<Plate>();
        for(Plate p : plates){
            if(p.isBool_report_plate())
                approvedPlates.add(p);
        }
        if(approvedPlates.size() == 0){
    %>

    <tr>
        <th colspan="2" style="font-style:oblique;">Apply selection to all Plates</th>
        <td align="1">
            <select name="preCoatedAll" onchange="selectAll(this,'preCoated')">
                <option value=""></option>
                <option value="true" >Yes</option>
                <option value="false">No</option>
            </select>All
        </td>
        <td align="1">
            <select name="plateTypesAll" onchange="selectAll(this,'plateTypes')">
                <option value=""></option>
                <%for(PlateType pt : platetypes){%>
                <option value="<%=pt.getPlatetype_seq_id()%>"><%=pt.getPlatetype_desc()%> </option>
                <%}%>
            </select>All
        </td>
        <td align="1">
            <select name="substratesAll" onchange="selectAll(this,'substrates')">
                <option value=""></option>
                <%for(Substrate subst : substrates){%>
                <option value="<%=subst.getSubstrate_seq_id()%>"><%=subst.getSubstrate_desc()%> </option>
                <%}%>
            </select>All
        </td>
    </tr>
    <%}%>
    <% for (int i = 0;i<plates.length;i++) {
        Plate plate = plates[i];
        String substrate = (plate.getSubstrate_seq_id() != null && StringUtils.trimToNull(plate.getSubstrate_seq_id().toString()) != null) ? plate.getSubstrate_seq_id().toString() : "";
        String platetype = (plate.getPlatetype_seq_id() != null && StringUtils.trimToNull(plate.getPlatetype_seq_id().toString()) != null) ? plate.getPlatetype_seq_id().toString() : "";

        String precoated = (plate.getIsprecoated() != null && StringUtils.trimToNull(plate.getIsprecoated()) != null ? plate.getIsprecoated().trim() :"");
    %>
    <tr>
        <%if(!(plate.getPlate_seq_id()==null)){%>
        <td align="1">
            <input type="hidden" name="plateIds" value="<%=plate.getPlate_seq_id()%>" />
            <%=plate.getPlate_seq_id()%>
        </td>
        <td align="1">
            <%=plate.getPlate_name()%>
        </td>
        <td align="1">
            <select name="preCoated" id="preCoated" <%=plate.isBool_report_plate()?"disabled":""%> >
                <option value=""></option>
                <option value="true" <%="true".equalsIgnoreCase(precoated)?"selected":""%>>Yes</option>
                <option value="false" <%="false".equalsIgnoreCase(precoated)?"selected":""%>>No</option>

            </select>
            <!-- <input type="radio" name="preCoated<%=i%>"  value="<%//plate.getPlate_seq_id()%>"  <%//"true".equalsIgnoreCase(plate.getIsprecoated())?"checked":""%> <%//plate.isBool_report_plate()?" disabled" :""%>/> Yes
                <input type="radio" name="preCoated<%=i%>"  value="<%//plate.getPlate_seq_id()%>" <%//"false".equalsIgnoreCase(plate.getIsprecoated())?"checked":""%> <%//plate.isBool_report_plate()?" disabled" :""%>/> No  -->
        </td>
        <td align="1">
            <select name="plateTypes"  <%=plate.isBool_report_plate()?"disabled":""%> >
                <option value=""></option>
                <%for(PlateType pt : platetypes){%>
                <option value="<%=pt.getPlatetype_seq_id()%>" <%=pt.getPlatetype_seq_id().toString().equals(platetype)?"selected":""%>><%=pt.getPlatetype_desc()%></option>
                <%}%>
            </select>
        </td>
        <td align="1">
            <select name="substrates"  <%=plate.isBool_report_plate()?"disabled":""%> >
                <option value=""></option>
                <%for(Substrate subst : substrates){%>
                <option value="<%=subst.getSubstrate_seq_id()%>" <%=subst.getSubstrate_seq_id().toString().equals(substrate)?"selected":""%>><%=subst.getSubstrate_desc()%></option>
                <%}%>
            </select>
        </td>
        <%}%>
    </tr>
    <%}%>
</table>
<p></p>
<%//if(!plate.isBool_report_plate()){%>
<%= button("Update Plates").submit(true) %> &nbsp;
<%//}%>
</labkey:form>
<%}
else{ %>
There are no plates in this batch <%=batchInfo.getBatch_description()%> .
<%}%>
<%= button("Go Back").href("displayBatchSummary.view?batchId=" + bean.getBatch_seq_id()+"&&labstudyseqId="+batchInfo.getLab_study_seq_id()) %>&nbsp;
<%}
catch(Exception e)
{%>
There is something wrong on this page.<%=e.getMessage()%>
<%}%>
