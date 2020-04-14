package org.scharp.atlas.elispot;

import org.apache.log4j.Logger;
import org.apache.commons.lang3.StringUtils;
import org.labkey.api.action.*;
import org.labkey.api.view.*;
import org.labkey.api.data.*;
import org.labkey.api.query.FieldKey;
import org.labkey.api.security.RequiresPermission;
import org.labkey.api.security.permissions.UpdatePermission;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.attachments.AttachmentFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.scharp.atlas.elispot.model.*;
import org.scharp.elispot.ProcessElispotDataFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;
import java.io.InputStream;


/**
 * User: sravani
 * Date: Jun 22, 2009
 * Time: 8:06:13 AM
 */
public class EliSpotController extends ElispotBaseController
{
    private final static Logger _log = Logger.getLogger(EliSpotController.class);
    private final static DefaultActionResolver _actionResolver = new DefaultActionResolver(EliSpotController.class);
    public static final String JSP_PAGE_MAIN = "/org/scharp/atlas/elispot/view/main.jsp";
    public static final String JSP_PAGE_BATCH_SELECT = "/org/scharp/atlas/elispot/view/batchSelect.jsp";
    public static final String JSP_PAGE_BATCH_MENU = "/org/scharp/atlas/elispot/view/batchMenu.jsp";
    public static final String JSP_PAGE_PLATE_INFO = "/org/scharp/atlas/elispot/view/plateInformation.jsp";
    public static final String JSP_PAGE_UPDATE_PLATESPEC = "/org/scharp/atlas/elispot/view/updatePlateSpecimens.jsp";
    public static final String JSP_PAGE_UPDATE_PLATE_INFO = "/org/scharp/atlas/elispot/view/updatePlateInfo.jsp";
    public static final String JSP_PAGE_INSERT_BATCH = "/org/scharp/atlas/elispot/view/insertBatch.jsp";
    public static final String JSP_PAGE_INSERT_PLATESPEC = "/org/scharp/atlas/elispot/view/plateSpecimens.jsp";
    public static final String JSP_PAGE_IMPORT_PTD = "/org/scharp/atlas/elispot/view/importPlateTempDetails.jsp";
    public static final String JSP_ADMIN_MAIN = "/org/scharp/atlas/elispot/view/adminMain.jsp";

    public EliSpotController()
    {
        setActionResolver(_actionResolver);
    }

    protected HttpServletRequest getRequest()
    {
        return getViewContext().getRequest();
    }

    protected HttpServletResponse getResponse()
    {
        return getViewContext().getResponse();
    }

    public ActionURL elispotURL(String action)
    {
        Container c = getViewContext().getContainer();
        return new ActionURL("ELISpot", action, c);
    }

    protected ButtonBar getButtonBar(ActionURL insertURL, ActionURL action)
    {
        return getButtonBar(insertURL, action, ActionButton.Action.GET);
    }

    protected ButtonBar getButtonBar(ActionURL insertURL, ActionURL action, ActionButton.Action insertActionType)
    {

        ButtonBar bb = new ButtonBar();
        ActionButton insertButton = new ActionButton(insertURL, "Insert a Row");
        insertButton.setActionType(insertActionType);
        bb.add(insertButton);


        ActionButton viewButton = new ActionButton(action, "View Data");
        bb.add(viewButton);

        ActionButton goBack = new ActionButton(new ActionURL(BeginAction.class, getContainer()), "Go Back");
        ActionURL backURL = new ActionURL(EliSpotModule.NAME, VIEW_BEGIN, getContainer());
        goBack.setURL(backURL.getLocalURIString());
        bb.add(goBack);
        return bb;
/*
        ActionButton viewButton = new ActionButton(actionName,"View Data");
        bb.add(viewButton);

        ActionButton goBack = new ActionButton(VIEW_BEGIN, "Go Back");
        ActionURL backURL = new ActionURL(EliSpotModule.NAME,VIEW_BEGIN, getContainer());
        goBack.setURL(backURL.getLocalURIString());
        bb.add(goBack);
        return bb;
        */
    }

    protected DataRegion studyDataRegion(boolean forGrid)
    {
        TableInfo tInfo = EliSpotSchema.getInstance().getTableInfoStudies();
        List<ColumnInfo> cInfo = tInfo.getColumns("study_seq_id,study_description,network_organization,study_identifier,protocol,status,plateinfo_reqd");
        //String actionName = "insertToStudy.view";
        ActionURL insertUrl = new ActionURL(InsertToStudyAction.class, getContainer());
        return getDataRegion(getContainer(), cInfo, insertUrl, forGrid);
    }

    protected DataRegion labDataRegion(boolean forGrid)
    {
        TableInfo tInfo = EliSpotSchema.getInstance().getTableInfoLabs();
        List<ColumnInfo> cInfo = tInfo.getColumns("lab_seq_id,lab_desc,permgroupname");
        //String actionName = "insertToLabs.view";
        ActionURL insertUrl = new ActionURL(InsertToLabsAction.class, getContainer());
        return getDataRegion(getContainer(), cInfo, insertUrl, forGrid);
        //return getDataRegion(getContainer(),cInfo,actionName);
    }

    protected DataRegion studylabDataRegion(boolean forGrid)
    {
        TableInfo tInfo = EliSpotSchema.getInstance().getTableInfostudyLabs();
        List<ColumnInfo> cInfo = tInfo.getColumns("lab_study_seq_id,study_seq_id,lab_seq_id");
        //String actionName = "insertToStudyLabs.view";
        ActionURL insertUrl = new ActionURL(InsertToStudyLabsAction.class, getContainer());
        return getDataRegion(getContainer(), cInfo, insertUrl, forGrid);
        //return getDataRegion(getContainer(),cInfo,actionName);
    }

    protected DataRegion batchDataRegion(String queryValue, boolean forGrid)
    {
        TableInfo tInfo = EliSpotSchema.getInstance().getTableInfoBatch();
        List<ColumnInfo> cInfo = tInfo.getColumns("batch_type,reader_seq_id,batch_seq_id,batch_description,lab_study_seq_id");
        List<DisplayColumn> displayColumnList = new ArrayList<DisplayColumn>();
        for (ColumnInfo col : cInfo)
        {
            DisplayColumn dc;
            if ("lab_study_seq_id".equals(col.getName()))
            {
                dc = new DClabstudyseqId(col);
            }
            else
            {
                dc = col.getRenderer();
            }
            displayColumnList.add(dc);
        }

        //String actionName = "insertToBatch.view?labstudyseqId="+queryValue;
        ActionURL insertUrl = new ActionURL(InsertToBatchAction.class, getContainer()).addParameter("labstudyseqId", queryValue);
        DataRegion rgn = getDataRegion(getContainer(), cInfo, insertUrl, forGrid);
        rgn.setDisplayColumns(displayColumnList);
        return rgn;
        //return getDataRegion(getContainer(),cInfo,actionName);
    }

    protected DataRegion plateTemplateDataRegion(boolean forGrid)
    {
        TableInfo tInfo = EliSpotSchema.getInstance().getTableInfoPlateTemplate();
        List<ColumnInfo> cInfo = tInfo.getColumns("template_seq_id,template_description,num_well_groups_per_plate,stimulated,incubate,readout,study_seq_id,bool_use_blinded_name");
        //String actionName = "insertToPlateTemplate.view";
        ActionURL insertUrl = new ActionURL(InsertToPlateTemplateAction.class, getContainer());
        return getDataRegion(getContainer(), cInfo, insertUrl, forGrid);
        //return getDataRegion(getContainer(),cInfo,actionName);
    }

