package org.scharp.atlas.elispot;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.labkey.api.action.SpringActionController;
import org.labkey.api.attachments.AttachmentFile;
import org.labkey.api.data.BeanViewForm;
import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.DataColumn;
import org.labkey.api.data.RenderContext;
import org.scharp.atlas.elispot.model.Group;
import org.scharp.atlas.elispot.model.Lab;
import org.scharp.atlas.elispot.model.Plate;
import org.scharp.atlas.elispot.model.PlateTemplate;
import org.scharp.atlas.elispot.model.Reader;
import org.scharp.atlas.elispot.model.Specimen;
import org.scharp.atlas.elispot.model.Study;
import org.scharp.atlas.elispot.model.StudyLab;
import org.springframework.validation.Errors;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Jun 22, 2009
 * Time: 8:06:46 AM
 * To change this template use File | Settings | File Templates.
 * Base Controller class which has all the forms and some methods used by actions
 */
public class ElispotBaseController extends SpringActionController
{
    private final static Logger _log = Logger.getLogger(ElispotBaseController.class);
    protected static final String VIEW_BEGIN = "begin.view";

    /*
     * This form is used by BatchMenuAction, BatchSelectAction, GetPlateInformationAction
     * ApprovePlateInformationAction, DisplayBatchSummaryAction, RemovePlateInformationAction
     * and SaveCommentsAction to pass the lab study information and etc.
    */
    public static class StudyLabBatchForm
    {
        private Integer studyId;
        private Integer labId;
        private String networkId;
        private Integer batchId;
        private Integer plateId;
        private Integer labstudyseqId;
        private String message;
        private String comments;
        private Integer reader_id;
        private AttachmentFile batchFile;

        public String getComments()
        {
            return comments;
        }

        public void setComments(String comments)
        {
            this.comments = comments;

        }

        public String getMessage()
        {
            return message;
        }

        public void setMessage(String message)
        {
            this.message = message;
        }

        public Integer getPlateId()
        {
            return plateId;
        }

        public void setPlateId(Integer plateId)
        {
            this.plateId = plateId;
        }

        public Integer getLabstudyseqId()
        {
            return labstudyseqId;
        }

        public void setLabstudyseqId(Integer labstudyseqId)
        {
            this.labstudyseqId = labstudyseqId;
        }

        public Integer getBatchId()
        {
            return batchId;
        }

        public void setBatchId(Integer batchId)
        {
            this.batchId = batchId;
        }

        public Integer getStudyId()
        {
            return studyId;
        }

        public void setStudyId(Integer studyId)
        {
            this.studyId = studyId;
        }

        public Integer getLabId()
        {
            return labId;
        }

        public void setLabId(Integer labId)
        {
            this.labId = labId;
        }

        public String getNetworkId()
        {
            return networkId;
        }

        public void setNetworkId(String networkId)
        {
            this.networkId = networkId;
        }

        public Integer getReader_id()
        {
            return reader_id;
        }

        public void setReader_id(Integer reader_id)
        {
            this.reader_id = reader_id;
        }

        public AttachmentFile getBatchFile()
        {
            return batchFile;
        }

        public void setBatchFile(AttachmentFile batchFile)
        {
            this.batchFile = batchFile;
        }

        /* (non-Javadoc)
        * @see java.lang.Object#toString()
        */
        public String toString()
        {
            return "StudyLabBatchForm[studIdy:" + studyId + ", labId:" + labId + ", networkId:" + networkId + "]";
        }

        /*
        This is used in BatchSelect Action validateForm method.
        */
        public void validate(Errors errors)
        {
            if (getStudyId() == null || StringUtils.trimToNull(getStudyId().toString()) == null)
                errors.reject(null, "Study is required.");
            if (getLabId() == null || StringUtils.trimToNull(getLabId().toString()) == null)
                errors.reject(null, "Lab is required.");
        }
    }

