package org.labkey.tools.ldapsync;

import org.apache.log4j.Logger;
import org.labkey.remoteapi.Connection;
import org.labkey.remoteapi.query.SelectRowsCommand;
import org.labkey.remoteapi.query.SelectRowsResponse;
import org.labkey.remoteapi.security.*;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: marki
 * Date: Jan 27, 2010
 * Time: 10:00:50 PM
 */
public class LDAPSync {
    private static Map<String, LabKeyUserRecord> labkeyUserMap = new HashMap<>();
    private static Map<String, LDAPUserRecord> ldapUserMap = new HashMap<>();
    private static String ldapDomain;

    private static final Logger LOG = Logger.getLogger(LDAPSync.class);

    public static void main(String[] params) throws Exception {
        LOG.info("Starting up LDAP sync - logger");
        System.out.println("Starting up LDAP sync - stdout direct");
        String user = System.getProperty("user");
        String pwd = System.getProperty("password");
        String server = System.getProperty("server");
        String suffix = System.getProperty("suffix", " (LDAP)");
        boolean cleanGroups = Boolean.parseBoolean(System.getProperty("cleanGroups", "true"));
        ldapDomain = System.getProperty("domain", "test.labkey.org");

        if (null == user)
        {
            System.console().printf("User Name: ");
            user = System.console().readLine();
        }

        if (null == pwd)
        {
            System.out.print("Password: ");
            char[] pwdChars = System.console().readPassword();
            if (null != pwdChars)
                pwd = new String(pwdChars);
        }

        if (null == server)
        {
            System.out.print("Server: ");
            server = System.console().readLine();
        }

        try {

            Connection c = new Connection(server, user, pwd);

            LOG.info("Reading users from LabKey Server.");
            SelectRowsCommand usersCommand = new SelectRowsCommand("core", "SiteUsers");
            usersCommand.setColumns(Arrays.asList("UserId", "Email", "DisplayName", "FirstName", "LastName", "Description"));
            SelectRowsResponse usersResponse = usersCommand.execute(c, "/");
            for (Map<String, Object> row : usersResponse.getRows()) {
                String email = (String) row.get("Email");
                labkeyUserMap.put(email, new LabKeyUserRecord(row));
            }

            LOG.info("Reading groups from LabKey Server.");
            SelectRowsCommand groupsCommand = new SelectRowsCommand("core", "groups");
            groupsCommand.setColumns(Arrays.asList("UserId", "Name"));
            SelectRowsResponse response = groupsCommand.execute(c, "/");

            List<String> members = new ArrayList<>();
            for (Map<String, Object> row : response.getRows()) {
                String name = (String) row.get("Name");
                if (!name.endsWith(suffix))
                    continue;

                int groupId = ((Number) row.get("UserId")).intValue();
                GetUsersCommand getUsersCommand = new GetUsersCommand();
                getUsersCommand.setGroupId(groupId);
                LOG.info("Reading users from groupId " + groupId + " from LabKey Server.");
                GetUsersResponse getUsersResponse = getUsersCommand.execute(c, "/");
                for (GetUsersResponse.UserInfo userInfo : getUsersResponse.getUsersInfo())
                    members.add(userInfo.getEmail());

                GroupInfo.addLabkey(name.substring(0, name.length() - suffix.length()), members, groupId);
                members.clear();
            }

            File usersFile = new File(params[0]);
            if (!usersFile.exists()) {
                throw new FileNotFoundException("File " + usersFile + " for users does not exist");
            }
            LOG.info("Reading users from LDAP server.");

            try (BufferedReader r = new BufferedReader(new FileReader(usersFile))) {
                LDAPRecord ldapRecord;
                while (null != (ldapRecord = readLDAPRecord(r))) {
                    String uid = ldapRecord.getFirst("uid");
                    if (null != uid)
                        ldapUserMap.put(uid, new LDAPUserRecord(ldapRecord));
                }
            }

            File groupsFile = new File(params[1]);
            if (!groupsFile.exists()) {
                throw new FileNotFoundException("File " + groupsFile + " for groups does not exist");
            }
            LOG.info("Reading groups from LDAP server.");

            //Load all the groups from the group file
            LDAPRecord groupLdapRecord;
            try (BufferedReader groupReader = new BufferedReader(new FileReader(groupsFile))) {
                while (null != (groupLdapRecord = readLDAPRecord(groupReader))) {
                    String groupName = groupLdapRecord.getFirst("cn");
                    List<String> memberUids = groupLdapRecord.get("memberUid");
                    if (null == memberUids)
                        continue;
                    List<String> memberEmails = new ArrayList<>();
                    for (String uid : memberUids) {
                        LDAPUserRecord record = ldapUserMap.get(uid);
                        if (null != record) {
                            String email = record.getEmail(ldapDomain, false);
                            if (null != email)
                                memberEmails.add(email);
                            else {
                                LOG.info("Skipping user " + uid + " in group " + groupName + " because no email name found.");
                            }
                        }
                    }
                    if (memberEmails.size() > 0)
                        GroupInfo.addLDAP(groupLdapRecord.getFirst("cn"), memberEmails);
                    else
                        LOG.info("Skipping group " + groupName + " because no valid users found. ");
                }
            }

            //Now go through all of the groups
            //If there is a LabKey group and no LDAP group, delete the labkey group
            //If there is an LDAP Group
            //   Create LabKey group if not there
            //   Add each user in ldap group who is not in LabKey group (add user to LabKey users if not there)
            //   Delete each user in LabKey group who is not in ldap group
            LOG.info("Synchronizing groups.");
            for (GroupInfo group : GroupInfo.getAll()) {
                if (null == group.getLabkeyUserId()) {
                    LOG.info("Adding group " + group.getName() + suffix);
                    CreateGroupCommand createGroupCommand = new CreateGroupCommand(group.getName() + suffix);
                    CreateGroupResponse createGroupResponse = createGroupCommand.execute(c, "/");
                    group.setLabkeyUserId(createGroupResponse.getGroupId().intValue());
                    group.setLabkeyUsers(Collections.<String>emptySet());
                }

                if (null == group.getLdapUsers() && cleanGroups) {
                    LOG.info("Deleting group " + group.getName() + suffix);
                    DeleteGroupCommand deleteGroupCommand = new DeleteGroupCommand(group.getLabkeyUserId());
                    deleteGroupCommand.execute(c, "/");
                } else if (null != group.getLdapUsers()) {
                    Set<String> newLabKeyUsers = new HashSet<>(group.getLdapUsers());
                    newLabKeyUsers.removeAll(group.getLabkeyUsers());
                    if (newLabKeyUsers.size() > 0) {
                        AddGroupMembersCommand addMembersCommand = new AddGroupMembersCommand(group.getLabkeyUserId());
                        for (String userEmail : newLabKeyUsers) {
                            LabKeyUserRecord labKeyUser = labkeyUserMap.get(userEmail);
                            Integer userId;
                            if (null != labKeyUser)
                                userId = labKeyUser.getUserId();
                            else {
                                LOG.info("Adding user " + userEmail);
                                CreateUserCommand createUserCommand = new CreateUserCommand(userEmail);
                                CreateUserResponse createUserResponse = createUserCommand.execute(c, "/");
                                userId = createUserResponse.getUserId().intValue();
                                labkeyUserMap.put(userEmail, new LabKeyUserRecord(userEmail, userId));
                            }

                            LOG.info("Adding user " + userEmail + " to group " + group.getName());
                            addMembersCommand.addPrincipalId(userId);
                        }
                        addMembersCommand.execute(c, "/");
                    }

                    Set<String> delLabkeyUsers = new HashSet<>(group.getLabkeyUsers());
                    delLabkeyUsers.removeAll(group.getLdapUsers());
                    if (delLabkeyUsers.size() > 0) {
                        RemoveGroupMembersCommand removeGroupMembersCommand = new RemoveGroupMembersCommand(group.getLabkeyUserId());
                        for (String userEmail : delLabkeyUsers) {
                            LOG.info("Removing user " + userEmail + " from group " + group.getName());
                            removeGroupMembersCommand.addPrincipalId(labkeyUserMap.get(userEmail).getUserId());
                        }

                        removeGroupMembersCommand.execute(c, "/");
                    }
                }
            }

            //Finally update any user information that is out of date in LabKey.
            for (String uid : ldapUserMap.keySet()) {
                LDAPUserRecord ldapUser = ldapUserMap.get(uid);
                LabKeyUserRecord labkeyUser = labkeyUserMap.get(ldapUser.getEmail(ldapDomain, false));
                if (null != labkeyUser && labkeyUser.updateFromLDAP(ldapUser)) {
                    LOG.info("Updating user information for " + labkeyUser.getUserField(UserField.Email));
                    UpdateUserCommand uuc = new UpdateUserCommand(labkeyUser);
                    UpdateUserResponse uur = uuc.execute(c, "/");
                    if (!uur.succeeded())
                        LOG.error("Error updating user information for " + labkeyUser.getUserField(UserField.Email));
                }
            }
        }
        catch (Exception e)
        {
            LOG.fatal("Failed to sync", e);
            System.exit(1);
        }
    }

