<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%@ page import="org.labkey.api.view.HttpView"%>
<%@ page import="org.labkey.api.view.JspView"%>
<%@ page import="org.scharp.atlas.elispot.ElispotBaseController.*" %>
<%@ page import="org.scharp.atlas.elispot.model.PlateTemplate" %>
<%@ page import="org.scharp.atlas.elispot.EliSpotManager" %>
<%@ page import="org.scharp.atlas.elispot.model.Study" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>

<div>
    <%  try{
        JspView<PTDetailsForm> me = (JspView<PTDetailsForm>) HttpView.currentView();
        PTDetailsForm bean = me.getModelBean();
        PlateTemplate[] plateTemplates = EliSpotManager.getPlateTemplates(HttpView.currentContext().getContainer());
        if (bean.getMessage() != null && bean.getMessage().length() != 0) {
    %>
    <table>
        <th style="color:red"><%=bean.getMessage()%></th>
    </table>
    <%}%>
    <labkey:errors/>
    <labkey:form action="importPTDetails.post" method="POST" enctype="multipart/form-data">
        <table class="normal">
            <tr>
                <td colspan="3">
                    <h4 align="center" style="color:blue">Import Plate Template Details</h4>
                </td>
            </tr>
            <tr>
                <td>
                    <th>File : </th>
                </td>
                <td colspan="2">
                    <input type="file" name="pFile" value=""/>
                </td>
             </tr>
           <tr>
               <td><th>Plate Template Name : </th></td>
               <td><select name="template_seq_id" >
                   <%for(PlateTemplate plateTemplate : plateTemplates){
                    Study s = EliSpotManager.getStudy(HttpView.currentContext().getContainer(),plateTemplate.getStudy_seq_id());%>
                   <option value="<%=plateTemplate.getTemplate_seq_id()%>"><%=s.getStudy_description()%>-<%=plateTemplate.getTemplate_description()%></option>
                   <%}%>
               </select> </td>
           </tr>

        </table>
        <%= button("Import PTDetails").submit(true) %>&nbsp;<%= button("Back").href("begin.view") %>
        <br>
        <h5 style="color:orangered;">
            Note: The File must be .txt extension and It should be tab delimited.<br><br>
        </h5>
    </labkey:form>
    <%}
catch(Exception e)
{ %>
    There is some Exception in the file.
   <%=e.getMessage()%>
<%} %>
</div>
