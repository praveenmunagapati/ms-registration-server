package org.scharp.atlas.peptide;

import org.labkey.api.action.SpringActionController;
import org.labkey.api.data.*;
import org.labkey.api.util.DateUtil;
import org.labkey.api.attachments.AttachmentFile;
import org.apache.log4j.Logger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.beanutils.ConversionException;
import org.springframework.validation.Errors;
import org.scharp.atlas.peptide.model.ReplicateHistory;
import org.scharp.atlas.peptide.model.PeptideGroup;
import org.scharp.atlas.peptide.model.GroupMetaData;

import java.util.*;
import java.io.Writer;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Jul 6, 2009
 * Time: 12:43:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class PeptideBaseController extends SpringActionController
{
    private final static Logger _log = Logger.getLogger(PeptideBaseController.class);

    private static final String VIEW_DISPLAY_PEPTIDE = "displayPeptide.view";
    protected static final String QRY_STRING_PEPTIDE_ID = "peptideId";

    public static class PeptideQueryForm
    {
        private String queryKey;
        private String queryValue;
        private String message;
        private TableInfo tInfo;
        private SimpleFilter filter;
        private List<ColumnInfo> cInfo;
        private Sort sort;

        public String getMessage()
        {
            return message;
        }

        public void setMessage(String message)
        {
            this.message = message;
        }

        public List<ColumnInfo> getCInfo()
        {
            return cInfo;
        }

        public void setCInfo(List<ColumnInfo> cInfo)
        {
            this.cInfo = cInfo;
        }

        public String getQueryValue()
        {
            return queryValue;
        }

        public void setQueryValue(String queryValue)
        {
            this.queryValue = queryValue;
        }

        public TableInfo getTInfo()
        {
            return tInfo;
        }

        public void setTInfo(TableInfo tInfo)
        {
            this.tInfo = tInfo;
        }

        public SimpleFilter getFilter()
        {
            return filter;
        }

        public void setFilter(SimpleFilter filter)
        {
            this.filter = filter;
        }

        public Sort getSort()
        {
            return sort;
        }

        public void setSort(Sort sort)
        {
            this.sort = sort;
        }

        public String getQueryKey()
        {
            return queryKey;
        }

        public void setQueryKey(String queryKey)
        {
            this.queryKey = queryKey;
        }

        public boolean validate(Errors errors)
        {
            if(getQueryKey() == null || StringUtils.trimToNull(getQueryKey()) == null)
                errors.reject(null, "The Search Criteria must be entered.");
            if (getQueryKey() != null && getQueryKey().equals(PeptideSchema.COLUMN_PEPTIDE_ID))
            {
                String qValue = getQueryValue();
                if (StringUtils.trimToNull(qValue) == null)
                    errors.reject(null, "The Peptide Id range must be entered.");
                if (qValue != null && qValue.length() > 0)
                {
                    if (!(qValue.matches("\\d+-\\d+")))
                    {
                        errors.reject(null, "To get the peptides in the range you should specify the Range of numbers.\n" +
                                "The format for specify the range of peptide is <number>-<number> Example would be 30-100");
                    }
                    else
                    {
                        String[] range = qValue.split("-");
                        if (Integer.parseInt(range[0]) > Integer.parseInt(range[1]))
                        {
                            errors.reject(null, "The minimum value which is before '-' should be less than the max value which is after '-'.\n");

                        }
                    }
                }
            }
            if (getQueryKey() != null && getQueryKey().equals(PeptideSchema.COLUMN_QC_PASSED))
            {
                String qValue = getQueryValue();
                if (StringUtils.trimToNull(qValue) == null)
                    errors.reject(null, "The Manufacture Status must be entered.");
            }
            if(errors != null && errors.getErrorCount() >0)
                return false;
            return true;
        }

    }

    public static class DisplayPeptideForm
    {
        private String peptideId;
        private boolean modify = false;
        private String manufactureStatus;

        public void setPeptideId(String id)
        {
            this.peptideId = id;
        }

        public String getPeptideId()
        {
            return this.peptideId;
        }

        public void setModify(boolean modify)
        {
            this.modify = modify;
        }

        public boolean getModify()
        {
            return this.modify;
        }

        public String getManufactureStatus()
        {
            return manufactureStatus;
        }

        public void setManufactureStatus(String manufactureStatus)
        {
            this.manufactureStatus = manufactureStatus;
        }

        public String toString()
        {
            return "DisplayPeptideForm [peptideId:" + this.peptideId +
                    ", modify:" + this.modify +
                    ", manufactureStatus:" + this.manufactureStatus + "]";
        }
    }

    public static class EditPeptideForm
    {
        private String peptideId;
        private String manufactureStatus;
        private String[] peptideGroup;
        private String[] transmittedStatus;
        public String getPeptideId()
        {
            return peptideId;
        }

        public void setPeptideId(String peptideId)
        {
            this.peptideId = peptideId;
        }

        public String getManufactureStatus()
        {
            return manufactureStatus;
        }

        public void setManufactureStatus(String manufactureStatus)
        {
            this.manufactureStatus = manufactureStatus;
        }

        public String[] getPeptideGroup()
        {
            return peptideGroup;
        }

        public void setPeptideGroup(String[] peptideGroup)
        {
            this.peptideGroup = peptideGroup;
        }

        public String[] getTransmittedStatus()
        {
            return transmittedStatus;
        }

        public void setTransmittedStatus(String[] transmittedStatus)
        {
            this.transmittedStatus = transmittedStatus;
        }
    }

    public static class PeptideAndGroupForm extends DisplayPeptideForm
    {

        private String peptide_group_id;

        public String getPeptide_group_id()
        {
            return peptide_group_id;
        }

        public void setPeptide_group_id(String peptide_group_id)
        {
            this.peptide_group_id = peptide_group_id;
        }

        public String toString()
        {
            return "PeptideAndGroupForm [peptideGroup:" + this.peptide_group_id + "] - " + super.toString();
        }

    }

     public static class PeptideAndPoolForm extends DisplayPeptideForm
    {
        private String peptidePool;

        public String getPeptidePool()
        {
            return this.peptidePool;
        }

        public void setPeptidePool(String peptidePool)
        {
            this.peptidePool = peptidePool;
        }

        public String toString()
        {
            return "PeptideAndPoolForm [peptidePool:" + this.peptidePool + "] - " + super.toString();
        }
    }

    public static class PeptideGroupForm extends BeanViewForm<PeptideGroup> {
        public PeptideGroupForm()
        {
            super(PeptideGroup.class, PeptideSchema.getInstance().getTableInfoPeptideGroups());

        }
        public PeptideGroupForm(String peptideGroupID)
        {
            this();
            set("peptide_group_id", String.valueOf(peptideGroupID));
        }

        public void validate(Errors errors)
        {
            PeptideGroup bean = getBean();
            if(bean.getPathogen_id() == null || StringUtils.trimToNull(bean.getPathogen_id().toString())==null)
                errors.reject(null,"Pathogen is Required");
            if(bean.getClade_id() == null || StringUtils.trimToNull(bean.getClade_id().toString())==null)
                errors.reject(null,"Clade is Required");
            if(bean.getGroup_type_id() == null || StringUtils.trimToNull(bean.getGroup_type_id().toString())==null)
                errors.reject(null,"Group Type is Required");
        }

        public void validateName(Errors errors) throws SQLException
        {
            PeptideGroup bean = getBean();
            if (StringUtils.trimToNull(bean.getPeptide_group_id()) == null)
                errors.reject(null, "Peptide Group Name is required.");
            else
            {
                HashMap<String,PeptideGroup> peptideGroupMap = PeptideManager.getPeptideGroupMap();
                if(peptideGroupMap.containsKey(bean.getPeptide_group_id().trim().toUpperCase()))
                    errors.reject(null, "Peptide Group with the name : "+bean.getPeptide_group_id()+" already exists in the database.");
            }
        }
    }

    public static class GroupMetaDataForm extends BeanViewForm<GroupMetaData> {
        public GroupMetaDataForm()
        {
            super(GroupMetaData.class, PeptideSchema.getInstance().getTableInfoGroupPatient());

        }

        public GroupMetaDataForm(String peptideGroupID)
        {
            this();
            set("peptide_group_id", String.valueOf(peptideGroupID));
        }

        public void validate(Errors errors)
        {
            GroupMetaData bean = getBean();
            if (StringUtils.trimToNull(bean.getPeptide_group_id()) == null)
                errors.reject(null,"Peptide Group Name is required.");
            if(StringUtils.trimToNull(bean.getDraw_date()) != null)
            {
                String drawDate = bean.getDraw_date();
                if(!(drawDate.toUpperCase().matches("\\d{1,2}\\-[A-Z]{3}\\-\\d{4}")))
                {
                    errors.reject(null,"Draw Date "+drawDate+" is not in the right format.It should be in 'dd-MMM-yyyy' format");
                }
                else{
                    Date validDate = isValidDate(drawDate);
                    if(validDate == null)
                        errors.reject(null,"Draw Date "+drawDate+" is not a valid date.");
                    else
                    {
                        if(validDate.after(new Date()))
                            errors.reject(null,"Draw Date must be on or before the current date.");
                    }
                }
            }
        }
    }

    public static class FileForm
    {
        private String message;
        private String actionType;

        public String getMessage()
        {
            return message;
        }

        public void setMessage(String message)
        {
            this.message = message;
        }

        public String getActionType()
        {
            return actionType;
        }

        public void setActionType(String actionType)
        {
            this.actionType = actionType;
        }

        public boolean validate(Errors errors, AttachmentFile file) throws Exception
        {
            if (file == null || file.getSize() == 0)
                errors.reject(null, "File is required.File should be tab delimited text file and the number of field vary depending on file type.");
            else
            {
                if (!(file.getFilename().endsWith(".txt")))
                {
                    errors.reject(null, "File name must end with in .txt.\nFile should be tab delimited text file and the number of field vary depending on file type.");
                }
            }
            if (getActionType() == null || getActionType().length() == 0)
                errors.reject(null, "File Type is required");
            if(errors.getErrorCount() > 0)
                return false;
            return true;
        }
    }

    public static class CreatePoolForm 
    {
        private String peptideGroup;
        private String matrixId;

        public String getPeptideGroup()
        {
            return peptideGroup;
        }

        public void setPeptideGroup(String peptideGroup)
        {
            this.peptideGroup = peptideGroup;
        }

        public String getMatrixId()
        {
            return matrixId;
        }

        public void setMatrixId(String matrixId)
        {
            this.matrixId = matrixId;
        }

        public void validate(Errors errors)
        {
            if (getPeptideGroup() == null || getPeptideGroup().length() == 0)
                errors.reject(null, "Peptide Group is required.");
            if (getMatrixId() == null || getMatrixId().length() == 0)
                errors.reject(null, "Matrix Id is required.");
        }
    }

    public class DCpeptideId extends DataColumn
    {
        public DCpeptideId(ColumnInfo col)
        {
            super(col);
        }

        public void renderGridCellContents(RenderContext ctx, Writer out) throws IOException
        {
            ColumnInfo c = getColumnInfo();
            Map rowMap = ctx.getRow();
            if (!PeptideSchema.COLUMN_PEPTIDE_ID.equals(c.getName()))
                super.renderGridCellContents(ctx, out);
            else
            {
                Integer peptideId = (Integer) rowMap.get(c.getName());
                try
                {
                    // Scot's failed attempt at using a displayPeptideDetail "view" based of of a LabKey DetailsView class
                    //String href = ViewURLHelper.toPathString("Peptide", "displayPeptideDetails.view", getContainer()) + "?"+QRY_STRING_PEPTIDE_ID+"="+peptideId;
                    //String href = ActionURL.toPathString("Peptide", VIEW_DISPLAY_PEPTIDE, getContainer()) + "?" + QRY_STRING_PEPTIDE_ID+ "=" + peptideId;
                    String href = "displayPeptide.view?" + QRY_STRING_PEPTIDE_ID + "=" + peptideId;
                    out.write("<a href ='");
                    out.write(href);
                    out.write("' ");
                    out.write("target='_self'>");
                    out.write("P"+toLZ(peptideId));
                    out.write("</a");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        }

        public Object getDisplayValue(RenderContext ctx)
        {
            ColumnInfo c = getColumnInfo();
            Map rowMap = ctx.getRow();
            if (!PeptideSchema.COLUMN_PEPTIDE_ID.equals(c.getName()))
                return super.getValue(ctx);
            else
            {
                Integer peptideId = (Integer) rowMap.get(c.getName());
                try
                {
                    return ("P"+toLZ(peptideId));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    return "EXPORT/OUTPUT ERROR";
                }
            }
        }

        public Class getValueClass()
        {
            return String.class;
        }

        public Class getDisplayValueClass()
        {
            return String.class;
        }
    }

    public class DCpeptidePoolId extends DataColumn
    {
        public DCpeptidePoolId(ColumnInfo col)
        {
            super(col);
        }

        public void renderGridCellContents(RenderContext ctx, Writer out) throws IOException
        {
            ColumnInfo c = getColumnInfo();
            Map rowMap = ctx.getRow();
            if (!PeptideSchema.COLUMN_PEPTIDE_POOL_ID.equals(c.getName()))
                super.renderGridCellContents(ctx, out);
            else
            {
                Integer peptidePoolId = (Integer) rowMap.get(c.getName());
                try
                {
                    out.write("PP" + toLZ(peptidePoolId));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        }

        public Object getDisplayValue(RenderContext ctx)
        {
            ColumnInfo c = getColumnInfo();
            Map rowMap = ctx.getRow();
            if (!PeptideSchema.COLUMN_PEPTIDE_POOL_ID.equals(c.getName()))
                return super.getValue(ctx);
            else
            {
                Integer peptidePoolId = (Integer) rowMap.get(c.getName());
                try
                {
                    return ("PP" + toLZ(peptidePoolId));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    return "EXPORT/OUTPUT ERROR";
                }
            }
        }

        public Class getValueClass()
        {
            return String.class;
        }

        public Class getDisplayValueClass()
        {
            return String.class;
        }
    }

    public class DChistoryId extends DataColumn
    {
        public DChistoryId(ColumnInfo col)
        {
            super(col);
        }

        public void renderGridCellContents(RenderContext ctx, Writer out) throws IOException
        {
            ColumnInfo c = getColumnInfo();
            Map rowMap = ctx.getRow();
            if (!"history_id".equals(c.getName()))
                super.renderGridCellContents(ctx, out);
            else
            {
                Integer historyId = (Integer) rowMap.get(c.getName());
                try {
                    if(historyId != null && historyId.toString().length() != 0){
                        ReplicateHistory historyRecord = PeptideManager.getHistoryRecord(historyId);
                        out.write(historyRecord.getPeptide_id().toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Integer validateInteger(String value)
    {
        try{
            Integer intValue = new Integer(value);
            return intValue;
        }
        catch(NumberFormatException e){return null;}
    }

    public static String toLZ(int i)
    {
        // converts integer to left-zero padded string, len  chars long.
        String s = Integer.toString(i);
        if (s.length() > 6) return s.substring(0, 6);
        else if (s.length() < 6) // pad on left with zeros
            return "000000000000000000000000000".substring(0, 6 - s.length()) + s;
        else return s;
    }

    public static String toLZ(String s)
    {
        if (s.length() > 6) return s.substring(0, 6);
        else if (s.length() < 6) // pad on left with zeros
            return "000000000000000000000000000".substring(0, 6 - s.length()) + s;
        else return s;
    }

    public static java.util.Date isValidDate(String sDateIn) {

        try {
            java.util.Date dDate = new java.util.Date(DateUtil.parseDateTime(sDateIn));
            return dDate;
        } catch (ConversionException x) {
            return null;
        }
    }
}
