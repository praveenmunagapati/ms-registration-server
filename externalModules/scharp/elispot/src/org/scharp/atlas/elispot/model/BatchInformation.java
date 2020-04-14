package org.scharp.atlas.elispot.model;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Aug 9, 2007
 * Time: 2:38:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class BatchInformation {
    private Integer batch_seq_id;
    private String batch_description;
    private Integer reader_seq_id;
    private Integer lab_study_seq_id;
    private String lab_desc;
    private String study_description;
    private String study_identifier;
    private Integer study_seq_id;
    private Integer lab_seq_id;

    public BatchInformation() {
    }

    public Integer getBatch_seq_id() {
        return batch_seq_id;
    }

    public void setBatch_seq_id(Integer batch_seq_id) {
        this.batch_seq_id = batch_seq_id;
    }

    public String getBatch_description() {
        return batch_description;
    }

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

    public String getLab_desc() {
        return lab_desc;
    }

    public void setLab_desc(String lab_desc) {
        this.lab_desc = lab_desc;
    }


    public String getStudy_description() {
        return study_description;
    }

    public void setStudy_description(String study_description) {
        this.study_description = study_description;
    }

    public Integer getStudy_seq_id() {
        return study_seq_id;
    }

    public void setStudy_seq_id(Integer study_seq_id) {
        this.study_seq_id = study_seq_id;
    }

    public Integer getLab_seq_id() {
        return lab_seq_id;
    }

    public void setLab_seq_id(Integer lab_seq_id) {
        this.lab_seq_id = lab_seq_id;
    }

    public String getStudy_identifier() {
        return study_identifier;
    }

    public void setStudy_identifier(String study_identifier) {
        this.study_identifier = study_identifier;
    }
}
