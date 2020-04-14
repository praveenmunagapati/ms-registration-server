package org.scharp.atlas.peptide;

import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.labkey.api.data.*;
import org.labkey.api.query.FieldKey;
import org.labkey.api.security.User;
import org.labkey.api.util.DateUtil;
import org.scharp.atlas.peptide.PeptideBaseController.CreatePoolForm;
import org.scharp.atlas.peptide.model.*;

/**
 * Handles Data Operations.
 *
 * @version $Id: PeptideManager.java 35871 2015-01-06 22:04:30Z arauch $
 */
public class PeptideManager
{

    private static Logger log = Logger.getLogger(PeptideManager.class);
    private static PeptideSchema schema = PeptideSchema.getInstance();
    /**
     * Static class
     */
    private PeptideManager()
    {
    }

    public static PeptideGroup[] getPeptideGroups()
    {
        Sort sort = new Sort(PeptideSchema.COLUMN_PEPTIDE_GROUP_ID);
        return new TableSelector(schema.getTableInfoPeptideGroups(), null, sort).getArray(PeptideGroup.class);
    }

    public static Matrix[] getMatrices()
    {
        Sort sort = new Sort(PeptideSchema.COLUMN_MATRIX_ID);

        return new TableSelector(schema.getTableInfoMatrices(), schema.getTableInfoMatrices().getColumns(PeptideSchema.COLUMN_MATRIX_ID), null, sort).getArray(Matrix.class);
    }

    public static PeptidePool[] getPeptidePools()
    {
        Sort sort = new Sort(PeptideSchema.COLUMN_PEPTIDE_POOL_ID);

        return new TableSelector(schema.getTableInfoViewPoolDetails(),
                schema.getTableInfoViewPoolDetails().getColumns("peptide_pool_id,pool_type,description,peptide_group_id,matrix_pool_id,matrix_id")
                , null, sort).getArray(PeptidePool.class);
    }

    public static GroupType getPeptideGroupType(Integer groupTypeId)
    {
        SimpleFilter sFilter = new SimpleFilter(FieldKey.fromParts("group_type_id"), groupTypeId);

        return new TableSelector(schema.getTableInfoPeptideGroupTypes(), sFilter, null).getObject(GroupType.class);
    }

    public static GroupMetaData getGroupMetaData(String peptidegroupid)
    {
        SimpleFilter sFilter = new SimpleFilter(FieldKey.fromParts("peptide_group_id"), peptidegroupid);

        return new TableSelector(schema.getTableInfoGroupPatient(), sFilter, null).getObject(GroupMetaData.class);
    }

    public static ProteinCategory[] getProteinCategory()
    {
        return new TableSelector(schema.getTableInfoProteinCat(),
                schema.getTableInfoProteinCat().getColumns("protein_cat_id,protein_cat_desc,protein_cat_mnem,protein_sort_value"), null,
                new Sort(PeptideSchema.COLUMN_PROTEIN_CAT_ID)).getArray(ProteinCategory.class);
    }

    public static Peptides[] getPeptides()
    {
        TableInfo tInfo = PeptideSchema.getInstance().getTableInfoPeptides();
        return new TableSelector(tInfo, tInfo.getColumns("peptide_id,protein_align_pep,peptide_sequence,sort_sequence,protein_cat_id,child,qc_passed,lanl_date,parent"),
                null, new Sort("peptide_id")).getArray(Peptides.class);
    }

    public static ReplicateHistory getHistoryRecord(Integer historyId)
    {
        SimpleFilter sFilter = new SimpleFilter(FieldKey.fromParts("history_id"), historyId);
        return new TableSelector(PeptideSchema.getInstance().getTableInfoReplicate(), sFilter, null).getObject(ReplicateHistory.class);
    }

    public static ReplicateHistory getReplicatePeptideId(Integer oldPeptideId)
    {
        SimpleFilter sFilter = new SimpleFilter(FieldKey.fromParts("peptide_id"), oldPeptideId);
        return new TableSelector(PeptideSchema.getInstance().getTableInfoReplicate(), sFilter, null).getObject(ReplicateHistory.class);
    }

    /**
     * @param peptideGroup
     * @return the number of peptides in a given group
     */
    public static Integer getCount(String peptideGroup)
    {
        SimpleFilter containerFilter = new SimpleFilter(PeptideSchema.COLUMN_PEPTIDE_GROUP_ID, peptideGroup);

        return (int)new TableSelector(PeptideSchema.getInstance().getTableInfoViewGroupPeptides(), new SimpleFilter("peptide_group_id", peptideGroup), null).getRowCount();
    }

