package org.labkey.security;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * User: Mark Igra
 * Date: Sep 10, 2007
 * Time: 5:19:25 PM
 */
class LoginInfo
{
    private String token;
    private String email;
    private String serverBaseUrl;
    private Map<String, Set<RemoteLogin.Permission>> permissionMap;

    LoginInfo(String baseUrl) {
        this.serverBaseUrl = baseUrl;
        permissionMap = new HashMap<String, Set<RemoteLogin.Permission>>();
    }

    LoginInfo(String baseUrl, String token, String email) {
        this(baseUrl);
        this.token = token;
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getServerBaseUrl() {
        return serverBaseUrl;
    }

    public void setServerBaseUrl(String serverBaseUrl) {
        this.serverBaseUrl = serverBaseUrl;
    }

    public Map<String,Set<RemoteLogin.Permission>> getPermissionMap()
    {
        return permissionMap;
    }
}
