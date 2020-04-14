package org.scharp.atlas.elispot;

import org.apache.log4j.Logger;
import org.labkey.api.data.*;
import org.labkey.api.security.User;
import org.labkey.api.query.FieldKey;
import org.labkey.api.view.ViewContext;
import org.labkey.api.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.beanutils.ConversionException;
import org.scharp.atlas.elispot.model.*;

import java.sql.SQLException;
import java.sql.Date;
import java.util.HashMap;

/**
 * Static Class the manages Data Access to the Elispotplatedata Schema
 * @author chuck
 * @version $Id: EliSpotManager.java 54658 2012-03-20 20:48:38Z kleemann $
 */
public class EliSpotManager {
    private static Logger log = Logger.getLogger(EliSpotManager.class);

    /* Static class */
    private EliSpotManager() { }
    /**
     * returns an array of Lab objects
     *
     * @return a Array of Lab Objects filtered on Network
     * @throws Exception
     * TODO implement
     */

    /* public static Lab[] getLabs(String network) throws Exception {
       Lab lab = new Lab();
       lab.setLabId("FH");
       lab.setLabDescription("Fred Hutch");
       return new Lab[] { lab };
   } */
    public static PlateInformation[] getPlateInformation(Integer plateSeqId)
    {
        SimpleFilter sFilter = new SimpleFilter(FieldKey.fromParts("plate_seq_id"), plateSeqId);
        return new TableSelector(EliSpotSchema.getInstance().getTableInfoPlateInfo(), sFilter, null).getArray(PlateInformation.class);
    }
    /**
     * Returns an Array of Batch Model objects filtered on studyId and labId
     * @param c
     * @param studylabseqId
     * @return Returns an Array of Batch Model objects
     */
    public static Batch[] getBatchInformation(Container c, Integer studylabseqId)
    {
        EliSpotSchema schema = EliSpotSchema.getInstance();
        TableInfo batchTableInfo = schema.getTableInfoBatch();
        Sort sort = new Sort(EliSpotSchema.COLUMN_BATCH_SEQ_ID);
        SimpleFilter filter = new SimpleFilter(EliSpotSchema.COLUMN_CONTAINER, c.getId());
        filter.addCondition(FieldKey.fromParts("lab_study_seq_id"), studylabseqId);
        return new TableSelector(batchTableInfo, filter, sort).getArray(Batch.class);
    }

    public static Integer getStudyCount(Container c)
    {
        return (int)new TableSelector(EliSpotSchema.getInstance().getTableInfoStudies(), SimpleFilter.createContainerFilter(c), null).getRowCount();
    }

    public static Study insertStudy(Container c, User user, Study study) throws SQLException
    {
        study.setContainer(c.getId());
        return Table.insert(user, EliSpotSchema.getInstance().getTableInfoStudies(), study);
    }

    public static Lab insertLab(Container c, User user, Lab lab) throws SQLException
    {
        lab.setContainer(c.getId());
        return Table.insert(user, EliSpotSchema.getInstance().getTableInfoLabs(), lab);
    }

    public static StudyLab getStudyLab(Container c,StudyLab studyLab)
    {
        StudyLab sLab = null;
        SimpleFilter sFilter = new SimpleFilter(FieldKey.fromParts("study_seq_id"), studyLab.getStudy_seq_id());
        sFilter.addCondition(FieldKey.fromParts("lab_seq_id"), studyLab.getLab_seq_id());
        //sFilter.addCondition("container",c.getId()); //This is not returning if there is any combination in other container.
        //I assume there will be case where a study and a lab are associated multiple times in different containers
        //Because of the primary key combination with lab id and study id If we check for container here it won't insert a record in the databse in different container. Or need to add container id in primary key combination.
        StudyLab[] studyLabs = new TableSelector(EliSpotSchema.getInstance().getTableInfostudyLabs(), sFilter, null).getArray(StudyLab.class);
        if (studyLabs.length > 0)
            sLab = studyLabs[0];
        return sLab;
    }

    public static StudyLab insertStudyLab(Container c, User user, StudyLab studyLab) throws SQLException
    {
        studyLab.setContainer(c.getId());
        return Table.insert(user, EliSpotSchema.getInstance().getTableInfostudyLabs(), studyLab);
    }

    public static BatchType[] getBatchTypes()
    {
        return new TableSelector(EliSpotSchema.getInstance().getTableInfoBatchType()).getArray(BatchType.class);
    }

