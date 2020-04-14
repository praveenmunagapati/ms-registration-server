package org.scharp.atlas.specimentracking;

import org.labkey.api.action.*;
import org.labkey.api.view.*;
import org.labkey.api.data.*;
import org.labkey.api.security.RequiresPermission;
import org.labkey.api.security.permissions.UpdatePermission;
import org.labkey.api.security.permissions.ReadPermission;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.query.FieldKey;
import org.labkey.api.attachments.AttachmentFile;
import org.apache.log4j.Logger;
import org.apache.commons.lang3.StringUtils;
import org.scharp.atlas.specimentracking.model.ManifestSpecimens;
import org.scharp.atlas.specimentracking.model.Manifests;
import org.springframework.validation.Errors;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.LinkedList;
import java.util.regex.PatternSyntaxException;
import java.sql.SQLException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;

/**
 * User: sravani
 * Date: Jul 6, 2009
 * Time: 8:19:23 AM
 */
public class SpecimentrackingController extends SpringActionController
{
    private final static Logger _log = Logger.getLogger(SpecimentrackingController.class);
    private static DefaultActionResolver _actionResolver =
            new DefaultActionResolver(SpecimentrackingController.class);

    private static final String STR_FILE_DOES_NOT_EXIST_MSG = "The file you requested does not exist. Please check the path and the filename.";
    private static final String STR_MANIFEST_WRONG_FORMAT_MSG = "The manifest you requested is in the wrong format.";

    public SpecimentrackingController()
    {
        setActionResolver(_actionResolver);
    }

