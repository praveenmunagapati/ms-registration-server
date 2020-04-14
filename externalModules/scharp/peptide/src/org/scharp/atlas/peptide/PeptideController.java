package org.scharp.atlas.peptide;

import org.labkey.api.action.*;
import org.labkey.api.security.RequiresPermission;
import org.labkey.api.view.*;
import org.labkey.api.data.*;
import org.labkey.api.security.permissions.ReadPermission;
import org.labkey.api.security.permissions.UpdatePermission;
import org.labkey.api.jsp.JspLoader;
import org.labkey.api.attachments.AttachmentFile;
import org.labkey.api.query.QuerySettings;
import org.labkey.api.query.FieldKey;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.beans.PropertyValues;
import org.scharp.atlas.peptide.model.*;
import org.scharp.atlas.peptide.view.PeptideDetailPage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;
import java.sql.SQLException;
import java.io.IOException;

/**
 * User: sravani
 * Date: Jul 6, 2009
 * Time: 12:19:21 PM
 */
public class PeptideController extends PeptideBaseController
{
    private final static Logger _log = Logger.getLogger(PeptideController.class);
    private static DefaultActionResolver _actionResolver =
            new DefaultActionResolver(PeptideController.class);

    private static final String PAGE_INDEX = "/org/scharp/atlas/peptide/view/index.jsp";
    private static final String PAGE_PEPTIDE_GROUP_SELECT = "/org/scharp/atlas/peptide/view/peptideGroupSelect.jsp";
    private static final String PAGE_PEPTIDE_DETAIL = "/org/scharp/atlas/peptide/view/peptideDetail.jsp";
    private static final String VIEW_DISPLAY_PEPTIDE_GROUP = "displayPeptideGroupInformation.view";
    private static final String QRY_STRING_PEPTIDE_GROUP_ID = "peptide_group_id";
    private static final String PAGE_IMPORT_PEPTIDES = "/org/scharp/atlas/peptide/view/importPeptides.jsp";
    private static final String PAGE_EDIT_PEPTIDE = "/org/scharp/atlas/peptide/view/editPeptide.jsp";
    // Maximum number of rows to display on a web page at once.  Specifying  Table.ALL_ROWS was causing a JavaScript timeout.
    private static final int MAX_ROWS = 1000;

    public PeptideController()
    {
        setActionResolver(_actionResolver);
    }

    public ActionURL peptideURL(String action)
    {
        Container c = getViewContext().getContainer();
        return new ActionURL("Peptide", action, c);
    }

    protected HttpServletRequest getRequest()
    {
        return getViewContext().getRequest();
    }

    protected HttpServletResponse getResponse()
    {
        return getViewContext().getResponse();
    }

    @RequiresPermission(ReadPermission.class)
    public class BeginAction extends SimpleViewAction<DisplayPeptideForm>
    {
        public ModelAndView getView(DisplayPeptideForm form, BindException errors) throws Exception
        {
            JspView v = new JspView(PAGE_INDEX, form, errors);
            return v;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Begin", peptideURL("begin"));
            return root;
        }
    }

    @RequiresPermission(ReadPermission.class)
    public class SearchForPeptidesAction extends FormViewAction<PeptideQueryForm>
    {
        public ModelAndView getView(PeptideQueryForm form, boolean reshow, BindException errors) throws Exception
        {
            JspView v = new JspView<PeptideQueryForm>(PAGE_PEPTIDE_GROUP_SELECT, form, errors);
            return v;
        }

        public boolean handlePost(PeptideQueryForm form, BindException errors) throws Exception
        {
            String actionType = getRequest().getParameter("action_type");
            if (actionType != null && actionType.equals("Get Peptides"))
                return true;
            else
                return false;
        }

        public void validateCommand(PeptideQueryForm form, Errors errors)
        {
            return;
        }

        public ActionURL getSuccessURL(PeptideQueryForm form)
        {
            ActionURL urlTest = new ActionURL(GetPeptidesAction.class, getContainer());
            urlTest.addParameter("queryKey", form.getQueryKey());
            urlTest.addParameter("queryValue", form.getQueryValue());
            return urlTest;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Search For Peptides By Criteria", peptideURL("searchForPeptides"));
            return root;
        }
    }

    @RequiresPermission(ReadPermission.class)
    public class GetPeptidesAction extends SimpleViewAction<PeptideQueryForm>
    {
        public ModelAndView getView(PeptideQueryForm form, BindException errors) throws Exception
        {
            if (!form.validate(errors))
            {
                JspView v = new JspView<PeptideQueryForm>(PAGE_PEPTIDE_GROUP_SELECT, form, errors);
                return v;
            }
            PropertyValues pv = this.getPropertyValues();
            GridView gridView = new GridView(new DataRegion(), (BindException) null);
            if (form.getQueryKey().equals(PeptideSchema.COLUMN_PEPTIDE_GROUP_ID))
            {
                gridView = getGridViewByGroup(form, pv);
            }
            if (form.getQueryKey().equals(PeptideSchema.COLUMN_PEPTIDE_POOL_ID))
            {
                gridView = getGridViewByPool(form, pv);
            }
            if (form.getQueryKey().equals(PeptideSchema.COLUMN_PEPTIDE_SEQUENCE))
            {
                gridView = getGridViewBySequence(form, pv);
            }
            if (form.getQueryKey().equals(PeptideSchema.COLUMN_QC_PASSED))
            {
                gridView = getGridViewByStatus(form, pv);
            }
            if (form.getQueryKey().equals(PeptideSchema.COLUMN_PEPTIDE_ID))
            {
                gridView = getGridViewByRange(form, pv);
            }
            if (gridView == null)
            {
                HttpView.redirect(new ActionURL(SearchForPeptidesAction.class, getContainer()));
            }
            return gridView;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Get Peptides By Criteria", peptideURL("getPeptides"));
            return root;
        }
    }

