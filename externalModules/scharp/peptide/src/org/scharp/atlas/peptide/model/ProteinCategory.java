package org.scharp.atlas.peptide.model;

import org.labkey.api.data.Entity;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Jan 9, 2007
 * Time: 1:34:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProteinCategory extends Entity
{
    private Integer protein_cat_id;
    private String protein_cat_desc;
    private String protein_cat_mnem;
    private Integer protein_sort_value;

    public ProteinCategory()
    {
    }

    public ProteinCategory(Integer protein_cat_id, String protein_cat_desc, String protein_cat_mnem, Integer protein_sort_value)
    {
        this.protein_cat_id = protein_cat_id;
        this.protein_cat_desc = protein_cat_desc;
        this.protein_cat_mnem = protein_cat_mnem;
        this.protein_sort_value = protein_sort_value;
    }

    public Integer getProtein_cat_id()
    {
        return protein_cat_id;
    }

    public void setProtein_cat_id(Integer protein_cat_id)
    {
        this.protein_cat_id = protein_cat_id;
    }

    public String getProtein_cat_desc()
    {
        return protein_cat_desc;
    }

    public void setProtein_cat_desc(String protein_cat_desc)
    {
        this.protein_cat_desc = protein_cat_desc;
    }

    public String getProtein_cat_mnem()
    {
        return protein_cat_mnem;
    }

    public void setProtein_cat_mnem(String protein_cat_mnem)
    {
        this.protein_cat_mnem = protein_cat_mnem;
    }

    public Integer getProtein_sort_value()
    {
        return protein_sort_value;
    }

    public void setProtein_sort_value(Integer protein_sort_value)
    {
        this.protein_sort_value = protein_sort_value;
    }
}