    /*
    * This validation method is used in BatchMenuAction handlePost method
    * to validate File Name and to make sure the file extensions match the reader file extensions
    */
    protected void validateFileForm(StudyLabBatchForm form, String sel_file, Errors errors)
    {
        if (!(sel_file.toUpperCase().endsWith(".TXT") || sel_file.toUpperCase().endsWith(".XLS")))
            errors.reject(null, "The file trying to import must end with either '.txt' or '.xls'.");
        else
        {
            ArrayList<String> uploadedFileList = new ArrayList<String>();
            String sel_file_ext = sel_file.substring(sel_file.indexOf(".") + 1, sel_file.length());
            if (sel_file_ext == null || sel_file_ext.length() == 0)
            {
                errors.reject(null, "Filename is not valid.It should contain a '.' with some extension.");
            }
            else
            {

                /*if (sel_file_ext.equalsIgnoreCase("ZIP"))
               {
                   uploadedFileList = getunZipFiles(getBatchFile());
                }*/
                //else
                uploadedFileList.add(sel_file);
            }

            String sel_reader_ext = getReaderObjfromid(form.getReader_id()).getFile_ext();
            if (sel_reader_ext == null || sel_reader_ext.length() == 0) //file ext from selected reader_type
            {
                errors.reject(null, "File extension required for the selected reader type in the database.");
            }
            else
            {
                if (uploadedFileList.size() > 0)
                {
                    boolean ext_match = false;
                    ArrayList<String> wrongextFiles = new ArrayList<String>();

                    for (String filename : uploadedFileList)
                    {
                        sel_file_ext = filename.substring(filename.indexOf(".") + 1, filename.length());
                        if (sel_file_ext.trim().equalsIgnoreCase(sel_reader_ext.trim()))
                            ext_match = true;
                        else
                            wrongextFiles.add(filename);

                    }
                    if (!ext_match)
                    {
                        if (wrongextFiles.size() > 0)
                        {
                            for (String extmismatchFile : wrongextFiles)
                                errors.reject(null, "The selected file extension does not match the selected reader: " + extmismatchFile);
                        }
                    }
                }
            }
        }
        //if(errors.getErrorCount() >0)
        // return false;
        // else return true;
    }

    protected boolean validateZipFile(StudyLabBatchForm form, HashMap<String, InputStream> uploadedFileMap, Errors errors)
    {
        for (String name : uploadedFileMap.keySet())
            validateFileForm(form, name, errors);
        if (errors.getErrorCount() > 0)
            return false;
        return true;
    }

    /*
     * This validation method is used by BatchSelectAction to validate
     * the study lab combination and to make sure the user is from the
     * selected Lab's  permission group.
     */
    protected boolean validateForm(StudyLabBatchForm form, Errors errors)
    {
        List<String> errorList = new LinkedList<String>();
        form.validate(errors);
        try
        {
            if (!validateStudyLab(form, errorList))
            {
                for (String e : errorList)
                    errors.reject(null, e);
            }
            else
            {
                if (!validateUser(form.getLabstudyseqId(), errorList))
                {
                    for (String e : errorList)
                        errors.reject(null, e);
                }
            }
        }
        catch (Exception e)
        {
            _log.error(e);
        }
        if (errors.getErrorCount() > 0)
            return false;
        else return true;
    }

    /*
     * This validation method is used by validateForm method which validates BatchSelectAction
     * values to make sure the study lab combination exists in the database.
     */
    private boolean validateStudyLab(StudyLabBatchForm form, List<String> errors) throws Exception
    {

        StudyLab sLab = new StudyLab();
        sLab.setLab_seq_id(form.getLabId());
        sLab.setStudy_seq_id(form.getStudyId());
        StudyLab studyLab = EliSpotManager.getStudyLab(getContainer(), sLab);
        if (studyLab == null)
        {
            errors.add("The selected Lab and the study are not associated.Please checkl with the administrator.");
            return false;
        }
        else
        {
            form.setLabstudyseqId(studyLab.getLab_study_seq_id());
            return true;
        }
    }

