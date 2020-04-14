package org.scharp.atlas.peptide.model;

import org.labkey.api.data.Entity;

import java.util.Date;


public class PeptideGroup extends Entity
{
    private  String peptide_group_id;
    private Integer pathogen_id;
    private String seq_ref;
    private  String seq_source;
    private Integer clade_id;
    private String pep_align_ref;
    private String pep_align_source;
    private String peptide_set;
    private Integer group_type_id;
    private String btk_code;
    private String transmitted_status;
    public String getTransmitted_status()
    {
        return transmitted_status;
    }

    public void setTransmitted_status(String transmitted_status)
    {
        this.transmitted_status = transmitted_status;
    }

    public String getBtk_code()
    {
        return btk_code;
    }

    public void setBtk_code(String btk_code)
    {
        this.btk_code = btk_code;
    }

    public PeptideGroup() {
    }

    public Integer getPathogen_id() {
        return pathogen_id;
    }

    public void setPathogen_id(Integer pathogen_id) {
        this.pathogen_id = pathogen_id;
    }

    public String getSeq_source() {
        return seq_source;
    }

    public void setSeq_source(String seq_source) {
        this.seq_source = seq_source;
    }

    public String getPeptide_group_id() {
        return peptide_group_id;
    }

    public void setPeptide_group_id(String peptide_group_id) {
        this.peptide_group_id = peptide_group_id;
    }

    public String getSeq_ref()
    {
        return seq_ref;
    }

    public void setSeq_ref(String seq_ref)
    {
        this.seq_ref = seq_ref;
    }

    public Integer getClade_id()
    {
        return clade_id;
    }

    public void setClade_id(Integer clade_id)
    {
        this.clade_id = clade_id;
    }

    public String getPep_align_ref()
    {
        return pep_align_ref;
    }

    public void setPep_align_ref(String pep_align_ref)
    {
        this.pep_align_ref = pep_align_ref;
    }

    public String getPep_align_source()
    {
        return pep_align_source;
    }

    public void setPep_align_source(String pep_align_source)
    {
        this.pep_align_source = pep_align_source;
    }

    public String getPeptide_set()
    {
        return peptide_set;
    }

    public void setPeptide_set(String peptide_set)
    {
        this.peptide_set = peptide_set;
    }

    public Integer getGroup_type_id()
    {
        return group_type_id;
    }

    public void setGroup_type_id(Integer group_type_id)
    {
        this.group_type_id = group_type_id;
    }
}
