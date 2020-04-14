package org.scharp.atlas.peptide.model;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Jan 9, 2008
 * Time: 7:56:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class ReplicateHistory
{
    private Integer history_id;
    private Integer peptide_id;
    private Integer master_peptide_id;

    public Integer getHistory_id()
    {
        return history_id;
    }

    public void setHistory_id(Integer history_id)
    {
        this.history_id = history_id;
    }

    public Integer getPeptide_id()
    {
        return peptide_id;
    }

    public void setPeptide_id(Integer peptide_id)
    {
        this.peptide_id = peptide_id;
    }

    public Integer getMaster_peptide_id()
    {
        return master_peptide_id;
    }

    public void setMaster_peptide_id(Integer master_peptide_id)
    {
        this.master_peptide_id = master_peptide_id;
    }
}
