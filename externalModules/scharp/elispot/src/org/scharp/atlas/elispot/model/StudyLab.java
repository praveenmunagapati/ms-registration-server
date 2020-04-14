package org.scharp.atlas.elispot.model;

import org.labkey.api.data.Entity;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Jul 27, 2007
 * Time: 2:10:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class StudyLab extends Entity {
    private Integer lab_study_seq_id;
     private Integer study_seq_id;
    private Integer lab_seq_id;


    public StudyLab() {
    }

    public Integer getLab_seq_id() {
        return lab_seq_id;
    }

    public void setLab_seq_id(Integer lab_seq_id) {
        this.lab_seq_id = lab_seq_id;
    }

    public Integer getStudy_seq_id() {
        return study_seq_id;
    }

    public void setStudy_seq_id(Integer study_seq_id) {
        this.study_seq_id = study_seq_id;
    }

    public Integer getLab_study_seq_id() {
        return lab_study_seq_id;
    }

    public void setLab_study_seq_id(Integer lab_study_seq_id) {
        this.lab_study_seq_id = lab_study_seq_id;
    }
}
