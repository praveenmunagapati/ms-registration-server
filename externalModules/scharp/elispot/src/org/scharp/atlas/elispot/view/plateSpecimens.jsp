<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%@ page import="org.labkey.api.view.HttpView"%>
<%@ page import="org.labkey.api.view.JspView"%>

<%@ page import="org.scharp.atlas.elispot.ElispotBaseController.*"%>
<%@ page import="org.scharp.atlas.elispot.EliSpotManager"%>

<%@ page import="org.scharp.atlas.elispot.model.*" %>
<%@ page import="org.labkey.api.data.Container" %>
<%@ page import="org.scharp.atlas.elispot.EliSpotSchema" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<labkey:errors/>
<%
    try {
        JspView<PlateSpecimensForm> me = (JspView<PlateSpecimensForm>) HttpView.currentView();
        PlateSpecimensForm bean = me.getModelBean();
        Container c = HttpView.currentContext().getContainer();
        Plate plate = EliSpotManager.getPlate(c, bean.getPlate_seq_id());
        PlateTemplate plateTemp = EliSpotManager.getPlateTemplate(c, plate.getTemplate_seq_id());
        PlateSpecimens[] pSpecimens = EliSpotManager.getPlateSpecimens(bean.getPlate_seq_id());
        if(pSpecimens == null || pSpecimens.length == 0)
        {
        Specimen[] specimens = EliSpotManager.getSpecimens(c,plateTemp.getStudy_seq_id());
%>

<labkey:form action='<%="insertToPlateSpecimens.post?plateseqId=" + plate.getPlate_seq_id()%>' method="post">
    <table border="2">
        <tr>
            <th>Template</th>
            <th>PlateName</th>
            <th>specwellgroup</th>
            <th>SpecimenSeqId</th>
            <th>runnum</th>
            <th>additive</th>
            <th>cryostatus</th>
        </tr>
        <input type="hidden" name = "num_well_groups_per_plate" value="<%=plateTemp.getNum_well_groups_per_plate()%>" />
        <input type="hidden" name="template_seq_id" value="<%=plateTemp.getTemplate_seq_id()%>"/>
        <input type="hidden" name="plate_seq_id" value="<%=plate.getPlate_seq_id()%>"/>
        <%for(int i=0;i<plateTemp.getNum_well_groups_per_plate();i++){%>
        <tr>
            <td align="1">
                <%=plateTemp.getTemplate_description()%></td>
            <td align="1">
                <%=plate.getPlate_name()%>
            </td>
            <td align="1">
                <input type="hidden" name="specwellgroups" value="<%=(i+1)%>"/>
                <%=(i+1)%>
            </td>
            <td align="1"><select name="specimenseqIds" >
                <option value=""></option>
                <%
                    for(Specimen s:specimens)  {
                %>
                <option value="<%=s.getSpecimen_seq_id()%>"><%=s.getSpecimen_seq_id()%>--<%=s.getPtid()%>--<%=s.getVisit_no()%></option>
                <%}%>
            </select>
            </td>
            <td align="1">
                <input type="text" name="runnums"/>
            </td>
            <td align="1">
                <select name="additives">
                    <option value=""></option>
                    <%Additive [] additives = EliSpotManager.getAdditives();
                        for(Additive additive : additives){%>
                       <option value="<%=additive.getAdditive_seq_id()%>"><%=additive.getAdditive_desc()%></option>
                    <%}%>
                </select></td>
            <td align="1">
                <select name="cryostatus">
                    <option value=""></option>
                    <%Cryostatus [] cryos = EliSpotManager.getCryostatus();
                        for(Cryostatus cryo : cryos){%>
                    <option value="<%=cryo.getCryostatus()%>"><%=cryo.getCryostatus_desc()%></option>
                    <%}%>
                </select> </td>
        </tr>
        <%}%>
    </table>
    <%= button("Insert Specimens").submit(true) %>

</labkey:form>
<%}
else
{%>
  You have already entered the specimens for this plate.If you want you can edit them on the back end.
<%}%>
<%= button("Go Back").href("showData.view?tableName=" + EliSpotSchema.TABLE_PLATE_SPECIMENS +"&&queryString="+EliSpotSchema.COLUMN_PLATE_SEQ_ID+"&&queryValue="+plate.getPlate_seq_id()) %>
<%}
catch(Exception e)
{%>
There is something wrong on this page.
<%}%>