    public static StudyLab[] getStudyLabs(Container c)
    {
        return new TableSelector(EliSpotSchema.getInstance().getTableInfostudyLabs(),
            new SimpleFilter(FieldKey.fromParts("container"), c.getId()), new Sort("study_seq_id")).getArray(StudyLab.class);
    }

    public static Lab getLab(Container c, Integer labId)
    {
        Lab lab = null;
        SimpleFilter sFilter = new SimpleFilter(FieldKey.fromParts("container"), c.getId());
        sFilter.addCondition(FieldKey.fromParts("lab_seq_id"), labId);
        Lab[] labs = new TableSelector(EliSpotSchema.getInstance().getTableInfoLabs(), sFilter, null).getArray(Lab.class);
        if (labs.length != 0)
            lab = labs[0];
        return lab;
    }

    public static StudyLab getStudyLab(Container c,Integer studylabseqId)
    {
        SimpleFilter sFilter = new SimpleFilter(FieldKey.fromParts("container"), c.getId());
        sFilter.addCondition(FieldKey.fromParts("lab_study_seq_id"), studylabseqId);

        return new TableSelector(EliSpotSchema.getInstance().getTableInfostudyLabs(), sFilter, null).getObject(StudyLab.class);
    }

    public static Study[] getStudies(Container c, boolean status)
    {
        EliSpotSchema schema = EliSpotSchema.getInstance();
        TableInfo studyTableInfo = schema.getTableInfoStudies();
        Sort sort = new Sort(EliSpotSchema.COLUMN_STUDY_SEQ_ID);
        SimpleFilter filter = new SimpleFilter(EliSpotSchema.COLUMN_CONTAINER,c.getId());
        if(status)
            filter.addCondition(FieldKey.fromParts("status"), "ACTIVE");
        return new TableSelector(studyTableInfo, filter, sort).getArray(Study.class);
    }

    public static HashMap getStudyDescs(Container c)
    {
        Study [] studies = getStudies(c,false);
        HashMap<Integer,String> studyMap = new HashMap<>();
        for(Study study : studies)
        {
            studyMap.put(study.getStudy_seq_id(),study.getStudy_description());
        }
        return studyMap;
    }

    public static Lab[] getLabs(Container c)
    {
        EliSpotSchema schema = EliSpotSchema.getInstance();
        TableInfo labTableInfo = schema.getTableInfoLabs();
        Sort sort = new Sort("lab_seq_id");
        SimpleFilter filter = new SimpleFilter(EliSpotSchema.COLUMN_CONTAINER,c.getId());
        return new TableSelector(labTableInfo, filter, sort).getArray(Lab.class);
    }

    public static HashMap getLabMap(Container c)
    {
        Lab [] labs = getLabs(c);
        HashMap labMap = new HashMap();
        for(Lab lab : labs)
        {
            labMap.put(lab.getLab_seq_id(),lab.getLab_desc());
        }
        return labMap;
    }

    public static Batch insertBatch(Container c, User user, Batch batch) throws SQLException
    {
        batch.setContainer(c.getId());
        return Table.insert(user, EliSpotSchema.getInstance().getTableInfoBatch(), batch);
    }

    public static PlateTemplate insertPlateTemplate(Container c, User user, PlateTemplate plateTemplate) throws SQLException
    {
        plateTemplate.setContainer(c.getId());
        return Table.insert(user, EliSpotSchema.getInstance().getTableInfoPlateTemplate(), plateTemplate);
    }

    public static PlateTemplate[] getPlateTemplates(Container c)
    {
        SimpleFilter sFilter = new SimpleFilter(EliSpotSchema.COLUMN_CONTAINER,c.getId());
        return new TableSelector(EliSpotSchema.getInstance().getTableInfoPlateTemplate(), sFilter, new Sort("template_seq_id")).getArray(PlateTemplate.class);
    }

    public static PTDetails[] getPTDetails(Container c, Integer templateSeqId)
    {
        SimpleFilter sFilter = new SimpleFilter(EliSpotSchema.COLUMN_CONTAINER,c.getId());
        sFilter.addCondition(FieldKey.fromParts("template_seq_id"), templateSeqId);
        return new TableSelector(EliSpotSchema.getInstance().getTableInfoPTDetails(), sFilter, null).getArray(PTDetails.class);
    }

    public static PTDetails insertPTDetails(ViewContext ctx,PTDetails ptd) throws SQLException
    {
        ptd.setContainer(ctx.getContainer().getId());
        return Table.insert(ctx.getUser(),EliSpotSchema.getInstance().getTableInfoPTDetails(),ptd);
    }

