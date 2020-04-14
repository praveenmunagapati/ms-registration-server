package org.scharp.atlas.elispot.model;

import org.labkey.api.data.Entity;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Aug 7, 2007
 * Time: 12:00:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlateData extends Entity {
    private String well_id;
    private String text_sfu;
    private Integer sfu;
    private Integer plate_seq_id;

    public PlateData() {
    }

    public String getWell_id() {
        return well_id;
    }

    public void setWell_id(String well_id) {
        this.well_id = well_id;
    }

    public String getText_sfu() {
        return text_sfu;
    }

    public void setText_sfu(String text_sfu) {
        this.text_sfu = text_sfu;
    }

    public Integer getSfu() {
        return sfu;
    }

    public void setSfu(Integer sfu) {
        this.sfu = sfu;
    }

    public Integer getPlate_seq_id() {
        return plate_seq_id;
    }

    public void setPlate_seq_id(Integer plate_seq_id) {
        this.plate_seq_id = plate_seq_id;
    }
}
