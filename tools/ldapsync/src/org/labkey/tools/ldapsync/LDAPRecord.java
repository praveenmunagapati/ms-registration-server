package org.labkey.tools.ldapsync;

import java.util.*;

/**
 * User: marki
 * Date: Jan 28, 2010
 * Time: 10:22:11 AM
 */
public class LDAPRecord extends HashMap<String, List<String>> {
    public LDAPRecord() {
        super();
    }

    public LDAPRecord(HashMap<String, List<String>> m)
    {
        super(m);
    }
    
    public void put(String key, String value)
    {
        List<String> result = get(key);
        if (null == result)
        {
            result = new ArrayList<String>();
            put(key, result);
        }
        result.add(value);
    }

    public String getFirst(String key)
    {
        List<String> result = get(key);
        if (null == result)
            return null;
        else
            return result.get(0);
    }
}
