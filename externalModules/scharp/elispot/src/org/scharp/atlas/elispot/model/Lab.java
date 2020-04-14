package org.scharp.atlas.elispot.model;

import org.labkey.api.data.Entity;

/**
 * Represents a Lab, this should be a intersection of Atlas Container (ie CHAVI) and
 * a locally define id in the Elispotplatedata schema
 *
 */
public class Lab extends Entity {
    private Integer lab_seq_id;
    private String lab_desc;
    private String permgroupname;
    /**
     * @return the labId
     */


    public Lab() {
    }

    public Integer getLab_seq_id() {
        return lab_seq_id;
    }

    public void setLab_seq_id(Integer lab_seq_id) {
        this.lab_seq_id = lab_seq_id;
    }

    public String getLab_desc() {
        return lab_desc;
    }

    public void setLab_desc(String lab_desc) {
        this.lab_desc = lab_desc;
    }

    public String getPermgroupname() {
        return permgroupname;
    }

    public void setPermgroupname(String permgroupname) {
        this.permgroupname = permgroupname;
    }
}
