package org.scharp.atlas.peptide.model;

import java.util.List;
import java.util.Map;

/**
 * Maps to a row from the peptide_view DB view.
 *
 * @version $Id: Peptide.java 26214 2013-05-02 02:49:04Z s.langley $
 */
public class Peptide
{

    private String peptide_id;
    // private String btk_code;
    private String peptide_sequence;
    private String child;
    private boolean parent;
    private String qc_passed;
    private String lanl_date;
    private String protein_cat_desc;
    private Integer protein_cat_id;
    //private String linked_parent;
    private String protein_align_pep;
    private List<PeptideGroup> peptideGroups;
    private List<PeptidePool> peptidePools;

    public List<PeptidePool> getPeptidePools()
    {
        return peptidePools;
    }

    public void setPeptidePools(List<PeptidePool> peptidePools)
    {
        this.peptidePools = peptidePools;
    }

    /**
     * @return the protein_align_pep
     */
    public String getProtein_align_pep()
    {
        return protein_align_pep;
    }

    /**
     * @param protein_align_pep the protein_align_pep to set
     */
    public void setProtein_align_pep(String protein_align_pep)
    {
        this.protein_align_pep = protein_align_pep;
    }

    /**
     * @return the peptide_id
     */
    public String getPeptide_id()
    {
        return peptide_id;
    }

    /**
     * @param peptide_id the peptide_id to set
     */
    public void setPeptide_id(String peptide_id)
    {
        this.peptide_id = peptide_id;
    }

    /**
     * @return the btk_code
     */
//    public String getBtk_code() {
//        return btk_code;
//    }
    /**
     * @param btk_code the btk_code to set
     */
    /*    public void setBtk_code(String btk_code) {
        this.btk_code = btk_code;
    }*/
    /**
     * @return the peptide_sequence
     */
    public String getPeptide_sequence()
    {
        return peptide_sequence;
    }

    /**
     * @param peptide_sequence the peptide_sequence to set
     */
    public void setPeptide_sequence(String peptide_sequence)
    {
        this.peptide_sequence = peptide_sequence;
    }

    /**
     * @return the child
     */
    public String getChild()
    {
        return child;
    }

    /**
     * @param child the child to set
     */
    public void setChild(String child)
    {
        this.child = child;
    }

    /**
     * @return the qc_passed
     */
    public String getQc_passed()
    {
        return qc_passed;
    }

    /**
     * @param qc_passed the qc_passed to set
     */
    public void setQc_passed(String qc_passed)
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

    /**
     * @return the protein_cat_desc
     */
    public String getProtein_cat_desc()
    {
        return protein_cat_desc;
    }

    /**
     * @param protein_cat_desc the protein_cat_desc to set
     */
    public void setProtein_cat_desc(String protein_cat_desc)
    {
        this.protein_cat_desc = protein_cat_desc;
    }

    /**
     * @return the linked_parent
     */
    /*    public String getLinked_parent() {
        return linked_parent;
    }*/
    /**
     * linked_parent the linked_parent to set
     */
/*    public void setLinked_parent(String linked_parent) {
        this.linked_parent = linked_parent;
    }*/
    public Integer getProtein_cat_id()
    {
        return protein_cat_id;
    }

    public void setProtein_cat_id(Integer protein_cat_id)
    {
        this.protein_cat_id = protein_cat_id;
    }

    /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("Peptide[");
        sb.append("peptide_id:").append(this.getPeptide_id()).append(", ");
        //sb.append("btk_code:").append(this.getBtk_code()).append(", ");
        sb.append("peptide_sequence:").append(this.getPeptide_sequence()).append(", ");
        sb.append("protein_cat_desc:").append(this.getProtein_cat_desc()).append(", ");
        sb.append("protein_cat_id:").append(this.getProtein_cat_id()).append(", ");
        sb.append("child:").append(this.getChild()).append(", ");
        sb.append("lanl_date:").append(this.getLanl_date()).append(", ");
        sb.append("qc_passed:").append(this.getQc_passed()).append(", ");
        //sb.append("linked_parent:").append(this.getLinked_parent()).append(", ");
        sb.append("protein_align_pep:").append(this.getProtein_align_pep());
        if (getPeptideGroups() != null)
        {
            sb.append(", PeptideGroups(");
            for (PeptideGroup group : getPeptideGroups())
            {
                sb.append(group.getPeptide_group_id()).append(",");
            }
            sb.append(")");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * @return the peptideGroups
     */
    public List<PeptideGroup> getPeptideGroups()
    {
        return peptideGroups;
    }

    /**
     * @param peptideGroups the peptideGroups to set
     */
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

}
