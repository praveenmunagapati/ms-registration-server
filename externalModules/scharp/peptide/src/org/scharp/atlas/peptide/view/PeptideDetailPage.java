package org.scharp.atlas.peptide.view;

import org.labkey.api.jsp.JspBase;
import org.scharp.atlas.peptide.model.Peptide;

/**
 * Is the Peptide Detail page.
 *
 * @version $Id: PeptideDetailPage.java 26214 2013-05-02 02:49:04Z s.langley $
 */
abstract public class PeptideDetailPage extends JspBase
{

    private Peptide peptide;
    //private boolean modify;
    private String replicateId;

    public void setPeptide(Peptide peptide)
    {
        this.peptide = peptide;
    }

    public Peptide getPeptide()
    {
        return this.peptide;
    }

    /**
     * @return the modify
     */
    /*public boolean getModify()
    {
        return modify;
    }

    /**
     * @param modify the modify to set
     */
    /*public void setModify(boolean modify)
    {
        this.modify = modify;
    }*/

    public String getReplicateId()
    {
        return replicateId;
    }

    public void setReplicateId(String replicateId)
    {
        this.replicateId = replicateId;
    }
}