    /**
     * @param peptideId id of the peptide to get
     * @return A single Peptide based on the supplied id
     */
    public static Peptide getPeptide(String peptideId)
    {
        Peptide[] peptides = null;
        Peptide peptide = null;
        int intPeptideId = new Integer(peptideId);
        try
        {
            peptides = new TableSelector(
                    schema.getTableInfoViewPeptides(),
                    new SimpleFilter(PeptideSchema.COLUMN_PEPTIDE_ID, intPeptideId),
                    null).getArray(Peptide.class);
            if (peptides.length < 1)
            {
                ReplicateHistory replicate = getReplicatePeptideId(intPeptideId);
                if (replicate != null)
                {
                    peptides = new TableSelector(
                            schema.getTableInfoViewPeptides(),
                            new SimpleFilter(PeptideSchema.COLUMN_PEPTIDE_ID, replicate.getMaster_peptide_id()),
                            null).getArray(Peptide.class);
                    // Use the master PeptideId in subsequent queries used inside the current method.
                    intPeptideId = replicate.getMaster_peptide_id();
                }
            }
            if (peptides.length < 1)
            {
                return null;
            }
            peptide = peptides[0];
            PeptideGroup[] groups = getPeptideGroups(intPeptideId);
            if (groups != null)
            {
                peptide.setPeptideGroups(Arrays.asList(groups));
            }
            PeptidePool[] pools = new TableSelector(
                    schema.getTableInfoViewPoolPeptides(),
                    new SimpleFilter(PeptideSchema.COLUMN_PEPTIDE_ID, intPeptideId),
                    null).getArray(PeptidePool.class
            );
            peptide.setPeptidePools(Arrays.asList(pools));
        }
        catch (NumberFormatException e)
        {
            log.error(e.getMessage(), e);
        }

        return peptide;
    }

    public static PeptideGroup[] getPeptideGroups(int peptideId)
    {
        return new TableSelector(schema.getTableInfoViewGroupPeptides(), new SimpleFilter(PeptideSchema.COLUMN_PEPTIDE_ID, peptideId), null).getArray(PeptideGroup.class);
    }

    public static Peptides insertPeptide(User user, Peptides p) throws Exception
    {
        TableInfo tInfo = PeptideSchema.getInstance().getTableInfoPeptides();
        Peptides dbPeptide = peptideExists(p, tInfo);
        if(dbPeptide == null)
            dbPeptide = Table.insert(user,tInfo,p);
        return dbPeptide;
    }

    public static Peptides updatePeptide(User user, Peptides p) throws Exception
    {
        TableInfo tInfo = PeptideSchema.getInstance().getTableInfoPeptides();
        return Table.update(user, tInfo, p, p.getPeptide_id());
    }

    public static int updateSource(User user, Source s)
    {
        java.sql.Timestamp date = new java.sql.Timestamp(System.currentTimeMillis());
        String sql = "UPDATE peptide.source SET transmitted_status = ?,modified = ?,modifiedby = ? WHERE peptide_id = ? and peptide_group_id = ?";
        Object[] params = { s.getTransmitted_status(),date,user.getUserId(),s.getPeptide_id(),s.getPeptide_group_id() };

        return new SqlExecutor(PeptideSchema.getInstance().getSchema()).execute(sql, params);
    }

    public static Parent insertParent(User user,Parent parent) throws Exception
    {
        TableInfo tInfo = PeptideSchema.getInstance().getTableInfoParent();
        return Table.insert(user,tInfo,parent);
    }

    public static Parent parentExists(Parent p)
    {
        TableInfo tInfo = PeptideSchema.getInstance().getTableInfoParent();
        Parent dbParent = null;
        SimpleFilter sFilter = new SimpleFilter(FieldKey.fromParts("peptide_id"), p.getPeptide_id());
        sFilter.addCondition(FieldKey.fromParts("linked_parent"), p.getLinked_parent());
        Parent[] dbParents = new TableSelector(tInfo,
                tInfo.getColumns("peptide_id,linked_parent,parent_position")
                ,sFilter,null).getArray(Parent.class);
        if(dbParents.length > 0)
            dbParent = dbParents[0];
        return dbParent;
    }

