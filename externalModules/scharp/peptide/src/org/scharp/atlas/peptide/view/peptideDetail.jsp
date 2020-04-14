<%@ page import="org.labkey.api.security.User"%>
<%@ page import="org.labkey.api.security.permissions.UpdatePermission"%>
<%@ page import="org.labkey.api.view.HttpView" %>
<%@ page import="org.labkey.api.view.ViewContext" %>
<%@ page import="org.scharp.atlas.peptide.PeptideController" %>
<%@ page import="org.scharp.atlas.peptide.PeptideManager" %>
<%@ page import="org.scharp.atlas.peptide.model.ParentChild" %>
<%@ page import="org.scharp.atlas.peptide.model.Peptide" %>
<%@ page import="org.scharp.atlas.peptide.model.PeptideManufactureStatus" %>
<%@ page import="org.scharp.atlas.peptide.model.PeptidePool" %>
<%@ page import="org.scharp.atlas.peptide.model.Source" %>
<%@ page import="org.scharp.atlas.peptide.model.TransmittedStatus" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page extends="org.scharp.atlas.peptide.view.PeptideDetailPage" %>

<% Peptide peptide = getPeptide();
    ViewContext ctx = getViewContext();
    User user = ctx.getUser();%>
<h3>Peptide Detail Information</h3>

<% if (peptide != null)
{ %>
<form>
<table>
    <tr>
        <td class='labkey-form-label'>Peptide Id</td>
        <td class='ms-vb'>P<%=PeptideController.toLZ(new Integer(peptide.getPeptide_id()))%>
            <% String replicateId = getReplicateId();
                if (replicateId != null)
                { %>
            (Duplicate PeptideId = <%=replicateId%>)
            <% }
            %>
        </td>
    </tr>
    <tr>
        <td class='labkey-form-label'>Peptide Sequence</td>
        <td class='ms-vb'><%=peptide.getPeptide_sequence() == null ? "" : peptide.getPeptide_sequence() %></td>
    </tr>
    <tr>
        <td class='labkey-form-label'>Protein Align Peptide</td>
        <td class='ms-vb'><%=peptide.getProtein_align_pep() == null ? "" : peptide.getProtein_align_pep()%></td>
    </tr>
    <tr>
        <td class='labkey-form-label'>Protein Category Description</td>
        <td class='ms-vb'><%=peptide.getProtein_cat_desc() == null ? "" : peptide.getProtein_cat_desc() %></td>
    </tr>
    <tr>
        <td class='labkey-form-label'>Manufactured</td>
        <td class='ms-vb'><%=PeptideManufactureStatus.InterpretManufactureStatus(peptide.getQc_passed()) %></td>
    </tr>
    <tr>
        <td class='labkey-form-label'>Date Added</td>
        <td class='ms-vb'><%=peptide.getLanl_date() == null ? "" : peptide.getLanl_date() %></td>
    </tr>

    <tr>
        <td class='labkey-form-label'>Is a Child Peptide</td>
        <td class='ms-vb'><%=peptide.getChild()%></td>
    </tr>
    <tr>
        <td class='labkey-form-label'>Is a Parent Peptide</td>
        <td class='ms-vb'><%=peptide.isParent()%></td>
    </tr>
</table>
<% if (peptide.isParent())
{ %>

<%
    ParentChild[] children = PeptideManager.getChildren(peptide.getPeptide_id());
    if (children != null && children.length > 0)
    {
        List<ParentChild> childList = Arrays.asList(children);
%>
<h4> This peptide has these children: </h4>
<ul>
    <%
        for (ParentChild child : childList)
        { %>
    <li><%= link("P"+child.getPeptide_id()).href("displayPeptide.view?peptideId=" + child.getPeptide_id())
    %>
    </li>
    <% } // end for loop %>
</ul>
<% } %>
<% } %>

<% if (Boolean.parseBoolean(peptide.getChild()))
{ %>

<%
    ParentChild[] parents = PeptideManager.getParents(peptide.getPeptide_id());
    if (parents != null && parents.length > 0)
    {
        List<ParentChild> parentList = Arrays.asList(parents);
%>
<h4>This peptide has this parent(s) : </h4>
<ul>
    <%
        for (ParentChild child : parentList)
        { %>
    <li><%=link("P"+child.getLinked_parent()).href("displayPeptide.view?peptideId=" + child.getLinked_parent())
    %>
    </li>
    <% } // end for loop %>
</ul>
<% } %>
<% } %>

<%
    Source[] sources = PeptideManager.getSourcesForAPeptideId(peptide.getPeptide_id());
    HashMap<String,TransmittedStatus> transmittedStatus = PeptideManager.getTransmittedMap();
    if (sources != null && sources.length > 0)
    {

        List<Source> sourceList = Arrays.asList(sources);
%>
<h4>This peptide is a member of these Peptide Groups: </h4>
<ul>
    <table>
        <%
            for (Source source : sourceList)
            { %>
        <tr>
            <td>
                <%= link(source.getPeptide_group_id()).href(
                        "displayPeptideGroupInformation.view?peptide_group_id=" + source.getPeptide_group_id()) %>
            </td>
            <td> (BTK Code =
                <%=source.getBtk_code()%>
                )
            </td>
            <td> (Transmitted Status =
                <%=transmittedStatus.get(source.getTransmitted_status()).getDescription()%>
                )
            </td>
        </tr>
        <%      } // end for loop %>
    </table>
</ul>
<% } // end if statement %>
<%
    List<PeptidePool> pools = peptide.getPeptidePools();
    if (pools != null && pools.size() > 0)
    {
        PeptidePool[] poolDetails = PeptideManager.getPeptidePools();
        Map<Integer, PeptidePool> poolDetailsMap = new HashMap<Integer, PeptidePool>();
        for (PeptidePool ppool : poolDetails)
        {
            poolDetailsMap.put(ppool.getPeptide_pool_id(), ppool);
        }
%>
<h4>This peptide is a member of these Peptide Pools: </h4>
<ul>
    <%
        for (PeptidePool pool : pools)
        {
            if (poolDetailsMap.containsKey(pool.getPeptide_pool_id()))
                pool = poolDetailsMap.get(pool.getPeptide_pool_id());%>
    <li><%=link("PP" + PeptideController.toLZ(pool.getPeptide_pool_id())).href(
            "displayPeptidePoolInformation.view?peptidePool=" +pool.getPeptide_pool_id()) %> -
        <%=pool.getPool_type()%> -
        <%=pool.getDescription() == null ? pool.getMatrix_id() + "|" + pool.getPeptide_group_id() : pool.getDescription()%>
    </li>
    <%      } // end for loop %>
</ul>
<% } // end if statement %>

<%if(ctx.getContainer().hasPermission(user, UpdatePermission.class)){%>
<%= button("Edit Peptide").href("editPeptide.view?peptideId="+ peptide.getPeptide_id()) %>
<%}%>
<%= button("Peptide Home").href("begin.view") %>
</labkey:form>
<% }
else
{
    PeptideController.DisplayPeptideForm  form = (PeptideController.DisplayPeptideForm)(HttpView.currentModel());%>
The selected Peptide <%=form.getPeptideId()%> could not be found.
<p />
<%= button("Peptide Home").href("begin.view") %>
<% } %>
