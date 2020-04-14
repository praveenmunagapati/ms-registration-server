<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%@ page import="org.labkey.api.view.HttpView"%>
<%@ page import="org.labkey.api.view.JspView"%>

<%@ page import="org.scharp.atlas.specimentracking.model.Sites" %>
<%@ page import="org.scharp.atlas.specimentracking.SpecimentrackingManager"%>
<%@ page import="org.scharp.atlas.specimentracking.SpecimentrackingController.*"%>
<%@ page import="org.scharp.atlas.specimentracking.model.Sites" %>

<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="java.util.Date"%>
<%@ page extends="org.labkey.api.jsp.JspBase" %>

<script type="text/javascript" src="<%=request.getContextPath()%>/specimentracking/CalendarPopup.js"></script>

<div>
    <%
        JspView<ManifestFileForm> me = (JspView<ManifestFileForm>) HttpView.currentView();
        ManifestFileForm bean = me.getModelBean();
        Sites [] sites = SpecimentrackingManager.getInstance().getLabs();
        if (bean.getMessage() != null) {
    %>
    <table>
        <th style="color:red"><%=bean.getMessage()%></th>
    </table>
    <%}%>
    <labkey:errors/>
    <h2 align="center" style="color:blue"> Manifest Tracking</h2>
    <labkey:form name="ManifestFileForm" action="upload.post" method="POST" enctype="multipart/form-data">
        <table class="normal">
            <tr>
                <td colspan="3">
                    <h4 align="center" style="color:blue">Upload Manifest</h4>
                </td>
            </tr>
            <tr>
                <td>
                    <th>File:</th>
                </td>
                <td colspan="2">
                    <input type="file" name="mFile" value=""/>
                </td>
            </tr>
            <tr>
                <td>
                    <th>Date Received:</th>
                </td>
                <td colspan="2">
                    <input type="text" name="dateReceived" size="11" maxlength="11" value="<%=bean.getDateReceived() == null ? new SimpleDateFormat("dd-MMM-yyyy").format(new Date()):bean.getDateReceived()%>"  style="display: block; float: left;" />
                    <span style="display:inline;padding-left:2px;">(dd-MMM-YYYY) Enter date shipment received if different from current date.
                    The Date Received must be between Shipdate on manifest file and current Date.</span>
                </td>
            </tr>
            <tr>
                <td>
                    <th>End User lab: </th>
                </td>
                <td colspan="2">
                    <select id="recipientLab" name="recipientLab" >
                        <option value="0" <%=String.valueOf(bean.getRecipientLab()) == null?" selected":""%>></option>
                        <%for(Sites site : sites){ %>
                        <option value="<%=site.getLdmslabcode()%>" <%=bean.getRecipientLab() == site.getLdmslabcode()?" selected": ""%>  > <%=site.getLabel()%></option>
                        <%}%>
                    </select>
                    Please select the End User lab.
                </td>
            </tr>

        </table>
        <%= button("Upload Manifest").submit(true) %>&nbsp;<%= button("Back").href("begin.view") %>
        <br>
        <h5 style="color:orangered;">
            Note: The File must be .csv extension and It should have the regular Manifest file format.<br><br>
        </h5>
    </labkey:form>
</div>
