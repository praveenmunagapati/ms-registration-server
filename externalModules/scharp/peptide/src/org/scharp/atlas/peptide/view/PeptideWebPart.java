package org.scharp.atlas.peptide.view;

import org.apache.log4j.Logger;
import org.labkey.api.view.JspView;
import org.scharp.atlas.peptide.PeptideManager;

import javax.servlet.ServletException;

/**
 * @version $Id$ 
 */
public class PeptideWebPart extends JspView<Object> {

    private static Logger _log = Logger.getLogger(PeptideWebPart.class);

    /**
     * 
     */
    public PeptideWebPart() {
        super("/org/scharp/atlas/peptide/view/peptideWebPart.jsp", null);
        setTitle("Peptide Web Part");
    }

    /* (non-Javadoc)
     * @see org.labkey.api.view.WebPartView#prepareWebPart(java.lang.Object)
     */
    @Override
    protected void prepareWebPart(Object object) throws ServletException {
        super.prepareWebPart(object);
        getViewContext().put("peptides", PeptideManager.getPeptideGroups());
    }
}
