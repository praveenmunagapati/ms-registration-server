package org.scharp.atlas.specimentracking;

import org.labkey.api.data.DbSchema;
import org.labkey.api.data.TableInfo;
import org.labkey.api.data.dialect.SqlDialect;


/**
 * @author
 *
 */
public class SpecimentrackingSchema
{
    /**
     *
     */
    private static final String MANIFEST_SPECIMENS_TABLE = "ManifestSpecimens";
    /**
     *
     */
    private static final String MANIFESTS_TABLE = "Manifests";
    /**
     *
     */
    private static final String SCHEMA_NAME = "specimentracking";
    private static SpecimentrackingSchema _instance = null;

    /**
     * Singleton, this method needs to be synchronized
     * @return
     */
    public static synchronized SpecimentrackingSchema getInstance()
    {
        if (null == _instance)
            _instance = new SpecimentrackingSchema();

        return _instance;
    }

    private SpecimentrackingSchema()
    {
        // private contructor to prevent instantiation from
        // outside this class: this singleton should only be
        // accessed via cpas.specimentracking.SpecimentrackingSchema.getInstance()
    }

    /**
     * @return
     */
    public DbSchema getSchema()
    {
        return DbSchema.get(SCHEMA_NAME);
    }
    public DbSchema getStudySchema()
    {
        return DbSchema.get("study");
    }
    public TableInfo getTableInfoSite()
    {
        return getStudySchema().getTable("Site");
    }
    /**
     * @return
     */
    public SqlDialect getSqlDialect()
    {
        return getSchema().getSqlDialect();
    }
    /**
     * @return
     */
    public TableInfo getTableInfoManifests()
    {
        return getSchema().getTable(MANIFESTS_TABLE);
    }
    /**
     * @return
     */
    public TableInfo getTableInfoManifestSpecimens()
    {
        return getSchema().getTable(MANIFEST_SPECIMENS_TABLE);
    }
    /**
     * @return
     */
    public String getSchemaName()
    {
        return SCHEMA_NAME;
    }
}
