package org.scharp.atlas.elispot.model;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: May 16, 2008
 * Time: 9:30:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class PlateType
{
    private Integer platetype_seq_id;
    private String platetype_desc;

    public PlateType()
    {
    }

    public Integer getPlatetype_seq_id()
    {
        return platetype_seq_id;
    }

    public void setPlatetype_seq_id(Integer platetype_seq_id)
    {
        this.platetype_seq_id = platetype_seq_id;
    }

    public String getPlatetype_desc()
    {
        return platetype_desc;
    }

    public void setPlatetype_desc(String platetype_desc)
    {
        this.platetype_desc = platetype_desc;
    }
}
