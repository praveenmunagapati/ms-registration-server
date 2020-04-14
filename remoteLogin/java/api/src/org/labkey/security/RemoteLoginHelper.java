package org.labkey.security;

import java.util.Set;
import java.io.IOException;

/**
 * User: Mark Igra
 * Date: Sep 10, 2007
 *
 * This is the main API for using the Labkey remote login service.
 */
public interface RemoteLoginHelper
{

    /**
     * Check to see if the login/redirect process is complete. If not complete, clients MUST
     * must redirect to the location returned by getLoginRedirect before calling getPermissions
     *
     * @return boolean True if login is complete
     */
    boolean isLoginComplete();

    /**
     * Return the location to redirect to. Redirect should be sent via response.sendRedirect.
     * If user is not logged into labkey server, login UI will be shown before redirecting back with parameters.
     * If user is already logged into labkey server, no UI will be shown and redirect will be completed.
     * Note that sendRedirect needs to be called *before* any response data has been streamed back to the server,
     * so this method should not be called early in request processing (before response buffer is filled).
     *
     * @return String suitable for redirecting
     */
    String getLoginRedirect();

    /**
     * Throw away any cached information. isLoginComplete will return false after this.
     * @throws RemoteLoginException
     */
    void reset() throws RemoteLoginException;

    /**
     * Returns a set of permissions that the user has to the specified folder
     * isLoginComplete MUST return true before this method can be called
     *
     * @param labkeyFolderPath Path to the folder on the labkey server, not including server name or context path. e.g. /CHAVI/Studies/001
     * @return true if all permissions are satisfied
     */
    public Set<RemoteLogin.Permission> getPermissions(String labkeyFolderPath) throws IOException, RemoteLoginException;

    /**
     * @return email of the logged in user.
     */
    String getUserEmail();

}