    public static Peptides peptideExists(Peptides p, TableInfo tInfo)
    {
        Peptides dbPeptide = null;
        SimpleFilter sFilter = new SimpleFilter(FieldKey.fromParts("peptide_sequence"), p.getPeptide_sequence());
        sFilter.addCondition(FieldKey.fromParts("protein_cat_id"), p.getProtein_cat_id());
        Peptides[] dbPeptides = new TableSelector(tInfo,
                tInfo.getColumns("peptide_id,protein_align_pep,peptide_sequence,sort_sequence,protein_cat_id,child,qc_passed"), sFilter, null).getArray(Peptides.class);
        if(dbPeptides.length > 0)
            dbPeptide = dbPeptides[0];
        return dbPeptide;
    }

    public static Source insertSource(User user, Source src)
    {
        TableInfo tInfo = PeptideSchema.getInstance().getTableInfoSource();
        SQLFragment sql = new SQLFragment("SELECT peptide_id,peptide_group_id FROM "+tInfo+" WHERE UPPER(peptide_group_id) = ? AND peptide_id = ?");
        sql.add(src.getPeptide_group_id().toUpperCase());
        sql.add(src.getPeptide_id());
        Source dbSrc ;
        Source[] dbSrcs = new SqlSelector(PeptideSchema.getInstance().getSchema(), sql).getArray(Source.class);
        if (dbSrcs.length > 0)
            return null;
        else
            dbSrc = Table.insert(user,tInfo,src);
        return dbSrc;
    }

    public static PeptidePool insertPeptidePool(User u,PeptidePool pPool) throws Exception
    {
        TableInfo tInfo = PeptideSchema.getInstance().getTableInfoPeptidePools();
        PeptidePool dbPool;
        dbPool = Table.insert(u,tInfo,pPool);
        return dbPool;
    }

    public static PeptidePoolAssignment insertPeptidesInPool(User user,PeptidePoolAssignment src) throws Exception
    {
        TableInfo tInfo = PeptideSchema.getInstance().getTableInfoPoolAssignment();
        SimpleFilter sFilter = new SimpleFilter(FieldKey.fromParts("peptide_pool_id"), src.getPeptide_pool_id());
        sFilter.addCondition(FieldKey.fromParts("peptide_id"), src.getPeptide_id());
        PeptidePoolAssignment dbSrc ;
        PeptidePoolAssignment[] dbSrcs = new TableSelector(tInfo, sFilter, null).getArray(PeptidePoolAssignment.class);
        if (dbSrcs.length > 0)
            return null;
        else
            dbSrc = Table.insert(user,tInfo,src);
        return dbSrc;
    }

    public static Integer createPool(CreatePoolForm form, User user)
    {
        Object[] parameters = new Object[]{form.getMatrixId(),form.getPeptideGroup()};
        String sql = schema.getSqlDialect().execute(PeptideSchema.getInstance().getSchema(), "createpeppools", "?, ?");
        return new SqlExecutor(PeptideSchema.getInstance().getSchema()).execute(sql, parameters);
    }

    public static ParentChild[] getParents(String peptideId)
    {
        ParentChild[] parents = null;
        try
        {
            SimpleFilter sfilter = new SimpleFilter(FieldKey.fromParts("peptide_id"), Integer.parseInt(peptideId));
            TableInfo tInfo = PeptideSchema.getInstance().getTableInfoParent();
            parents = new TableSelector(tInfo,
                    tInfo.getColumns("peptide_id,linked_parent,parent_position"),
                    sfilter,
                    null).getArray(ParentChild.class);
            if (parents.length < 1)
                return null;
        }
        catch (NumberFormatException e)
        {
            log.error(e.getMessage(), e);
        }
        return parents;
    }

    public static ParentChild[] getChildren(String peptideId)
    {
        ParentChild[] children = null;
        try
        {
            SimpleFilter sfilter = new SimpleFilter(FieldKey.fromParts("linked_parent"), Integer.parseInt(peptideId));
            TableInfo tInfo = PeptideSchema.getInstance().getTableInfoParent();
            children = new TableSelector(tInfo, tInfo.getColumns("peptide_id,linked_parent,parent_position"), sfilter, null).getArray(ParentChild.class);
            if (children.length < 1)
                return null;
        }
        catch (NumberFormatException e)
        {
            log.error(e.getMessage(), e);
        }
        return children;
    }

    public static Source[] getSourcesForAPeptideId(String peptideId)
    {
        Source[] sources = null;
        try
        {
            SimpleFilter sfilter = new SimpleFilter(FieldKey.fromParts("peptide_id"), Integer.parseInt(peptideId));
            TableInfo tInfo = PeptideSchema.getInstance().getTableInfoSource();
            sources = new TableSelector(tInfo, tInfo.getColumns("peptide_id,peptide_group_id,btk_code,transmitted_status"), sfilter, null).getArray(Source.class);
            if (sources.length < 1)
                return null;
        }
        catch (NumberFormatException e)
        {
            log.error(e.getMessage(), e);
        }
        return sources;
    }

