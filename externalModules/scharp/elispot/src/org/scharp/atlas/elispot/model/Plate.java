package org.scharp.atlas.elispot.model;

import org.labkey.api.data.Entity;

import java.sql.Date;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Jul 11, 2007
 * Time: 9:04:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class Plate extends Entity {
    private Integer plate_seq_id;
    private String plate_name;
    private Integer template_seq_id;
    private Integer batch_seq_id;
    private Date import_date;
    private Date test_date;
    private String freezer_plate_id;
    private String tech_id;
    private String plate_filename;
    private boolean bool_report_plate;
    private int approved_by;
    private String comment;
    private String isprecoated;
    private Integer substrate_seq_id;
    private Integer platetype_seq_id;


    public Plate() {
    }

    public String getPlate_name() {
        return plate_name;
    }

    public void setPlate_name(String plate_name) {
        this.plate_name = plate_name;
    }

    public Date getImport_date() {
        return import_date;
    }

    public void setImport_date(Date import_date) {
        this.import_date = import_date;
    }

    public Date getTest_date() {
        return test_date;
    }

    public void setTest_date(Date test_date) {
        this.test_date = test_date;
    }
    public String getFreezer_plate_id() {
        return freezer_plate_id;
    }

    public void setFreezer_plate_id(String freezer_plate_id) {
        this.freezer_plate_id = freezer_plate_id;
    }

    public String getTech_id() {
        return tech_id;
    }

    public void setTech_id(String tech_id) {
        this.tech_id = tech_id;
    }

    public String getPlate_filename() {
        return plate_filename;
    }

    public void setPlate_filename(String plate_filename) {
        this.plate_filename = plate_filename;
    }

    public boolean isBool_report_plate() {
        return bool_report_plate;
    }

    public void setBool_report_plate(boolean bool_report_plate) {
        this.bool_report_plate = bool_report_plate;
    }

    public Integer getBatch_seq_id() {
        return batch_seq_id;
    }

    public void setBatch_seq_id(Integer batch_seq_id) {
        this.batch_seq_id = batch_seq_id;
    }

    public Integer getPlate_seq_id() {
        return plate_seq_id;
    }

    public void setPlate_seq_id(Integer plate_seq_id) {
        this.plate_seq_id = plate_seq_id;
    }

    public Integer getTemplate_seq_id() {
        return template_seq_id;
    }

    public void setTemplate_seq_id(Integer template_seq_id) {
        this.template_seq_id = template_seq_id;
    }

    public int getApproved_by() {
        return approved_by;
    }

    public void setApproved_by(int approved_by) {
        this.approved_by = approved_by;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
    public String getIsprecoated()
    {
        return isprecoated;
    }

    public void setIsprecoated(String isprecoated)
    {
        this.isprecoated = isprecoated;
    }

    public Integer getSubstrate_seq_id()
    {
        return substrate_seq_id;
    }

    public void setSubstrate_seq_id(Integer substrate_seq_id)
    {
        this.substrate_seq_id = substrate_seq_id;
    }

    public Integer getPlatetype_seq_id()
    {
        return platetype_seq_id;
    }

    public void setPlatetype_seq_id(Integer platetype_seq_id)
    {
        this.platetype_seq_id = platetype_seq_id;
    }
}
