package org.scharp.atlas.elispot.model;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Aug 7, 2007
 * Time: 9:28:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class Cryostatus {
    private Integer cryostatus;
    private String cryostatus_desc;

    public Cryostatus() {
    }

    public Integer getCryostatus() {
        return cryostatus;
    }

    public void setCryostatus(Integer cryostatus) {
        this.cryostatus = cryostatus;
    }

    public String getCryostatus_desc() {
        return cryostatus_desc;
    }

    public void setCryostatus_desc(String cryostatus_desc) {
        this.cryostatus_desc = cryostatus_desc;
    }
}