    public static ManuFactureStatus statusValue(String s)
    {
        TableInfo tInfo = schema.getTableInfoPeptideStatus();
        SimpleFilter sFilter = new SimpleFilter(FieldKey.fromParts("qc_passed"), s);
        return new TableSelector(tInfo, sFilter, null).getObject(ManuFactureStatus.class);
    }

    public static ManuFactureStatus[] getManufactureStatus()
    {
        return new TableSelector(schema.getTableInfoPeptideStatus()).getArray(ManuFactureStatus.class);
    }

    public static PeptideGroup insertGroup(Container c, User user, PeptideGroup pg) throws SQLException
    {
        GroupType groupType = getPeptideGroupType(pg.getGroup_type_id());
        PeptideGroup resultGroup = Table.insert(user,PeptideSchema.getInstance().getTableInfoPeptideGroups(), pg);
        if(groupType.getGroup_type_desc().equalsIgnoreCase("Autologous"))
        {
            GroupMetaData metaData = new GroupMetaData();
            metaData.setPeptide_group_id(pg.getPeptide_group_id());
            Table.insert(user, PeptideSchema.getInstance().getTableInfoGroupPatient(),metaData);
        }
        return resultGroup;
    }

    public static int updateMetaData(Container c,User user,GroupMetaData metaData)
    {
        java.sql.Timestamp date = new java.sql.Timestamp(System.currentTimeMillis());
        Integer visitNo = metaData.getVisit_no() == null || metaData.getVisit_no().toString().length() == 0 ? 0 :+metaData.getVisit_no();
        String ptid = metaData.getPtid() == null || metaData.getPtid().length() == 0 ? null : metaData.getPtid();
        String study = metaData.getStudy() == null || metaData.getStudy().length() == 0 ? null : metaData.getStudy();
        Date drawDate = metaData.getDraw_date() == null || metaData.getDraw_date().length() ==0 ? null :new Date(new java.util.Date(DateUtil.parseDateTime(metaData.getDraw_date())).getTime());
        String queryString = "UPDATE "+ PeptideSchema.getInstance().getTableInfoGroupPatient()+
                "\nSET ptid=?"+(drawDate != null ?",draw_date=?": "")+",study=?,visit_no=?,modified=?,modifiedby=?\n WHERE peptide_group_id=?";
        log.debug(queryString);
        Object[] params = null;
        if(drawDate != null)
            params =  new Object[] {ptid,drawDate,study,visitNo,date,user.getUserId(),metaData.getPeptide_group_id()};
        else
            params = new Object[] {ptid,study,visitNo,date,user.getUserId(),metaData.getPeptide_group_id()};
        return new SqlExecutor(PeptideSchema.getInstance().getSchema()).execute(queryString, params);
        //return Table.update(user, PeptideSchema.getInstance().getTableInfoGroupPatient(),metaData,metaData.getPeptide_group_id());
    }

    public static int insertMetaData(Container c,User user,GroupMetaData metaData)
    {
        java.sql.Timestamp date = new java.sql.Timestamp(System.currentTimeMillis());
        Integer visitNo = metaData.getVisit_no() == null || metaData.getVisit_no().toString().length() == 0 ? 0 :+metaData.getVisit_no();
        String ptid = metaData.getPtid() == null || metaData.getPtid().length() == 0 ? null : metaData.getPtid();
        String study = metaData.getStudy() == null || metaData.getStudy().length() == 0 ? null : metaData.getStudy();
        Date drawDate = metaData.getDraw_date() == null || metaData.getDraw_date().length() ==0 ? null : new Date(new java.util.Date(DateUtil.parseDateTime(metaData.getDraw_date())).getTime());
        String queryString = "INSERT INTO "+ PeptideSchema.getInstance().getTableInfoGroupPatient()+
                "(_ts,createdby,created,modified,modifiedby,peptide_group_id,ptid"+(drawDate != null ?",draw_date":"")+",study,visit_no)"+
                "VALUES(?,?,?,?,?,?,?"+(drawDate != null ?",?":"")+",?,?)";
        log.info(queryString);
        Object[] params = null;
        if(drawDate != null)
            params =  new Object[] {date,user.getUserId(),date,date,user.getUserId(),metaData.getPeptide_group_id(),ptid,drawDate,study,visitNo};
        else
            params = new Object[] {date,user.getUserId(),date,date,user.getUserId(),metaData.getPeptide_group_id(),ptid,study,visitNo};
        return new SqlExecutor(PeptideSchema.getInstance().getSchema()).execute(queryString, params);
    }

