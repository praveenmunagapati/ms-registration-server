package org.labkey.tools.ldapsync;

import java.util.*;

/**
 * User: marki
 * Date: Jan 28, 2010
 * Time: 2:09:14 PM
 */
public class GroupInfo {
    private static Map<String, GroupInfo> groupMap = new HashMap<String,GroupInfo>();

    private GroupInfo(String name)
    {
        this.name = name;
    }

    public static GroupInfo get(String name)
    {
        return groupMap.get(name);
    }

    public static Collection<GroupInfo> getAll()
    {
        return groupMap.values();
    }

    public static GroupInfo addLDAP(String name, List<String> members)
    {
        GroupInfo g = getOrCreate(name);
        g.setLDAPUsers(new HashSet(members));

        return g;
    }

    public static GroupInfo addLabkey(String name, List<String> members, int userId)
    {
        GroupInfo g = getOrCreate(name);
        g.setLabkeyUsers(new HashSet<String>(members));
        g.setLabkeyUserId(userId);
        
        return g;
    }

    private static GroupInfo getOrCreate(String name)
    {
        GroupInfo g = get(name);
        if (null == g)
        {
            g = new GroupInfo(name);
            groupMap.put(name, g);
        }

        return g;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLabkeyUserId() {
        return labkeyUserId;
    }

    public void setLabkeyUserId(Integer labkeyUserId) {
        this.labkeyUserId = labkeyUserId;
    }

    public Set<String> getLabkeyUsers() {
        return labkeyUsers;
    }

    public void setLabkeyUsers(Set<String> labkeyUsers) {
        this.labkeyUsers = labkeyUsers;
    }

    public Set<String> getLdapUsers() {
        return ldapUsers;
    }

    public void setLDAPUsers(Set<String> ldapUsers) {
        this.ldapUsers = ldapUsers;
    }

    private String name;
    private Integer labkeyUserId;
    private Set<String> labkeyUsers;
    private Set<String> ldapUsers;


}
