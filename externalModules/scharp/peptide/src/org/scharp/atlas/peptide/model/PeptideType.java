package org.scharp.atlas.peptide.model;

import org.labkey.api.data.Entity;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Jan 4, 2007
 * Time: 2:49:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class PeptideType extends Entity {
    private int pep_type_id;
    private String pep_type_desc;

    public PeptideType() {
    }


    public PeptideType(int pep_type_id, String pep_type_desc) {
        this.pep_type_id = pep_type_id;
        this.pep_type_desc = pep_type_desc;
    }

    public int getPep_type_id() {
        return pep_type_id;
    }

    public void setPep_type_id(int pep_type_id) {
        this.pep_type_id = pep_type_id;
    }

    public String getPep_type_desc() {
        return pep_type_desc;
    }

    public void setPep_type_desc(String pep_type_desc) {
        this.pep_type_desc = pep_type_desc;
    }
}