    public static Specimen insertSpecimen(Container c, User user, Specimen specimen) throws SQLException
    {
        specimen.setContainer(c.getId());
        return Table.insert(user, EliSpotSchema.getInstance().getTableInfoSpecimen(), specimen);
    }

    public static Plate insertPlate(Container c, User user, Plate plate) throws SQLException
    {
        plate.setContainer(c.getId());
        return Table.insert(user, EliSpotSchema.getInstance().getTableInfoPlate(), plate);
    }

    public static PlateTemplate getPlateTemplate(Container c,Integer templateseqId)
    {
        PlateTemplate pt = null;
        SimpleFilter sFilter = new SimpleFilter(FieldKey.fromParts("container"), c.getId());
        sFilter.addCondition(FieldKey.fromParts("template_seq_id"), templateseqId);
        PlateTemplate[] plateTemplates = new TableSelector(EliSpotSchema.getInstance().getTableInfoPlateTemplate(), sFilter, null).getArray(PlateTemplate.class);
        if (plateTemplates.length > 0)
            pt = plateTemplates[0];
        return pt;
    }

    public static Specimen[] getSpecimens(Container c,Integer studyseqId)
    {
        SimpleFilter sFilter = new SimpleFilter(FieldKey.fromParts("container"), c.getId());
        sFilter.addCondition(FieldKey.fromParts("study_seq_id"), studyseqId);

        return new TableSelector(EliSpotSchema.getInstance().getTableInfoSpecimen(), sFilter, new Sort("specimen_seq_id")).getArray(Specimen.class);
    }

    public static Cryostatus[] getCryostatus()
    {
        return new TableSelector(EliSpotSchema.getInstance().getTableInfoCryos(), null, new Sort("cryostatus")).getArray(Cryostatus.class);
    }

    public static Additive[] getAdditives()
    {
        return new TableSelector(EliSpotSchema.getInstance().getTableInfoAdditive(), null, new Sort("additive_seq_id")).getArray(Additive.class);
    }

    public static PlateSpecimens insertPlateSpecimens(Container c, User user, PlateSpecimens plateSpecimens) throws SQLException
    {
        plateSpecimens.setContainer(c.getId());
        return Table.insert(user, EliSpotSchema.getInstance().getTableInfoPlateSpecimens(), plateSpecimens);
    }

    public static Reader[] getReaderInformation()
    {
        EliSpotSchema schema = EliSpotSchema.getInstance();
        TableInfo readerTableInfo = schema.getTableInfoReader();
        Sort sort = new Sort(EliSpotSchema.COLUMN_READER_SEQ_ID);
        return new TableSelector(readerTableInfo, null, sort).getArray(Reader.class);
    }

    /**
     * Returns Reader objects filtered on reader_seq_id
     * @param reader_seq_id
     * @return Returns Reader objects
     */
    public static Reader getReaderInformation(Integer reader_seq_id)
    {
        EliSpotSchema schema = EliSpotSchema.getInstance();
        TableInfo readerTableInfo = schema.getTableInfoReader();
        Sort sort = new Sort(EliSpotSchema.COLUMN_READER_SEQ_ID);
        SimpleFilter filter = new SimpleFilter(EliSpotSchema.COLUMN_READER_SEQ_ID, reader_seq_id);
        return new TableSelector(readerTableInfo, filter, sort).getObject(Reader.class);

    }

    public static Plate getPlate(Container c,Integer plateseqid)
    {
        SimpleFilter sFilter = new SimpleFilter(FieldKey.fromParts("container"), c.getId());
        sFilter.addCondition(FieldKey.fromParts("plate_seq_id"), plateseqid);
        return new TableSelector(EliSpotSchema.getInstance().getTableInfoPlate(), sFilter, null).getArray(Plate.class)[0];
    }

    public static Plate getPlates(Container c, String plateid, Integer batch_seq_id)
    {
        Plate plate = null;
        SQLFragment sql = new SQLFragment("SELECT * FROM " + EliSpotSchema.getInstance().getTableInfoPlate() + " WHERE UPPER(plate_name) = ? AND batch_seq_id = ? AND container = ?");
        sql.add(plateid);
        sql.add(batch_seq_id);
        sql.add(c.getId());
        Plate[] plates = new SqlSelector(EliSpotSchema.getInstance().getSchema(), sql).getArray(Plate.class);
        if (plates.length != 0)
            plate = plates[0];
        return plate;
    }