    public static Set<String> keepProperties = null;
    private static LDAPRecord readLDAPRecord(BufferedReader r) throws IOException {
        String line = r.readLine();
        while (null != line && line.trim().length() == 0)
            line = r.readLine();

        if (null == line)
            return null;

        if (null == keepProperties)
        {
            keepProperties = new HashSet<>();
            for (UserField u : UserField.values())
            {
                if (null != u.getLDAPName())
                    keepProperties.add(u.getLDAPName());
            }
            keepProperties.add("memberUid");
            keepProperties.add("cn");
        }

        LDAPRecord result = new LDAPRecord();
        String lastKey = null, lastValue = null;
        Pattern keyValue = Pattern.compile("([^:]*)::?(.*)");
        while (null != line && line.trim().length() > 0)
        {
            Matcher m = keyValue.matcher(line);
            if (!m.matches())
            {
                throw new IllegalArgumentException("Bad line in LDAP record: " + line);
            }
            lastKey = m.group(1);
            lastValue = m.group(2).trim();
            line = r.readLine();
            while (null != line && line.trim().length() > 0 && line.startsWith(" ")) //continuation
            {
                lastValue = lastValue + line.substring(1);
                line = r.readLine();
            }

            //Don't bother storing properties we don't use...
            if (keepProperties.contains(lastKey))
                result.put(lastKey, lastValue);
       }

        return result;
    }

}
