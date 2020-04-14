<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%@ page import="org.labkey.api.view.HttpView"%>
<%@ page import="org.scharp.atlas.pepdb.PepDBManager" %>
<%@ page import="org.scharp.atlas.pepdb.PepDBBaseController.*" %>
<%@ page import="org.labkey.api.view.JspView" %>
<%@ page import="org.scharp.atlas.pepdb.model.PeptidePool" %>
<%@ page import="org.scharp.atlas.pepdb.model.Source" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Arrays" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%
    JspView<PeptideQueryForm> me = (JspView<PeptideQueryForm>) HttpView.currentView();
    PeptideQueryForm bean = me.getModelBean();
%>
<table>
    <%
        Source[] sources = PepDBManager.getSourcesForPeptide(bean.getQueryValue());
        if (sources != null && sources.length > 0)
        {%>
        <h4>Peptide P<%=bean.getQueryValue()%> is a member of these Peptide Groups: </h4>
    <%     List<Source> sourceList = Arrays.asList(sources);
        for (Source source : sourceList)
        { %>

    <tr>
        <td>
            <%= link(source.getPeptide_group_name()).href("displayPeptideGroupInformation.view?peptide_group_id=" + source.getPeptide_group_id().toString()) %>
            (PEPTIDE NUMBER =<%=source.getPeptide_id_in_group()%>)
            <%if(source.getFrequency_number() != null){%>
            - Frequency Number =
            <%= source.getFrequency_number()%>
            <%}if(source.getFrequency_number_date() != null){%>
            - Frequency Update Date = <%=source.getFrequency_number_date()%><%}%>
        </td>
    </tr>

    <%      }} // end for loop %>
</table>
<table>
    <%
        PeptidePool[] pools = PepDBManager.getPoolsForPeptide(bean.getQueryValue());
        if (pools != null && pools.length > 0)
        {
            List<PeptidePool> poolList = Arrays.asList(pools); %>
        <h4>Peptide P<%=bean.getQueryValue()%> is a member of these Peptide Pools: </h4>
    <%    for (PeptidePool pool : poolList)
    {%>
    <tr>
        <td>
            <%= link("PP"+pool.getPeptide_pool_id()).href(
                    "displayPeptidePoolInformation.view?peptide_pool_id=" +pool.getPeptide_pool_id()) %> -
            <%=pool.getPeptide_pool_name()%>
            <%=pool.getPool_type_desc()%>
        </td>
    </tr>
    <% }} // end if statement %>
</table> 