    public static PeptideGroup  updatePeptideGroup(Container c,User user,PeptideGroup pg) throws SQLException
    {
        return Table.update(user, PeptideSchema.getInstance().getTableInfoPeptideGroups(), pg, pg.getPeptide_group_id());
    }

    public static HashMap<String,Peptides> getPeptideSequenceMap()
    {
        HashMap<String,Peptides> peptideSequenceMap = new HashMap<>();
        Peptides[] peptides = getPeptides();
        for(Peptides peptide : peptides)
            peptideSequenceMap.put(peptide.getPeptide_sequence().trim().toUpperCase(), peptide);
        return peptideSequenceMap;
    }

    public static HashMap<Integer,Peptides> getPeptideIdMap()
    {
        HashMap<Integer,Peptides> peptideIdMap = new HashMap<>();
        Peptides[] peptides = getPeptides();
        for(Peptides peptide : peptides)
            peptideIdMap.put(peptide.getPeptide_id(), peptide);
        return peptideIdMap;
    }

    public static HashMap<String,PeptideGroup> getPeptideGroupMap()
    {
        HashMap<String,PeptideGroup> groupsMap = new HashMap<>();
        PeptideGroup[] peptideGroups = getPeptideGroups();
        for(PeptideGroup pGroup:peptideGroups)
            groupsMap.put(pGroup.getPeptide_group_id().trim().toUpperCase(),pGroup);
        return groupsMap;
    }

    public static HashMap<String,ProteinCategory> getProteinCatMap()
    {
        HashMap<String,ProteinCategory> proCatMap = new HashMap<>();
        ProteinCategory[] proCats = getProteinCategory();
        for(ProteinCategory proCat : proCats)
            proCatMap.put(proCat.getProtein_cat_desc().trim().toUpperCase(),proCat);
        return proCatMap;
    }

    public static HashMap<String,TransmittedStatus> getTransmittedMap()
    {
        HashMap<String,TransmittedStatus> transMap = new HashMap<>();
        TransmittedStatus[] trans = getTransmittedStatus();
        for(TransmittedStatus ts : trans)
            transMap.put(ts.getTransmitted_status().trim().toUpperCase(), ts);
        return transMap;
    }

    public static HashMap<Character,ManuFactureStatus> getStatusMap()
    {
        HashMap<Character,ManuFactureStatus> transMap = new HashMap<>();
        ManuFactureStatus[] trans = getManufactureStatus();
        for(ManuFactureStatus ts : trans)
            transMap.put(ts.getQc_passed(),ts);
        return transMap;
    }

    public static TransmittedStatus[] getTransmittedStatus()
    {
        return new TableSelector(schema.getTableInfoTransmittedStatus()).getArray(TransmittedStatus.class);
    }

    public static Peptides getPeptideById(Integer peptideId)
    {
        TableInfo tInfo = schema.getTableInfoPeptides();
        SimpleFilter sFilter = new SimpleFilter(PeptideSchema.COLUMN_PEPTIDE_ID,peptideId);
        return new TableSelector(tInfo, sFilter, null).getObject(Peptides.class);
    }

    public static PeptidePoolAssignment[] getNonMatrixPools() throws SQLException
    {
        String sql = "Select ppa.peptide_pool_id,ppa.peptide_id from peptide.peptide_pool_assignment ppa,peptide.peptide_pool pp \n"+
                " where ppa.peptide_pool_id = pp.peptide_pool_id and pp.pool_type != 'Matrix' order by ppa.peptide_pool_id,ppa.peptide_id";
        PeptidePoolAssignment[] pools = new SqlSelector(PeptideSchema.getInstance().getSchema(), sql).getArray(PeptidePoolAssignment.class);
        return pools;
    }

    public static PeptideGroup getPeptideGroupByID(String groupId)
    {
        TableInfo tInfo = schema.getTableInfoPeptideGroups();
        SimpleFilter sFilter = new SimpleFilter(PeptideSchema.COLUMN_PEPTIDE_GROUP_ID,groupId);
        return new TableSelector(tInfo, sFilter, null).getObject(PeptideGroup.class);
    }
}