    @RequiresPermission(ReadPermission.class)
    public class DisplayPeptideAction extends SimpleViewAction<DisplayPeptideForm>
    {
        public ModelAndView getView(DisplayPeptideForm form, BindException errors) throws Exception
        {

            if (form.getPeptideId() == null || form.getPeptideId().length() == 0 || validateInteger(form.getPeptideId()) == null)
            {
                errors.reject(null, "Peptide Id is required and It has to be an Integer.");
                JspView v = new JspView(PAGE_INDEX, form, errors);
                return v;
            }
            Peptide peptide = PeptideManager.getPeptide(form.getPeptideId());

            _log.info("Got Peptide Object:" + peptide);

            PeptideDetailPage page = (PeptideDetailPage) JspLoader.createPage(PAGE_PEPTIDE_DETAIL);
            JspView v = new JspView(page, form, errors);

            page.setPeptide(peptide);
            if (peptide != null && !(toLZ(peptide.getPeptide_id()).contentEquals(toLZ(form.getPeptideId().trim()))))
            {
                page.setReplicateId(form.getPeptideId());
            }
            return v;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Display Peptide Details", peptideURL("displayPeptide"));
            return root;
        }
    }

    @RequiresPermission(UpdatePermission.class)
    public class EditPeptideAction extends FormViewAction<EditPeptideForm>
    {
        public ModelAndView getView(EditPeptideForm form, boolean reshow, BindException errors) throws Exception
        {
            if (form.getPeptideId() == null || form.getPeptideId().length() == 0 || validateInteger(form.getPeptideId()) == null)
            {
                errors.reject(null, "Peptide Id is required and It has to be an Integer.");
                JspView v = new JspView(PAGE_INDEX, form, errors);
                return v;
            }
            ModelAndView mv = new JspView(PAGE_EDIT_PEPTIDE, form, errors);
            return mv;

        }

        public boolean handlePost(EditPeptideForm form, BindException errors) throws Exception
        {
            Peptides peptide = PeptideManager.getPeptideById(Integer.parseInt(form.getPeptideId()));
            if (!peptide.getQc_passed().toString().equalsIgnoreCase(form.getManufactureStatus()))
            {
                peptide.setQc_passed(form.getManufactureStatus().trim().charAt(0));
                PeptideManager.updatePeptide(getUser(), peptide);
            }
            if (form.getPeptideGroup() != null && form.getPeptideGroup().length != 0)
            {
                HashMap<String, String> transmittedMap = new HashMap<String, String>();
                String[] transStatus = form.getTransmittedStatus();
                String[] peptideGroups = form.getPeptideGroup();
                if (transStatus.length != peptideGroups.length)
                {
                    errors.reject("There's something wrong in the page Please Refresh the page and try again.");
                    return false;
                }
                for (int i = 0; i < peptideGroups.length; i++)
                    transmittedMap.put(peptideGroups[i], transStatus[i]);
                Source[] sources = PeptideManager.getSourcesForAPeptideId(form.getPeptideId());
                for (Source s : sources)
                {
                    if (!s.getTransmitted_status().equalsIgnoreCase(transmittedMap.get(s.getPeptide_group_id())))
                    {
                        s.setTransmitted_status(transmittedMap.get(s.getPeptide_group_id()));
                        PeptideManager.updateSource(getUser(), s);
                    }
                }
            }
            return true;
        }

        public void validateCommand(EditPeptideForm form, Errors errors)
        {
            if (form.getPeptideId() == null || form.getPeptideId().length() == 0 || validateInteger(form.getPeptideId()) == null)
                errors.reject(null, "Peptide Id is required and It has to be an Integer.");
        }

        public ActionURL getSuccessURL(EditPeptideForm form)
        {
            ActionURL url = new ActionURL(DisplayPeptideAction.class, getContainer());
            url.addParameter(QRY_STRING_PEPTIDE_ID, form.getPeptideId());
            return url;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Edit Peptide", peptideURL("editPeptide"));
            return root;
        }
    }

    @RequiresPermission(ReadPermission.class)
    public class ShowAllPeptideGroupsAction extends SimpleViewAction<PeptideQueryForm>
    {
        public ModelAndView getView(PeptideQueryForm form, BindException errors) throws Exception
        {
            TableInfo tableInfo = PeptideSchema.getInstance().getTableInfoPeptideGroups();
            PropertyValues pv = this.getPropertyValues();
            form.setTInfo(tableInfo);
            List<ColumnInfo> columns = tableInfo.getColumns("peptide_group_id,pathogen_id,seq_ref,seq_source,clade_id,pep_align_ref,pep_align_source,peptide_set,group_type_id,createdby,created,modifiedby,modified");
            form.setCInfo(columns);
            form.setMessage("AllPeptideGroups");
            DataRegion rgn = getDataRegion(getContainer(), form, Table.ALL_ROWS);
            rgn.setButtonBar(getGridButtonBar(getContainer(), pv), DataRegion.MODE_GRID);
            DisplayColumn col = rgn.getDisplayColumn(PeptideSchema.COLUMN_PEPTIDE_GROUP_ID);
            ActionURL displayAction = new ActionURL(DisplayPeptideGroupInformationAction.class, getContainer());
            displayAction.addParameter(QRY_STRING_PEPTIDE_GROUP_ID,"${" + PeptideSchema.COLUMN_PEPTIDE_GROUP_ID + "}");
            col.setURL(displayAction.toString());
            GridView gridView = new GridView(rgn, errors);
            gridView.setTitle("All the Peptide Groups in the System are : ");
            return gridView;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Display All Peptide Groups", peptideURL("showAllPeptideGroups"));
            return root;
        }
    }

