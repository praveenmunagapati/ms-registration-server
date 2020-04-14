package org.labkey.tools.ldapsync;

import java.util.List;

/**
 * User: marki
 * Date: Mar 3, 2010
 * Time: 2:19:29 PM
 */
public class LDAPUserRecord extends LDAPRecord implements UserRecord {
    public LDAPUserRecord() {
        super();
    }

    public LDAPUserRecord(LDAPRecord copyFrom)
    {
        super(copyFrom);
    }
    
    public String getEmail() {
        return getFirst("mail");
    }

    public String getEmail(String preferredDomain, boolean ignoreOtherDomains) {
        List<String> emails = get("mail");
        if (emails == null)
            return null;

        for (String email : emails)
            if (email.endsWith("@" + preferredDomain))
                return email;

        return ignoreOtherDomains ? null : emails.get(0);
    }

    public String getUserField(UserField userField) {
        return getFirst(userField.getLDAPName());
    }
}
