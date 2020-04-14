package org.scharp.atlas.peptide.model;

import org.labkey.api.data.Entity;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: slangley
 * Date: Nov 21, 2007
 * Time: 1:18:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class PeptideManufactureStatus extends Entity
{
    // Should default to New
    private String qc_passed;// = "n";
    private String description;// = "New";

    private static HashMap<String, String> lookupTable = new HashMap<String, String>();

    static
    {
        lookupTable.put("n", "New");
        lookupTable.put("o", "Ordered");
        lookupTable.put("s", "Success");
        lookupTable.put("f", "Failed");
        lookupTable.put("t", "Terminated");
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getQc_passed()
    {
        return qc_passed;
    }

    public void setQc_passed(String qc_passed)
    {
        this.qc_passed = qc_passed;
    }

    public static String InterpretManufactureStatus(String qc_passed)
    {
        return lookupTable.get(qc_passed);
    }
}