    @RequiresPermission(ReadPermission.class)
    public class DisplayPeptideGroupInformationAction extends SimpleViewAction<PeptideAndGroupForm>
    {
        public ModelAndView getView(PeptideAndGroupForm form, BindException errors) throws Exception
        {
            _log.debug("PeptideAndGroupForm: " + form.toString());

            DataRegion rgn1 = new DataRegion();
            TableInfo tableInfo1 = PeptideSchema.getInstance().getTableInfoPeptideGroups();
            rgn1.setColumns(tableInfo1.getColumns("peptide_group_id,pathogen_id,seq_ref,seq_source,clade_id,pep_align_ref,pep_align_source,peptide_set,group_type_id,createdby,created,modifiedby,modified"));
            ButtonBar buttonBar1 = new ButtonBar();
            if (getContainer().hasPermission(getUser(), UpdatePermission.class))
            {
                ActionURL updateGroupUrl = new ActionURL(UpdatePeptideGroupAction.class, getContainer());
                updateGroupUrl.addParameter(QRY_STRING_PEPTIDE_GROUP_ID, form.getPeptide_group_id());
                ActionButton updateGroupButton = new ActionButton("Update Group Data", updateGroupUrl);
                buttonBar1.add(updateGroupButton);
            }
            rgn1.setButtonBar(buttonBar1, DataRegion.MODE_DETAILS);
            DetailsView dataView = new DetailsView(rgn1, form.getPeptide_group_id());
            dataView.setTitle("Group Information from peptide_group table for group : " + form.getPeptide_group_id());

            DataRegion rgn2 = new DataRegion();
            TableInfo tableInfo2 = PeptideSchema.getInstance().getTableInfoGroupPatient();
            rgn2.setColumns(tableInfo2.getColumns("peptide_group_id,ptid,draw_date,study,visit_no,createdby,created,modifiedby,modified"));
            ButtonBar buttonBar2 = new ButtonBar();
            ActionURL backUrl = new ActionURL(ShowAllPeptideGroupsAction.class, getContainer());
            ActionButton goBack = new ActionButton("List All Groups", backUrl);
            buttonBar2.add(goBack);
            if (getContainer().hasPermission(getUser(), UpdatePermission.class))
            {
                ActionURL addUrl = new ActionURL(InsertGroupMetadataAction.class, getContainer());
                addUrl.addParameter(QRY_STRING_PEPTIDE_GROUP_ID, form.getPeptide_group_id());
                ActionButton addButton = new ActionButton("Insert Group MetaData", addUrl);
                ActionURL updateUrl = new ActionURL(UpdateGroupMetadataAction.class, getContainer());
                updateUrl.addParameter(QRY_STRING_PEPTIDE_GROUP_ID, form.getPeptide_group_id());
                ActionButton updateButton = new ActionButton("Update Group MetaData", updateUrl);
                if (PeptideManager.getGroupMetaData(form.getPeptide_group_id()) == null)
                    buttonBar2.add(addButton);
                else
                    buttonBar2.add(updateButton);
            }
            rgn2.setButtonBar(buttonBar2, DataRegion.MODE_DETAILS);
            DetailsView metaDataView = new DetailsView(rgn2, form.getPeptide_group_id());
            metaDataView.setTitle("Metadata or Group Information from group_patient table for group : " + form.getPeptide_group_id());

            VBox vbox = new VBox();
            vbox.addView(dataView);
            vbox.addView(metaDataView);
            return vbox;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Display Peptide Group Details", peptideURL("displayPeptideGroupInformation"));
            return root;
        }
    }

    @RequiresPermission(ReadPermission.class)
    public class DisplayPeptidePoolInformationAction extends SimpleViewAction<PeptideAndPoolForm>
    {
        public ModelAndView getView(PeptideAndPoolForm form, BindException errors) throws Exception
        {
            _log.debug("PeptideAndPoolForm: " + form.toString());
            PeptideQueryForm queryform = new PeptideQueryForm();
            TableInfo tableInfo = PeptideSchema.getInstance().getTableInfoPeptidePools();
            PropertyValues pv = this.getPropertyValues();
            queryform.setTInfo(tableInfo);
            queryform.setCInfo(tableInfo.getColumns("peptide_pool_id,pool_type,description,comment,exists,create_date,createdby,created,modified"));
            DataRegion rgn = getDataRegion(getContainer(), queryform, Table.ALL_ROWS);
            rgn.setShowBorders(true);
            rgn.setShadeAlternatingRows(true);
            ButtonBar buttonBar = new ButtonBar();
            ActionURL homeUrl = new ActionURL(BeginAction.class, getContainer());
            ActionButton homeButton = new ActionButton("Peptide Home", homeUrl);
            //buttonBar.add(homeButton);
            rgn.setButtonBar(buttonBar, DataRegion.MODE_DETAILS);
            DetailsView dataView = new DetailsView(rgn, form.getPeptidePool());
            dataView.setTitle("Pool Information from peptide_pool table for pool : PP" + toLZ(form.getPeptidePool()));
            //return dataView;
            queryform.setQueryValue(form.getPeptidePool());
            GridView gv = getGridViewByPool(queryform, pv);
            VBox box = new VBox();
            box.addView(dataView);
            box.addView(gv);
            return box;

        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Display Peptide Pool Details", peptideURL("displayPeptidePoolInformation"));
            return root;
        }
    }

    @RequiresPermission(UpdatePermission.class)
    public class UpdatePeptideGroupAction extends FormViewAction<PeptideGroupForm>
    {
        public ModelAndView getView(PeptideGroupForm form, boolean reshow, BindException errors) throws Exception
        {
            UpdateView uView = new UpdateView(form, errors);
            ButtonBar bb = new ButtonBar();
            //bb.add(new ActionButton("updatePeptideGroup.post","Save Changes"));
            bb.add(new ActionButton(new ActionURL(UpdatePeptideGroupAction.class, getContainer()), "Save Changes"));
            ActionURL backURL = new ActionURL(DisplayPeptideGroupInformationAction.class, getContainer());
            backURL.addParameter(QRY_STRING_PEPTIDE_GROUP_ID, form.getBean().getPeptide_group_id());
            ActionButton cancelButton = new ActionButton(backURL, "Cancel");
            cancelButton.setActionType(ActionButton.Action.LINK);
            bb.add(cancelButton);
            uView.getDataRegion().setButtonBar(bb);
            uView.getDataRegion().getDisplayColumn("peptide_group_id").setVisible(false);
            uView.setTitle("Update Peptide Group data for : " + form.getBean().getPeptide_group_id());
            return uView;
        }

