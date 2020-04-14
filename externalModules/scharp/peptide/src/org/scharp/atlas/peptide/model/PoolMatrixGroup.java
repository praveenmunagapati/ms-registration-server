package org.scharp.atlas.peptide.model;

import org.labkey.api.data.Entity;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Nov 5, 2007
 * Time: 8:58:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class PoolMatrixGroup extends Entity
{
    private Integer peptide_pool_id;
    private String matrix_pool_id;
    private String peptide_group_id;

    public Integer getPeptide_pool_id()
    {
        return peptide_pool_id;
    }

    public void setPeptide_pool_id(Integer peptide_pool_id)
    {
        this.peptide_pool_id = peptide_pool_id;
    }

    public String getMatrix_pool_id()
    {
        return matrix_pool_id;
    }

    public void setMatrix_pool_id(String matrix_pool_id)
    {
        this.matrix_pool_id = matrix_pool_id;
    }

    public String getPeptide_group_id()
    {
        return peptide_group_id;
    }

    public void setPeptide_group_id(String peptide_group_id)
    {
        this.peptide_group_id = peptide_group_id;
    }
}
