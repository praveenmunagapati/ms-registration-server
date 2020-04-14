package org.scharp.atlas.elispot.model;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Jun 1, 2007
 * Time: 3:36:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlateInformation {
    private String plate_seq_id;
    private String friendly_name;
    private String blinded_name;
    private String spec_well_group;
    private String replicate;
    private String final_well_id;
    private String text_sfu;
    private boolean bool_use_blinded_name;
    public PlateInformation() {
    }

    public PlateInformation(String plate_seq_id, String friendly_name, String blinded_name, String spec_well_group, String replicate, String final_well_id, String text_sfu, boolean bool_use_blinded_name)
    {
        this.plate_seq_id = plate_seq_id;
        this.friendly_name = friendly_name;
        this.blinded_name = blinded_name;
        this.spec_well_group = spec_well_group;
        this.replicate = replicate;
        this.final_well_id = final_well_id;
        this.text_sfu = text_sfu;
        this.bool_use_blinded_name = bool_use_blinded_name;
    }

    public String getFriendly_name() {
        return friendly_name;
    }

    public void setFriendly_name(String friendly_name) {
        this.friendly_name = friendly_name;
    }

    public String getSpec_well_group() {
        return spec_well_group;
    }

    public void setSpec_well_group(String spec_well_group) {
        this.spec_well_group = spec_well_group;
    }

    public String getReplicate() {
        return replicate;
    }

    public void setReplicate(String replicate) {
        this.replicate = replicate;
    }

    public String getFinal_well_id() {
        return final_well_id;
    }

    public void setFinal_well_id(String final_well_id) {
        this.final_well_id = final_well_id;
    }

    public String getText_sfu() {
        return text_sfu;
    }

    public void setText_sfu(String text_sfu) {
        this.text_sfu = text_sfu;
    }

    public String getPlate_seq_id() {
        return plate_seq_id;
    }

    public void setPlate_seq_id(String plate_seq_id) {
        this.plate_seq_id = plate_seq_id;
    }

    public String getBlinded_name()
    {
        return blinded_name;
    }

    public void setBlinded_name(String blinded_name)
    {
        this.blinded_name = blinded_name;
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
