package org.labkey.tools.ldapsync;

/**
 * User: marki
 * Date: Mar 5, 2010
 * Time: 4:21:49 PM
 */
public enum UserField {
    DisplayName("uid", "DisplayName", "displayName"),
    FirstName("givenName", "FirstName", "firstName"),
    LastName("sn", "LastName", "lastName"),
    Description("title", "Description", "description"),
    Email("mail", "Email", null);
    
    private final String _ldapName, _lkName, _formName;
    UserField(String ldapName, String lkName, String formName)
    {
        _ldapName = ldapName;
        _lkName = lkName;
        _formName = formName;
    }

    public String getLDAPName()
    {
        return _ldapName;
    }
    public String getLabKeyName()
    {
        return _lkName;
    }
    public String getFormName()
    {
        return _formName;
    }

}