        public boolean handlePost(PeptideGroupForm form, BindException errors) throws Exception
        {
            PeptideGroup bean = form.getBean();
            PeptideGroup dbBean = PeptideManager.getPeptideGroupByID(bean.getPeptide_group_id());
            bean.setCreated(dbBean.getCreated());
            bean.setCreatedBy(dbBean.getCreatedBy());
            PeptideManager.updatePeptideGroup(getContainer(), getUser(), bean);
            return true;
        }

        public void validateCommand(PeptideGroupForm form, Errors errors)
        {
            form.validate(errors);
        }

        public ActionURL getSuccessURL(PeptideGroupForm form)
        {
            ActionURL url = new ActionURL(DisplayPeptideGroupInformationAction.class, getContainer());
            url.addParameter(QRY_STRING_PEPTIDE_GROUP_ID, form.getBean().getPeptide_group_id());
            return url;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Update Peptide Group", peptideURL("updatePeptideGroup"));
            return root;
        }
    }

    @RequiresPermission(UpdatePermission.class)
    public class InsertPeptideGroupAction extends FormViewAction<PeptideGroupForm>
    {
        public ModelAndView getView(PeptideGroupForm form, boolean reshow, BindException errors) throws Exception
        {
            form.setContainer(getContainer());
            ButtonBar bb = new ButtonBar();
            //bb.add(new ActionButton("insertPeptideGroup.post","Add New Peptide Group"));
            bb.add(new ActionButton(new ActionURL(InsertPeptideGroupAction.class, getContainer()), "Add New Peptide Group"));
            ActionURL backURL = new ActionURL(BeginAction.class, getContainer());
            ActionButton goBack = new ActionButton(backURL, "Cancel");
            goBack.setActionType(ActionButton.Action.LINK);
            bb.add(goBack);
            PeptideQueryForm qForm = new PeptideQueryForm();
            TableInfo tInfo = PeptideSchema.getInstance().getTableInfoPeptideGroups();
            qForm.setTInfo(tInfo);
            qForm.setCInfo(tInfo.getColumns());
            DataRegion rgn = getDataRegion(getContainer(), qForm, Table.ALL_ROWS);
            rgn.setButtonBar(bb);
            InsertView iView = new InsertView(rgn, form, errors);
            return iView;
        }

        public boolean handlePost(PeptideGroupForm form, BindException errors) throws Exception
        {
            PeptideGroup group = form.getBean();
            group.setContainerId(getContainer().getId());
            PeptideManager.insertGroup(getContainer(), getUser(), group);
            form.setBean(group);
            return true;
        }

        public void validateCommand(PeptideGroupForm form, Errors errors)
        {
            try
            {
                form.validate(errors);
                form.validateName(errors);
            }
            catch (SQLException e)
            {
                errors.reject(null, "There's something wrong with database when trying to get all the existing groups.");
            }
        }

        public ActionURL getSuccessURL(PeptideGroupForm form)
        {
            ActionURL url = new ActionURL(DisplayPeptideGroupInformationAction.class, getContainer());
            url.addParameter(QRY_STRING_PEPTIDE_GROUP_ID, form.getBean().getPeptide_group_id());
            return url;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Insert Peptide Group", peptideURL("insertPeptideGroup"));
            return root;
        }
    }

    @RequiresPermission(UpdatePermission.class)
    public class UpdateGroupMetadataAction extends FormViewAction<GroupMetaDataForm>
    {
        public ModelAndView getView(GroupMetaDataForm form, boolean reshow, BindException errors) throws Exception
        {
            UpdateView uView = new UpdateView(form, errors);
            ButtonBar bb = new ButtonBar();
            //bb.add(new ActionButton("updateGroupMetadata.post","Save Changes"));
            bb.add(new ActionButton(new ActionURL(UpdateGroupMetadataAction.class, getContainer()), "Save Changes"));
            ActionURL backURL = new ActionURL(DisplayPeptideGroupInformationAction.class, getContainer());
            backURL.addParameter(QRY_STRING_PEPTIDE_GROUP_ID, form.getBean().getPeptide_group_id());
            ActionButton backButton = new ActionButton(backURL, "Cancel");
            backButton.setActionType(ActionButton.Action.LINK);
            bb.add(backButton);
            uView.getDataRegion().setButtonBar(bb);
            uView.setTitle("Update Metadata for Peptide Group : " + form.getBean().getPeptide_group_id());
            return uView;
        }

        public boolean handlePost(GroupMetaDataForm form, BindException errors) throws Exception
        {
            GroupMetaData bean = form.getBean();
            GroupMetaData dbBean = PeptideManager.getGroupMetaData(bean.getPeptide_group_id());
            bean.setCreated(dbBean.getCreated());
            bean.setCreatedBy(dbBean.getCreatedBy());
            PeptideManager.updateMetaData(getContainer(), getUser(), bean);
            return true;
        }

        public void validateCommand(GroupMetaDataForm form, Errors errors)
        {
            form.validate(errors);
        }

        public ActionURL getSuccessURL(GroupMetaDataForm form)
        {
            ActionURL url = new ActionURL(DisplayPeptideGroupInformationAction.class, getContainer());
            url.addParameter(QRY_STRING_PEPTIDE_GROUP_ID, form.getBean().getPeptide_group_id());
            return url;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Update Group Metadata", peptideURL("updateGroupMetadata"));
            return root;
        }
    }

