package org.scharp.atlas.elispot.model;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: May 16, 2008
 * Time: 9:28:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class Substrate
{
    private Integer substrate_seq_id;
    private String substrate_desc;

    public Substrate()
    {
    }

    public Integer getSubstrate_seq_id()
    {
        return substrate_seq_id;
    }

    public void setSubstrate_seq_id(Integer substrate_seq_id)
    {
        this.substrate_seq_id = substrate_seq_id;
    }

    public String getSubstrate_desc()
    {
        return substrate_desc;
    }

    public void setSubstrate_desc(String substrate_desc)
    {
        this.substrate_desc = substrate_desc;
    }
}
