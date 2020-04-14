package org.scharp.atlas.elispot.model;

import org.labkey.api.data.Entity;

import java.sql.Date;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Jul 11, 2007
 * Time: 12:55:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class Specimen extends Entity {
    private Integer specimen_seq_id;
    private String ptid;
    private Float visit_no;
    private Date draw_date;
    private Integer study_seq_id;

    public Specimen() {
    }


    public Integer getSpecimen_seq_id() {
        return specimen_seq_id;
    }

    public void setSpecimen_seq_id(Integer specimen_seq_id) {
        this.specimen_seq_id = specimen_seq_id;
    }

    public String getPtid() {
        return ptid;
    }

    public void setPtid(String ptid) {
        this.ptid = ptid;
    }

    public Float getVisit_no() {
        return visit_no;
    }

    public void setVisit_no(Float visit_no) {
        this.visit_no = visit_no;
    }

    public Date getDraw_date() {
        return draw_date;
    }

    public void setDraw_date(Date draw_date) {
        this.draw_date = draw_date;
    }

    public Integer getStudy_seq_id() {
        return study_seq_id;
    }

    public void setStudy_seq_id(Integer study_seq_id) {
        this.study_seq_id = study_seq_id;
    }
}
