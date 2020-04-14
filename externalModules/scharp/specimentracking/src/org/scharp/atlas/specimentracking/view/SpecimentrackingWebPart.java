package org.scharp.atlas.specimentracking.view;

import org.apache.log4j.Logger;
import org.labkey.api.view.JspView;
import org.labkey.api.view.ViewContext;
import org.scharp.atlas.specimentracking.SpecimentrackingManager;

import javax.servlet.ServletException;

public class SpecimentrackingWebPart extends JspView<Object>
{
    static Logger _log = Logger.getLogger(SpecimentrackingWebPart.class);

    public SpecimentrackingWebPart()
    {
        super("/org/scharp/atlas/specimentracking/view/specimentrackingWebPart.jsp", null);
        setTitle("Specimentracking Web Part");
    }

    @Override
    protected void prepareWebPart(Object object) throws ServletException
    {
        ViewContext context = getViewContext();
        super.prepareWebPart(context);
        context.put("manifests", SpecimentrackingManager.getInstance().getManifests(context.getContainer(),context.getUser()));
    }
}
