<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%@ page import="org.labkey.api.view.HttpView"%>
<%@ page import="org.labkey.api.view.ViewContext" %>
<%@ page import="org.labkey.api.view.ActionURL" %>
<%@ page import="org.scharp.atlas.elispot.model.PlateInformation" %>
<%@ page import="org.scharp.atlas.elispot.EliSpotManager" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="org.scharp.atlas.elispot.ElispotBaseController.*" %>
<%@ page import="org.scharp.atlas.elispot.model.Plate" %>
<%@ page import="org.scharp.atlas.elispot.model.PlateTemplate" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<labkey:errors/>
<%
    ViewContext context = HttpView.currentContext();
    StudyLabBatchForm form = (StudyLabBatchForm) HttpView.currentModel();
    ActionURL url = context.cloneActionURL();
    Integer plateseqId = form.getPlateId();
    Integer batchId = form.getBatchId();
    Plate plate = EliSpotManager.getPlate(context.getContainer(), plateseqId);
    PlateTemplate pt = EliSpotManager.getPlateTemplate(context.getContainer(),plate.getTemplate_seq_id());
    String[] letters = {"A", "B", "C", "D", "E", "F", "G", "H"};
    PlateInformation[] plateInfo = EliSpotManager.getPlateInformation(plateseqId);
    if (plateInfo != null && plateInfo.length > 0) {
        HashMap<String, PlateInformation> plateMap = new HashMap<String, PlateInformation>();
        for (PlateInformation pInfo : plateInfo) {
            plateMap.put(pInfo.getFinal_well_id(), pInfo);
        }
        int i;
        if (form.getMessage() != null && form.getMessage().length() != 0)
            out.print(form.getMessage());
%>
<table><tr><td>
    <th>Plate Information for Plate <%=plateseqId%> : <%=plate.getPlate_name()%></th>
</td></tr></table>
<br>
<labkey:form action="approvePlateInformation.post" method="post">
    <input type="hidden" name="labstudyseqId" value="<%=form.getLabstudyseqId()%>"/>
    <input type="hidden" name="plateId" value="<%=form.getPlateId()%>"/>
    <input type="hidden" name="batchId" value="<%=form.getBatchId()%>"/>
    <table border="1" cellpadding="5">
        <tr>
            <td></td><td></td>
            <%for(i = 1;i<13;i++){%>
            <td><%=i%></td>
            <%}%>
        </tr>
        <%for(String letter : letters){%>
        <tr>
            <td>Well Id<br><%if(!pt.isBool_use_blinded_name()){%>Antigen<%}else{%>Antigen<%}%><br>Spec Position-Replicate<br>SFU</td>
            <td><%=letter%></td>
            <%for (i = 1; i < 13; i++) {
                String wellId;
                if(i <= 9)
                { wellId = letter + "0"+i;  }
                else{ wellId = letter +i;}
                PlateInformation pInfo = plateMap.get(wellId);
            %>
            <td id = "<%=wellId%>">
                <%=wellId%><br><%=pt.isBool_use_blinded_name()?(pInfo.getBlinded_name() == null?"":pInfo.getBlinded_name()):(pInfo.getFriendly_name() == null?"":pInfo.getFriendly_name())%><br>
                <%=pInfo.getSpec_well_group() == null ?"":pInfo.getSpec_well_group()%> - <%=pInfo.getReplicate() == null?"":pInfo.getReplicate()%><br>
                <%=pInfo.getText_sfu() == null?"":pInfo.getText_sfu()%>
            </td>
            <%}%>
        </tr>

        <%}%>
    </table>
    <table>
        <tr>
            <th class='ms-searchform'> Comments: </th>
            <td class='ms-vb'><textarea name="comments" cols="80" rows="5" <%=plate.isBool_report_plate()?"disabled":""%>>
                <%=plate.getComment()==null?"":plate.getComment()%>
            </textarea></td>
        </tr>
        <tr><th colspan="2">The comments are stored in the database when you hit Approve,Remove and Save Comments only.</th></tr>
    </table>
    <%//buttonLink("Go Back","displayBatchSummary.view?batchId="+batchId+"&&labstudyseqId="+form.getLabstudyseqId())%>
    <input type="submit" name="actionName" value="Go Back"/>
    <%//buttonLink("View/Edit SampleInfo","editCellCounts.view?plate_seq_id="+plateseqId+"&&batch_seq_id="+form.getBatchId()+"&&lab_study_seq_id="+form.getLabstudyseqId())%>
    <input type="submit" name="actionName" value="View/Edit SampleInfo"/>
    <%if(!plate.isBool_report_plate()){%>
    <input type="submit" name="actionName" value="Approve" onclick="return confirm('Once approved the plate and related sample info will be locked.Do you wish to continue?')"/>
    <%//button("Approve").submit(true).onClick("return confirm('Once approved the plate and related sample info will be locked.Do you wish to continue?')")%>
    <%//buttonLink("Remove","removePlateInformation.view?plateId="+plateseqId+"&&batchId="+batchId+"&&labstudyseqId="+form.getLabstudyseqId(),
        // "return confirm('Are you sure you want to remove the plate data?')")%>
    <input type="submit" name="actionName" value="Remove Plate File"onclick="return confirm('Are you sure you want to remove the plate data?')"/>
    <input type="submit"name="actionName" value="Save Comments"/>
    <%//buttonLink("Save Comments","saveComments.view?plateId="+plateseqId+"&&batchId="+batchId+"&&labstudyseqId="+form.getLabstudyseqId())%>
    <%}%>
</labkey:form>
<%}
else{%>
<table><tr><td>
    <th>There is no plate data for the plate : <%=plate.getPlate_name()%><br> Make sure that you upload a file for this plate.</th>
</td></tr></table>
<%= button("Go Back").href("displayBatchSummary.view?batchId="+batchId+"&&labstudyseqId="+form.getLabstudyseqId()) %>
<%}%>

