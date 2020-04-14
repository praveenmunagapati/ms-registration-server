package org.scharp.atlas.elispot.model;

import org.labkey.api.data.Entity;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Jul 11, 2007
 * Time: 12:42:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlateTemplate extends Entity {
  private String template_description;
    private Integer num_well_groups_per_plate;
    private boolean stimulated;
    private Float incubate;
    private String readout;
    private Integer template_seq_id;
    private Integer study_seq_id;
    private boolean bool_use_blinded_name;

    public PlateTemplate() {
    }

    public String getTemplate_description() {
        return template_description;
    }

    public void setTemplate_description(String template_description) {
        this.template_description = template_description;
    }

    public Integer getNum_well_groups_per_plate() {
        return num_well_groups_per_plate;
    }

    public void setNum_well_groups_per_plate(Integer num_well_groups_per_plate) {
        this.num_well_groups_per_plate = num_well_groups_per_plate;
    }

    public boolean isStimulated() {
        return stimulated;
    }

    public void setStimulated(boolean stimulated) {
        this.stimulated = stimulated;
    }

    public Float getIncubate() {
        return incubate;
    }

    public void setIncubate(Float incubate) {
        this.incubate = incubate;
    }

    public String getReadout() {
        return readout;
    }

    public void setReadout(String readout) {
        this.readout = readout;
    }

    public Integer getTemplate_seq_id() {
        return template_seq_id;
    }

    public void setTemplate_seq_id(Integer template_seq_id) {
        this.template_seq_id = template_seq_id;
    }

    public Integer getStudy_seq_id() {
        return study_seq_id;
    }

    public void setStudy_seq_id(Integer study_seq_id) {
        this.study_seq_id = study_seq_id;
    }

    public boolean isBool_use_blinded_name()
    {
        return bool_use_blinded_name;
    }

    public void setBool_use_blinded_name(boolean bool_use_blinded_name)
    {
        this.bool_use_blinded_name = bool_use_blinded_name;
    }
}
