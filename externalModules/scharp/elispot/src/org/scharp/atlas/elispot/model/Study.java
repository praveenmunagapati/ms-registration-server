package org.scharp.atlas.elispot.model;

import org.labkey.api.data.Entity;
import org.labkey.api.data.Container;
import org.scharp.atlas.elispot.EliSpotManager;

import java.util.HashMap;

/**
 * Represents a row from the tblstudy DB table in the elispotplatedata schema
 *
 * @version $Id: Study.java 21843 2008-05-22 20:42:54Z sravani $
 */
public class Study extends Entity {
    private Integer study_seq_id;
    private String study_description;
    private String network_organization;
    private String study_identifier;
    private String protocol;
    private String status;
    private boolean plateinfo_reqd;

    public String getStudy_identifier() {
        return study_identifier;
    }

    public void setStudy_identifier(String study_identifier) {
        this.study_identifier = study_identifier;
    }

    /**
     * @return the network_organization
     */
    public String getNetwork_organization() {
        return network_organization;
    }

    public Study() {
    }

    /**
     * @param network_organization the network_organization to set
     */
    public void setNetwork_organization(String network_organization) {
        this.network_organization = network_organization;
    }
    /**
     * @return the study_seq_id
     */

    public Integer getStudy_seq_id() {
        return study_seq_id;
    }

    public void setStudy_seq_id(Integer study_seq_id) {
        this.study_seq_id = study_seq_id;
    }

    /**
     * @return the study_description
     */
    public String getStudy_description() {
        return study_description;
    }
    /**
     * @param study_description the study_description to set
     */
    public void setStudy_description(String study_description) {
        this.study_description = study_description;
    }
    /**
     * @return the protocol
     */
    public String getProtocol() {
        return protocol;
    }
    /**
     * @param protocol the protocol to set
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }


    public String getStatus() { 
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isPlateinfo_reqd()
    {
        return plateinfo_reqd;
    }

    public void setPlateinfo_reqd(boolean plateinfo_reqd)
    {
        this.plateinfo_reqd = plateinfo_reqd;
    }
}
