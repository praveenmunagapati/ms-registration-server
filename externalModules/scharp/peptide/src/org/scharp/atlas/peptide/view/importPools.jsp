<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%@ page import="org.labkey.api.util.PageFlowUtil"%>
<%@ page import="org.labkey.api.view.HttpView"%>
<%@ page import="org.labkey.api.view.JspView"%>
<%@ page import="org.scharp.atlas.peptide.PeptideController" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>


<div>
    <%
        JspView<PeptideController.FileForm> me = (JspView<PeptideController.FileForm>) HttpView.currentView();
        PeptideController.FileForm bean = me.getModelBean();
    %>
    <labkey:errors/>
    <labkey:form name="FileForm" action="importPeptidePools.post" method="POST" enctype="multipart/form-data">
        <table class="normal">
            <tr>
                <td colspan="3">
                    <h4 align="center" style="color:blue">Import Peptide Pools</h4>
                </td>
            </tr>
            <tr>
                <td><th>File Type : </th></td>
                <td><select id="actionType" name="actionType">
                    <option value="" <%=bean.getActionType() == null?" selected" :""%>></option>
                    <option value="NEWPOOL" <%=bean.getActionType() != null && bean.getActionType().equals("NEWPOOL")?" selected":""%>>New Pool File</option>
                    <option value="POOLDESC" <%=bean.getActionType() != null && bean.getActionType().equals("POOLDESC")?" selected":""%>>Existing Pool Descriptions</option>
                    <option value="POOLPEPTIDES" <%=bean.getActionType() != null && bean.getActionType().equals("POOLPEPTIDES")?" selected":""%>>Existing Peptides in Pool</option>
                </select>Select One of the File Type. Existing peptide pools mean the pools which already have pool numbers assigned to them.
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
        </table>
        <%= button("Import Peptide Pools").submit(true) %>&nbsp;<%= button("Back").href("begin.view") %>
        <br>
        <h5 style="color:orangered;">
            Note: The File must be .txt extension and It should be tab delimited.<br><br>
        </h5>
    </labkey:form>
</div>
