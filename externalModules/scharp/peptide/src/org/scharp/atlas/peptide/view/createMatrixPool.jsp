<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%@ page import="org.labkey.api.view.HttpView"%>
<%@ page import="org.labkey.api.view.ViewContext"%>
<%@ page import="org.labkey.api.view.JspView"%>
<%@ page import="org.scharp.atlas.peptide.PeptideManager" %>
<%@ page import="org.scharp.atlas.peptide.model.PeptideGroup" %>
<%@ page import="org.scharp.atlas.peptide.model.Matrix" %>
<%@ page import="org.scharp.atlas.peptide.PeptideBaseController" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%
    JspView<PeptideBaseController.CreatePoolForm> view = (JspView<PeptideBaseController.CreatePoolForm>) HttpView.currentView();
    PeptideBaseController.CreatePoolForm form =  view.getModelBean();
    PeptideGroup[] peptideGroups = PeptideManager.getPeptideGroups();
    Matrix [] matrices = PeptideManager.getMatrices();

%>
<labkey:errors/>
<labkey:form action="createPeptidePool.view" method="POST">
    <h4 align="center">Create Matrix Peptide Pool</h4>
    <table>
          <tr>
            <th>Peptide Group : </th>
            <td>
            	<select id="peptideGroup" name="peptideGroup">
                	<option value="" ></option>
		           <% for(PeptideGroup pepGroup : peptideGroups) {    %>
                       <% if (form.getPeptideGroup() != null && form.getPeptideGroup().equals(pepGroup.getPeptide_group_id())) { %>
		            	  <option value="<%=pepGroup.getPeptide_group_id()%>" selected ><%=pepGroup.getPeptide_group_id()%></option>
                       <% } else { %>
		            	  <option value="<%=pepGroup.getPeptide_group_id()%>"><%=pepGroup.getPeptide_group_id()%></option>
                       <% } %>
		           <% } %>
	        	</select>
	        </td>
        </tr>
        <tr>
            <th>Apply Matrix Id : </th>
            <td>
            	<select id="matrixId" name="matrixId">
                	<option value="" ></option>
		           <% for(Matrix matrix : matrices) {    %>
                       <% if (form.getMatrixId() != null && form.getMatrixId().equals(matrix.getMatrix_id())) { %>
		            	  <option value="<%=matrix.getMatrix_id()%>" selected ><%=matrix.getMatrix_id()%></option>
                       <% } else { %>
		            	  <option value="<%=matrix.getMatrix_id()%>"><%=matrix.getMatrix_id()%></option>
                       <% } %>
		           <% } %>
	        	</select>
	        </td>
        </tr>
    </table>
    <%= button("Create").submit(true) %>
    <%= button("Cancel").href("begin.view") %>
</labkey:form>