    @RequiresPermission(UpdatePermission.class)
    public class InsertGroupMetadataAction extends FormViewAction<GroupMetaDataForm>
    {
        public ModelAndView getView(GroupMetaDataForm form, boolean reshow, BindException errors) throws Exception
        {
            ButtonBar bb = new ButtonBar();
            //bb.add(new ActionButton("insertGroupMetadata.post?peptide_group_id="+form.getBean().getPeptide_group_id(),"Add Group MetaData"));
            bb.add(new ActionButton(new ActionURL(InsertGroupMetadataAction.class, getContainer()).addParameter("peptide_group_id", form.getBean().getPeptide_group_id()), "Add Group MetaData"));
            //insertButton.setActionName("insertGroupMetaData.post?peptide_group_id="+getRequest().getParameter("peptide_group_id"));
            ActionURL backURL = new ActionURL(DisplayPeptideGroupInformationAction.class, getContainer());
            backURL.addParameter(QRY_STRING_PEPTIDE_GROUP_ID, form.getBean().getPeptide_group_id());
            ActionButton goBack = new ActionButton(backURL, "Cancel");
            goBack.setActionType(ActionButton.Action.LINK);
            bb.add(goBack);
            PeptideQueryForm qForm = new PeptideQueryForm();
            TableInfo tInfo = PeptideSchema.getInstance().getTableInfoGroupPatient();
            qForm.setTInfo(tInfo);
            qForm.setCInfo(tInfo.getColumns());
            DataRegion rgn = getDataRegion(getContainer(), qForm, Table.ALL_ROWS);
            rgn.setButtonBar(bb);
            InsertView iView = new InsertView(rgn, form, errors);
            iView.setTitle("Add Metadata for Peptide Group : " + form.getBean().getPeptide_group_id());
            return iView;
        }

        public boolean handlePost(GroupMetaDataForm form, BindException errors) throws Exception
        {
            GroupMetaData bean = form.getBean();
            PeptideManager.insertMetaData(getContainer(), getUser(), bean);
            return true;
        }

        public void validateCommand(GroupMetaDataForm form, Errors errors)
        {
            form.validate(errors);
        }

        public ActionURL getSuccessURL(GroupMetaDataForm form)
        {
            ActionURL url = new ActionURL(DisplayPeptideGroupInformationAction.class, getContainer());
            url.addParameter(QRY_STRING_PEPTIDE_GROUP_ID, form.getBean().getPeptide_group_id());
            return url;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Insert Group Metadata", peptideURL("insertGroupMetadata"));
            return root;
        }
    }

    @RequiresPermission(ReadPermission.class)
    public class ShowPeptidesToBeMadeAction extends SimpleViewAction<PeptideQueryForm>
    {
        public ModelAndView getView(PeptideQueryForm form, BindException errors) throws Exception
        {
            PropertyValues pv = this.getPropertyValues();
            form.setQueryValue("n");
            GridView gridView = getGridViewByStatus(form, pv);
            gridView.setTitle("The peptides to be Manufactured are : ");
            return gridView;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Show Peptides tobe Made", peptideURL("showPeptidesToBeMade"));
            return root;
        }
    }

    @RequiresPermission(ReadPermission.class)
    public class poolManufactureStatus extends SimpleViewAction<PeptideQueryForm>
    {
        public ModelAndView getView(PeptideQueryForm form, BindException errors) throws Exception
        {
            TableInfo tInfo = PeptideSchema.getInstance().getTableInfoPeptidePools();
            PropertyValues pv = this.getPropertyValues();
            form.setTInfo(tInfo);
            form.setCInfo(tInfo.getColumns("peptide_pool_id,exists"));
            form.setSort(new Sort(PeptideSchema.COLUMN_PEPTIDE_POOL_ID));
            form.setMessage("Peptide_Pools_Status");
            DataRegion rgn = getDataRegion(getContainer(), form, Table.ALL_ROWS);
            rgn.setButtonBar(getGridButtonBar(getContainer(), pv), DataRegion.MODE_GRID);
            GridView gridView = new GridView(rgn, (BindException) null);
            gridView.setTitle("All the Peptide Pools and the Manufacture status of them in the System.");
            return gridView;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Peptide Pool Status ", peptideURL("poolManufactureStatus"));
            return root;
        }
    }

    @RequiresPermission(UpdatePermission.class)
    public class ImportPeptidesAction extends FormViewAction<FileForm>
    {
        private List<Peptides> resultPeptides = new LinkedList<Peptides>();

        public ModelAndView getView(FileForm form, boolean reshow, BindException errors) throws Exception
        {
            JspView v = new JspView<FileForm>(PAGE_IMPORT_PEPTIDES, form, errors);
            return v;
        }

        public boolean handlePost(FileForm form, BindException errors) throws Exception
        {
            try
            {
                List<AttachmentFile> importFiles = getAttachmentFileList();
                AttachmentFile importFile = null;
                for (AttachmentFile a : importFiles)
                {
                    if (a != null && a.getSize() != 0)
                        importFile = a;
                }
                if (!form.validate(errors, importFile))
                    return false;
                if (isPost() && form.getActionType().equalsIgnoreCase("NONCHILD"))
                {
                    NonChildPeptideImporter importer = new NonChildPeptideImporter();
                    if (!importer.process(getViewContext().getUser(), importFile, errors, resultPeptides))
                        return false;
                    return true;
                }
                if (isPost() && form.getActionType().equalsIgnoreCase("LANL"))
                {
                    LANLFileImporter importer = new LANLFileImporter();
                    if (!importer.process(getViewContext().getUser(), importFile, errors, resultPeptides))
                        return false;
                    return true;
                }
                if (isPost() && form.getActionType().equalsIgnoreCase("CHILD"))
                {
                    ChildPeptideImporter importer = new ChildPeptideImporter();
                    if (!importer.process(getViewContext().getUser(), importFile, errors, resultPeptides))
                        return false;
                    return true;
                }
                if (isPost() && form.getActionType().equalsIgnoreCase(("STATUS")))
                {
                    ManufactureStatusImporter importer = new ManufactureStatusImporter();
                    if (!importer.process(getViewContext().getUser(), importFile, errors, resultPeptides))
                        return false;
                    return true;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                _log.error(e.getMessage(), e);
                errors.reject(null, "There was a problem uploading File: " + e.getMessage());
                return false;
            }
            return true;
        }

        public void validateCommand(FileForm form, Errors errors)
        {

        }

        public ActionURL getSuccessURL(FileForm form)
        {
            ActionURL url = new ActionURL(DisplayResultAction.class, getContainer());
            url.addParameter("message", "The file has been successfully imported.");
            getRequest().getSession().setAttribute("RESULT_PEPTIDES", resultPeptides);
            return url;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Import Peptides", peptideURL("importPeptides"));
            return root;
        }
    }

