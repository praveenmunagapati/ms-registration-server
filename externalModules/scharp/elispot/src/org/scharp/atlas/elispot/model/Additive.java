package org.scharp.atlas.elispot.model;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Aug 8, 2007
 * Time: 1:05:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class Additive {
    private Integer additive_seq_id;
    private String additive_desc;

    public Additive() {
    }

    public Integer getAdditive_seq_id() {
        return additive_seq_id;
    }

    public void setAdditive_seq_id(Integer additive_seq_id) {
        this.additive_seq_id = additive_seq_id;
    }

    public String getAdditive_desc() {
        return additive_desc;
    }

    public void setAdditive_desc(String additive_desc) {
        this.additive_desc = additive_desc;
    }
}
