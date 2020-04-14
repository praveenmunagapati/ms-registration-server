package org.scharp.atlas.peptide.model;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Aug 11, 2008
 * Time: 11:26:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class ManuFactureStatus
{
    private Character qc_passed;
    private String  description;

    public Character getQc_passed()
    {
        return qc_passed;
    }

    public void setQc_passed(Character qc_passed)
    {
        this.qc_passed = qc_passed;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
}
