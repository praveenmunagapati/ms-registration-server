package org.scharp.atlas.elispot.model;

import org.labkey.api.data.Entity;

/**
 * Represents a row from the tblBatch DB table
 *
 */
public class Batch extends Entity {

    private Character batch_type;
    private Integer reader_seq_id;
    private Integer batch_seq_id;
    private String batch_description;
    private Integer lab_study_seq_id;


    public Batch() {
    }

    public Integer getBatch_seq_id() {
        return batch_seq_id;
    }

    public void setBatch_seq_id(Integer batch_seq_id) {
        this.batch_seq_id = batch_seq_id;
    }
    /**
     * @return the batch_type
     */
    public Character getBatch_type() {
        return batch_type;
    }

    public void setBatch_type(Character batch_type) {
        this.batch_type = batch_type;
    }
    /**
     * @return the batch_description
     */
    public String getBatch_description() {
        return batch_description;
    }
    /**
     * @param batch_description the batch_description to set
     */
    public void setBatch_description(String batch_description) {
        this.batch_description = batch_description;
    }


    public Integer getReader_seq_id() {
        return reader_seq_id;
    }

    public void setReader_seq_id(Integer reader_seq_id) {
        this.reader_seq_id = reader_seq_id;
    }

    public Integer getLab_study_seq_id() {
        return lab_study_seq_id;
    }

    public void setLab_study_seq_id(Integer lab_study_seq_id) {
        this.lab_study_seq_id = lab_study_seq_id;
    }
}
