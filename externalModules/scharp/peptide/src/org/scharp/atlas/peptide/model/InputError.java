package org.scharp.atlas.peptide.model;

import org.labkey.api.data.Entity;
/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Jun 11, 2007
 * Time: 3:27:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class InputError extends Entity{
    private Integer record_id;
    private String input_row;
    private String reason;

    public InputError() {
    }

    public InputError(Integer record_id, String input_row, String reason) {
        this.record_id = record_id;
        this.input_row = input_row;
        this.reason = reason;
    }

    public Integer getRecord_id() {
        return record_id;
    }

    public void setRecord_id(Integer record_id) {
        this.record_id = record_id;
    }

    public String getInput_row() {
        return input_row;
    }

    public void setInput_row(String input_row) {
        this.input_row = input_row;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
