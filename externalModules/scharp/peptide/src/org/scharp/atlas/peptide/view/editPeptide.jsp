<%@ page import="java.util.List"%>
<%@ page import="org.labkey.api.view.HttpView"%>
<%@ page import="org.scharp.atlas.peptide.PeptideManager" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="org.scharp.atlas.peptide.model.*" %>
<%@ page import="org.scharp.atlas.peptide.PeptideController" %>
<%@ page import="org.labkey.api.view.JspView" %>
<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>


<%  JspView<PeptideController.EditPeptideForm> me = (JspView<PeptideController.EditPeptideForm>) HttpView.currentView();
    PeptideController.EditPeptideForm bean = me.getModelBean();
    Peptide peptide = PeptideManager.getPeptide(bean.getPeptideId()); %>
<labkey:form  action="editPeptide.post" method="post">
<labkey:errors/>
<h3>Peptide Detail Information</h3>

<% if (peptide != null)
{ %>

<table>
    <tr>
        <td class="labkey-form-label">Peptide Id : </td>
        <td class='ms-vb'>
            <input type="hidden" name="peptideId" value="<%=peptide.getPeptide_id()%>"/>
            P<%=PeptideController.toLZ(new Integer(peptide.getPeptide_id()))%></td>
    </tr>
    <tr>
        <td class='labkey-form-label'>Peptide Sequence : </td>
        <td class='ms-vb'><%=peptide.getPeptide_sequence() == null ? "" : peptide.getPeptide_sequence() %></td>
    </tr>
    <tr>
        <td class='labkey-form-label'>Protein Align Peptide : </td>
        <td class='ms-vb'><%=peptide.getProtein_align_pep() == null ? "" : peptide.getProtein_align_pep()%></td>
    </tr>
    <tr>
        <td class='labkey-form-label'>Manufacture Status : </td>
        <td><select name="manufactureStatus">
            <%ManuFactureStatus[] status = PeptideManager.getManufactureStatus();
                for(ManuFactureStatus st : status){%>
            <option value="<%=st.getQc_passed()%>" <%=peptide.getQc_passed().equalsIgnoreCase(st.getQc_passed().toString()) ? "selected" : ""%>><%=st.getDescription()%></option>
            <%}%>
        </select>
        </td>
    </tr>
    <tr>
        <td class='labkey-form-label'>Date Added : </td>
        <td class='ms-vb'><%=peptide.getLanl_date() == null ? "" : peptide.getLanl_date() %>
        </td>
    </tr>
    <tr>
        <td class='labkey-form-label'>Protein Category Description : </td>
        <td class='ms-vb'><%=peptide.getProtein_cat_desc() == null ? "" : peptide.getProtein_cat_desc() %>
        </td>
    </tr>
    <tr>
        <td class='labkey-form-label'>Is a Child Peptide : </td>
        <td class='ms-vb'><%=peptide.getChild()%>
        </td>
    </tr>
    <tr>
        <td class='labkey-form-label'>Is a Parent Peptide : </td>
        <td class='ms-vb'><%=peptide.isParent()%>
        </td>
    </tr>
</table>
<%
    Source[] sources = PeptideManager.getSourcesForAPeptideId(peptide.getPeptide_id());
    if (sources != null && sources.length > 0)
    {
        List<Source> sourceList = Arrays.asList(sources);
%>
<h4>This peptide is a member of these Peptide Groups:</h4>
<ul>
    <table>
        <%
            for (Source source : sourceList)
            { %>
        <tr>
            <td>
                <input type="hidden" name="peptideGroup" value="<%=source.getPeptide_group_id()%>"/>
                <%= source.getPeptide_group_id()%>
            </td>
            <td> (BTK Code =
                <%=source.getBtk_code()%>
                )
            </td>
            <td class='labkey-form-label'>Transmitted Status : </td>
            <td><select name="transmittedStatus">
            <%TransmittedStatus[] tStatus = PeptideManager.getTransmittedStatus();
                for(TransmittedStatus st : tStatus){%>
            <option value="<%=st.getTransmitted_status()%>"
                    <%=source.getTransmitted_status().equalsIgnoreCase(st.getTransmitted_status()) ? "selected" : ""%>><%=st.getDescription()%></option>
            <%}%>
        </select>
        </tr>
        <%      } // end for loop %>
    </table>
</ul>
<% } // end if statement %>

<%= button("Save Changes").submit(true) %>
<%= button("Cancel").href("displayPeptide.view?peptideId= " + peptide.getPeptide_id()) %>
<% }
else
{
    PeptideController.DisplayPeptideForm  form = (PeptideController.DisplayPeptideForm)(HttpView.currentModel());%>
The selected Peptide <%=form.getPeptideId()%> could not be found.
<p />
<%= button("Peptide Home").href("begin.view") %>
<% } %>
</labkey:form>


