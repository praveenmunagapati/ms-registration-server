package org.scharp.atlas.elispot.model;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Aug 10, 2007
 * Time: 10:10:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class CellCounter {
    private Integer counter_seq_id;
    private String counter_desc;

    public CellCounter() {
    }

    public Integer getCounter_seq_id() {
        return counter_seq_id;
    }

    public void setCounter_seq_id(Integer counter_seq_id) {
        this.counter_seq_id = counter_seq_id;
    }

    public String getCounter_desc() {
        return counter_desc;
    }

    public void setCounter_desc(String counter_desc) {
        this.counter_desc = counter_desc;
    }
}
