package org.scharp.atlas.elispot;

import org.jetbrains.annotations.NotNull;
import org.labkey.api.data.ContainerManager.ContainerListener;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;
import org.apache.log4j.Logger;

import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.Collections;

/**
 * User: sravani
 * Date: Mar 12, 2009
 * Time: 3:01:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class ElispotContainerListener implements ContainerListener
{
    private static final Logger _log = Logger.getLogger(ElispotContainerListener.class);

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
