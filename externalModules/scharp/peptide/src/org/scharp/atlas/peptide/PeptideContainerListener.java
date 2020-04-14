package org.scharp.atlas.peptide;

import org.jetbrains.annotations.NotNull;
import org.labkey.api.data.ContainerManager.ContainerListener;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;
import org.apache.log4j.Logger;

import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Mar 16, 2009
 * Time: 10:18:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class PeptideContainerListener implements ContainerListener
{

    private static final Logger _log = Logger.getLogger(PeptideContainerListener.class);

    public void containerCreated(Container c, User user)
    {
    }

    public void containerDeleted(Container c, User user)
    {
    }

    @Override
    public void containerMoved(Container c, Container oldParent, User user)
    {
    }

    @NotNull
    @Override
    public Collection<String> canMove(Container c, Container newParent, User user)
    {
        return Collections.emptyList();
    }

    public void propertyChange(PropertyChangeEvent evt)
       {
       }    

}