    /*
     *
     */
    protected boolean validateUser(Integer lab_study_seq_id, List<String> errors) throws Exception
    {
        StudyLab sLab = EliSpotManager.getStudyLab(getContainer(), lab_study_seq_id);
        Lab lab = EliSpotManager.getLab(getContainer(), sLab.getLab_seq_id());
        if (lab != null)
        {
            Group group = EliSpotManager.getGroupId(getContainer(), lab.getPermgroupname());
            if (group == null)
            {
                errors.add("The permission group " + lab.getPermgroupname() + " doesn't exists.Please check the permission group information for the selected lab " + lab.getLab_desc() + " and try again.");
                return false;
            }
            if (!(getUser().isInGroup(group.getUserid())))
            {
                errors.add("The user " + getUser().getEmail() + " is not in the ATLAS Permission group " + group.getName() + " .");
                return false;
            }
        }
        return true;
    }

    public static class UpdatePSpecimensForm
    {
        private Integer batch_seq_id;
        private Integer lab_study_seq_id;
        private Integer plate_seq_id;
        private String[] specwellgroups;
        private String[] specimenseqIds;
        private String[] counterseqIds;
        private String[] d1_cellcounts;
        private String[] d2_cellcounts;
        private String[] d1_viabilities;
        private String[] d2_viabilities;
        private String actionName;
        private String test_date;


        public String getTest_date()
        {
            return test_date;
        }

        public void setTest_date(String test_date)
        {
            this.test_date = test_date;
        }

        public String getActionName()
        {
            return actionName;
        }

        public void setActionName(String actionName)
        {
            this.actionName = actionName;
        }

        public Integer getLab_study_seq_id()
        {
            return lab_study_seq_id;
        }

        public void setLab_study_seq_id(Integer lab_study_seq_id)
        {
            this.lab_study_seq_id = lab_study_seq_id;
        }

        public Integer getBatch_seq_id()
        {
            return batch_seq_id;
        }

        public void setBatch_seq_id(Integer batch_seq_id)
        {
            this.batch_seq_id = batch_seq_id;
        }

        public Integer getPlate_seq_id()
        {
            return plate_seq_id;
        }

        public void setPlate_seq_id(Integer plate_seq_id)
        {
            this.plate_seq_id = plate_seq_id;
        }

        public String[] getSpecimenseqIds()
        {
            return specimenseqIds;
        }

        public void setSpecimenseqIds(String[] specimenseqIds)
        {
            this.specimenseqIds = specimenseqIds;
        }

        public String[] getCounterseqIds()
        {
            return counterseqIds;
        }

        public void setCounterseqIds(String[] counterseqIds)
        {
            this.counterseqIds = counterseqIds;
        }

        public String[] getD1_cellcounts()
        {
            return d1_cellcounts;
        }

        public void setD1_cellcounts(String[] d1_cellcounts)
        {
            this.d1_cellcounts = d1_cellcounts;
        }

        public String[] getD2_cellcounts()
        {
            return d2_cellcounts;
        }

        public void setD2_cellcounts(String[] d2_cellcounts)
        {
            this.d2_cellcounts = d2_cellcounts;
        }

        public String[] getD1_viabilities()
        {
            return d1_viabilities;
        }

        public void setD1_viabilities(String[] d1_viabilities)
        {
            this.d1_viabilities = d1_viabilities;
        }

        public String[] getD2_viabilities()
        {
            return d2_viabilities;
        }

        public void setD2_viabilities(String[] d2_viabilities)
        {
            this.d2_viabilities = d2_viabilities;
        }

        public String[] getSpecwellgroups()
        {
            return specwellgroups;
        }

        public void setSpecwellgroups(String[] specwellgroups)
        {
            this.specwellgroups = specwellgroups;
        }

