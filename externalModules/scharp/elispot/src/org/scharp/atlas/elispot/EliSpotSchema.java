package org.scharp.atlas.elispot;

import org.labkey.api.data.DbSchema;
import org.labkey.api.data.DbSchemaType;
import org.labkey.api.data.TableInfo;
import org.labkey.api.data.dialect.SqlDialect;

/**
 * Represents the Elispotplatedata Schema.
 *
 */
public class  EliSpotSchema
{
    private static final String SCHEMA_NAME   = "elispot";

    // Table Names
    public static final String TABLE_STUDIES = "tblstudy";
    public static final String TABLE_LABS = "tbllabs";
    public static final String TABLE_STUDY_LABS = "tblstudylabs";
    public static final String TABLE_BATCH_TYPE = "tblbatchtype";
    public static final String TABLE_BATCH  = "tblbatch";
    public static final String TABLE_PLATE = "tblplate";
    public static final String TABLE_PLATE_TEMPLATE = "tblplatetemplate";
    public static final String TABLE_SPECIMEN = "tblspecimen";
    public static final String VIEW_PLATE = "plateinformation" ;
    public static final String VIEW_BATCH = "batchinformation" ;
     public static final String TABLE_PLATE_SPECIMENS = "tblplatespecimens";
    public static final String TABLE_PLATETEMPLATE_DETAILS = "tblplatetemplatedetails";
    public static final String TABLE_READER = "tblreaders";
    public static final String TABLE_CRYOSTATUS ="tblcryostatus";
    public static final String TABLE_ADDTIIVE ="tbladditive";
    public static final String TABLE_PLATEDATA = "tblplatedata";
    public static final String TABLE_PLATEMAP = "tblplatemap";
    public static final String TABLE_CELLCOUNTER ="tblcellcounter";
    public static final String TABLE_SUBSTRATE ="tblsubstrate";
    public static final String TABLE_PLATETYPE="tblplatetype";
    // Column Names
    public static final String COLUMN_CONTAINER      = "container";
    public static final String COLUMN_STUDY_SEQ_ID      = "study_seq_id";
    public static final String COLUMN_STUDY_NETWORK_ORG = "network_organization";
    public static final String COLUMN_STUDY_DESC        = "study_description";
    public static final String COLUMN_STUDY_PROTOCOL    = "protocol";
    public static final String COLUMN_READER_SEQ_ID    = "reader_seq_id";

    public static final String COLUMN_BATCH_SEQ_ID         = "batch_seq_id";
    public static final String COLUMN_BATCH_LAB_ID         = "lab_id";
    public static final String COLUMN_BATCH_TYPE           = "batch_type";
    public static final String COLUMN_BATCH_STUDY_SEQ_ID   = "study_seq_id";
    public static final String COLUMN_LABSTUDY_SEQ_ID ="lab_study_seq_id";
    public static final String COLUMN_PLATE_SEQ_ID ="plate_seq_id";
    public static final String COLUMN_BATCH_DESCRIPTION    = "batch_description";


    private static EliSpotSchema _instance = null;

    public static synchronized EliSpotSchema getInstance()
    {
        if (null == _instance)
            _instance = new EliSpotSchema();

        return _instance;
    }

    private EliSpotSchema()
    {
        // private contructor to prevent instantiation from
        // outside this class: this singleton should only be
        // accessed via cpas.elispot.EliSpotSchema.getInstance()
    }

    public String getSchemaName()
    {
        return SCHEMA_NAME;
    }

    public DbSchema getSchema()
    {
        return DbSchema.get(SCHEMA_NAME, DbSchemaType.Module);
    }

    public TableInfo getTableInfoStudies() {
        return getSchema().getTable(EliSpotSchema.TABLE_STUDIES);
    }
    public TableInfo getTableInfoLabs() {
        return getSchema().getTable(EliSpotSchema.TABLE_LABS);
    }
    public TableInfo getTableInfostudyLabs() {
        return getSchema().getTable(EliSpotSchema.TABLE_STUDY_LABS);
    }
    public TableInfo getTableInfoBatchType() {
        return getSchema().getTable(EliSpotSchema.TABLE_BATCH_TYPE);
    }
    public TableInfo getTableInfoPlate()
    {
      return getSchema().getTable(EliSpotSchema.TABLE_PLATE);  
    }

    public TableInfo getTableInfoBatch() {
        return getSchema().getTable(EliSpotSchema.TABLE_BATCH);
    }
     public TableInfo getTableInfoPlateInfo() {
        return getSchema().getTable(EliSpotSchema.VIEW_PLATE);
    }
    public TableInfo getTableInfoBatchInfo() {
        return getSchema().getTable(EliSpotSchema.VIEW_BATCH);
    }
    public TableInfo getTableInfoPlateTemplate() {
        return getSchema().getTable(EliSpotSchema.TABLE_PLATE_TEMPLATE);
    }
    public TableInfo getTableInfoSpecimen() {
        return getSchema().getTable(EliSpotSchema.TABLE_SPECIMEN);
    }
    public TableInfo getTableInfoPlateSpecimens() {
        return getSchema().getTable(EliSpotSchema.TABLE_PLATE_SPECIMENS);
    }
     public TableInfo getTableInfoPTDetails() {
        return getSchema().getTable(EliSpotSchema.TABLE_PLATETEMPLATE_DETAILS);
    }
    public TableInfo getTableInfoReader() {
        return getSchema().getTable(EliSpotSchema.TABLE_READER);
    }
    public TableInfo getTableInfoCryos(){
        return getSchema().getTable(EliSpotSchema.TABLE_CRYOSTATUS);
    }
     public TableInfo getTableInfoAdditive(){
        return getSchema().getTable(EliSpotSchema.TABLE_ADDTIIVE);
    }
     public TableInfo getTableInfoCellcounter(){
        return getSchema().getTable(EliSpotSchema.TABLE_CELLCOUNTER);
    }
    public TableInfo getTableInfoPlateData(){
        return getSchema().getTable(EliSpotSchema.TABLE_PLATEDATA);
    }
    public TableInfo getTableInfoPlateMap(){
        return getSchema().getTable(EliSpotSchema.TABLE_PLATEMAP);
    }
    public TableInfo getTableInfoSubstrate(){
        return getSchema().getTable(EliSpotSchema.TABLE_SUBSTRATE);
    }
    public TableInfo getTableInfoPlateType(){
        return getSchema().getTable(EliSpotSchema.TABLE_PLATETYPE);
    }
}
