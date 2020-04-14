package org.scharp.atlas.specimentracking;

import org.apache.log4j.Logger;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.security.User;

import java.sql.SQLException;

/**
 * User: sravani
 * Date: Mar 12, 2009
 * Time: 11:33:06 AM
 * To change this template use File | Settings | File Templates.
 */
public class SpecimentrackingContainerListener extends ContainerManager.AbstractContainerListener
{
    private static final Logger _log = Logger.getLogger(SpecimentrackingContainerListener.class);

    public void containerDeleted(Container c, User user)
    {
        try
        {
             SpecimentrackingManager.getInstance().deleteAllData(c);
        }
        catch (SQLException e)
        {
            // ignore any failures.
            _log.error("Failure cleaning up manifest data when deleting container " + c.getPath(), e);
        }
    }
}