    public ActionURL specimentrackingURL(String action)
    {
        Container c = getViewContext().getContainer();
        return new ActionURL("Specimen_tracking", action, c);
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
    public class BeginAction extends SimpleViewAction
    {
        public ModelAndView getView(Object form, BindException errors) throws Exception
        {
            if(getContainer().hasPermission(getUser(), AdminPermission.class))
            {
                GridView gridView = new GridView(getDataRegion(getContainer()),(BindException)null);
                gridView.setSort(new Sort("RowId"));
                gridView.setTitle("All the Manifests in the system are : ");
                return gridView;
            }
            else
            {
                getContainer().hasPermission(getUser(), UpdatePermission.class);
                GridView gridView = new GridView(getDataRegion(getContainer()),(BindException)null);
                gridView.setFilter(new SimpleFilter(FieldKey.fromParts("CreatedBy"), getUser().getUserId()));
                gridView.setSort(new Sort("RowId"));
                gridView.setTitle("The Manifests uploaded by the user "+getUser().getEmail()+": ");
                JspView newView = new JspView<ViewContext>("/org/scharp/atlas/specimentracking/view/notes.jsp");
                VBox vBox = new VBox();
                vBox.addView(gridView);
                vBox.addView(newView);
                return vBox;
            }
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Begin", specimentrackingURL("begin"));
            return root;
        }
    }

    @RequiresPermission(UpdatePermission.class)
    public class UploadAction extends FormViewAction<ManifestFileForm>
    {
        ActionURL urlTest = null;
        public ModelAndView getView(ManifestFileForm form, boolean reshow, BindException errors) throws Exception
        {
            JspView v = new JspView<ManifestFileForm>("/org/scharp/atlas/specimentracking/view/UploadManifest.jsp",form,errors);
            return v;
        }

        public boolean handlePost(ManifestFileForm form, BindException errors) throws Exception
        {
            try{
                List<AttachmentFile> mFiles = getAttachmentFileList();
                String manifestFileName;
                AttachmentFile mFile = null;
                for(AttachmentFile a : mFiles)
                {
                    if(a != null && a.getSize() != 0)
                        mFile = a;
                }
                if (mFile == null || mFile.getFilename() == null || mFile.getFilename().length() == 0){
                    errors.reject(null,"Manifest file is required.");
                    return false;
                }
                else
                {
                    manifestFileName = mFile.getFilename();
                    if (!(manifestFileName.endsWith(".csv")))
                    {
                        errors.reject(null,"Manifest file must end with in .csv");
                        return false;
                    }
                }
                InputStream st = mFile.openInputStream();
                InputStreamReader inStream = new InputStreamReader(st);
                BufferedReader br = new BufferedReader(inStream);
                String line;
                List<String> errorList = new LinkedList<String>();
                line = br.readLine();
                if(line != null)
                {
                    if(!(line.split(",").length >=19 && line.split(",").length <=20))
                    {
                        form.setMessage(STR_MANIFEST_WRONG_FORMAT_MSG+"\nThere should be 19 or 20 fields in a row in the file. ");
                        return false;
                    }
                    else
                    {
                        int lineNo = 1;
                        Manifests manifests = null;
                        ArrayList<ManifestSpecimens> mSpecimens =new ArrayList<ManifestSpecimens>();
                        while ((line = br.readLine()) != null){
                            if(line.length() > 0)
                            {
                                lineNo++;
                                if(lineNo == 2)
                                {
                                    manifests = createManifest(line,form,manifestFileName);
                                    if(manifests == null)
                                    {
                                        errorList.add(form.getMessage());
                                        form.setMessage(null);
                                    }
                                }
                                ManifestSpecimens mSpecimen = createManifestSpecimen(line,form,lineNo);
                                if(mSpecimen == null)
                                {
                                    errorList.add(form.getMessage());
                                    form.setMessage(null);
                                }
                                else
                                    mSpecimens.add(mSpecimen);
                            }
                        }
                        if(!errorList.isEmpty())
                        {
                            for (String error : errorList)
                                errors.reject(null,error);
                            return false;
                        }
                        Manifests result =SpecimentrackingManager.getInstance().insertManifest(getContainer(), getUser(),manifests);
                        ArrayList resultList = new ArrayList<String>();

                        if(result == null)
                        {
                            errors.reject(null,"The manifest you requested already exists in the system.");
                            return false;
                        }
                        else
                        {
                            for(ManifestSpecimens specimen : mSpecimens)
                            {
                                ManifestSpecimens resultSpecimen = SpecimentrackingManager.getInstance().insertManifestSpecimen(getContainer(),getUser(),specimen);
                                if (resultSpecimen == null)
                                {
                                    resultList.add(specimen.getSpecimenId());
                                }
                            }
                        }
                        String shipId = manifests.getShipId();
                        urlTest = new ActionURL(SpecimentrackingModule.NAME,"specimenTracking.view",getContainer());
                        urlTest.addParameter("shipId",shipId);
                        if(resultList.size() > 0)
                        {
                            StringBuilder message = new StringBuilder();
                            message.append("Manifest was uploaded. The following duplicate specimens were not uploaded: ");
                            for(int i =0;i<resultList.size();i++)
                            {
                                message.append(resultList.get(i)).append(" ");
                            }
                            urlTest.addParameter("message",message.toString());
                        }
                    }
                }
                else
                {
                    errors.reject(null,STR_FILE_DOES_NOT_EXIST_MSG);
                    return false;
                }
            }
            catch(PatternSyntaxException e)
            {
                e.printStackTrace();
            }
            catch(FileNotFoundException e)
            {
                errors.reject(null,STR_FILE_DOES_NOT_EXIST_MSG);
                return false;
            }
            catch(NullPointerException e)
            {
                e.printStackTrace();
            }
            return true;
        }

        public void validateCommand(ManifestFileForm form,  Errors errors)
        {
            form.validate(errors);
        }

        public ActionURL getSuccessURL(ManifestFileForm form)
        {
            return urlTest;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Upload File", specimentrackingURL("upload"));
            return root;
        }
    }

    @RequiresPermission(UpdatePermission.class)
    public class SpecimenTrackingAction extends FormViewAction<SpecimenForm>
    {
        ActionURL urlTest = null;
        public ModelAndView getView(SpecimenForm form, boolean reshow, BindException errors) throws Exception
        {
            form.setContainer(getContainer());
            if(form.getSpecimen() == null || form.getSpecimen().size() == 0)
            {
                ManifestSpecimens[] mSpecimens = SpecimentrackingManager.getInstance().getManifestSpecimens(getContainer(),form.getShipId());
                ArrayList<ManifestSpecimens> specimen = new ArrayList<ManifestSpecimens>();
                for(ManifestSpecimens specimens : mSpecimens)
                {
                    specimen.add(specimens);
                }
                form.setSpecimen(specimen);
            }
            JspView v = new JspView<>("/org/scharp/atlas/specimentracking/view/Specimens.jsp", form, errors);
            return v;
        }

        public boolean handlePost(SpecimenForm form, BindException errorMessages) throws Exception
        {
            form.setContainer(getContainer());
            String actionType = getRequest().getParameter("action_type");
            String [] specimenChecked = form.getSpecimenCheck();
            String specimenCode = form.getSpecimenCode();
            ArrayList<String> specimenId = new ArrayList<String>();
            ManifestSpecimens [] mSpecimens =  SpecimentrackingManager.getInstance().getManifestSpecimens(getContainer(),form.getShipId());
            ArrayList<ManifestSpecimens> manifestSpecimens = new ArrayList<ManifestSpecimens>();

            for(ManifestSpecimens mspecimen : mSpecimens)
            {
                manifestSpecimens.add(mspecimen);
                specimenId.add(mspecimen.getSpecimenId());
            }
            if(specimenCode!= null)
            {
                if(specimenCode.startsWith("FSQ"))
                {
                    specimenCode = specimenCode.substring(3);
                }
                if(SpecimentrackingManager.specimenExists(specimenCode,getContainer()))
                {
                    if(!(specimenId.contains(specimenCode)))
                        form.setMessage("The Specimen you are trying enter "+specimenCode+" already exists in the system.\n"+
                                "This specimen may be in this shipment but already Reconciled OR it may be in the other shipment.\n");
                }
                if(!(SpecimentrackingManager.specimenExists(specimenCode,getContainer())))
                {
                    ManifestSpecimens newSpecimen = new ManifestSpecimens();
                    newSpecimen.setSpecimenId(specimenCode);
                    newSpecimen.setOnManifest(false);
                    manifestSpecimens.add(newSpecimen);
                }
            }
            if(specimenChecked != null)
            {
                for(String specimenCheck : specimenChecked)
                {
                    if(!(specimenId.contains(specimenCheck)))
                    {
                        ManifestSpecimens newSpecimen = new ManifestSpecimens();
                        newSpecimen.setSpecimenId(specimenCheck);
                        newSpecimen.setOnManifest(false);
                        manifestSpecimens.add(newSpecimen);
                        if("Reconcile".equalsIgnoreCase(actionType))
                        {
                            newSpecimen.setShipId(form.getShipId());
                            newSpecimen.setReConciled(true);
                            SpecimentrackingManager.getInstance().insertManifestSpecimen(getContainer(),getUser(),newSpecimen);
                        }
                    }
                    else
                    {
                        if("Reconcile".equalsIgnoreCase(actionType))
                        {
                            ManifestSpecimens updateSpecimen = SpecimentrackingManager.getInstance().getManifestSpecimen(getContainer(),form.getShipId(),specimenCheck);
                            updateSpecimen.setReConciled(true);
                            SpecimentrackingManager.getInstance().updateManifestSpecimen(getContainer(),getUser(),updateSpecimen);
                        }
                    }
                }
            }
            for(ManifestSpecimens specimen : manifestSpecimens)
            {
                if(specimenCode == null && specimenChecked != null)
                {
                    for(String specimenCheck : specimenChecked)
                    {
                        if(specimenCheck.equals(specimen.getSpecimenId()))
                            specimen.setReConciled(true);
                    }
                }
                else
                {


                    if(specimenChecked == null && specimenCode != null)
                    {
                        if(specimenCode.equals(specimen.getSpecimenId()))
                        {
                            specimen.setReConciled(true);
                            break;
                        }


                    }
                    if(specimenChecked != null)
                    {

                        if(specimenCode.equals(specimen.getSpecimenId()))
                            specimen.setReConciled(true);
                        for(String specimenCheck :specimenChecked)
                        {
                            if(specimenCheck.equals(specimen.getSpecimenId()))
                                specimen.setReConciled(true);
                        }
                    }
                }
            }
            form.setSpecimen(manifestSpecimens);
            if("Reconcilliation Complete".equalsIgnoreCase(actionType))
            {
                urlTest = new ActionURL(ConfirmSpecimensAction.class, getContainer());
                urlTest.addParameter("shipId", form.getShipId());
                getRequest().getSession().setAttribute("CONFIRM_SPECIMENS",manifestSpecimens);
            }
            else if("Reconcile".equalsIgnoreCase(actionType))
                urlTest = new ActionURL(BeginAction.class,getContainer());
            else
                return false;
            return true;
        }

        public void validateCommand(SpecimenForm form,  Errors errors)
        {
            form.validate(errors);
        }

        public ActionURL getSuccessURL(SpecimenForm form)
        {
            return urlTest;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Specimen Tracking", specimentrackingURL("specimenTracking"));
            return root;
        }
    }

    @RequiresPermission(UpdatePermission.class)
    public class ConfirmSpecimensAction extends SimpleViewAction<SpecimenForm>
    {
        public ModelAndView getView(SpecimenForm form, BindException errors) throws Exception
        {
            ArrayList<ManifestSpecimens> manifestSpecimens = (ArrayList<ManifestSpecimens>) getRequest().getSession().getAttribute("CONFIRM_SPECIMENS");
            if(manifestSpecimens != null && manifestSpecimens.size() >0)
            {
            form.setSpecimen(manifestSpecimens);
            getRequest().getSession().removeAttribute("CONFIRM_SPECIMENS");
            }
            JspView v = new JspView<SpecimenForm>("/org/scharp/atlas/specimentracking/view/confirmSpecimens.jsp",form,errors);
            v.setTitle("Shipment Evaluation form for Manifest Id : "+form.getShipId());
            return  v;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Confirm Specimens", specimentrackingURL("confirmSpecimens"));
            return root;
        }
    }

    @RequiresPermission(ReadPermission.class)
    public class SpecimenExportAction extends ExportAction<SpecimenForm>
    {
        public void export(SpecimenForm form, HttpServletResponse response, BindException errors) throws Exception
        {
            try
            {
                RenderContext ctx = new RenderContext(getViewContext());
                SimpleFilter containerFilter = new SimpleFilter(FieldKey.fromParts("Container"), getContainer().getId());
                containerFilter.addCondition(FieldKey.fromParts("ShipId"), form.getShipId());
                ctx.setBaseFilter(containerFilter);
                try (ExcelWriter ew = new ExcelWriter(getGridRegion().getResults(ctx), getGridRegion().getDisplayColumns()))
                {
                    ew.setAutoSize(true);
                    ew.setSheetName("SpecimensIn" + form.getShipId());
                    ew.setFooter("SpecimensIn" + form.getShipId());
                    ew.write(getResponse());
                }
            }
            catch (SQLException e)
            {
                _log.error("export: " + e);
            }
        }
    }

    @RequiresPermission(ReadPermission.class)
    public class DetailsAction extends SimpleViewAction<SpecimenForm>
    {
        public ModelAndView getView(SpecimenForm form, BindException errors) throws Exception
        {
            DataRegion rgn =  getGridRegion();
            rgn.setShowBorders(true);
            ButtonBar gridButtonBar = new ButtonBar();
            ActionURL gridURL = new ActionURL(SpecimentrackingModule.NAME, "begin.view", getContainer());
            ActionButton grid = new ActionButton(gridURL, "Back");
            gridButtonBar.add(grid);
            ActionURL exportUrl = new ActionURL(SpecimenExportAction.class,getContainer());
            exportUrl.addParameter("shipId",form.getShipId());
            ActionButton export = new ActionButton(exportUrl, "Export to Excel");
            export.setActionType(ActionButton.Action.LINK);
            gridButtonBar.add(export);
            rgn.setButtonBar(gridButtonBar);
            GridView gridView = new GridView(rgn,(BindException)null);
            gridView.setFilter(new SimpleFilter(FieldKey.fromParts("ShipId"), form.getShipId()));
            gridView.setSort(new Sort("RowId"));
            gridView.setTitle("The details of the Manifest Id : "+form.getShipId());
            JspView newView = new JspView<ViewContext>("/org/scharp/atlas/specimentracking/view/detailsNotes.jsp");
            VBox vBox = new VBox();
            vBox.addView(gridView);
            vBox.addView(newView);
            return vBox;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("Details", specimentrackingURL("details"));
            return root;
        }
    }

    private Manifests createManifest(String line,ManifestFileForm form,String manifestFileName) throws Exception
    {
        if(!isValidLine(line))
        {
            form.setMessage(STR_MANIFEST_WRONG_FORMAT_MSG+ " "+
                    "The fields length should be minimum 11 and maximum 19.");
            return null;
        }
        String [] fields = line.split(",",19);
        if(fields[0] == null || fields[0].length() == 0)
        {
            form.setMessage(STR_MANIFEST_WRONG_FORMAT_MSG + " " + "Ship ID is required.");
            return null;
        }
        Date receivedDate = SpecimentrackingManager.getInstance().isValidDate(form.getDateReceived());
        java.util.Date shipDate = SpecimentrackingManager.getInstance().isValidDate(fields[1]);
        if(shipDate == null)
        {
            form.setMessage(STR_MANIFEST_WRONG_FORMAT_MSG + " " + "Ship Date must be a valid date.");
            return null;
        }
        if(shipDate.after(receivedDate))
        {
            form.setMessage("The Ship Date has to be before the received date. Check the Received date and enter again.");
            return null;
        }
        if(fields[2] == null || "000".equals(fields[2])|| "0".equals(fields[2]) || fields[2].length() == 0 || validateInteger(fields[2]) == null)
        {
            if(form.getRecipientLab() == 0)
            {
                form.setMessage("End User lab is required because there is no Recipient lab in the manifest or the Recipient lab may be in the text form.\nPlease select end user lab from the drop down list.");
                return null;
            }
            else
            {
                fields[2] = String.valueOf(form.getRecipientLab());
            }
        }
        Manifests manifests =  new Manifests();
        manifests.setDateReceived(new java.sql.Date(receivedDate.getTime()));
        manifests.setShipId(fields[0]);
        manifests.setShipDate(new java.sql.Date(shipDate.getTime()));
        manifests.setRecipientLab(fields[2]);
        manifests.setShippingLab(fields[3]);
        manifests.setShippingMethod(fields[4]);
        manifests.setManifestFilename(manifestFileName);
        return manifests;

    }

    private ManifestSpecimens createManifestSpecimen(String line,ManifestFileForm form,int lineNo) throws Exception
    {
        String [] fields = line.split(",",19);
        if(!isValidLine(fields)) {
            form.setMessage(STR_MANIFEST_WRONG_FORMAT_MSG+ " Line Number " +lineNo+" : "+
                    "The fields length should be minimum 11 and maximum 20 OR There is extraneous data at the end of one or more records.");
            return null;
        }
        ManifestSpecimens mSpecimen = new ManifestSpecimens();
        if(StringUtils.trimToNull(fields[5]) == null || fields[5].length() == 0 ||StringUtils.trim(fields[5]).length() != 11)
        {
            form.setMessage(STR_MANIFEST_WRONG_FORMAT_MSG+ " Line Number " +lineNo+" : "+ "Specimen Id(LIMS ID) is required and the length must be 11.");
            return null;
        }
        else
        {
            String specimenId = fields[5];
            if(!(specimenId.toUpperCase().matches("[A-Z]\\w+\\-\\d+")))
            {
                form.setMessage( "There is something wrong in specimenId(LIMS ID) in Line " +lineNo+" : "+".\n"+
                        "The specimen Id should start with a letter and end with two digits preceded by '-'.");
                return null;
            }
        }
        java.util.Date collectionDate = SpecimentrackingManager.getInstance().isValidDate(fields[10]);
        if(collectionDate == null)
        {
            form.setMessage(STR_MANIFEST_WRONG_FORMAT_MSG + " Line Number " +lineNo+" : "+
                    "Collection Date (11th field) must be a valid date.");
            return null;
        }

        else
        {
            mSpecimen.setShipId(fields[0]);
            mSpecimen.setSpecimenId(fields[5]);
            mSpecimen.setGroupName(fields[6]);
            mSpecimen.setPtid(fields[7]);
            mSpecimen.setProtocol(fields[8]);
            mSpecimen.setVisit(fields[9]);
            mSpecimen.setCollectionDate(new java.sql.Date(collectionDate.getTime()));
            mSpecimen.setSampleType(fields[11]);
            mSpecimen.setAdditive(fields[12]);
            mSpecimen.setCellsperVial(fields[13]);
            mSpecimen.setVolperVial(fields[14]);
            mSpecimen.setBoxNumber(fields[15]);
            mSpecimen.setRowNumber(fields[16]);
            mSpecimen.setColumnNumber(fields[17]);
            mSpecimen.setVisitType(fields[18]);
            mSpecimen.setOnManifest(true);
            mSpecimen.setReConciled(false);
            return mSpecimen;
        }
    }
    public boolean isValidLine(String [] fields)
    {
        if(fields.length < 11 || fields.length>20)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public boolean isValidLine(String line)
    {
        String [] fields = line.split(",");
        return isValidLine(fields);
    }

    public static Integer validateInteger(String value)
    {
        try
        {
            Integer intValue = Integer.valueOf(value);
            return intValue;
        }
        catch(NumberFormatException e){return null;}
    }

    private DataRegion getDataRegion(Container container)
    {
        DataRegion rgn = new DataRegion();
        TableInfo tableInfo = SpecimentrackingSchema.getInstance().getTableInfoManifests();
        rgn.setColumns(tableInfo.getColumns("RowId,ShipId, Created,DateReceived,ShippingLab,RecipientLab"));
        rgn.setShowBorders(true);
        rgn.setShadeAlternatingRows(true);
        SimpleDisplayColumn details = new UrlColumn(specimentrackingURL("details")+ "shipId=${ShipId}", "Details");
//        details.setDisplayModes(DataRegion.MODE_GRID);
        rgn.addDisplayColumn(details);
        ButtonBar gridButtonBar = new ButtonBar();
        if(getContainer().hasPermission(getUser(), AdminPermission.class))
        {
            rgn.addColumn(tableInfo.getColumn("CreatedBy"));
        }
        else
        {
            DisplayColumn col = rgn.getDisplayColumn("ShipId");
            ActionURL trackAction = new ActionURL(SpecimenTrackingAction.class, container);
            col.setURL(trackAction.toString()+"?shipId=${ShipId}");
            col.setDisplayPermission(UpdatePermission.class);
            ActionURL uploadURL = new ActionURL(UploadAction.class, container);
            ActionButton upload = new ActionButton(uploadURL, "Upload New Manifest");
            upload.setActionType(ActionButton.Action.LINK);
            gridButtonBar.add(upload);
        }
        rgn.setButtonBar(gridButtonBar, DataRegion.MODE_GRID);
        return rgn;
    }

    private DataRegion getGridRegion() throws ServletException
    {
        DataRegion rgn = new DataRegion();
        TableInfo tableInfo = SpecimentrackingSchema.getInstance().getTableInfoManifestSpecimens();
        rgn.setColumns(tableInfo.getColumns("RowId,SpecimenId,Created,Modified,Reconciled,OnManifest"));
        return rgn;
    }

    public static class SpecimenForm
    {
        private Container container;
        private ArrayList<ManifestSpecimens> specimen;
        private String shipId;
        public String specimenCode;
        private String[] specimenId;
        private boolean reConciled;
        private boolean onManifest;
        private String [] specimenCheck;
        private  String message;

        public ArrayList<ManifestSpecimens> getSpecimen() {
            return specimen;
        }

        public void setSpecimen(ArrayList<ManifestSpecimens> specimen) {
            this.specimen = specimen;
        }


        public Container getContainer() {
            return container;
        }

        public void setContainer(Container container) {
            this.container = container;
        }


        public String getShipId() {
            return shipId;
        }

        public void setShipId(String shipId) {
            this.shipId = shipId;
        }

        public String[] getSpecimenCheck() {
            return specimenCheck;
        }

        public void setSpecimenCheck(String[] specimenCheck) {
            this.specimenCheck = specimenCheck;
        }

        public String[] getSpecimenId() {
            return specimenId;
        }

        public void setSpecimenId(String[] specimenId) {
            this.specimenId = specimenId;
        }
        public boolean getReConciled() {
            return reConciled;
        }

        public void setReConciled(boolean reConciled) {
            this.reConciled = reConciled;
        }

        public boolean getOnManifest() {
            return onManifest;
        }

        public void setOnManifest(boolean onManifest) {
            this.onManifest = onManifest;
        }

        public String getSpecimenCode() {
            return specimenCode;
        }

        public void setSpecimenCode(String specimenCode) {
            this.specimenCode = specimenCode;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void validate(Errors errors)
        {
            if(getSpecimenCode() == null && getSpecimenCheck()==null)
                errors.reject(null,"To reconcile specimen You have to enter the specimen Id or check atleast one of the specimen Id's.");
        }
    }

    public static class ManifestFileForm
    {
        private String dateReceived;
        private int recipientLab;
        private String message;
        private Container container;

        public Container getContainer() {
            return container;
        }

        public void setContainer(Container container) {
            this.container = container;
        }

        public String getDateReceived() {
            return dateReceived;
        }
        public void setDateReceived(String dateReceived) {
            this.dateReceived = dateReceived;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }


        public int getRecipientLab() {
            return recipientLab;
        }

        public void setRecipientLab(int recipientLab) {
            this.recipientLab = recipientLab;
        }

        public void validate(Errors errors)
        {
            if ((getDateReceived()) == null)
                errors.reject(null, "Date Received is required.");
            else
            {
                String dateReceived = getDateReceived();
                Date receivedDate = SpecimentrackingManager.getInstance().isValidDate(dateReceived);
                if(receivedDate == null)
                    errors.reject(null,"Date Recieved is not in the right format or is not a valid date.");
                else
                {
                    if(receivedDate.after(new Date()))
                        errors.reject(null,"Date Recieved must be on or before the current date.");
                }
            }
        }
    }


}