    @RequiresPermission(UpdatePermission.class)
    public class DisplayResultAction extends SimpleViewAction<FileForm>
    {
        public ModelAndView getView(FileForm form, BindException errors) throws Exception
        {
            ModelAndView v = new JspView<FileForm>("/org/scharp/atlas/peptide/view/resultPage.jsp", form, errors);
            List<Peptides> peptides = (List<Peptides>) getRequest().getSession().getAttribute("RESULT_PEPTIDES");
            if (peptides != null && peptides.size() > 0)
            {
                v.addObject("peptides", peptides);
                getRequest().getSession().removeAttribute("RESULT_PEPTIDES");
            }
            return v;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Display Results Page", peptideURL("displayResult"));
            return root;
        }
    }

    @RequiresPermission(UpdatePermission.class)
    public class ImportPeptidePoolsAction extends FormViewAction<FileForm>
    {
        ActionURL url = null;

        public ModelAndView getView(FileForm form, boolean reshow, BindException errors) throws Exception
        {
            JspView v = new JspView<FileForm>("/org/scharp/atlas/peptide/view/importPools.jsp", form, errors);
            return v;
        }

        public boolean handlePost(FileForm form, BindException errors) throws Exception
        {
            try
            {
                List<AttachmentFile> importFiles = getAttachmentFileList();
                AttachmentFile importFile = null;
                for (AttachmentFile a : importFiles)
                {
                    if (a != null && a.getSize() != 0)
                        importFile = a;
                }
                if (!form.validate(errors, importFile))
                    return false;
                if (form.getActionType().equalsIgnoreCase("POOLDESC") || form.getActionType().equalsIgnoreCase("POOLPEPTIDES"))
                {
                    ExistingPoolImporter importer = new ExistingPoolImporter();
                    if (!importer.process(getViewContext().getUser(), form, importFile, errors))
                        return false;
                    url = new ActionURL(BeginAction.class, getContainer());
                    return true;
                }
                if (isPost() && form.getActionType().equalsIgnoreCase("NEWPOOL"))
                {
                    Integer poolId = null;
                    NewPoolImporter importer = new NewPoolImporter();
                    poolId = importer.process(getViewContext().getUser(), importFile, errors);
                    if (poolId == null)
                        return false;
                    url = new ActionURL(DisplayPeptidePoolInformationAction.class, getContainer());
                    url.addParameter("peptidePool", poolId);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                _log.error(e.getMessage(), e);
                errors.reject(null, "There was a problem uploading File: " + e.getMessage());
                return false;
            }
            return true;
        }

        public void validateCommand(FileForm form, Errors errors)
        {

        }

        public ActionURL getSuccessURL(FileForm form)
        {
            url.addParameter("message", "The file has been successfully imported.");
            return url;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Import Peptide Pools", peptideURL("importPeptidePools"));
            return root;
        }
    }

    @RequiresPermission(UpdatePermission.class)
    public class CreatePeptidePoolAction extends FormViewAction<CreatePoolForm>
    {
        public ModelAndView getView(CreatePoolForm form, boolean reshow, BindException errors) throws Exception
        {
            JspView v = new JspView<CreatePoolForm>("/org/scharp/atlas/peptide/view/createMatrixPool.jsp", form, errors);
            return v;
        }

        public boolean handlePost(CreatePoolForm form, BindException errors) throws Exception
        {
            Integer i = PeptideManager.createPool(form, getUser());
            System.out.println("Int : " + i);
            return true;
        }

        public void validateCommand(CreatePoolForm form, Errors errors)
        {
            form.validate(errors);
        }

        public ActionURL getSuccessURL(CreatePoolForm form)
        {
            ActionURL url = new ActionURL(BeginAction.class, getContainer());
            return url;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Create Matrix Peptide Pool", peptideURL("createPeptidePool"));
            return root;
        }
    }

    @RequiresPermission(ReadPermission.class)
    public class PeptideExcelExportAction extends ExportAction
    {
        public void export(Object bean, HttpServletResponse response, BindException errors) throws Exception
        {
            try
            {
                ViewContext ctx = getViewContext();
                HttpSession session = ctx.getRequest().getSession();
                PeptideQueryForm form = (PeptideQueryForm) session.getAttribute("PEPTIDE_QUERY_FORM");
                RenderContext context = new RenderContext(getViewContext());
                DataRegion rgn = getDataRegion(getContainer(), form, Table.ALL_ROWS);
                context.setBaseFilter(form.getFilter());
                context.setBaseSort(form.getSort());
                try (ExcelWriter ew = new ExcelWriter(rgn.getResults(context), rgn.getDisplayColumns()))
                {
                    ew.setAutoSize(true);
                    ew.setFilenamePrefix(form.getMessage());
                    ew.setSheetName(form.getMessage());
                    ew.setFooter(form.getMessage());
                    ew.write(getResponse());
                }
            }
            catch (SQLException e)
            {
                _log.error("export: " + e);
            }
            catch (IOException e)
            {
                _log.error("export: " + e);
            }
            catch (Exception e)
            {
                _log.error("export: " + e);
            }

        }
    }

    @RequiresPermission(ReadPermission.class)
    public class PeptideTextExportAction extends ExportAction
    {
        public void export(Object bean, HttpServletResponse response, BindException errors) throws Exception
        {
            try
            {
                ViewContext ctx = getViewContext();
                HttpSession session = ctx.getRequest().getSession();
                PeptideQueryForm form = (PeptideQueryForm) session.getAttribute("PEPTIDE_QUERY_FORM");
                RenderContext context = new RenderContext(getViewContext());
                DataRegion rgn = getDataRegion(getContainer(), form, Table.ALL_ROWS);
                context.setBaseFilter(form.getFilter());
                context.setBaseSort(form.getSort());

                try (TSVGridWriter tsv = new TSVGridWriter(rgn.getResults(context), rgn.getDisplayColumns()))
                {
                    tsv.setFilenamePrefix(form.getMessage());
                    tsv.write(getResponse());
                }
            }
            catch (SQLException e)
            {
                _log.error("export: " + e);
            }
            catch (IOException e)
            {
                _log.error("export: " + e);
            }
            catch (Exception e)
            {
                _log.error("export: " + e);
            }
        }
    }

