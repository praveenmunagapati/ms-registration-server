package org.scharp.atlas.elispot.model;

import org.labkey.api.data.Entity;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Jul 11, 2007
 * Time: 1:10:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class PTDetails extends Entity {
    private String spec_well_group;
    private String antigen_id;
    private String friendly_name;
    private Integer replicate;
    private float pepconc;
    private String pepunit;
    private String stcl;
    private String effector;
    private float stimconc;
    private Integer cellsperwell;
    private String well_id;
    private Integer template_seq_id;
    private String blinded_name;

    public PTDetails() {
    }


    public String getSpec_well_group() {
        return spec_well_group;
    }

    public void setSpec_well_group(String spec_well_group) {
        this.spec_well_group = spec_well_group;
    }

    public String getAntigen_id() {
        return antigen_id;
    }

    public void setAntigen_id(String antigen_id) {
        this.antigen_id = antigen_id;
    }

    public String getFriendly_name() {
        return friendly_name;
    }

    public void setFriendly_name(String friendly_name) {
        this.friendly_name = friendly_name;
    }

    public Integer getReplicate() {
        return replicate;
    }

    public void setReplicate(Integer replicate) {
        this.replicate = replicate;
    }

    public float getPepconc() {
        return pepconc;
    }

    public void setPepconc(float pepconc) {
        this.pepconc = pepconc;
    }

    public String getPepunit() {
        return pepunit;
    }

    public void setPepunit(String pepunit) {
        this.pepunit = pepunit;
    }

    public String getStcl() {
        return stcl;
    }

    public void setStcl(String stcl) {
        this.stcl = stcl;
    }

    public String getEffector() {
        return effector;
    }

    public void setEffector(String effector) {
        this.effector = effector;
    }

    public float getStimconc() {
        return stimconc;
    }

    public void setStimconc(float stimconc) {
        this.stimconc = stimconc;
    }

    public Integer getCellsperwell() {
        return cellsperwell;
    }

    public void setCellsperwell(Integer cellsperwell) {
        this.cellsperwell = cellsperwell;
    }

    public String getWell_id() {
        return well_id;
    }

    public void setWell_id(String well_id) {
        this.well_id = well_id;
    }

    public Integer getTemplate_seq_id() {
        return template_seq_id;
    }

    public void setTemplate_seq_id(Integer template_seq_id) {
        this.template_seq_id = template_seq_id;
    }

    public String getBlinded_name()
    {
        return blinded_name;
    }

    public void setBlinded_name(String blinded_name)
    {
        this.blinded_name = blinded_name;
    }
}
