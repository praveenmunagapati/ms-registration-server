<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%@ page import="org.labkey.api.view.HttpView"%>
<%@ page import="org.labkey.api.view.JspView"%>

<%@ page import="org.scharp.atlas.specimentracking.SpecimentrackingController.*"%>
<%@ page import="org.scharp.atlas.specimentracking.model.ManifestSpecimens" %>
<%@ page import="org.scharp.atlas.specimentracking.model.Manifests"%>
<%@ page import="org.scharp.atlas.specimentracking.SpecimentrackingManager"%>

<%@ page import="java.util.ArrayList"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%
    JspView<SpecimenForm> me = (JspView<SpecimenForm>) HttpView.currentView();
    SpecimenForm bean = me.getModelBean();

    Manifests shipment = SpecimentrackingManager.getInstance().getManifest(bean.getShipId(),HttpView.currentContext().getContainer());
    ArrayList<ManifestSpecimens> mSpecimens = bean.getSpecimen();
    if(bean.getMessage() != null)
    {
%>
<table>
    <th style="color:red"><%=bean.getMessage()%></th>
</table>
<%}
if (mSpecimens != null && mSpecimens.size() > 0)
    {
%>
<labkey:errors/>
<labkey:form action="specimenTracking.post"  method="POST" >

    <h2>ManifestId : <%=bean.getShipId()%></h2>
    <h4>Received Date: <%=new SimpleDateFormat("dd-MMM-yyyy").format(shipment.getDateReceived())%></h4>
    <br>
    <p>
        The screen displays the contents of the electronic manifest. Compare this to the contents of the shipment
        by scanning each vial's barcode or clicking the box next to each vial identifier listed below. This screen
        displays only those vials that have not yet been reconciled.
    </p>
    <input type="hidden" name="shipId" value="<%=bean.getShipId()%>" />
    <h2 align="center" style="color:blue"> Manifest Tracking</h2>
    <h3 align="center" style="color:blue">Reconcile Specimens</h3>
    <table class="normal">
        <tr><td>Scan specimen barcode: <input type="text"  name ="specimenCode"/>
        </td><td><input type="submit" name="action_type" value="Submit" />&nbsp Note: Cursor must be in field before scanning.</td>

        </tr>

    </table>

    <hr/>
    <h4 align="center" style="color:blue">Specimens In This Shipment and not Reconciled: </h4>
    <p>
        If vials appear in the shipment that are not on the manifest, they may be added by entering their unique
        vial identifier in the 'Scan specimen barcode' field and clicking 'Submit'.<br><br>
        Click 'Submit' to update the view. Selected vial identifiers will change to green.
        <br><br>
        Click 'Reconciliation Complete to view the Shipment Evaluation Form prior to storing results of the comparison in the database<br><br>

    </p>
    <table class="normal">

        <% for (ManifestSpecimens specimen : mSpecimens)
        {
        %>
        <tr>
            <td style="<%=specimen.isReConciled() ? "color:green" : ""%>">

                <input type="checkbox" name="specimenCheck" <%= specimen.isReConciled() ? "Checked" : "" %> value="<%=specimen.getSpecimenId()%>"/>
                <input type="hidden" name="specimenId" value="<%=specimen.getSpecimenId()%>"/>
                <%=specimen.getSpecimenId()%>(<%=specimen.getBoxNumber()==null?"":"box: "+specimen.getBoxNumber()%>
                <%=specimen.getRowNumber()==null?"":" , row: "+specimen.getRowNumber()%>
                <%=specimen.getColumnNumber() == null?"":" , column: "+specimen.getColumnNumber()%>
                <%=specimen.isOnManifest()?")":"Not On Manifest File.)"%>
            </td>
        </tr>
        <%}%>

        <tr><td><input type="submit" name="action_type" value="Submit" /></td>
            <th>Click 'Submit' to update the view. Selected vial identifiers will change to green.</th>
        </tr>
    </table>
    <table>
        <tr><td><input type = "submit" name="action_type" value="Reconcilliation Complete"/></td>
            <td><%= button("Back").href("begin.view") %></td>
            <th> Click 'Reconciliation Complete to view the Shipment Evaluation Form prior to storing results of the comparison in the database.</th>
        </tr>

    </table>
</labkey:form>
<%
}
else
{
%>
<span class="normal">There are no unreconciled specimens to update in this shipment.</span><br>
<%= button("Back").href("begin.view") %>
<%
    }
%>
<script for=window event=onload type="text/javascript">try {document.getElementById("specimenCode").focus();} catch(x){}</script>