    protected DataRegion plateTemplateDetailsDataRegion(boolean forGrid)
    {
        TableInfo tInfo = EliSpotSchema.getInstance().getTableInfoPTDetails();
        List<ColumnInfo> cInfo = tInfo.getColumns("well_id,friendly_name,antigen_id,spec_well_group,replicate,pepconc,pepunit,effector,cellsperwell,stcl,stimconc,template_seq_id,blinded_name");
        //String actionName = "importPTDetails.view";
        ActionURL actionUrl = new ActionURL(ImportPTDetailsAction.class, getContainer());
        return getDataRegion(getContainer(), cInfo, actionUrl, forGrid);
        //return getDataRegion(getContainer(),cInfo,actionName);
    }

    protected DataRegion specimenDataRegion(boolean forGrid)
    {
        TableInfo tInfo = EliSpotSchema.getInstance().getTableInfoSpecimen();
        List<ColumnInfo> cInfo = tInfo.getColumns("specimen_seq_id,ptid,visit_no,draw_date,study_seq_id");
        //String actionName = "insertToSpecimen.view";
        ActionURL insertUrl = new ActionURL(InsertToSpecimenAction.class, getContainer());
        return getDataRegion(getContainer(), cInfo, insertUrl, forGrid);
        // return getDataRegion(getContainer(),cInfo,actionName);
    }

    protected DataRegion plateDataRegion(String queryValue, boolean forGrid)
    {
        TableInfo tInfo = EliSpotSchema.getInstance().getTableInfoPlate();
        List<ColumnInfo> cInfo = tInfo.getColumns("plate_seq_id,plate_name,template_seq_id,batch_seq_id,import_date,test_date,freezer_plate_id,tech_id,plate_filename,bool_report_plate,approved_by");
        List<DisplayColumn> displayColumnList = new ArrayList<DisplayColumn>();
        for (ColumnInfo col : cInfo)
        {
            DisplayColumn dc;
            if ("template_seq_id".equals(col.getName()))
            {
                dc = new DCtemplateseqId(col);
            }
            else {
                dc = col.getRenderer();
            }
            displayColumnList.add(dc);
        }

        //String actionName = "insertToPlate.view?batchseqId="+queryValue;
        ActionURL insertUrl = new ActionURL(InsertToPlateAction.class, getContainer()).addParameter("batchseqId", queryValue);
        DataRegion rgn = getDataRegion(getContainer(), cInfo, insertUrl, forGrid);
        rgn.setDisplayColumns(displayColumnList);
        return rgn;
        //return getDataRegion(getContainer(),cInfo,actionName);
    }

    protected DataRegion plateSpecimenDataRegion(String queryValue, boolean forGrid)
    {
        TableInfo tInfo = EliSpotSchema.getInstance().getTableInfoPlateSpecimens();
        List<ColumnInfo> cInfo = tInfo.getColumns("spec_well_group,specimen_id,bool_report_specimen,runnum,additive_seq_id,cryostatus,plate_seq_id,d1_cellcount,d2_cellcount,d1_viability,d2_viability,counter_seq_id,specimen_seq_id");
        //actionName = "insertToPlateSpecimens.view";
        //String actionName = "insertToPlateSpecimens.view?plateseqId="+queryValue;
        ActionURL insertUrl = new ActionURL(InsertToPlateSpecimensAction.class, getContainer()).addParameter("plateseqId", queryValue);
        return getDataRegion(getContainer(), cInfo, insertUrl, forGrid);
        //return getDataRegion(getContainer(),cInfo,actionName);
    }

    private DataRegion getDataRegion(Container c, List<ColumnInfo> cInfo, ActionURL actionURL, boolean forGrid)
    {
        DataRegion rgn = new DataRegion();
        rgn.setColumns(cInfo);
        ButtonBar gridButtonBar = new ButtonBar();
        if (forGrid)
        {
            ActionButton insert = new ActionButton(actionURL, "Insert a Record");
            insert.setActionType(ActionButton.Action.LINK);
            gridButtonBar.add(insert);
        }
        //ActionURL showDataURL = new ActionURL(EliSpotModule.NAME, actionName, c);
        //ActionButton showData = new ActionButton(actionName, "Show Data");
        //showData.setDisplayModes(DataRegion.MODE_INSERT);
        //showData.setURL(showDataURL.getLocalURIString());
        //gridButtonBar.add(showData);

        //TODO: Should we pass the variable c OR call getContainer() in the construction of the goBack button?
        ActionButton goBack = new ActionButton(new ActionURL(BeginAction.class, getContainer()), "Go Back");
        ActionURL backURL = new ActionURL(EliSpotModule.NAME, VIEW_BEGIN, c);
        goBack.setURL(backURL.getLocalURIString());
        gridButtonBar.add(goBack);


        rgn.setButtonBar(gridButtonBar, DataRegion.MODE_GRID);
        rgn.setShowBorders(true);
        rgn.setShadeAlternatingRows(true);
        return rgn;
    }

    @RequiresPermission(UpdatePermission.class)
    public class BeginAction extends SimpleViewAction
    {
        public ModelAndView getView(Object form, BindException errors) throws Exception
        {
            BindException e = (BindException) getRequest().getSession().getAttribute("ELISPOT_USER_ERRORS");
            if (e != null && e.getErrorCount() > 0)
            {
                errors = e;
                getRequest().getSession().removeAttribute("ELISPOT_USER_ERRORS");
            }
            String pageString;
            if (getContainer().hasPermission(getUser(), AdminPermission.class))
            {
                pageString = JSP_ADMIN_MAIN;
            }
            else
            {
                pageString = JSP_PAGE_MAIN;
            }
            JspView v = new JspView(pageString, form, errors);
            return v;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Begin", elispotURL("begin"));
            return root;
        }
    }

    @RequiresPermission(UpdatePermission.class)
    public class BatchSelectAction extends SimpleViewAction<StudyLabBatchForm>
    {
        public ModelAndView getView(StudyLabBatchForm form, BindException errors) throws Exception
        {
            _log.debug("Got Form: " + form);
            if (!validateForm(form, errors))
            {
                getRequest().getSession().setAttribute("ELISPOT_USER_ERRORS", errors);
                return HttpView.redirect(new ActionURL(BeginAction.class, getContainer()));
                //v = new JspView(JSP_PATH + JSP_PAGE_MAIN, form,errors);
            }
            else
            {
                JspView<StudyLabBatchForm> v = new JspView(JSP_PAGE_BATCH_SELECT, form, errors);
                return v;
            }
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Batch Select", elispotURL("batchSelect"));
            return root;
        }
    }

    @RequiresPermission(UpdatePermission.class)
    public class BatchMenuAction extends FormViewAction<StudyLabBatchForm>
    {
        public ModelAndView getView(StudyLabBatchForm form, boolean reshow, BindException errors) throws Exception
        {
            List<String> errorList = new LinkedList<String>();
            if (!validateUser(form.getLabstudyseqId(), errorList))
            {
                for (String error : errorList)
                    errors.reject(null, error);
                getRequest().getSession().setAttribute("ELISPOT_USER_ERRORS", errors);
                return HttpView.redirect(new ActionURL(BeginAction.class, getContainer()));
            }
            StudyLab sLab = EliSpotManager.getStudyLab(getContainer(), form.getLabstudyseqId());
            form.setLabId(sLab.getLab_seq_id());
            form.setStudyId(sLab.getStudy_seq_id());
            _log.debug("Got StudyLabBatchForm: " + form);
            JspView<StudyLabBatchForm> v = new JspView(JSP_PAGE_BATCH_MENU, form, errors);
            return v;
        }

