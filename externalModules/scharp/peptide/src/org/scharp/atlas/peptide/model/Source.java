package org.scharp.atlas.peptide.model;

import org.labkey.api.data.Entity;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Jun 21, 2007
 * Time: 11:49:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class Source extends Entity
{
    private Integer peptide_id;
    private String peptide_group_id;
    private String btk_code;
    private String transmitted_status;

    public Source()
    {
    }

    public Integer getPeptide_id()
    {
        return peptide_id;
    }

    public void setPeptide_id(Integer peptide_id)
    {
        this.peptide_id = peptide_id;
    }

    public String getPeptide_group_id()
    {
        return peptide_group_id;
    }

    public void setPeptide_group_id(String peptide_group_id)
    {
        this.peptide_group_id = peptide_group_id;
    }

    public String getBtk_code()
    {
        return btk_code;
    }

    public void setBtk_code(String btk_code)
    {
        this.btk_code = btk_code;
    }

    public String getTransmitted_status()
    {
        return transmitted_status;
    }

    public void setTransmitted_status(String transmitted_status)
    {
        this.transmitted_status = transmitted_status;
    }
}