        public void validate(Errors errors)
        {
            if (getTest_date() != null)
            {
                String testDate = getTest_date();
                if (!(testDate.toUpperCase().matches("\\d{1,2}\\-[A-Z]{3}\\-\\d{4}")))
                {
                    errors.reject(null, "Date Plated " + testDate + " is not in the right format.It should be in 'dd-MMM-yyyy' format");
                }
                else
                {
                    Date validDate = EliSpotManager.isValidDate(testDate);
                    if (validDate == null)
                        errors.reject(null, "Date Plated " + testDate + " is not a valid date.");
                    else
                    {
                        if (validDate.after(new Date()))
                            errors.reject(null, "Date Plated must be on or before the current date.");
                    }
                }
            }

            if (getCounterseqIds() != null & getCounterseqIds().length > 0)
            {
                for (int i = 0; i < getCounterseqIds().length; i++)
                {
                    String counterseqId = getCounterseqIds()[i];
                    if (counterseqId != null && counterseqId.length() != 0)
                    {
                        Integer validInteger = validateInteger(counterseqId);
                        if (validInteger == null)
                            errors.reject(null, "The Cell counter in the row " + (i + 1) + " is not a valid Integer.");
                    }

                }
            }
            if (getD1_cellcounts() != null && getD1_cellcounts().length > 0)
            {
                int count = 0;
                for (String d1Cellcount : getD1_cellcounts())
                {
                    count++;
                    if (d1Cellcount != null && d1Cellcount.length() > 0)
                    {
                        Float floatValue = validateFloat(d1Cellcount);
                        if (floatValue == null)
                            errors.reject(null, "The d1Cellcount(Day 1 Cell Count) in the row " + count + " is not a valid Float.");
                    }
                }
            }
            if (getD1_viabilities() != null && getD1_viabilities().length > 0)
            {
                int count = 0;
                for (String d1Viability : getD1_viabilities())
                {
                    count++;
                    if (d1Viability != null && d1Viability.length() > 0)
                    {
                        Float floatValue = validateFloat(d1Viability);
                        if (floatValue == null)
                            errors.reject(null, "The d1Viability(Day 1 Viability) in the row " + count + " is not a valid Float.");
                    }
                }
            }
            if (getD2_cellcounts() != null && getD2_cellcounts().length > 0)
            {
                int count = 0;
                for (String d2Cellcount : getD2_cellcounts())
                {
                    count++;
                    if (d2Cellcount != null && d2Cellcount.length() > 0)
                    {
                        Float floatValue = validateFloat(d2Cellcount);
                        if (floatValue == null)
                            errors.reject(null, "The d2Cellcount(Day 2 Viability) in the row " + count + " is not a valid Float.");
                    }
                }
            }
            if (getD2_viabilities() != null && getD2_viabilities().length > 0)
            {
                int count = 0;
                for (String d2Viability : getD2_viabilities())
                {
                    count++;
                    if (d2Viability != null && d2Viability.length() > 0)
                    {
                        Float floatValue = validateFloat(d2Viability);
                        if (floatValue == null)
                            errors.reject(null, "The d2Viability(Day 2 Viability) in the row " + count + " is not a valid Float.");
                    }
                }
            }
        }
    }

