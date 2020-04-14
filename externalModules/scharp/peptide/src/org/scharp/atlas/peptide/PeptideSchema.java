package org.scharp.atlas.peptide;

import org.labkey.api.data.DbSchema;
import org.labkey.api.data.DbSchemaType;
import org.labkey.api.data.TableInfo;
import org.labkey.api.data.dialect.SqlDialect;

/**
 * Singleton for storing DB information
 *
 * @version $Id$
 */
public class PeptideSchema
{

    private static PeptideSchema _instance   = null;
    private static final String  SCHEMA_NAME = "peptide";

    public static final String TABLE_PEPTIDE_GROUP     = "peptide_group";
    public static final String TABLE_GROUP_TYPE        = "group_type";
    public static final String TABLE_PROTEIN_CATEGORY  = "protein_category";
    public static final String TABLE_GROUP_PATIENT     = "group_patient";
    public static final String TABLE_PEPTIDES          = "peptides";
    public static final String TABLE_PEPTIDE_POOL          = "peptide_pool";
    public static final String TABLE_INPUT_ERROR        = "lanl_input_error";
    public static final String TABLE_POOL_ASSIGNMENT        = "peptide_pool_assignment";
    public static final String TABLE_MATRICES = "matrices";
    public static final String TABLE_SOURCE = "source";
    public static final String TABLE_PATHOGEN = "pathogen";
    public static final String TABLE_REPLICATE_HISTORY = "replicate_history";
    public static final String TABLE_PARENT = "parent";
    public static final String TABLE_PEPTIDE_STATUS = "peptide_status";
    public static final String TABLE_TRANSMITTED_STATUS = "transmitted_status";

    public static final String VIEW_GROUP_PEPTIDES     = "group_peptides";
    public static final String VIEW_PEPTIDES           = "peptide_view";
    public static final String VIEW_POOL_PEPTIDES     = "pool_peptides";
    public static final String VIEW_POOL_DETAILS = "pool_details";

    public static final String COLUMN_PEPTIDE_GROUP_ID = "peptide_group_id";
    public static final String COLUMN_SORT_SEQUENCE    = "sort_sequence";
    public static final String COLUMN_PROTEIN_CAT_ID   = "protein_cat_id";
    public static final String COLUMN_GROUP_TYPE_ID    = "group_type_id";
    public static final String COLUMN_PEPTIDE_ID       = "peptide_id";
    public static final String COLUMN_PEPTIDE_SEQUENCE       = "peptide_sequence";
    public static final String COLUMN_PEPTIDE_POOL_ID       = "peptide_pool_id";
    public static final String COLUMN_BTK_CODE      = "btk_code";
    public static final String COLUMN_QC_PASSED ="qc_passed";
    public static final String COLUMN_MATRIX_ID ="matrix_id";
    public static final String COLUMN_HISTORY_ID = "history_id";
    public static final String SEQUENCE_PEPTIDE_TABLE = SCHEMA_NAME + ".peptides_peptide_id_seq";


    /**
     * Singleton
     *
     * @return the only instance allowed of this class
     */
    public synchronized static PeptideSchema getInstance()
    {
        if (_instance == null)
            _instance = new PeptideSchema();

        return _instance;
    }

    private PeptideSchema()
    {
        // private contructor to prevent instantiation from
        // outside this class.
    }

    public String getSchemaName()
    {
        return SCHEMA_NAME;
    }

    public DbSchema getSchema()
    {
        return DbSchema.get(SCHEMA_NAME, DbSchemaType.Module);
    }

    public TableInfo getTableInfoParent()
    {
        return getSchema().getTable(PeptideSchema.TABLE_PARENT);
    }

    public TableInfo getTableInfoPeptideGroups()
    {
        return getSchema().getTable(PeptideSchema.TABLE_PEPTIDE_GROUP);
    }

    public TableInfo getTableInfoPeptideGroupTypes()
    {
        return getSchema().getTable(PeptideSchema.TABLE_GROUP_TYPE);
    }

    public TableInfo getTableInfoGroupPatient()
    {
        return getSchema().getTable(PeptideSchema.TABLE_GROUP_PATIENT);
    }

    public TableInfo getTableInfoPeptides()
    {
        return getSchema().getTable(PeptideSchema.TABLE_PEPTIDES);
    }

    public TableInfo getTableInfoProteinCat()
    {
        return getSchema().getTable(PeptideSchema.TABLE_PROTEIN_CATEGORY);
    }

    public TableInfo getTableInfoViewGroupPeptides()
    {
        return getSchema().getTable(PeptideSchema.VIEW_GROUP_PEPTIDES);
    }

    public TableInfo getTableInfoViewPeptides()
    {
        return getSchema().getTable(PeptideSchema.VIEW_PEPTIDES);
    }

    public TableInfo getTableInfoPeptidePools()
    {
        return getSchema().getTable(PeptideSchema.TABLE_PEPTIDE_POOL);
    }
    public TableInfo getTableInfoPoolAssignment() {
        return getSchema().getTable(PeptideSchema.TABLE_POOL_ASSIGNMENT);
    }
    public TableInfo getTableInfoViewPoolPeptides()
    {
        return getSchema().getTable(PeptideSchema.VIEW_POOL_PEPTIDES);
    }

    public TableInfo getTableInfoInputError()
    {
        return getSchema().getTable(PeptideSchema.TABLE_INPUT_ERROR);
    }

    public TableInfo getTableInfoMatrices()
    {
        return getSchema().getTable(PeptideSchema.TABLE_MATRICES);
    }

    public TableInfo getTableInfoSource()
    {
        return getSchema().getTable(PeptideSchema.TABLE_SOURCE);
    }

    public TableInfo getTableInfoPathogen()
    {
        return getSchema().getTable(PeptideSchema.TABLE_PATHOGEN);
    }
    public SqlDialect getSqlDialect() 
    {
        return getSchema().getSqlDialect();
    }

    public TableInfo getTableInfoViewPoolDetails()
    {
        return getSchema().getTable(PeptideSchema.VIEW_POOL_DETAILS);
    }

    public TableInfo getTableInfoReplicate()
    {
        return getSchema().getTable(PeptideSchema.TABLE_REPLICATE_HISTORY);
    }
    public TableInfo getTableInfoPeptideStatus()
    {
        return getSchema().getTable(PeptideSchema.TABLE_PEPTIDE_STATUS);
    }

    public TableInfo getTableInfoTransmittedStatus()
    {
        return getSchema().getTable(PeptideSchema.TABLE_TRANSMITTED_STATUS);
    }
}
