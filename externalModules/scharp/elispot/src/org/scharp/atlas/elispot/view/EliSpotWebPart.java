package org.scharp.atlas.elispot.view;


import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.labkey.api.view.JspView;
import org.scharp.atlas.elispot.EliSpotController;


public class EliSpotWebPart extends JspView<Object> {

    private static Logger log = Logger.getLogger(EliSpotWebPart.class);

    /**
     *
     */
    public EliSpotWebPart() {
        super(EliSpotController.JSP_PAGE_MAIN, null);
        setTitle("EliSpot Web Part");
    }

    /* (non-Javadoc)
     * @see org.labkey.api.view.WebPartView#prepareWebPart(java.lang.Object)
     */
    @Override
    protected void prepareWebPart(Object object) throws ServletException {
        super.prepareWebPart(object);
    }

}