    public static Integer validateInteger(String value)
    {
        try
        {
            Integer intValue = Integer.valueOf(value);
            return intValue;
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    public static Float validateFloat(String value)
    {
        try
        {
            Float floatValue = Float.valueOf(value);
            return floatValue;
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    public static class UpdatePInfoForm
    {
        private Integer batch_seq_id;
        private Integer lab_study_seq_id;
        private String[] plateIds;
        private String[] preCoated;
        private String[] plateTypes;
        private String[] substrates;

        public Integer getBatch_seq_id()
        {
            return batch_seq_id;
        }

        public void setBatch_seq_id(Integer batch__seq_id)
        {
            this.batch_seq_id = batch__seq_id;
        }

        public Integer getLab_study_seq_id()
        {
            return lab_study_seq_id;
        }

        public void setLab_study_seq_id(Integer lab_study_seq_id)
        {
            this.lab_study_seq_id = lab_study_seq_id;
        }

        public String[] getPlateIds()
        {
            return plateIds;
        }

        public void setPlateIds(String[] plateIds)
        {
            this.plateIds = plateIds;
        }

        public String[] getPreCoated()
        {
            return preCoated;
        }

        public void setPreCoated(String[] preCoated)
        {
            this.preCoated = preCoated;
        }

        public String[] getPlateTypes()
        {
            return plateTypes;
        }

        public void setPlateTypes(String[] plateTypes)
        {
            this.plateTypes = plateTypes;
        }

        public String[] getSubstrates()
        {
            return substrates;
        }

        public void setSubstrates(String[] substrates)
        {
            this.substrates = substrates;
        }
    }

    public static class ShowDataForm
    {
        private String tableName;
        private String message;
        private String queryString;
        private String queryValue;

        public ShowDataForm()
        {
        }

        public String getMessage()
        {
            return message;
        }

        public void setMessage(String message)
        {
            this.message = message;
        }

        public String getTableName()
        {
            return tableName;
        }

        public void setTableName(String tableName)
        {
            this.tableName = tableName;
        }

        public String getQueryString()
        {
            return queryString;
        }

        public void setQueryString(String queryString)
        {
            this.queryString = queryString;
        }

        public String getQueryValue()
        {
            return queryValue;
        }

        public void setQueryValue(String queryValue)
        {
            this.queryValue = queryValue;
        }
    }

    public class DClabstudyseqId extends DataColumn
    {
        //private final Container _container;
        public DClabstudyseqId(ColumnInfo col)
        {
            super(col);
            //_container = c;
        }

        public void renderGridCellContents(RenderContext ctx, Writer out) throws IOException
        {
            ColumnInfo c = getColumnInfo();
            Map rowMap = ctx.getRow();
            if (!"lab_study_seq_id".equals(c.getName()))
            {
                super.renderGridCellContents(ctx, out);
            }
            else
            {
                Integer labstudyseqId = (Integer) rowMap.get(c.getName());
                try
                {
                    StudyLab studyLab = EliSpotManager.getStudyLab(getContainer(), labstudyseqId);
                    HashMap studyMap = EliSpotManager.getStudyDescs(getContainer());
                    HashMap labMap = EliSpotManager.getLabMap(getContainer());
                    out.write((String) studyMap.get(studyLab.getStudy_seq_id()));
                    out.write("-");
                    out.write((String) labMap.get(studyLab.getLab_seq_id()));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        }
    }

    public class DCtemplateseqId extends DataColumn
    {
        //private final Container _container;
        public DCtemplateseqId(ColumnInfo col)
        {
            super(col);
            //_container = c;
        }

        public void renderGridCellContents(RenderContext ctx, Writer out) throws IOException
        {
            ColumnInfo c = getColumnInfo();
            Map rowMap = ctx.getRow();
            if (!"template_seq_id".equals(c.getName()))
            {
                super.renderGridCellContents(ctx, out);
            }
            else
            {
                Integer templateseqId = (Integer) rowMap.get(c.getName());
                try
                {
                    PlateTemplate pt = EliSpotManager.getPlateTemplate(ctx.getContainer(), templateseqId);
                    Study s = EliSpotManager.getStudy(ctx.getContainer(), pt.getStudy_seq_id());
                    out.write(s.getStudy_description());
                    out.write("-");
                    out.write(pt.getTemplate_description());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        }
    }

    public static String toLZ(int i, int len)
    {
        // converts integer to left-zero padded string, len  chars long.
        String s = Integer.toString(i);
        if (s.length() > len) return s.substring(0, len);
        else if (s.length() < len) // pad on left with zeros
            return "000000000000000000000000000".substring(0, len - s.length()) + s;
        else return s;
    }

    public static Reader getReaderObjfromid(Integer reader_id)
    {
        Reader readerObj = null;
        try
        {
            readerObj = EliSpotManager.getReaderInformation(reader_id);
        }
        catch (Exception e)
        {
            System.err.println("readerInfo object null in ElispotController(getReaderObjfromid): " + e);
        }
        return readerObj;
    }

    public static class StudyForm extends BeanViewForm<Study>
    {
        public StudyForm()
        {
            super(Study.class, EliSpotSchema.getInstance().getTableInfoStudies());

        }

        public StudyForm(int studyseqid)
        {
            this();
            set("study_seq_id", String.valueOf(studyseqid));
        }

        public void validate(Errors errors)
        {
            Study bean = getBean();
            if (StringUtils.trimToNull(bean.getStudy_description()) == null)
                errors.reject(null, "Study Description is required.");
        }
    }

    public static class LabForm extends BeanViewForm<Lab>
    {
        public LabForm()
        {
            super(Lab.class, EliSpotSchema.getInstance().getTableInfoLabs());

        }

        public LabForm(int labseqid)
        {
            this();
            set("lab_seq_id", String.valueOf(labseqid));
        }

        public void validate(Errors errors)
        {
            Lab bean = getBean();
            if (StringUtils.trimToNull(bean.getLab_desc()) == null)
                errors.reject(null, "Lab Description is required.");
            if (StringUtils.trimToNull(bean.getPermgroupname()) == null)
                errors.reject(null, "Permission Group Name is Required");
        }
    }

    public static class StudyLabForm extends BeanViewForm<StudyLab>
    {
        public StudyLabForm()
        {
            super(StudyLab.class, EliSpotSchema.getInstance().getTableInfostudyLabs());

        }

        public StudyLabForm(int labstudyseqid)
        {
            this();
            set("lab_study_seq_id", String.valueOf(labstudyseqid));
        }

        public void validate(Errors errors)
        {
            StudyLab bean = getBean();
            if (StringUtils.trimToNull((String) get("lab_seq_id")) == null)
                errors.reject(null, "Lab Name is required.");
            if (StringUtils.trimToNull((String) get("study_seq_id")) == null)
                errors.reject(null, "Study Name is Required");
            if (StringUtils.trimToNull((String) get("lab_seq_id")) != null && StringUtils.trimToNull((String) get("study_seq_id")) != null)
            {
                if (EliSpotManager.getStudyLab(getContainer(), bean) != null)
                    errors.reject(null, "The Study and the Lab are already associated.");
            }
        }
    }

    public static class BatchForm
    {
        private Character batch_type;
        private String batch_description;
        private Integer lab_study_seq_id;

        public Character getBatch_type()
        {
            return batch_type;
        }

        public void setBatch_type(Character batch_type)
        {
            this.batch_type = batch_type;
        }

        public String getBatch_description()
        {
            return batch_description;
        }

        public void setBatch_description(String batch_description)
        {
            this.batch_description = batch_description;
        }

        public Integer getLab_study_seq_id()
        {
            return lab_study_seq_id;
        }

        public void setLab_study_seq_id(Integer lab_study_seq_id)
        {
            this.lab_study_seq_id = lab_study_seq_id;
        }

        public void validate(Errors errors)
        {
            if (StringUtils.trimToNull(getBatch_description()) == null || getBatch_type() == null)
                errors.reject(null, "The batch type and batch description are required.");
        }
    }

    public static class PlateForm extends BeanViewForm<Plate>
    {
        public PlateForm()
        {
            super(Plate.class, EliSpotSchema.getInstance().getTableInfoPlate());
        }

        public PlateForm(int plateseqid)
        {
            this();
            set("plate_seq_id", String.valueOf(plateseqid));
        }

        public void validate(Errors errors)
        {
            Plate bean = getBean();
            if (StringUtils.trimToNull(bean.getPlate_name()) == null)
                errors.reject(null, "Plate Name is required.");
            if (bean.getTemplate_seq_id() == null || StringUtils.trimToNull(bean.getTemplate_seq_id().toString()) == null)
                errors.reject(null, "Template Seq Id is Required.");
        }
    }

    public static class PlateSpecimensForm
    {
        private String[] specwellgroups;
        private String[] specimenIds;
        private boolean[] boolrepspecs;
        private String[] runnums;
        private String[] additives;
        private String[] cryostatus;
        private Integer plate_seq_id;
        private Integer template_seq_id;
        private Integer num_well_groups_per_plate;
        private String[] specimenseqIds;


        public Integer getNum_well_groups_per_plate()
        {
            return num_well_groups_per_plate;
        }

        public void setNum_well_groups_per_plate(Integer num_well_groups_per_plate)
        {
            this.num_well_groups_per_plate = num_well_groups_per_plate;
        }

        public Integer getTemplate_seq_id()
        {
            return template_seq_id;
        }

        public void setTemplate_seq_id(Integer template_seq_id)
        {
            this.template_seq_id = template_seq_id;
        }

        public String[] getSpecwellgroups()
        {
            return specwellgroups;
        }

        public void setSpecwellgroups(String[] specwellgroups)
        {
            this.specwellgroups = specwellgroups;
        }

        public String[] getSpecimenIds()
        {
            return specimenIds;
        }

        public void setSpecimenIds(String[] specimenIds)
        {
            this.specimenIds = specimenIds;
        }

        public boolean[] getBoolrepspecs()
        {
            return boolrepspecs;
        }

        public void setBoolrepspecs(boolean[] boolrepspecs)
        {
            this.boolrepspecs = boolrepspecs;
        }

        public String[] getRunnums()
        {
            return runnums;
        }

        public void setRunnums(String[] runnums)
        {
            this.runnums = runnums;
        }

        public String[] getAdditives()
        {
            return additives;
        }

        public void setAdditives(String[] additives)
        {
            this.additives = additives;
        }

        public String[] getCryostatus()
        {
            return cryostatus;
        }

        public void setCryostatus(String[] cryostatus)
        {
            this.cryostatus = cryostatus;
        }

        public Integer getPlate_seq_id()
        {
            return plate_seq_id;
        }

        public void setPlate_seq_id(Integer plate_seq_id)
        {
            this.plate_seq_id = plate_seq_id;
        }

        public String[] getSpecimenseqIds()
        {
            return specimenseqIds;
        }

        public void setSpecimenseqIds(String[] specimenseqIds)
        {
            this.specimenseqIds = specimenseqIds;
        }

        public void validate(Errors errors)
        {
            if (getSpecwellgroups().length != getNum_well_groups_per_plate())
                errors.reject(null, "The spec well group must be specified in all the rows.It must not be null.");
            for (String specPos : getSpecwellgroups())
            {
                if (StringUtils.trimToNull(specPos) == null)
                    errors.reject("main", "The Specimen Position caan not be null.");
            }
            if (getSpecimenseqIds() == null || getSpecimenseqIds().length == 0)
                errors.reject(null, "Atleast one of the specimens should be entered to create Plate Specimens.");
            else
            {
                StringBuilder specimenseqidsString = new StringBuilder();
                for (String s : getSpecimenseqIds())
                    if (s != null && s.trim().length() > 0)
                    {
                        specimenseqidsString.append(s);
                    }
                if (specimenseqidsString == null || specimenseqidsString.length() == 0)
                    errors.reject(null, "Atleast one of the specimens should be entered to create Plate Specimens.");
            }
        }
    }

    public static class PlateTemplateForm extends BeanViewForm<PlateTemplate>
    {
        public PlateTemplateForm()
        {
            super(PlateTemplate.class, EliSpotSchema.getInstance().getTableInfoPlateTemplate());
        }

        public PlateTemplateForm(int templateseqid)
        {
            this();
            set("template_seq_id", String.valueOf(templateseqid));
        }

        public void validate(Errors errors)
        {
            PlateTemplate bean = getBean();
            if (StringUtils.trimToNull(bean.getTemplate_description()) == null)
                errors.reject(null, "Template Description is Required.");
            if (bean.getNum_well_groups_per_plate() == null || StringUtils.trimToNull(bean.getNum_well_groups_per_plate().toString()) == null)
                errors.reject(null, "Number of well groups per plate is Required.");
            if (bean.getIncubate() == null || bean.getIncubate() == 0 || StringUtils.trimToNull(Float.toString(bean.getIncubate())) == null)
                errors.reject(null, "Incubate is Required.");
            if (StringUtils.trimToNull(bean.getReadout()) == null)
                errors.reject(null, "ReadOut is Required.");
            if (bean.getStudy_seq_id() == null || StringUtils.trimToNull(bean.getStudy_seq_id().toString()) == null)
                errors.reject(null, "Study Name is Required.");
        }
    }

    public static class SpecimenForm extends BeanViewForm<Specimen>
    {
        public SpecimenForm()
        {
            super(Specimen.class, EliSpotSchema.getInstance().getTableInfoSpecimen());
        }

        public SpecimenForm(int specimenseqid)
        {
            this();
            set("specimen_seq_id", String.valueOf(specimenseqid));
        }

        public void validate(Errors errors)
        {
            Specimen bean = getBean();
            if (StringUtils.trimToNull(bean.getPtid()) == null)
                errors.reject(null, "PTID is Required.");
            if (bean.getStudy_seq_id() == null || StringUtils.trimToNull(bean.getStudy_seq_id().toString()) == null)
                errors.reject(null, "Study Name is Required.");
        }
    }

    public static class PTDetailsForm
    {
        private Integer template_seq_id;
        private String message;

        public String getMessage()
        {
            return message;
        }

        public void setMessage(String message)
        {
            this.message = message;
        }

        public Integer getTemplate_seq_id()
        {
            return template_seq_id;
        }

        public void setTemplate_seq_id(Integer template_seq_id)
        {
            this.template_seq_id = template_seq_id;
        }

        public void validate(Errors errors)
        {
            if (getTemplate_seq_id() == null || StringUtils.trimToNull(getTemplate_seq_id().toString()) == null)
                errors.reject(null, "The Plate Template Name is Required.");
        }
    }

    //This method is not used as of  07/01/2009 -- Sravani Left this just incase if we decide to do in future
    public static HashMap<String, InputStream> getunZippedFiles(AttachmentFile batchFile, Errors errors)
    {
        HashMap<String, InputStream> unZipFiles = new HashMap<String, InputStream>();
        try
        {
            String prefix = batchFile.getFilename().substring(0, batchFile.getFilename().indexOf("."));
            String suffix = ".tmp";
            File tempFile = File.createTempFile(prefix, suffix);
            InputStream in = batchFile.openInputStream();
            OutputStream out = new FileOutputStream(tempFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0)
            {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            ZipFile zipFileStream = new ZipFile(tempFile);
            ZipEntry entry;
            Enumeration e = zipFileStream.entries();
            while (e.hasMoreElements())
            {
                entry = (ZipEntry) e.nextElement();
                if (!entry.isDirectory())
                {
                    //byte[] contents = new byte[(int) entry.getSize()];
                    //zipInputStream.read(contents);
                    //BufferedInputStream biss = new BufferedInputStream(
                    //new ByteArrayInputStream(contents));
                    String name = entry.getName().substring(entry.getName().indexOf("/") + 1);
                    InputStream biss = zipFileStream.getInputStream(entry);
                    unZipFiles.put(name, biss);
                }
            }
            tempFile.delete();
        }
        catch (Exception ze)
        {
            errors.reject(null, "Error in unzipping files ElispotController (getunzipFiles): " + ze);
        }
        return unZipFiles;
    }
}
