package org.scharp.atlas.elispot.model;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Jul 30, 2007
 * Time: 12:12:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class BatchType {
    private Character batch_type;
    private String batch_type_desc;


    public BatchType() {
    }

    public Character getBatch_type() {
        return batch_type;
    }

    public void setBatch_type(Character batch_type) {
        this.batch_type = batch_type;
    }

    public String getBatch_type_desc() {
        return batch_type_desc;
    }

    public void setBatch_type_desc(String batch_type_desc) {
        this.batch_type_desc = batch_type_desc;
    }
}
