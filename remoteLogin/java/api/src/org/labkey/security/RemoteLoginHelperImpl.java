package org.labkey.security;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.net.URLEncoder;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.Enumeration;
import java.util.Set;
import java.util.HashSet;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.InputStream;

/**
 * User: Mark Igra
 * Date: Sep 10, 2007
 * Time: 5:18:59 PM
 */
class RemoteLoginHelperImpl implements RemoteLoginHelper
{
    private LoginInfo loginInfo;
    private HttpServletRequest request;
    private static final int TIMEOUT = 5000;

    RemoteLoginHelperImpl(HttpServletRequest request, LoginInfo loginInfo) {
        this.request = request;
        this.loginInfo = loginInfo;
        if (null == this.loginInfo.getToken() && null != request.getParameter(RemoteLogin.TOKEN_PARAM))
        {
            loginInfo.setToken(request.getParameter(RemoteLogin.TOKEN_PARAM));
            loginInfo.setEmail(request.getParameter(RemoteLogin.EMAIL_PARAM));
        }
    }

    public boolean isLoginComplete() {
        return null != loginInfo.getToken();
    }

    public String getLoginRedirect() {
        try {
            //If we have NO login information, need to redirect to the labkey server
            if (null == loginInfo.getToken()) {
                StringBuffer returnUrl = request.getRequestURL();
                if (null != request.getQueryString())
                    returnUrl.append("?").append(request.getQueryString());
                return loginInfo.getServerBaseUrl() + "/Login/createToken.view?returnUrl=" + URLEncoder.encode(returnUrl.toString(), "UTF-8");
            }
            else {
                //Redirect back to current URL with all parameters except token && email
                StringBuffer url = request.getRequestURL();
                String sep = "?";
                Enumeration paramNames = request.getParameterNames();
                while (paramNames.hasMoreElements()) {
                    String paramName = (String) paramNames.nextElement();
                    if (RemoteLogin.TOKEN_PARAM.equals(paramName) || RemoteLogin.EMAIL_PARAM.equals(paramName))
                        continue;

                    url.append(sep);
                    for (String paramVal : request.getParameterValues(paramName))
                        url.append(paramName).append("=").append(URLEncoder.encode(paramVal, "UTF-8"));
                    sep = "&";
                }
                return url.toString();
            }
        } catch (UnsupportedEncodingException x) { //UTF-8 is always supported
            throw new RuntimeException(x);
        }
    }

    public void reset() throws RemoteLoginException
    {
        URL url;
        String urlString = loginInfo.getServerBaseUrl() + "/Login/invalidateToken.view?" + RemoteLogin.TOKEN_PARAM + "=" + loginInfo.getToken();
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            throw new RemoteLoginException("Bad URL: " + urlString, e);
        }

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(TIMEOUT);
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new RemoteLoginException("Bad response from server: " + responseCode + " " + connection.getResponseMessage());
            }
        } catch (IOException e) {
            throw new RemoteLoginException("IOException connecting to url: " + urlString, e);
        }
        finally
        {
            if (null != connection)
                connection.disconnect();
        }

        loginInfo.setToken(null);
        loginInfo.getPermissionMap().clear();
    }

    public Set<RemoteLogin.Permission> getPermissions(String labkeyFolderPath) throws IOException, RemoteLoginException
    {
        if (null == loginInfo.getToken())
            throw new IllegalStateException("Can't get permissions without checking login state first.");

        if (!labkeyFolderPath.startsWith("/"))
            labkeyFolderPath = "/" + labkeyFolderPath;
        if (labkeyFolderPath.endsWith("/"))
            labkeyFolderPath = labkeyFolderPath.substring(0, labkeyFolderPath.length() - 1);

        Set<RemoteLogin.Permission> permissions = loginInfo.getPermissionMap().get(labkeyFolderPath);
        if (null != permissions)
            return permissions;

        String urlString = loginInfo.getServerBaseUrl() + "/Login" + labkeyFolderPath + "/verifyToken.view?" + RemoteLogin.TOKEN_PARAM + "=" + loginInfo.getToken();
            Document doc = getUrlContent(urlString);
            //<TokenAuthentication success="true" token="token" email="email" permissions="bits">
            Element tokenElt = doc.getDocumentElement();
            String successStr = tokenElt.getAttribute("success");
            if (Boolean.parseBoolean(successStr)) {
                int bits = Integer.parseInt(tokenElt.getAttribute("permissions"));
                permissions = new HashSet<RemoteLogin.Permission>();
                for (RemoteLogin.Permission perm : RemoteLogin.Permission.values()) {
                    if ((perm.getBits() & bits) == perm.getBits())
                        permissions.add(perm);
                }
                loginInfo.getPermissionMap().put(labkeyFolderPath, permissions);
            }
            else {
                throw new IllegalStateException("Permission check failed with message: " + tokenElt.getAttribute("message"));
            }

        return permissions;
    }

    public String getUserEmail() {
        return loginInfo.getEmail();
    }

    public String getSecurityToken() {
        return loginInfo.getToken();
    }

    private Document getUrlContent(String urlStr) throws RemoteLoginException
    {
        URL url = null;
        HttpURLConnection connection = null;
        try {
            url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(TIMEOUT);
        } catch(MalformedURLException e){
            throw new RemoteLoginException("Malformed URL: " + urlStr, e);
        } catch (IOException e) {
            throw new RemoteLoginException("IOException connecting to url: " + urlStr, e);
        }

        InputStream in = null;
        try {
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
                in = connection.getInputStream();
                Document doc = domBuilder.parse(in);
                in.close();
                in = null;

                return doc;
            }
            else
                throw new RemoteLoginException("Response code: " + responseCode + " " + connection.getResponseMessage());
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        } catch (SAXException e) {
            throw new RemoteLoginException("Failed parsing XML response.", e);
        } catch (IOException e) {
            throw new RemoteLoginException("IOException connecting to url " + url.toString(), e);
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            connection.disconnect();
        }
    }
}
