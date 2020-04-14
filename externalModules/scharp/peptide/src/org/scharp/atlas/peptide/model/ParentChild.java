package org.scharp.atlas.peptide.model;

import org.scharp.atlas.peptide.PeptideController;

/**
 * Created by IntelliJ IDEA.
 * User: slangley
 * Date: Jan 11, 2008
 * Time: 1:23:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class ParentChild
{
    private String peptide_id;
    private String linked_parent;
    private String parent_position;

    public String getPeptide_id()
    {
        return peptide_id;
    }

    public void setPeptide_id(String peptide_id)
    {
        this.peptide_id = PeptideController.toLZ(peptide_id);
    }

    public String getLinked_parent()
    {
        return linked_parent;
    }

    public void setLinked_parent(String linked_parent)
    {
        this.linked_parent = PeptideController.toLZ(linked_parent);
    }

    public String getParent_position()
    {
        return parent_position;
    }

    public void setParent_position(String parent_position)
    {
        this.parent_position = parent_position;
    }
}
