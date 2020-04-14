package org.scharp.atlas.peptide.model;

import org.labkey.api.data.Entity;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Nov 5, 2007
 * Time: 9:01:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class PeptidePoolAssignment extends Entity
{
   private Integer peptide_pool_id;
    private Integer peptide_id;
    private boolean peptide_in_pool;

    public Integer getPeptide_pool_id()
    {
        return peptide_pool_id;
    }

    public void setPeptide_pool_id(Integer peptide_pool_id)
    {
        this.peptide_pool_id = peptide_pool_id;
    }

    public Integer getPeptide_id()
    {
        return peptide_id;
    }

    public void setPeptide_id(Integer peptide_id)
    {
        this.peptide_id = peptide_id;
    }

    public boolean isPeptide_in_pool()
    {
        return peptide_in_pool;
    }

    public void setPeptide_in_pool(boolean peptide_in_pool)
    {
        this.peptide_in_pool = peptide_in_pool;
    }
}
