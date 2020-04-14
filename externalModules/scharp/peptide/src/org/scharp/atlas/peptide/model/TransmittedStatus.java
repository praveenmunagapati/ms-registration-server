package org.scharp.atlas.peptide.model;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Jul 16, 2009
 * Time: 12:01:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class TransmittedStatus
{
    private String transmitted_status;
    private String description;

    public String getTransmitted_status()
    {
        return transmitted_status;
    }

    public void setTransmitted_status(String transmitted_status)
    {
        this.transmitted_status = transmitted_status;
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