    protected GridView getGridViewByGroup(PeptideQueryForm form, PropertyValues pv) throws Exception
    {
        TableInfo tableInfo = PeptideSchema.getInstance()
                .getTableInfoViewGroupPeptides();
        form.setTInfo(tableInfo);
        _log.debug("Creating a Filter for peptideGroup." + PeptideSchema.COLUMN_PEPTIDE_GROUP_ID + ": " + form);
        SimpleFilter sFilter = new SimpleFilter(PeptideSchema.COLUMN_PEPTIDE_GROUP_ID, form.getQueryValue());
        Sort sort = new Sort(PeptideSchema.COLUMN_SORT_SEQUENCE);
        form.setFilter(sFilter);
        //form.setCInfo(tableInfo.getColumns("peptide_id,peptide_sequence,protein_cat_id,protein_align_pep,btk_code,sort_sequence,peptide_group_id,pathogen_id,child,parent,qc_passed,history_id"));
        form.setCInfo(tableInfo.getColumns("peptide_id,peptide_sequence,protein_cat_id,protein_align_pep,sort_sequence,peptide_group_id,transmitted_status,pathogen_id,child,parent,qc_passed,history_id"));
        form.setSort(sort);
        form.setMessage("Peptides_IN_Group_" + form.getQueryValue());
        DataRegion rgn = getDataRegion(getContainer(), form, Table.ALL_ROWS);
        rgn.setButtonBar(getGridButtonBar(getContainer(), pv), DataRegion.MODE_GRID);
        GridView gridView = new GridView(rgn, (BindException) null);
        gridView.setFilter(sFilter);
        gridView.setSort(sort);
        gridView.setTitle(
                "There are (" +
                        PeptideManager.getCount(form.getQueryValue()).intValue() +
                        ") peptides in the '" + form.getQueryValue() + "' peptide group.");
        return gridView;
    }

    protected GridView getGridViewByPool(PeptideQueryForm form, PropertyValues pv) throws Exception
    {
        TableInfo tableInfo = PeptideSchema.getInstance()
                .getTableInfoViewPoolPeptides();
        form.setTInfo(tableInfo);
        _log.debug("Creating a Filter for peptidePool." + PeptideSchema.COLUMN_PEPTIDE_POOL_ID + ": " + form);
        SimpleFilter sFilter = new SimpleFilter(PeptideSchema.COLUMN_PEPTIDE_POOL_ID, Integer.parseInt(form.getQueryValue()));
        Sort sort = new Sort(PeptideSchema.COLUMN_PEPTIDE_ID);
        form.setFilter(sFilter);
        form.setCInfo(tableInfo.getColumns());
        form.setSort(sort);
        form.setMessage("Peptides_IN_Pool_PP" + toLZ(form.getQueryValue()));
        DataRegion rgn = getDataRegion(getContainer(), form, Table.ALL_ROWS);
        rgn.setButtonBar(getGridButtonBar(getContainer(), pv), DataRegion.MODE_GRID);
        GridView gridView = new GridView(rgn, (BindException) null);
        gridView.setFilter(sFilter);
        gridView.setSort(sort);
        gridView.setTitle(
                "The Peptides in the pool PP" + toLZ(form.getQueryValue()) + " are : ");
        return gridView;
    }

    protected GridView getGridViewBySequence(PeptideQueryForm form, PropertyValues pv) throws Exception
    {
        TableInfo tableInfo = PeptideSchema.getInstance()
                .getTableInfoPeptides();
        form.setTInfo(tableInfo);
        _log.debug("Creating a Filter for peptideSequence." + PeptideSchema.COLUMN_PEPTIDE_SEQUENCE + ": " + form);
        SimpleFilter sFilter = new SimpleFilter();
        boolean sequenceIsEmpty = true;
        String sequence = form.getQueryValue();
        if((sequence != null) && (!sequence.trim().isEmpty())){
            sequenceIsEmpty = false;
        }
        if(!sequenceIsEmpty)
        {
            sequence = sequence.trim().toUpperCase();
            sFilter.addWhereClause(PeptideSchema.COLUMN_PEPTIDE_SEQUENCE + " LIKE ?", new Object[]{"%" + sequence + "%"},
                    FieldKey.fromString(PeptideSchema.COLUMN_PEPTIDE_SEQUENCE));
            form.setFilter(sFilter);
        }

        Sort sort = new Sort(PeptideSchema.COLUMN_SORT_SEQUENCE);

        form.setCInfo(tableInfo.getColumns("peptide_id,peptide_sequence,protein_align_pep,protein_cat_id,sort_sequence,child,parent,qc_passed,lanl_date,src_file_name,created,createdby,modified,modifiedby"));
        form.setSort(sort);
        if(!sequenceIsEmpty)
        {
            form.setMessage("Peptides_WITH_Sequence_" + sequence);
        }
        else
        {
            form.setMessage("All_Peptides_In_DB");
        }
        DataRegion rgn = getDataRegion(getContainer(), form, MAX_ROWS);
        rgn.setButtonBar(getGridButtonBar(getContainer(), pv), DataRegion.MODE_GRID);
        GridView gridView = new GridView(rgn, (BindException) null);
        gridView.setSort(sort);
        if(!sequenceIsEmpty) {
            gridView.setFilter(sFilter);
            gridView.setTitle(
                               "The Peptides Containing the Sequence string '" + sequence + "' are : ");
        }
        else
        {
            gridView.setTitle("All the Peptides in Peptide DB : ");
        }
        return gridView;
    }

