/*
 * Copyright (c) 2003-2006 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*  This script creates sp_lock2, a variation on the built-in system stored procedure sp_lock that comes with
 *  SQL Server.  sp_lock2 rolls up the indidual locking info into summary statistcs and also uses a facility
 *  in SQL Server to grab the last executed statement of the spid holding or requesting a lock.  It is
 *  especially useful in identifying long running queries and contention problems.
 *
 *  CAVEAT:  only tested on SQL 2000 SP4.
 *
 *  USAGE:  Use Query Analyzer to execute this script on a SQL Server that is running CPAS.  Then open a new
 *  Query Analyzer window and set the database to "cpas" (or whatever it is called on your system).
 *  and execute "sp_lock2".  Any SQL process that is currently holding or waiting for a lock is shown.
 *
 *  WHAT TO LOOK FOR:  
 *   1) execute a few times to see if processes are getting and releasing locks as expected, or are "stuck"
 *   2) Table locks of type S or X are bad and indicate either huge or inefficient queries.  Get the
 *      SQL command from the right-most column and figure out where in CPAS code it is issued.
 *      Table locks of tye IS or IX are expected and not an indication of a problem.
 *   3) A status of "WAIT" instead of "GRANT" shouldn't last more than one execution of sp_lock2.
 *
 *   Author: peter@labkey.com  Oct 2006
 */


USE master
GO

IF EXISTS (SELECT * FROM sysobjects WHERE id = object_id('dbo.sp_lock2') AND sysstat & 0xf = 4)
    DROP PROCEDURE dbo.sp_lock2
GO

CREATE PROCEDURE sp_lock2
AS
SET NOCOUNT ON
    CREATE TABLE #splockresults (spid int, dbid int, ObjId int, IndId int
        , Type varchar(5), Resource varchar(20), Mode varchar(5), Status varchar(8))
    CREATE TABLE #splocksummary (rowid int identity PRIMARY KEY, spid int, obj nvarchar(30) NULL
        , type varchar(5), mode varchar(5), status varchar(8), lockCount int
        , cmd nvarchar(3800) NULL, sstart int, send int, sql_handle binary(20))

INSERT INTO #splockresults
execute('master.dbo.sp_lock')

INSERT INTO #splocksummary (spid, obj, type, mode, status, lockcount
, sstart, send, sql_handle
)
SELECT lr.spid, object_name(lr.ObjId) AS obj, lr.Type, lr.Mode, lr.Status, COUNT(DISTINCT resource)
    , MIN(sp.stmt_start/2) AS sstart
    , MAX(CASE WHEN sp.stmt_end = -1 THEN -1 ELSE sp.stmt_end/2 END) AS send
    , MAX(sp.sql_handle) AS sql_handle
FROM #splockresults lr
    INNER JOIN master.dbo.sysprocesses sp ON (lr.spid = sp.spid)
WHERE db_name(lr.dbid)= db_name() AND lr.Type <> 'DB'
AND sp.sql_handle <> 0x00
GROUP BY lr.spid, lr.dbid, object_name(lr.ObjId), lr.Type, lr.Mode, lr.Status
ORDER BY lr.spid, lr.Mode DESC

DECLARE c1 cursor for SELECT rowid, spid, sql_handle, sstart, send FROM #splocksummary ORDER BY spid ASC
DECLARE @rowid int, @spid int, @lastspid int, @sql_handle binary(20), @cmd nvarchar(3800), @sstart int, @send int
SELECT @lastspid=0
open c1
fetch next FROM c1 INTO @rowid, @spid, @sql_handle, @sstart, @send
while @@fetch_status=0
begin
    IF (@spid <> @lastspid)
    begin

        SELECT @lastspid = @spid
        SELECT @cmd = (SELECT SUBSTRING(text, COALESCE(NULLIF(@sstart, 0), 1),CASE @send WHEN -1 THEN DATALENGTH(text) ELSE (@send - @sstart) END)
            FROM ::fn_get_sql(@sql_handle) )

        SELECT @cmd=REPLACE(@cmd, CHAR(10), ' ')
        SELECT @cmd=REPLACE(@cmd, CHAR(13), ' ')

        UPDATE #splocksummary
        SET cmd = @cmd
        WHERE rowid = @rowid
    end
    else
        UPDATE #splocksummary
        SET cmd = ' '
        WHERE rowid = @rowid
    fetch next FROM c1 INTO @rowid, @spid, @sql_handle, @sstart, @send
end
close c1
deallocate c1

SELECT spid, obj, type, mode, status, lockCount, cmd FROM #splocksummary ORDER BY spid
GO

GRANT EXECUTE ON sp_lock2 TO PUBLIC
GO