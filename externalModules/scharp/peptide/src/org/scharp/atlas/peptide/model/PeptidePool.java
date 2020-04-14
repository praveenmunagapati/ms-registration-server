package org.scharp.atlas.peptide.model;

import org.labkey.api.data.DbScope;
import org.labkey.api.data.Entity;
import org.scharp.atlas.peptide.PeptideSchema;
import java.sql.Timestamp;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Jun 7, 2007
 * Time: 12:23:53 PM
 * Scott changed getDescription()
 */
public class PeptidePool extends Entity
{
    private Integer peptide_pool_id;
    private String comment;
    private Character exists;
    private Timestamp create_date;
    private PoolMatrixGroup pmGroup;
    private String description;
    private String pool_type;
    private String peptide_group_id;
    private String matrix_pool_id;
    private String matrix_id;
    private static Logger log = Logger.getLogger(PeptidePool.class);

    public PeptidePool()
    {
    }

    public PeptidePool(Integer peptide_pool_id, String description)
    {
        this.peptide_pool_id = peptide_pool_id;
        this.description = description;
    }

    public Integer getPeptide_pool_id()
    {
        return peptide_pool_id;
    }

    public void setPeptide_pool_id(Integer peptide_pool_id)
    {
        this.peptide_pool_id = peptide_pool_id;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getPool_type()
    {
        return pool_type;
    }

    public void setPool_type(String pool_type)
    {
        this.pool_type = pool_type;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public Character getExists()
    {
        return exists;
    }

    public void setExists(Character exists)
    {
        this.exists = exists;
    }

    public Timestamp getCreate_date()
    {
        return create_date;
    }

    public void setCreate_date(Timestamp create_date)
    {
        this.create_date = create_date;
    }

    public PoolMatrixGroup getPmGroup()
    {
        return pmGroup;
    }

    public void setPmGroup(PoolMatrixGroup pmGroup)
    {
        this.pmGroup = pmGroup;
    }
    public String getPeptide_group_id()
    {
        return peptide_group_id;
    }

    public void setPeptide_group_id(String peptide_group_id)
    {
        this.peptide_group_id = peptide_group_id;
    }

    public String getMatrix_id()
    {
        return matrix_id;
    }

    public void setMatrix_id(String matrix_id)
    {
        this.matrix_id = matrix_id;
    }

    public String getMatrix_pool_id()
    {
        return matrix_pool_id;
    }

    public void setMatrix_pool_id(String matrix_pool_id)
    {
        this.matrix_pool_id = matrix_pool_id;
    }
}
