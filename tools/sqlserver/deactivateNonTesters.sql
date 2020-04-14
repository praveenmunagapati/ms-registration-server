--  This script is designed to deactivate most of the users in a LabKey database. This is useful, for example,
--  when restored a production database to a test or staging server where only a small subset of users should
--  have access.
--
--  The script leaves the following users:
--
--  1. Users in the labkey.com mail domain
--  2. Site administrators
--  3. Specific named users (e.g., those who have been using staging/test to test features under development)

--
--  NOTE: Set the database name correctly on the next line

USE labkey
GO

UPDATE core.Principals SET Active=0
    WHERE Type='u'
    AND Name NOT IN ('xxxyyy@zzz.com', 'xxxyyy@zzz.com')
    AND Name NOT LIKE '%@labkey.com'
    AND UserId NOT IN
        (SELECT p.UserId FROM core.Principals p INNER JOIN core.Members m ON (p.UserId = m.UserId) AND m.GroupId=-1)
GO

-- Consider adding statements that do the following:
--
-- 1. Change the web theme to a different color scheme to distinguish test/staging from production
-- 2. Change the short name to a different name to distinguish test/staging from production
-- 3. Change the base server url (see site settings)