        public boolean handlePost(StudyLabBatchForm form, BindException errors) throws Exception
        {
            try
            {
                List<AttachmentFile> uploadFiles = getAttachmentFileList();
                AttachmentFile uploadFile = null;
                for (AttachmentFile a : uploadFiles)
                {
                    if (a != null && a.getSize() != 0)
                        uploadFile = a;
                }
                if (form.getReader_id() == null || StringUtils.trimToNull(form.getReader_id().toString()) == null ||
                        uploadFile == null || StringUtils.trimToNull(uploadFile.getFilename()) == null)
                {
                    errors.reject(null, "Reader Id & File Name are Required.");
                    return false;
                }
                else
                {
                    HashMap<String, InputStream> uploadedFileMap = new HashMap<String, InputStream>();
                    if (uploadFile.getFilename().toUpperCase().endsWith(".ZIP"))
                    {
                        uploadedFileMap = getunZippedFiles(uploadFile, errors);
                        if (!validateZipFile(form, uploadedFileMap, errors))
                        {
                            errors.reject(null, "The zip file " + uploadFile.getFilename() + " was not uploaded because it contains one or more files which do not have the required extension for the selected reader type.");
                            return false;
                        }
                        else
                        {
                            for (String name : uploadedFileMap.keySet())
                            {
                                ProcessElispotDataFile pedf = new ProcessElispotDataFile();
                                List<String> errorList = new LinkedList<String>();
                                if (!pedf.processFile(form, name, uploadedFileMap.get(name), getViewContext(), errorList))
                                {
                                    for (String error : errorList)
                                        errors.reject(null, error);
                                }
                                else
                                    errors.reject(null, "File " + name + " was successfully imported");
                            }
                        }
                    }
                    else
                    {
                        validateFileForm(form, uploadFile.getFilename(), errors);
                        if (errors.getErrorCount() > 0)
                            return false;
                        ProcessElispotDataFile pedf = new ProcessElispotDataFile();
                        List<String> errorList = new LinkedList<String>();
                        if (!pedf.processFile(form, uploadFile.getFilename(), uploadFile.openInputStream(), getViewContext(), errorList))
                        {
                            for (String error : errorList)
                                errors.reject(null, error);
                            return false;
                        }
                        else
                            errors.reject(null, "File " + uploadFile.getFilename() + " was successfully imported");
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                _log.error(e.getMessage(), e);
                errors.reject(null, "There was a problem uploading File: " + e.getMessage());
                return false;
            }

            return false;
        }

        public void validateCommand(StudyLabBatchForm form, Errors errors)
        {
            return;
        }

        public ActionURL getSuccessURL(StudyLabBatchForm form)
        {
            return null;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Batch Menu/Import File", elispotURL("batchMenu"));
            return root;

        }
    }

    @RequiresPermission(UpdatePermission.class)
    public class DisplayBatchSummaryAction extends SimpleViewAction<StudyLabBatchForm>
    {
        public ModelAndView getView(StudyLabBatchForm form, BindException errors) throws Exception
        {
            List<String> errorList = new LinkedList<String>();
            if (!validateUser(form.getLabstudyseqId(), errorList))
            {
                for (String error : errorList)
                    errors.reject(null, error);
                getRequest().getSession().setAttribute("ELISPOT_USER_ERRORS", errors);
                return HttpView.redirect(new ActionURL(BeginAction.class, getContainer()));
                //return new JspView(JSP_PATH + JSP_PAGE_MAIN, form,errors);
            }
            Integer batchId = form.getBatchId();
            BatchInformation batchInfo = EliSpotManager.getBatchInfo(batchId);
            Integer labstudyseqId = form.getLabstudyseqId();
            DataRegion rgn = new DataRegion();
            TableInfo tInfo = EliSpotSchema.getInstance().getTableInfoPlate();
            rgn.setColumns(tInfo.getColumns("plate_seq_id,plate_name,template_seq_id,tech_id,plate_filename,import_date,bool_report_plate,approved_by,modified,comment"));
            DisplayColumn col = rgn.getDisplayColumn("plate_name");
            ActionURL plateInfoAction = new ActionURL(GetPlateInformationAction.class, getContainer());
            plateInfoAction.addParameter("plateId","${plate_seq_id}");
            plateInfoAction.addParameter("batchId","${batch_seq_id}");
            plateInfoAction.addParameter("labstudyseqId", form.getLabstudyseqId());
            col.setURL(plateInfoAction.toString());

            SimpleDisplayColumn editCounts = new UrlColumn(elispotURL("editCellCounts") + "plate_seq_id=${plate_seq_id}&batch_seq_id=" + form.getBatchId() + "&lab_study_seq_id=" + form.getLabstudyseqId(), "View/Edit SampleInfo");
            rgn.addDisplayColumn(editCounts);
            rgn.setShowBorders(true);
            rgn.setShadeAlternatingRows(true);
            ButtonBar gridButtonBar = new ButtonBar();
            //ActionButton goBack = new ActionButton("batchMenu.view?batchId="+batchId+"&&labstudyseqId="+labstudyseqId,"Go Back");

            ActionURL actionUrl = new ActionURL(BatchMenuAction.class, getContainer());
            actionUrl.addParameter("batchId", batchId);
            actionUrl.addParameter("labstudyseqId", labstudyseqId);
            ActionButton goBack = new ActionButton(actionUrl, "Go Back");

            goBack.setActionType(ActionButton.Action.LINK);
            gridButtonBar.add(goBack);
            Study study = EliSpotManager.getStudy(getContainer(), batchInfo.getStudy_seq_id());
            if (study.isPlateinfo_reqd())
            {
                //ActionButton plateInfo = new ActionButton("displayPlateInfo.view?batch_seq_id="+batchId+"&&lab_study_seq_id="+labstudyseqId,"[View/Edit]\nPlateInfo");
                ActionURL displayUrl = new ActionURL(DisplayPlateInfoAction.class, getContainer());
                displayUrl.addParameter("batch_seq_id", batchId);
                displayUrl.addParameter("lab_study_seq_id", labstudyseqId);
                ActionButton plateInfo = new ActionButton(displayUrl, "[View/Edit]\nPlateInfo");

                gridButtonBar.add(plateInfo);
            }
            rgn.setButtonBar(gridButtonBar);
            GridView v = new GridView(rgn, (BindException) null);
            v.setTitle("The plates in the batch : " + batchInfo.getBatch_description() + " for the study " + batchInfo.getStudy_description() + " and lab " + batchInfo.getLab_desc());
            v.setFilter(new SimpleFilter(FieldKey.fromParts("batch_seq_id"), batchId));
            v.setSort(new Sort("plate_seq_id"));
            return v;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Batch Summary", elispotURL("displayBatchSummary"));
            return root;
        }
    }

    @RequiresPermission(UpdatePermission.class)
    public class GetPlateInformationAction extends SimpleViewAction<StudyLabBatchForm>
    {
        public ModelAndView getView(StudyLabBatchForm form, BindException errors) throws Exception
        {
            List<String> errorList = new LinkedList<String>();
            if (!validateUser(form.getLabstudyseqId(), errorList))
            {
                for (String error : errorList)
                    errors.reject(null, error);
                getRequest().getSession().setAttribute("ELISPOT_USER_ERRORS", errors);
                return HttpView.redirect(new ActionURL(BeginAction.class, getContainer()));
                //return new JspView(JSP_PATH + JSP_PAGE_MAIN, form,errors);
            }
            JspView<StudyLabBatchForm> v = new JspView<StudyLabBatchForm>(JSP_PAGE_PLATE_INFO, form);
            return v;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Plate Information", elispotURL("getPlateInformation"));
            return root;
        }
    }

    @RequiresPermission(UpdatePermission.class)
    public class ApprovePlateInformationAction extends SimpleViewAction<StudyLabBatchForm>
    {
        public ModelAndView getView(StudyLabBatchForm form, BindException errors) throws Exception
        {
            ActionURL url = new ActionURL();
            String actionName = getRequest().getParameter("actionName");
            List<String> errorList = new LinkedList<String>();
            if (!validateUser(form.getLabstudyseqId(), errorList))
            {
                for (String error : errorList)
                    errors.reject(null, error);
                getRequest().getSession().setAttribute("ELISPOT_USER_ERRORS", errors);
                return HttpView.redirect(new ActionURL(BeginAction.class, getContainer()));
                //return new JspView(JSP_PATH + JSP_PAGE_MAIN, form,errors);
            }
            if (actionName != null && actionName.equalsIgnoreCase("Save Comments"))
            {
                url = new ActionURL(SaveCommentsAction.class, getContainer());
                url.addParameter("plateId", form.getPlateId());
                url.addParameter("comments", form.getComments());
                url.addParameter("batchId", form.getBatchId());
                url.addParameter("labstudyseqId", form.getLabstudyseqId());
                return HttpView.redirect(url);
            }
            if (actionName != null && actionName.equalsIgnoreCase("Remove Plate File"))
            {
                url = new ActionURL(RemovePlateInformationAction.class, getContainer());
                url.addParameter("plateId", form.getPlateId());
                url.addParameter("comments", form.getComments());
                url.addParameter("batchId", form.getBatchId());
                url.addParameter("labstudyseqId", form.getLabstudyseqId());
                return HttpView.redirect(url);
            }
            if (actionName != null && actionName.equalsIgnoreCase("Go Back"))
            {
                url = new ActionURL(DisplayBatchSummaryAction.class, getContainer());
                url.addParameter("batchId", form.getBatchId());
                url.addParameter("labstudyseqId", form.getLabstudyseqId());
                return HttpView.redirect(url);
            }
            if (actionName != null && actionName.equalsIgnoreCase("View/Edit SampleInfo"))
            {
                url = new ActionURL(EditCellCountsAction.class, getContainer());
                url.addParameter("plate_seq_id", form.getPlateId());
                url.addParameter("batch_seq_id", form.getBatchId());
                url.addParameter("lab_study_seq_id", form.getLabstudyseqId());
                return HttpView.redirect(url);
            }
            Integer plateId = form.getPlateId();
            Integer batchId = form.getBatchId();
            Integer labstudyseqId = form.getLabstudyseqId();
            String comments = form.getComments();
            boolean approved = false;
            String message = null;
            Plate plate = EliSpotManager.getPlate(getContainer(), plateId);

            if (plate != null && plate.getPlate_seq_id() > 0)
            {
                if (plate.getTest_date() == null || plate.getTest_date().toString().length() == 0)
                    errors.reject(null, "Sorry ! could not approve the plate.\nThe test date for the plate must be entered before approve the plate.\n Enter the test date in edit/view Sample Info page.");
                PlateSpecimens[] pltspecimens = EliSpotManager.getPlateSpecimens(plate.getPlate_seq_id());
                if (pltspecimens != null && pltspecimens.length > 0)
                {
                    for (PlateSpecimens pltspec : pltspecimens)
                    {
                        if (pltspec.getSpecimen_seq_id() != null && (pltspec.getCounter_seq_id() == null || pltspec.getCounter_seq_id().toString().length() == 0
                                || pltspec.getD2_cellcount() == null || pltspec.getD2_cellcount().length() == 0
                                || pltspec.getD2_viability() == null || pltspec.getD2_viability().length() == 0))
                            errors.reject(null, "Check Plate Specimen in spec position " + pltspec.getSpec_well_group() +
                                    " to make sure you enter the values for cell counter, d2Cellcount and d2Viability before approve the plate ");
                    }
                }
                if (errors != null && errors.getErrorCount() > 0)
                {
                    getRequest().getSession().setAttribute("ELISPOT_PLATE_ERRORS", errors);
                    url = new ActionURL(EditCellCountsAction.class, getContainer());
                    url.addParameter("plate_seq_id", form.getPlateId());
                    url.addParameter("batch_seq_id", form.getBatchId());
                    url.addParameter("lab_study_seq_id", form.getLabstudyseqId());
                    return HttpView.redirect(url);
                }
                BatchInformation batchInfo = EliSpotManager.getBatchInfo(batchId);
                Study study = EliSpotManager.getStudy(getContainer(), batchInfo.getStudy_seq_id());
                if (study.isPlateinfo_reqd())
                {
                    if (plate.getIsprecoated() == null || plate.getIsprecoated().length() == 0
                            || plate.getPlatetype_seq_id() == null || plate.getPlatetype_seq_id().toString().length() == 0
                            || plate.getSubstrate_seq_id() == null || plate.getSubstrate_seq_id().toString().length() == 0)
                    {
                        errors.reject(null, "Sorry ! could not approve the plate.\nThe study in which the plate you are trying to approve needs some extra info about the plate to be filled out before you approve the plate.\n" +
                                "The fields 'isprecoated' , 'platetype' and 'substrate' to be filled before approve the plate.\n " +
                                "Enter the details for the plate " + plate.getPlate_name() + " in [View/Edit] PlateInfo page.");
                        getRequest().getSession().setAttribute("ELISPOT_PLATE_ERRORS", errors);
                        url = new ActionURL(DisplayPlateInfoAction.class, getContainer());
                        url.addParameter("batch_seq_id", form.getBatchId());
                        url.addParameter("lab_study_seq_id", form.getLabstudyseqId());
                        return HttpView.redirect(url);
                    }

                }
                plate.setBool_report_plate(true);
                plate.setComment(form.getComments());
                plate.setApproved_by(getUser().getUserId());
                approved = EliSpotManager.updatePlate(getContainer(), getUser(), plate);
                if (approved)
                    message = "The Plate has been approved successfully.";

            }
            if (plate == null || (!approved))
                message = "There is something wrong with the update.Could not be updated";
            url = new ActionURL(GetPlateInformationAction.class, getContainer());
            url.addParameter("plateId", plateId.toString());
            url.addParameter("batchId", batchId.toString());
            url.addParameter("labstudyseqId", labstudyseqId.toString());
            url.addParameter("message", message);
            return HttpView.redirect(url);
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Plate Information", elispotURL("approvePlateInformation"));
            return root;
        }
    }

    @RequiresPermission(UpdatePermission.class)
    public class SaveCommentsAction extends RedirectAction<StudyLabBatchForm> // TODO: Should extend FormHandlerAction instead
    {
        public boolean doAction(StudyLabBatchForm form, BindException errors) throws Exception
        {
            String comments = form.getComments();
            Plate plate = EliSpotManager.getPlate(getContainer(), form.getPlateId());
            plate.setComment(comments);
            EliSpotManager.updatePlate(getContainer(), getUser(), plate);
            return true;
        }

        public ActionURL getSuccessURL(StudyLabBatchForm form)
        {
            ActionURL url = new ActionURL(GetPlateInformationAction.class, getContainer());
            url.addParameter("plateId", form.getPlateId());
            url.addParameter("batchId", form.getBatchId());
            url.addParameter("labstudyseqId", form.getLabstudyseqId());
            return url;
        }
    }

    @RequiresPermission(UpdatePermission.class)
    public class RemovePlateInformationAction extends RedirectAction<StudyLabBatchForm> // TODO: Should extend FormHandlerAction instead
    {
        public boolean doAction(StudyLabBatchForm form, BindException errors) throws Exception
        {
            Integer plateId = form.getPlateId();
            String comments = form.getComments();
            boolean removed = EliSpotManager.removePlateInfo(getUser(), plateId, comments);
            if (!removed)
                return false;
            else
                return true;
        }

        public ActionURL getSuccessURL(StudyLabBatchForm form)
        {
            ActionURL url = new ActionURL(DisplayBatchSummaryAction.class, getContainer());
            url.addParameter("batchId", form.getBatchId());
            url.addParameter("labstudyseqId", form.getLabstudyseqId());
            return url;
        }
    }

    @RequiresPermission(UpdatePermission.class)
    public class EditCellCountsAction extends FormViewAction<UpdatePSpecimensForm>
    {
        public ModelAndView getView(UpdatePSpecimensForm form, boolean reshow, BindException errors) throws Exception
        {
            List<String> errorList = new LinkedList<String>();
            if (!validateUser(form.getLab_study_seq_id(), errorList))
            {
                for (String error : errorList)
                    errors.reject(null, error);
                getRequest().getSession().setAttribute("ELISPOT_USER_ERRORS", errors);
                return HttpView.redirect(new ActionURL(BeginAction.class, getContainer()));
                //return new JspView(JSP_PATH + JSP_PAGE_MAIN, form,errors);
            }
            BindException e = (BindException) getRequest().getSession().getAttribute("ELISPOT_PLATE_ERRORS");
            if (e != null && e.getErrorCount() > 0)
            {
                errors = e;
                getRequest().getSession().removeAttribute("ELISPOT_PLATE_ERRORS");
            }
            Integer plateId = form.getPlate_seq_id();
            JspView<UpdatePSpecimensForm> v = new JspView<UpdatePSpecimensForm>(JSP_PAGE_UPDATE_PLATESPEC, form, errors);
            return v;
        }

        public boolean handlePost(UpdatePSpecimensForm form, BindException errorMessages) throws Exception
        {
            Integer plateseqId = form.getPlate_seq_id();
            String[] specwellgroups = form.getSpecwellgroups();
            String[] specimenseqIds = form.getSpecimenseqIds();
            String[] counterseqIds = form.getCounterseqIds();
            String[] d1_cellcounts = form.getD1_cellcounts();
            String[] d2_cellcounts = form.getD2_cellcounts();
            String[] d1_viabilities = form.getD1_viabilities();
            String[] d2_viabilities = form.getD2_viabilities();
            String testDate = form.getTest_date();
            if (testDate != null && testDate.length() > 0 && EliSpotManager.isValidDate(testDate) != null)
            {
                java.sql.Date date = new java.sql.Date(EliSpotManager.isValidDate(testDate).getTime());
                EliSpotManager.updateTestDate(getUser(), plateseqId, date);
            }
            for (int i = 0; i < specimenseqIds.length; i++)
            {
                String specimenseqId = specimenseqIds[i];
                if (specimenseqId != null && specimenseqId.length() != 0)
                {
                    PlateSpecimens pSpecimen = new PlateSpecimens();
                    pSpecimen.setPlate_seq_id(plateseqId);
                    pSpecimen.setSpecimen_seq_id(Integer.parseInt(specimenseqIds[i]));
                    if (counterseqIds[i] != null && counterseqIds[i].length() != 0)
                        pSpecimen.setCounter_seq_id(Integer.parseInt(counterseqIds[i]));
                    pSpecimen.setD1_cellcount(d1_cellcounts[i]);
                    pSpecimen.setD1_viability(d1_viabilities[i]);
                    pSpecimen.setD2_cellcount(d2_cellcounts[i]);
                    pSpecimen.setD2_viability(d2_viabilities[i]);
                    EliSpotManager.updateCellCounts(getUser(), pSpecimen);
                }
            }
            return true;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("View/Edit Cell Counts", elispotURL("editCellCounts"));
            return root;
        }

        public void validateCommand(UpdatePSpecimensForm form, Errors errors)
        {
            form.validate(errors);
        }

        public ActionURL getSuccessURL(UpdatePSpecimensForm form)
        {
            ActionURL url = new ActionURL(DisplayBatchSummaryAction.class, getContainer());
            url.addParameter("batchId", form.getBatch_seq_id().toString());
            url.addParameter("labstudyseqId", form.getLab_study_seq_id().toString());
            return url;
        }
    }

    @RequiresPermission(UpdatePermission.class)
    public class DisplayPlateInfoAction extends SimpleViewAction<UpdatePInfoForm>
    {
        public ModelAndView getView(UpdatePInfoForm form, BindException errors) throws Exception
        {
            List<String> errorList = new LinkedList<String>();
            if (!validateUser(form.getLab_study_seq_id(), errorList))
            {
                for (String error : errorList)
                    errors.reject(null, error);
                getRequest().getSession().setAttribute("ELISPOT_USER_ERRORS", errors);
                return HttpView.redirect(new ActionURL(BeginAction.class, getContainer()));
                //return new JspView(JSP_PATH + JSP_PAGE_MAIN, form,errors);
            }
            BindException e = (BindException) getRequest().getSession().getAttribute("ELISPOT_PLATE_ERRORS");
            if (e != null && e.getErrorCount() > 0)
            {
                errors = e;
                getRequest().getSession().removeAttribute("ELISPOT_PLATE_ERRORS");
            }

            JspView<UpdatePInfoForm> v = new JspView<UpdatePInfoForm>(JSP_PAGE_UPDATE_PLATE_INFO, form, errors);
            return v;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Display Plate Information", elispotURL("displayPlateInfo"));
            return root;
        }
    }

    @RequiresPermission(UpdatePermission.class)
    public class UpdatePlateInfoAction extends RedirectAction<UpdatePInfoForm> // TODO: Should extend FormHandlerAction instead
    {
        public boolean doAction(UpdatePInfoForm form, BindException errors) throws Exception
        {
            String[] plateseqIds = form.getPlateIds();
            String[] precoated = form.getPreCoated();
            String[] platetypes = form.getPlateTypes();
            String[] substrates = form.getSubstrates();
            for (int i = 0; i < plateseqIds.length; i++)
            {
                String plateseqId = plateseqIds[i];
                if (plateseqId != null && plateseqId.length() != 0)
                {
                    Plate plate = new Plate();
                    plate.setPlate_seq_id(Integer.parseInt(plateseqId));
                    if (precoated != null && precoated.length != 0)
                        plate.setIsprecoated(precoated[i] == null || precoated[i].length() == 0 ? null : precoated[i]);
                    if (platetypes != null && platetypes.length != 0)
                        plate.setPlatetype_seq_id(platetypes[i] == null || platetypes[i].length() == 0 ? null : Integer.parseInt(platetypes[i]));
                    if (substrates != null && substrates.length != 0)
                        plate.setSubstrate_seq_id(substrates[i] == null || substrates[i].length() == 0 ? null : Integer.parseInt(substrates[i]));
                    EliSpotManager.updatePInfo(getUser(), plate);
                }
            }
            return true;
        }

        public ActionURL getSuccessURL(UpdatePInfoForm form)
        {
            ActionURL url = new ActionURL(DisplayBatchSummaryAction.class, getContainer());
            url.addParameter("batchId", form.getBatch_seq_id());
            url.addParameter("labstudyseqId", form.getLab_study_seq_id());
            return url;
        }
    }

    @RequiresPermission(AdminPermission.class)
    public class ShowDataAction extends SimpleViewAction<ShowDataForm>
    {
        public ModelAndView getView(ShowDataForm form, BindException errors) throws Exception
        {
            String tableName = form.getTableName();
            String message = form.getMessage();
            SimpleFilter sFilter = new SimpleFilter();
            Sort sort = new Sort();
            StringBuilder builder = new StringBuilder();
            ActionURL showDataAction = new ActionURL(ShowDataAction.class, getContainer());
            if (message != null && message.length() != 0)
                builder.append(message).append("\n");

            DataRegion rgn = new DataRegion();
            if (tableName.equals(EliSpotSchema.TABLE_STUDIES))
            {
                rgn = studyDataRegion(true);
                SimpleDisplayColumn activate = new UrlColumn(elispotURL("activateStudy") + "studyId=${study_seq_id}", "Activate Study");
                rgn.addDisplayColumn(activate);
                builder.append("The studies in the network : ").append(getContainer().getProject().getName());
            }
            if (tableName.equals(EliSpotSchema.TABLE_LABS))
            {
                rgn = labDataRegion(true);
                builder.append("The Labs in the network : ").append(getContainer().getProject().getName());
            }
            if (tableName.equals(EliSpotSchema.TABLE_STUDY_LABS))
            {
                rgn = studylabDataRegion(true);
                DisplayColumn col = rgn.getDisplayColumn("lab_study_seq_id");
                showDataAction.addParameter("tableName",EliSpotSchema.TABLE_BATCH);
                showDataAction.addParameter("queryString",EliSpotSchema.COLUMN_LABSTUDY_SEQ_ID);
                showDataAction.addParameter("queryValue","${lab_study_seq_id}");
                col.setURL(showDataAction.toString());
                builder.append("The Study Lab Combinations in the network : ").append(getContainer().getProject().getName());
            }
            if (tableName.equals(EliSpotSchema.TABLE_BATCH))
            {
                rgn = batchDataRegion(form.getQueryValue(), true);
                DisplayColumn col = rgn.getDisplayColumn("batch_seq_id");
                showDataAction.addParameter("tableName", EliSpotSchema.TABLE_PLATE);
                showDataAction.addParameter("queryString", EliSpotSchema.COLUMN_BATCH_SEQ_ID );
                showDataAction.addParameter("queryValue","${batch_seq_id}");
                col.setURL(showDataAction.toString());
                sFilter.addCondition(form.getQueryString(), Integer.parseInt(form.getQueryValue()));
                builder.append("The Batches in the Study Lab Combination : ");
            }
            if (tableName.equals(EliSpotSchema.TABLE_PLATE_TEMPLATE))
            {
                rgn = plateTemplateDataRegion(true);
                builder.append("The Templates in the network : ").append(getContainer().getProject().getName());
            }
            if (tableName.equals(EliSpotSchema.TABLE_PLATETEMPLATE_DETAILS))
            {
                rgn = plateTemplateDetailsDataRegion(true);
                builder.append("The Template Details in the network : ").append(getContainer().getProject().getName());
            }
            if (tableName.equals(EliSpotSchema.TABLE_SPECIMEN))
            {
                rgn = specimenDataRegion(true);
                builder.append("The Specimens in the network : ").append(getContainer().getProject().getName());
            }
            if (tableName.equals(EliSpotSchema.TABLE_PLATE))
            {
                rgn = plateDataRegion(form.getQueryValue(), true);
                DisplayColumn col = rgn.getDisplayColumn("plate_seq_id");
                showDataAction.addParameter("tableName", EliSpotSchema.TABLE_PLATE_SPECIMENS );
                showDataAction.addParameter("queryString", EliSpotSchema.COLUMN_PLATE_SEQ_ID);
                showDataAction.addParameter("queryValue","${plate_seq_id}");
                col.setURL(showDataAction.toString());
                sFilter.addCondition(form.getQueryString(), Integer.parseInt(form.getQueryValue()));
                sort.insertSort(new Sort("plate_seq_id"));
                builder.append("The Plates in the Batch : ").append(form.getQueryValue());
            }
            if (tableName.equals(EliSpotSchema.TABLE_PLATE_SPECIMENS))
            {
                rgn = plateSpecimenDataRegion(form.getQueryValue(), true);
                sFilter.addCondition(form.getQueryString(), Integer.parseInt(form.getQueryValue()));
                builder.append("The Plate Specimens in the Plate : ").append(form.getQueryValue());
            }
            GridView gridView = new GridView(rgn, (BindException) null);
            gridView.setTitle(builder.toString());
            gridView.setFilter(sFilter);
            gridView.setSort(sort);
            return gridView;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Display Admin Data", elispotURL("showData"));
            return root;
        }
    }

    @RequiresPermission(AdminPermission.class)
    public class InsertToStudyAction extends FormViewAction<StudyForm>
    {
        public ModelAndView getView(StudyForm form, boolean reshow, BindException errors) throws Exception
        {
            DataRegion rgn = studyDataRegion(false);
            //rgn.setButtonBar(getButtonBar("insertToStudy.post","showData.view?tableName="+EliSpotSchema.TABLE_STUDIES));
            ActionURL insertURL = new ActionURL(InsertToStudyAction.class, getContainer());
            ActionURL actionURL = new ActionURL(ShowDataAction.class, getContainer());
            actionURL.addParameter("tableName", EliSpotSchema.TABLE_STUDIES);
            rgn.setButtonBar(getButtonBar(insertURL, actionURL, ActionButton.Action.POST));

            InsertView insertView = new InsertView(rgn, form, errors);
            insertView.setTitle("Insert Data into the table " + EliSpotSchema.TABLE_STUDIES);
            return insertView;
        }

        public boolean handlePost(StudyForm form, BindException errors) throws Exception
        {
            Study study = form.getBean();
            study.setNetwork_organization(getContainer().getProject().getName());
            Integer studyCount = EliSpotManager.getStudyCount(getContainer());
            studyCount = studyCount + 1;
            study.setStudy_identifier("S" + toLZ(studyCount, 2));
            study.setStatus("INACTIVE");
            EliSpotManager.insertStudy(getContainer(), getUser(), study);
            return true;
        }

        public void validateCommand(StudyForm form, Errors errors)
        {
            form.validate(errors);
        }

        public ActionURL getSuccessURL(StudyForm form)
        {
            ActionURL url = new ActionURL(EliSpotController.ShowDataAction.class, getContainer());
            url.addParameter("tableName", EliSpotSchema.TABLE_STUDIES);
            url.addParameter("message", "The study has been inserted successfully.");
            return url;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Insert To Study", elispotURL("insertToStudy"));
            return root;
        }
    }

    @RequiresPermission(AdminPermission.class)
    public class ActivateStudyAction extends RedirectAction // TODO: Should extend FormHandlerAction instead
    {
        public boolean doAction(Object form, BindException errors) throws Exception
        {
            Integer studyId = Integer.parseInt(getRequest().getParameter("studyId"));
            Study study = EliSpotManager.getStudy(getContainer(), studyId);
            if (study != null && study.getStatus().equalsIgnoreCase("INACTIVE"))
            {
                study.setStatus("ACTIVE");
                EliSpotManager.updateStudy(getUser(), getContainer(), study);
            }
            return true;
        }

        public ActionURL getSuccessURL(Object form)
        {
            ActionURL url = new ActionURL(ShowDataAction.class, getContainer());
            url.addParameter("tableName", EliSpotSchema.TABLE_STUDIES);
            return url;
        }
    }

    @RequiresPermission(AdminPermission.class)
    public class InsertToLabsAction extends FormViewAction<LabForm>
    {
        public ModelAndView getView(LabForm form, boolean reshow, BindException errors) throws Exception
        {
            DataRegion rgn = labDataRegion(false);
            //rgn.setButtonBar(getButtonBar("insertToLabs.post","showData.view?tableName="+EliSpotSchema.TABLE_LABS));

            ActionURL insertURL = new ActionURL(InsertToLabsAction.class, getContainer());
            ActionURL actionURL = new ActionURL(ShowDataAction.class, getContainer());
            actionURL.addParameter("tableName", EliSpotSchema.TABLE_LABS);
            rgn.setButtonBar(getButtonBar(insertURL, actionURL, ActionButton.Action.POST));

            InsertView insertView = new InsertView(rgn, form, errors);
            insertView.setTitle("Insert Data into the table " + EliSpotSchema.TABLE_LABS);
            return insertView;
        }

        public boolean handlePost(LabForm form, BindException errors) throws Exception
        {
            Lab lab = form.getBean();
            EliSpotManager.insertLab(getContainer(), getUser(), lab);
            return true;
        }

        public void validateCommand(LabForm form, Errors errors)
        {
            form.validate(errors);
        }

        public ActionURL getSuccessURL(LabForm form)
        {
            ActionURL url = new ActionURL(ShowDataAction.class, getContainer());
            url.addParameter("tableName", EliSpotSchema.TABLE_LABS);
            url.addParameter("message", "The lab has been inserted successfully.");
            return url;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Insert To Lab", elispotURL("insertToLab"));
            return root;
        }
    }

    @RequiresPermission(AdminPermission.class)
    public class InsertToStudyLabsAction extends FormViewAction<StudyLabForm>
    {
        public ModelAndView getView(StudyLabForm form, boolean reshow, BindException errors) throws Exception
        {
            DataRegion rgn = studylabDataRegion(false);
            //rgn.setButtonBar(getButtonBar("insertToStudyLabs.post","showData.view?tableName="+EliSpotSchema.TABLE_STUDY_LABS));

            ActionURL insertURL = new ActionURL(InsertToStudyLabsAction.class, getContainer());
            ActionURL actionURL = new ActionURL(ShowDataAction.class, getContainer());
            actionURL.addParameter("tableName", EliSpotSchema.TABLE_STUDY_LABS);
            rgn.setButtonBar(getButtonBar(insertURL, actionURL, ActionButton.Action.POST));

            InsertView insertView = new InsertView(rgn, form, errors);
            insertView.setTitle("Insert Data into the table " + EliSpotSchema.TABLE_STUDY_LABS);
            return insertView;
        }

        public boolean handlePost(StudyLabForm form, BindException errors) throws Exception
        {
            StudyLab studyLab = form.getBean();
            EliSpotManager.insertStudyLab(getContainer(), getUser(), studyLab);
            return true;
        }

        public void validateCommand(StudyLabForm form, Errors errors)
        {
            form.validate(errors);
        }

        public ActionURL getSuccessURL(StudyLabForm form)
        {
            ActionURL url = new ActionURL(ShowDataAction.class, getContainer());
            url.addParameter("tableName", EliSpotSchema.TABLE_STUDY_LABS);
            url.addParameter("message", "The study and the lab are associated successfully.");
            return url;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Insert To Study Lab", elispotURL("insertToStudyLabs"));
            return root;
        }
    }

    @RequiresPermission(AdminPermission.class)
    public class InsertToBatchAction extends FormViewAction<BatchForm>
    {
        public ModelAndView getView(BatchForm form, boolean reshow, BindException errors) throws Exception
        {
            if (form.getLab_study_seq_id() == null)
                form.setLab_study_seq_id(Integer.parseInt(getRequest().getParameter("labstudyseqId")));
            JspView<BatchForm> v = new JspView<BatchForm>(JSP_PAGE_INSERT_BATCH, form, errors);
            return v;
        }

        public boolean handlePost(BatchForm form, BindException errors) throws Exception
        {
            Batch batch = new Batch();
            batch.setLab_study_seq_id(form.getLab_study_seq_id());
            batch.setBatch_description(form.getBatch_description());
            batch.setBatch_type(form.getBatch_type());
            EliSpotManager.insertBatch(getContainer(), getUser(), batch);
            return true;
        }

        public void validateCommand(BatchForm form, Errors errors)
        {
            form.validate(errors);
        }

        public ActionURL getSuccessURL(BatchForm form)
        {
            ActionURL url = new ActionURL(ShowDataAction.class, getContainer());
            url.addParameter("tableName", EliSpotSchema.TABLE_BATCH);
            url.addParameter("message", "The batch has been inserted successfully.");
            url.addParameter("queryString", EliSpotSchema.COLUMN_LABSTUDY_SEQ_ID);
            url.addParameter("queryValue", form.getLab_study_seq_id().toString());
            return url;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Insert To Batch", elispotURL("insertToBatch"));
            return root;
        }
    }

    @RequiresPermission(AdminPermission.class)
    public class InsertToPlateAction extends FormViewAction<PlateForm>
    {
        public ModelAndView getView(PlateForm form, boolean reshow, BindException errors) throws Exception
        {
            String batchId = getRequest().getParameter("batchseqId");
            DataRegion rgn = plateDataRegion(batchId, false);
            //rgn.setButtonBar(getButtonBar("insertToPlate.post?batchseqId="+batchId,"showData.view?tableName="+EliSpotSchema.TABLE_PLATE+"&&queryString="+EliSpotSchema.COLUMN_BATCH_SEQ_ID+"&&queryValue="+batchId));
            ActionURL insertURL = new ActionURL(InsertToPlateAction.class, getContainer());
            insertURL.addParameter("batchseqId", batchId);
            ActionURL actionURL = new ActionURL(ShowDataAction.class, getContainer());
            actionURL.addParameter("tableName", EliSpotSchema.TABLE_PLATE);
            actionURL.addParameter("queryString", EliSpotSchema.COLUMN_BATCH_SEQ_ID);
            actionURL.addParameter("queryValue", batchId);
            rgn.setButtonBar(getButtonBar(insertURL, actionURL, ActionButton.Action.POST));

            InsertView insertView = new InsertView(rgn, form, errors);
            insertView.setTitle("Insert Data into the table " + EliSpotSchema.TABLE_STUDY_LABS);
            return insertView;
        }

        public boolean handlePost(PlateForm form, BindException errors) throws Exception
        {
            String batchId = getRequest().getParameter("batchseqId");
            Plate plate = form.getBean();
            plate.setBatch_seq_id(Integer.parseInt(batchId));
            EliSpotManager.insertPlate(getContainer(), getUser(), plate);
            return true;
        }

        public void validateCommand(PlateForm form, Errors errors)
        {
            form.validate(errors);
        }

        public ActionURL getSuccessURL(PlateForm form)
        {
            String batchId = getRequest().getParameter("batchseqId");
            ActionURL url = new ActionURL(ShowDataAction.class, getContainer());
            url.addParameter("tableName", EliSpotSchema.TABLE_PLATE);
            url.addParameter("message", "The plate has been inserted successfully.");
            url.addParameter("queryString", EliSpotSchema.COLUMN_BATCH_SEQ_ID);
            url.addParameter("queryValue", batchId);
            return url;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Insert To Plate", elispotURL("insertToBatch"));
            return root;
        }
    }

    @RequiresPermission(AdminPermission.class)
    public class InsertToPlateSpecimensAction extends FormViewAction<PlateSpecimensForm>
    {
        public ModelAndView getView(PlateSpecimensForm form, boolean reshow, BindException errors) throws Exception
        {
            if (form.getPlate_seq_id() == null)
                form.setPlate_seq_id(Integer.parseInt(getRequest().getParameter("plateseqId")));
            JspView<PlateSpecimensForm> v = new JspView<PlateSpecimensForm>(JSP_PAGE_INSERT_PLATESPEC, form, errors);
            return v;
        }

        public boolean handlePost(PlateSpecimensForm form, BindException errors) throws Exception
        {
            String[] specwellgroups = form.getSpecwellgroups();
            String[] specimenseqIds = form.getSpecimenseqIds();
            String[] runnums = form.getRunnums();
            String[] additives = form.getAdditives();
            String[] cryostatus = form.getCryostatus();
            for (int i = 0; i < form.getNum_well_groups_per_plate(); i++)
            {
                PlateSpecimens pSpecimen = new PlateSpecimens();
                pSpecimen.setBool_report_specimen(true);
                pSpecimen.setSpec_well_group(specwellgroups[i]);
                if (runnums != null && runnums[i] != null && runnums[i].length() != 0)
                    pSpecimen.setRunnum(Integer.parseInt(runnums[i]));
                if (additives != null && additives[i] != null && additives[i].length() != 0)
                    pSpecimen.setAdditive_seq_id(Integer.parseInt(additives[i]));
                if (cryostatus != null && cryostatus[i] != null && cryostatus[i].length() != 0)
                    pSpecimen.setCryostatus(Integer.parseInt(cryostatus[i]));
                pSpecimen.setPlate_seq_id(form.getPlate_seq_id());
                if (specimenseqIds != null && specimenseqIds[i] != null && specimenseqIds[i].length() != 0)
                    pSpecimen.setSpecimen_seq_id(Integer.parseInt(specimenseqIds[i]));
                EliSpotManager.insertPlateSpecimens(getContainer(), getUser(), pSpecimen);
            }
            return true;
        }

        public void validateCommand(PlateSpecimensForm form, Errors errors)
        {
            form.validate(errors);
        }

        public ActionURL getSuccessURL(PlateSpecimensForm form)
        {
            ActionURL url = new ActionURL(ShowDataAction.class, getContainer());
            url.addParameter("tableName", EliSpotSchema.TABLE_PLATE_SPECIMENS);
            url.addParameter("message", "The Plate specimens are inserted successfully");
            url.addParameter("queryString", EliSpotSchema.COLUMN_PLATE_SEQ_ID);
            url.addParameter("queryValue", form.getPlate_seq_id().toString());
            return url;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Insert To Plate Specimens", elispotURL("insertToPlateSpecimen"));
            return root;
        }
    }

    @RequiresPermission(AdminPermission.class)
    public class InsertToPlateTemplateAction extends FormViewAction<PlateTemplateForm>
    {
        public ModelAndView getView(PlateTemplateForm form, boolean reshow, BindException errors) throws Exception
        {
            DataRegion rgn = plateTemplateDataRegion(false);
            //rgn.setButtonBar(getButtonBar("insertToPlateTemplate.post","showData.view?tableName="+EliSpotSchema.TABLE_PLATE_TEMPLATE));
            ActionURL insertURL = new ActionURL(InsertToPlateTemplateAction.class, getContainer());
            ActionURL actionURL = new ActionURL(ShowDataAction.class, getContainer());
            actionURL.addParameter("tableName", EliSpotSchema.TABLE_PLATE_TEMPLATE);
            rgn.setButtonBar(getButtonBar(insertURL, actionURL, ActionButton.Action.POST));

            InsertView insertView = new InsertView(rgn, form, errors);
            insertView.setTitle("Insert Data into the table " + EliSpotSchema.TABLE_PLATE_TEMPLATE);
            return insertView;
        }

        public boolean handlePost(PlateTemplateForm form, BindException errors) throws Exception
        {
            EliSpotManager.insertPlateTemplate(getContainer(), getUser(), form.getBean());
            return true;
        }

        public void validateCommand(PlateTemplateForm form, Errors errors)
        {
            form.validate(errors);
        }

        public ActionURL getSuccessURL(PlateTemplateForm form)
        {
            ActionURL url = new ActionURL(ShowDataAction.class, getContainer());
            url.addParameter("tableName", EliSpotSchema.TABLE_PLATE_TEMPLATE);
            url.addParameter("message", "The plate template has been inserted successfully.");
            return url;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Insert To Plate Template", elispotURL("insertToPlateTemplate"));
            return root;
        }
    }

    @RequiresPermission(AdminPermission.class)
    public class InsertToSpecimenAction extends FormViewAction<SpecimenForm>
    {
        public ModelAndView getView(SpecimenForm form, boolean reshow, BindException errors) throws Exception
        {
            DataRegion rgn = specimenDataRegion(false);
            //rgn.setButtonBar(getButtonBar("insertToSpecimen.post","showData.view?tableName="+EliSpotSchema.TABLE_SPECIMEN));
            ActionURL insertURL = new ActionURL(InsertToSpecimenAction.class, getContainer());
            ActionURL actionURL = new ActionURL(ShowDataAction.class, getContainer());
            actionURL.addParameter("tableName", EliSpotSchema.TABLE_SPECIMEN);
            rgn.setButtonBar(getButtonBar(insertURL, actionURL, ActionButton.Action.POST));

            InsertView insertView = new InsertView(rgn, form, errors);
            insertView.setTitle("Insert Data into the table " + EliSpotSchema.TABLE_SPECIMEN);
            return insertView;
        }

        public boolean handlePost(SpecimenForm form, BindException errors) throws Exception
        {
            EliSpotManager.insertSpecimen(getContainer(), getUser(), form.getBean());
            return true;
        }

        public void validateCommand(SpecimenForm form, Errors errors)
        {
            form.validate(errors);
        }

        public ActionURL getSuccessURL(SpecimenForm form)
        {
            ActionURL url = new ActionURL(ShowDataAction.class, getContainer());
            url.addParameter("tableName", EliSpotSchema.TABLE_SPECIMEN);
            url.addParameter("message", "The specimen has been inserted successfully.");
            return url;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Insert To Specimens", elispotURL("insertToSpecimen"));
            return root;
        }
    }

    @RequiresPermission(AdminPermission.class)
    public class ImportPTDetailsAction extends FormViewAction<PTDetailsForm>
    {
        public ModelAndView getView(PTDetailsForm form, boolean reshow, BindException errors) throws Exception
        {
            JspView<PTDetailsForm> v = new JspView<PTDetailsForm>(JSP_PAGE_IMPORT_PTD, form, errors);
            return v;
        }

        public boolean handlePost(PTDetailsForm form, BindException errors) throws Exception
        {
            try
            {
                List<AttachmentFile> uploadFiles = getAttachmentFileList();
                AttachmentFile uploadFile = null;
                for (AttachmentFile a : uploadFiles)
                {
                    if (a != null && a.getSize() != 0)
                        uploadFile = a;
                }
                if (uploadFile == null || StringUtils.trimToNull(uploadFile.getFilename()) == null || !uploadFile.getFilename().toUpperCase().endsWith(".TXT"))
                {
                    errors.reject(null, "File is Required and has to be .txt file.");
                    return false;
                }
                else
                {
                    PTDetailsImporter importer = new PTDetailsImporter();
                    List<String> errorList = new LinkedList<String>();
                    if (!importer.process(getViewContext(), form, uploadFile, errorList))
                    {
                        for (String error : errorList)
                            errors.reject(null, error);
                        return false;
                    }
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
        }

        public void validateCommand(PTDetailsForm form, Errors errors)
        {
            form.validate(errors);
        }

        public ActionURL getSuccessURL(PTDetailsForm form)
        {
            ActionURL url = new ActionURL(ShowDataAction.class, getContainer());
            url.addParameter("tableName", EliSpotSchema.TABLE_PLATETEMPLATE_DETAILS);
            url.addParameter("message", "The Plate Template details have been imported successfully for the template" + form.getTemplate_seq_id() + " .");
            return url;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Import To Plate Template Details", elispotURL("importPTDetails"));
            return root;
        }
    }
}
