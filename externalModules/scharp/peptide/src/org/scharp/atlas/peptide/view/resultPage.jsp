<%@ page import="java.util.List" %>

<%@ page import="org.scharp.atlas.peptide.model.Peptides" %>
<%@ page import="org.scharp.atlas.peptide.model.ManuFactureStatus" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="org.scharp.atlas.peptide.PeptideManager" %>
<%@ page import="org.scharp.atlas.peptide.model.Parent" %>
<%@ page import="org.scharp.atlas.peptide.PeptideController" %>
<%@ page import="org.scharp.atlas.peptide.PeptideBaseController" %>
<%@ page import="org.labkey.api.view.JspView" %>
<%@ page import="org.labkey.api.view.HttpView" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%
    JspView<PeptideController.FileForm> me = (JspView<PeptideController.FileForm>) HttpView.currentView();
    PeptideController.FileForm bean = me.getModelBean();
    List<Peptides> peptides = (List<Peptides>) request.getAttribute("peptides");
    HashMap<Character, ManuFactureStatus> statusMap = PeptideManager.getStatusMap();
    if(bean.getMessage() != null){%>
<h4 style="color:green;"><%=bean.getMessage()%></h4>
<%   }
    if (peptides != null && peptides.size() > 0)
    {
%>
<form>
    <table class="normal" border="1" style="border:black">
        <tr>
            <th>Peptide Id</th>
            <th>Peptide Sequence</th>
            <th>Manufacture Status</th>

        </tr>
        <%
            for (Peptides peptide : peptides)
            {
                String peptideId = peptide.getPeptide_id().toString() != null ? "P"+ PeptideController.toLZ(peptide.getPeptide_id()) : "";
                String peptideSeq = peptide.getPeptide_sequence() != null ? peptide.getPeptide_sequence() : "";
                String status = peptide.getQc_passed() != null ? statusMap.get(peptide.getQc_passed()).getDescription() :"";

        %>
        <tr style="border:black">
            <td><%=peptideId%></td>
            <td><%=peptideSeq%></td>
            <td><%=status%></td>
        </tr>
        <%
            }
        %>
    </table>
    <%= button("Home").href("begin.view") %>
</labkey:form>
<%
}
else
{
%>
<span class="normal">There is no data to update.</span><br>
<%= button("Grid View").href("begin.view") %>
<%
    }
%>