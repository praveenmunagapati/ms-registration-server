package org.scharp.atlas.peptide.model;

import org.labkey.api.data.Entity;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Jul 12, 2007
 * Time: 1:11:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class Peptides extends Entity
{
    private Integer peptide_id;
    private String btk_code;
    private String protein_align_pep;
    private String peptide_sequence;
    private Integer sort_sequence;
    private Integer protein_cat_id;
    private boolean child;
    private boolean parent;
    private Character qc_passed;
    private String lanl_date;
    private String src_file_name;
    private List<PeptideGroup> peptideGroups;
    private ProteinCategory proteinCat;
    private Pathogen pathogen;
    private Parent parentPep;
    private List<Parent> parents;

    public List<Parent> getParents()
    {
        return parents;
    }

    public void setParents(List<Parent> parents)
    {
        this.parents = parents;
    }


    public Parent getParentPep()
    {
        return parentPep;
    }

    public void setParentPep(Parent parentPep)
    {
        this.parentPep = parentPep;
    }

    public ProteinCategory getProteinCat()
    {
        return proteinCat;
    }

    public void setProteinCat(ProteinCategory proteinCat)
    {
        this.proteinCat = proteinCat;
    }

    public Pathogen getPathogen()
    {
        return pathogen;
    }

    public void setPathogen(Pathogen pathogen)
    {
        this.pathogen = pathogen;
    }

    public Integer getPeptide_id()
    {
        return peptide_id;
    }

    public void setPeptide_id(Integer peptide_id)
    {
        this.peptide_id = peptide_id;
    }

    public String getBtk_code()
    {
        return btk_code;
    }

    public void setBtk_code(String btk_code)
    {
        this.btk_code = btk_code;
    }

    public String getProtein_align_pep()
    {
        return protein_align_pep;
    }

    public void setProtein_align_pep(String protein_align_pep)
    {
        this.protein_align_pep = protein_align_pep;
    }

    public String getPeptide_sequence()
    {
        return peptide_sequence;
    }

    public void setPeptide_sequence(String peptide_sequence)
    {
        this.peptide_sequence = peptide_sequence;
    }

    public Integer getSort_sequence()
    {
        return sort_sequence;
    }

    public void setSort_sequence(Integer sort_sequence)
    {
        this.sort_sequence = sort_sequence;
    }

    public Integer getProtein_cat_id()
    {
        return protein_cat_id;
    }

    public void setProtein_cat_id(Integer protein_cat_id)
    {
        this.protein_cat_id = protein_cat_id;
    }

    public boolean isChild()
    {
        return child;
    }

    public void setChild(boolean child)
    {
        this.child = child;
    }

    public Character getQc_passed()
    {
        return qc_passed;
    }

    public void setQc_passed(Character qc_passed)
    {
        this.qc_passed = qc_passed;
    }

    public String getLanl_date()
    {
        return lanl_date;
    }

    public void setLanl_date(String lanl_date)
    {
        this.lanl_date = lanl_date;
    }

    public List<PeptideGroup> getPeptideGroups()
    {
        return peptideGroups;
    }

    public void setPeptideGroups(List<PeptideGroup> peptideGroups)
    {
        this.peptideGroups = peptideGroups;
    }

    public boolean isParent()
    {
        return parent;
    }

    public void setParent(boolean parent)
    {
        this.parent = parent;
    }

    public String getSrc_file_name()
    {
        return src_file_name;
    }

    public void setSrc_file_name(String src_file_name)
    {
        this.src_file_name = src_file_name;
    }
}