    protected GridView getGridViewByRange(PeptideQueryForm form, PropertyValues pv) throws Exception
    {
        TableInfo tableInfo = PeptideSchema.getInstance().getTableInfoPeptides();
        form.setTInfo(tableInfo);
        String qValue = form.getQueryValue();
        String[] range = qValue.split("-");
        _log.debug("Creating a Filter for peptideRange." + PeptideSchema.COLUMN_PEPTIDE_ID + ": " + form);
        SimpleFilter sFilter = new SimpleFilter();
        sFilter.addWhereClause(PeptideSchema.COLUMN_PEPTIDE_ID + " BETWEEN ? AND ? ",
                new Object[]{Integer.parseInt(range[0]), Integer.parseInt(range[1])},
                FieldKey.fromString(PeptideSchema.COLUMN_PEPTIDE_ID));
        Sort sort = new Sort(PeptideSchema.COLUMN_PEPTIDE_ID);
        form.setFilter(sFilter);
        form.setCInfo(tableInfo.getColumns("peptide_id,peptide_sequence,protein_align_pep,protein_cat_id,sort_sequence,child,parent,qc_passed,lanl_date,src_file_name,created,createdby,modified,modifiedby"));
        form.setSort(sort);
        form.setMessage("Peptides_IN_Range_" + Integer.parseInt(range[0]) + "AND" + Integer.parseInt(range[1]));
        DataRegion rgn = getDataRegion(getContainer(), form, Table.ALL_ROWS);
        rgn.setButtonBar(getGridButtonBar(getContainer(), pv), DataRegion.MODE_GRID);
        GridView gridView = new GridView(rgn, (BindException) null);
        gridView.setFilter(sFilter);
        gridView.setSort(sort);
        gridView.setTitle(
                "The Peptides in the Peptide Id Range " + toLZ(range[0]) + " - " + toLZ(range[1]) + " are : ");
        return gridView;
    }

    protected GridView getGridViewByStatus(PeptideQueryForm form, PropertyValues pv) throws Exception
    {
        TableInfo tableInfo = PeptideSchema.getInstance()
                .getTableInfoPeptides();
        form.setTInfo(tableInfo);
        _log.debug("Creating a Filter for peptideSequence." + PeptideSchema.COLUMN_QC_PASSED + ": " + form);
        SimpleFilter sFilter = new SimpleFilter(FieldKey.fromParts("qc_passed"), form.getQueryValue());
        Sort sort = new Sort(PeptideSchema.COLUMN_PEPTIDE_ID);
        form.setFilter(sFilter);
        form.setCInfo(tableInfo.getColumns("peptide_id,peptide_sequence,qc_passed"));
        form.setSort(sort);
        form.setMessage("Peptides_WITH_Status_" + PeptideManager.statusValue(form.getQueryValue()).getDescription());
        DataRegion rgn = getDataRegion(getContainer(), form, Table.ALL_ROWS);
        rgn.setButtonBar(getGridButtonBar(getContainer(), pv), DataRegion.MODE_GRID);
        GridView gridView = new GridView(rgn, (BindException) null);
        gridView.setFilter(sFilter);
        gridView.setSort(sort);
        gridView.setTitle(
                "The Peptides with the manufacture status " + PeptideManager.statusValue(form.getQueryValue()).getDescription() + "(" + form.getQueryValue() + ") are : ");
        return gridView;
    }

    private DataRegion getDataRegion(Container c, PeptideQueryForm form, int maxRows) throws Exception
    {
        DataRegion rgn = new DataRegion();
        List<String> columnList = new ArrayList<String>();

        List<DisplayColumn> displayColumnList = new ArrayList<DisplayColumn>();

        for (ColumnInfo col : form.getCInfo())
        {
            if(col != null)
            {
                columnList.add(col.getName());
                DisplayColumn dc;

                if (PeptideSchema.COLUMN_PEPTIDE_ID.equals(col.getName()))
                {
                    dc = new DCpeptideId(col);
                }
                else if (PeptideSchema.COLUMN_PEPTIDE_POOL_ID.equals(col.getName()))
                {
                    dc = new DCpeptidePoolId(col);
                }
                else if (PeptideSchema.COLUMN_HISTORY_ID.equals(col.getName()))
                {
                    dc = new DChistoryId(col);
                }
                else {
                    dc = col.getRenderer();
                }
                displayColumnList.add(dc);
            }
        }

        rgn.setColumns(form.getCInfo());
        rgn.setDisplayColumns(displayColumnList);
        rgn.setShowBorders(true);
        rgn.setShadeAlternatingRows(true);
        rgn.setMaxRows(maxRows);
        ViewContext ctx = getViewContext();
        HttpSession session = ctx.getRequest().getSession(true);
        session.setAttribute("PEPTIDE_QUERY_FORM", form);

        if (columnList.contains(PeptideSchema.COLUMN_PEPTIDE_ID))
        {
            ColumnInfo ci = rgn.getTable().getColumn("peptide_id");
            QuerySettings qs = new QuerySettings(getViewContext(), rgn.getName());
            qs.addAggregates(new Aggregate(ci, Aggregate.BaseType.COUNT));
            qs.setMaxRows(Table.ALL_ROWS);
            rgn.setSettings(qs);
            // We want MOST of the query settings into our dataregion settings, but we still want to paginate the rows.
            rgn.setMaxRows(maxRows);
        }
        return rgn;
    }

    private ButtonBar getGridButtonBar(Container c, PropertyValues pv)
    {
        ButtonBar gridButtonBar = new ButtonBar();

        ActionURL backURL = new ActionURL(BeginAction.class, c);
        ActionButton goBack = new ActionButton(backURL, "Peptide Home");
        gridButtonBar.add(goBack);

        ActionURL searchURL = new ActionURL(SearchForPeptidesAction.class, c);
        ActionButton searchButton = new ActionButton(searchURL, "Peptide Search Page");
        gridButtonBar.add(searchButton);

        ActionURL exportUrl = new ActionURL(PeptideExcelExportAction.class, getContainer());
        exportUrl.setPropertyValues(pv);
        ActionButton export = new ActionButton(exportUrl, "Export to Excel");
        export.setActionType(ActionButton.Action.LINK);
        gridButtonBar.add(export);

        ActionURL exportTextURL = new ActionURL(PeptideTextExportAction.class, getContainer());
        exportTextURL.setPropertyValues(pv);
        ActionButton exportToText = new ActionButton(exportTextURL, "Export All To Text");
        exportToText.setActionType(ActionButton.Action.LINK);
        gridButtonBar.add(exportToText);

        return gridButtonBar;
        //rgn.setButtonBar(gridButtonBar, DataRegion.MODE_GRID);
    }


}