    public static Plate[] getPlatesInfo(Container c,Integer batchseqid)
    {
        EliSpotSchema schema = EliSpotSchema.getInstance();
        TableInfo plateTableInfo = schema.getTableInfoPlate();
        Sort sort = new Sort(EliSpotSchema.COLUMN_PLATE_SEQ_ID);
        SimpleFilter sFilter = new SimpleFilter(EliSpotSchema.COLUMN_CONTAINER,c.getId());
        sFilter.addCondition(FieldKey.fromParts("batch_seq_id"), batchseqid);
        Plate[] plates = new TableSelector(plateTableInfo, sFilter, sort).getArray(Plate.class);
        return plates;
    }

    public static boolean updatePlate(Container c, User user, Plate p) throws SQLException
    {
        Plate plate = null;
        if (p.getPlate_seq_id() == null)
            throw new IllegalStateException("Can't update a row with a null plateseqId");
        if (p.getContainerId() == null)
            p.setContainerId(c.getId());
        if (!p.getContainerId().equals(c.getId()))
            throw new IllegalStateException("Can't update a row with a null rowId");
        plate = Table.update(user,EliSpotSchema.getInstance().getTableInfoPlate(),p,p.getPlate_seq_id());
        if(plate == null)
            return false;
        return true;
    }

    public static PlateData insertPlateData(ViewContext ctx,PlateData pData) throws SQLException
    {
        pData.setContainer(ctx.getContainer().getId());
        return Table.insert(ctx.getUser(),EliSpotSchema.getInstance().getTableInfoPlateData(),pData);
    }

    public static Group getGroupId(Container c,String permGroupName)
    {
        Group group = null;
        SQLFragment sql = new SQLFragment("SELECT userid,name,container FROM "+CoreSchema.getInstance().getTableInfoPrincipals()+" WHERE UPPER(name) = ? AND container = ?");
        sql.add(permGroupName.toUpperCase());
        sql.add(c.getProject().getId());
        Group[] groups = new SqlSelector(CoreSchema.getInstance().getSchema(), sql).getArray(Group.class);
        if(groups.length > 0)
            group = groups[0];
        return group;
    }

    public static BatchInformation getBatchInfo(Integer batchSeqId)
    {
        SimpleFilter sFilter = new SimpleFilter(FieldKey.fromParts("batch_seq_id"), batchSeqId);
        return new TableSelector(EliSpotSchema.getInstance().getTableInfoBatchInfo(), sFilter, null).getObject(BatchInformation.class);
    }

    public static boolean updateBatch(User user, Integer batchSeqId, Integer readerseqId) throws SQLException
    {
        TableInfo batchTableInfo = EliSpotSchema.getInstance().getTableInfoBatch();
        SimpleFilter filter = new SimpleFilter(FieldKey.fromParts("batch_seq_id"), batchSeqId);
        Batch batch = null;
        batch = new TableSelector(batchTableInfo, filter, null).getObject(Batch.class);
        if(batch.getReader_seq_id() == null || batch.getReader_seq_id().toString().length() == 0)
        {
            batch.setReader_seq_id(readerseqId);
            batch = Table.update(user,batchTableInfo,batch,batchSeqId);
        }
        if(batch == null)
            return false;
        return true;
    }

    public static PlateSpecimens[] getPlateSpecimens(Integer plateseqId)
    {
        SimpleFilter sFilter = new SimpleFilter(FieldKey.fromParts("plate_seq_id"), plateseqId);
        return new TableSelector(EliSpotSchema.getInstance().getTableInfoPlateSpecimens(), sFilter, new Sort("spec_well_group")).getArray(PlateSpecimens.class);
    }

    public static CellCounter[] getCellCounters()
    {
        return new TableSelector(EliSpotSchema.getInstance().getTableInfoCellcounter()).getArray(CellCounter.class);
    }

    public static Substrate[] getSubstrates()
    {
        return new TableSelector(EliSpotSchema.getInstance().getTableInfoSubstrate()).getArray(Substrate.class);
    }

    public static PlateType[] getPlateTypes()
    {
        return new TableSelector(EliSpotSchema.getInstance().getTableInfoPlateType()).getArray(PlateType.class);
    }

    public static Study getStudy(Container c,Integer studyId)
    {
        SimpleFilter sFilter = new SimpleFilter(EliSpotSchema.COLUMN_CONTAINER,c.getId());
        sFilter.addCondition(FieldKey.fromParts("study_seq_id"), studyId);
        return new TableSelector(EliSpotSchema.getInstance().getTableInfoStudies(), sFilter, null).getObject(Study.class);
    }

    public static Study updateStudy(User user,Container c,Study study) throws SQLException
    {
        return Table.update(user,EliSpotSchema.getInstance().getTableInfoStudies(),study,study.getStudy_seq_id());
    }

