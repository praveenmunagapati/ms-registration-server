<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%@ page import="org.labkey.api.view.HttpView"%>
<%@ page import="org.labkey.api.view.JspView"%>

<%@ page import="org.scharp.atlas.specimentracking.SpecimentrackingController.*" %>
<%@ page import="org.scharp.atlas.specimentracking.model.ManifestSpecimens" %>
<%@ page import="org.scharp.atlas.specimentracking.model.Manifests"%>
<%@ page import="org.scharp.atlas.specimentracking.SpecimentrackingManager"%>

<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.List"%>
<%@ page import="java.text.SimpleDateFormat"%>

<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%
    JspView<SpecimenForm> me = (JspView<SpecimenForm>) HttpView.currentView();
    SpecimenForm bean = me.getModelBean();
    Manifests shipment = SpecimentrackingManager.getInstance().getManifest(bean.getShipId(), HttpView.currentContext().getContainer());
    ArrayList<ManifestSpecimens> mSpecimens = bean.getSpecimen();
    if(bean.getMessage() != null)
    {
    %>
    <table>
        <th style="color:red"><%=bean.getMessage()%></th>
    </table>
    <%}
  if (mSpecimens != null && mSpecimens.size() > 0)
    {%>
<labkey:errors/>
<labkey:form action="specimenTracking.post"  method="POST" >
    <input type="hidden" name="shipId" value="<%=bean.getShipId()%>" />
    <%
        List<String>  list1= new ArrayList<String>();
        List<String>  list2= new ArrayList<String>();
        List<String>  list3= new ArrayList<String>();
        for (ManifestSpecimens specimen : mSpecimens)
        { %>
    <input type="hidden" name="specimenId" value="<%=specimen.getSpecimenId()%>"/>
    <%     if(specimen.isOnManifest() && specimen.isReConciled())
    {%>
    <input type="hidden" name="specimenCheck" value="<%=specimen.getSpecimenId()%>" />
    <%    list1.add(specimen.getSpecimenId());
    }
    else if(specimen.isOnManifest() && !specimen.isReConciled())
    {
        list2.add(specimen.getSpecimenId());

    }
    else if(!specimen.isOnManifest() && specimen.isReConciled())
    { %>
    <input type="hidden" name="specimenCheck" value="<%=specimen.getSpecimenId()%>" />
    <%        list3.add(specimen.getSpecimenId());
    }
    }
    %>

    <h4>Date Received : <%=new SimpleDateFormat("dd-MMM-yyyy").format(shipment.getDateReceived())%></h4>
    <h6 style="color:red;">Please double check the specimens under "Reconciled but not On Manifest file Specimens" for misspelling because they are not on Manifest File.</h6>
    <h5 style="color:blue;">Reconciled and On Manifest file Specimens</h5>
    <table>


        <%  if(!list1.isEmpty()){
            for(String sId : list1){%>
        <tr><td><%=sId%></td></tr>
        <%}}else{%>
        <th>There are no specimens in this category.</th>
        <%}%>

    </table>

    <br><br><br>
    <h5 style="color:blue;">UnReconciled and On Manifest file Specimens</h5>
    <table>

        <%if(!list2.isEmpty()){
            for(String sId :list2){%>
        <input type="hidden" name="specimenId" value="<%=sId%>"/>
        <tr><td><%=sId%></td></tr>
        <%}}else{%>
        <th style="color:red">There are no specimens in this category.<br>
            This is the last time you can edit specimens for this manifest.
            If you have any specimens remaining in the shipment that are not on the manifest file,<br>
            please click on 'Edit' to enter them now.
            </th>
        <%}%>
    </table>

    <br><br><br>
    <h5 style="color:blue;">Reconciled but not On Manifest file Specimens</h5>
    <h6 style="color:red;">Please double check these specimens for misspelling because they are not on Manifest File.</h6>
    <table>

        <%if(!list3.isEmpty()) {
            for(String sId :list3){%>
        <input type="hidden" name="specimenId" value="<%=sId%>"/>
        <tr><td style="color:red;"><%=sId%></td></tr>

        <%}}else{%>
        <th>There are no specimens in this category.</th>
        <%}%>


    </table>
    <table>
        <tr><td><input type="submit" name="action_type" value="Reconcile"/> </td>
            <td><input type="submit" name="action_type" value="Edit" /></td>
        </tr>

    </table>
    <br>
    <p style="font-size:medium;">
        Clicking 'Reconcile' stores all vials listed as 'Reconciled and On Manifest'and 'Reconciled and not On Manifest'
        in the CHAVI specimen tracking database. Vials listed as 'UnReconciled and On Manifest' may be reconciled at a later time.
    </p>
    <p style="font-size:medium;">
        Clicking 'Edit' returns you to the previous page so you can make changes.
    </p>

</labkey:form>
<%
}
else
{
%>
<span class="normal">There are no Specimens to update.</span><br>
<%= button("Back").href("begin.view") %>
<%
    }
%>
