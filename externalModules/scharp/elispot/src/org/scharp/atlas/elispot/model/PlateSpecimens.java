package org.scharp.atlas.elispot.model;

import org.labkey.api.data.Entity;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Jul 11, 2007
 * Time: 12:30:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlateSpecimens extends Entity {
    private String spec_well_group;
    private String specimen_id;
    private boolean bool_report_specimen;
    private Integer runnum;
    private Integer additive_seq_id;
    private Integer cryostatus;
    private Integer plate_seq_id;
    private String d1_cellcount ;
    private String d2_cellcount;
    private String d1_viability;
    private String d2_viability;
    private Integer counter_seq_id;
    private Integer specimen_seq_id;

    public Integer getSpecimen_seq_id() {
        return specimen_seq_id;
    }

    public void setSpecimen_seq_id(Integer specimen_seq_id) {
        this.specimen_seq_id = specimen_seq_id;
    }

    public Integer getCounter_seq_id() {
        return counter_seq_id;
    }

    public void setCounter_seq_id(Integer counter_seq_id) {
        this.counter_seq_id = counter_seq_id;
    }

    public PlateSpecimens() {
    }


    public String getSpec_well_group() {
        return spec_well_group;
    }

    public void setSpec_well_group(String spec_well_group) {
        this.spec_well_group = spec_well_group;
    }

    public String getSpecimen_id() {
        return specimen_id;
    }

    public void setSpecimen_id(String specimen_id) {
        this.specimen_id = specimen_id;
    }

    public boolean isBool_report_specimen() {
        return bool_report_specimen;
    }

    public void setBool_report_specimen(boolean bool_report_specimen) {
        this.bool_report_specimen = bool_report_specimen;
    }

    public Integer getRunnum() {
        return runnum;
    }

    public void setRunnum(Integer runnum) {
        this.runnum = runnum;
    }

    public Integer getPlate_seq_id() {
        return plate_seq_id;
    }

    public void setPlate_seq_id(Integer plate_seq_id) {
        this.plate_seq_id = plate_seq_id;
    }


    public String getD1_cellcount() {
        return d1_cellcount;
    }

    public void setD1_cellcount(String d1_cellcount) {
        this.d1_cellcount = d1_cellcount;
    }

    public String getD2_cellcount() {
        return d2_cellcount;
    }

    public void setD2_cellcount(String d2_cellcount) {
        this.d2_cellcount = d2_cellcount;
    }

    public String getD1_viability() {
        return d1_viability;
    }

    public void setD1_viability(String d1_viability) {
        this.d1_viability = d1_viability;
    }

    public String getD2_viability() {
        return d2_viability;
    }

    public void setD2_viability(String d2_viability) {
        this.d2_viability = d2_viability;
    }

    public Integer getAdditive_seq_id() {
        return additive_seq_id;
    }

    public void setAdditive_seq_id(Integer additive_seq_id) {
        this.additive_seq_id = additive_seq_id;
    }

    public Integer getCryostatus() {
        return cryostatus;
    }

    public void setCryostatus(Integer cryostatus) {
        this.cryostatus = cryostatus;
    }
}