    public static boolean removePlateInfo(User user,Integer plateId,String comments)
    {
        SimpleFilter sFilter = new SimpleFilter(FieldKey.fromParts("plate_seq_id"), plateId);
        Date date = null;
        DbScope scope = EliSpotSchema.getInstance().getSchema().getScope();
        try (DbScope.Transaction transaction = scope.ensureTransaction())
        {
            Table.delete(EliSpotSchema.getInstance().getTableInfoPlateMap(),sFilter);
            Table.delete(EliSpotSchema.getInstance().getTableInfoPlateData(),sFilter);
            Plate plate = new TableSelector(EliSpotSchema.getInstance().getTableInfoPlate(), sFilter, null).getObject(Plate.class);
            plate.setImport_date(date);
            plate.setPlate_filename(null);
            plate.setComment(comments);
            Table.update(user,EliSpotSchema.getInstance().getTableInfoPlate(),plate,plateId);
            /*Table.execute(EliSpotSchema.getInstance().getSchema(),
                    "UPDATE "+EliSpotSchema.getInstance().getTableInfoPlate()+" SET import_date = ?,plate_filename = ? where plate_seq_id = ?",
                    new Object[]{date,null,plateId}); */
            transaction.commit();
        }
        catch(Exception e)
        {
            return false;
        }
        return true;
    }

    public static int updateTestDate(User user, Integer plate_seq_id, Date testDate)
    {
        java.sql.Timestamp date = new java.sql.Timestamp(System.currentTimeMillis());
        String dateQuery = "UPDATE "+EliSpotSchema.getInstance().getTableInfoPlate()+
                "\nSET test_date = ? ,modifiedby = ?,modified = ? where plate_seq_id = ? ";
        return new SqlExecutor(EliSpotSchema.getInstance().getSchema()).execute(dateQuery, testDate, user.getUserId(), date,plate_seq_id);
    }

    public static int updateCellCounts(User user, PlateSpecimens p)
    {
        java.sql.Timestamp date = new java.sql.Timestamp(System.currentTimeMillis());
        String sqlQuery =
                "UPDATE "+EliSpotSchema.getInstance().getTableInfoPlateSpecimens()+
                        "\nSET counter_seq_id =" +(p.getCounter_seq_id() == null ||p.getCounter_seq_id().toString().length() ==0 ? null :p.getCounter_seq_id())+
                        ", d1_cellcount="+(StringUtils.trimToNull(p.getD1_cellcount()) == null ? null :p.getD1_cellcount())+
                        " , d1_viability="+(StringUtils.trimToNull(p.getD1_viability()) == null ? null :p.getD1_viability())+
                        " , d2_cellcount="+(StringUtils.trimToNull(p.getD2_cellcount()) == null ? null:p.getD2_cellcount())+
                        " , d2_viability="+(StringUtils.trimToNull(p.getD2_viability()) == null ? null:p.getD2_viability())+
                        " , modifiedby=? , modified=? \nWHERE plate_seq_id=? and specimen_seq_id=?";
        return new SqlExecutor(EliSpotSchema.getInstance().getSchema()).execute(sqlQuery, user.getUserId(), date, p.getPlate_seq_id(), p.getSpecimen_seq_id());
    }

    public static int updatePInfo(User user, Plate p)
    {
        java.sql.Timestamp date = new java.sql.Timestamp(System.currentTimeMillis());
        String sqlQuery =
                "UPDATE "+EliSpotSchema.getInstance().getTableInfoPlate()+
                        "\nSET isprecoated =" +(p.getIsprecoated() == null ||p.getIsprecoated().length() ==0 ? null :"'"+p.getIsprecoated()+"'")+
                        ", platetype_seq_id="+(p.getPlatetype_seq_id() == null ||p.getPlatetype_seq_id().toString().length() ==0 ? null :p.getPlatetype_seq_id())+
                        " , substrate_seq_id="+(p.getSubstrate_seq_id() == null ||p.getSubstrate_seq_id().toString().length() ==0 ? null :p.getSubstrate_seq_id())+
                        " , modifiedby=? , modified=? \nWHERE plate_seq_id=?";
        return new SqlExecutor(EliSpotSchema.getInstance().getSchema()).execute(sqlQuery, user.getUserId(), date, p.getPlate_seq_id());
    }

    public static java.util.Date isValidDate(String sDateIn)
    {
        try
        {
            java.util.Date dDate = new java.util.Date(DateUtil.parseDateTime(sDateIn));
            return dDate;
        } catch (ConversionException x) {
            return null;
        }
    }
}
