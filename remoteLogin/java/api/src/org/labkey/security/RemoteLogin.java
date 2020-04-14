package org.labkey.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * User: Mark Igra
 * Date: Sep 10, 2007
 * Time: 1:33:19 PM
 */
public class RemoteLogin
{
    public static final String TOKEN_PARAM = "labkeyToken";
    public static final String EMAIL_PARAM = "labkeyEmail";

    /**
     * Returns a RemoteLoginHelper that can be used for connecting to and querying permissions
     * from the server. Note that this class will save login state in the session under the key labkey.logininfo.serverUrl
     * for each server used for remote login
     * @param request
     * @param labkeyServerUrl
     * @return
     */
    public static RemoteLoginHelper getHelper(HttpServletRequest request, String labkeyServerUrl) {
        HttpSession session = request.getSession(true);
        String sessionKey = "labkey.loginInfo." + labkeyServerUrl; //Could theoretically support remote login to several
        LoginInfo loginInfo = (LoginInfo) session.getAttribute(sessionKey);
        if (null == loginInfo) {
            loginInfo = new LoginInfo(labkeyServerUrl);
            session.setAttribute(sessionKey, loginInfo);
        }
        return new RemoteLoginHelperImpl(request, loginInfo);
    }

    public enum Permission
    {
        READ(0x00000001),
        INSERT(0x00000002),
        UPDATE(0x00000004),
        ADMIN(0x00008000);

        private int bits;

        int getBits() {
            return bits;
        }

        Permission(int bits) {
            this.bits = bits;
        }
    }

}
