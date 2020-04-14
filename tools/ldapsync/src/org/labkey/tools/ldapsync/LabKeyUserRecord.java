package org.labkey.tools.ldapsync;

import java.util.HashMap;
import java.util.Map;

/**
 * User: marki
 * Date: Mar 3, 2010
 * Time: 2:49:08 PM
 */
public class LabKeyUserRecord extends HashMap<String, Object> implements UserRecord {
    private Integer userId;

    public LabKeyUserRecord(Map<String,Object> m) {
        super(m);
        this.userId = ((Number) m.get("UserId")).intValue();
    }

    public LabKeyUserRecord(String email, Integer userId)
    {
        set(UserField.Email, email);
        this.userId = userId;
    }

    public String getUserField(UserField field)
    {
        return (String) get(field.getLabKeyName());
    }

    public void set(UserField field, String value)
    {
        put(field.getLabKeyName(), value);
    }

    public Integer getUserId()
    {
        return userId;
    }

    public boolean updateFromLDAP(LDAPUserRecord ldapRecord)
    {
        boolean updateRequired = false;
        for (UserField uf : UserField.values())
        {
            String val = ldapRecord.getUserField(uf);
            if (null != val && !val.equals(getUserField(uf)))
            {
                set(uf, val);
                updateRequired = true;
            }
        }

        return updateRequired;
    }
}
