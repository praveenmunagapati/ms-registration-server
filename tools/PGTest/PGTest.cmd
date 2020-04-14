@echo off
setlocal

IF "%1" == "" GOTO :instructions

SET JAVA=java
SET JAVAC=javac

IF "%JAVA_HOME%"=="" goto :run
IF EXIST %JAVA_HOME%\bin\java.exe SET JAVA=%JAVA_HOME%\bin\java.exe
IF EXIST %JAVA_HOME%\bin\javac.exe SET JAVAC=%JAVA_HOME%\bin\javac.exe

:run
"%JAVAC%" -cp "..\..\external\lib\common\postgresql.jar" PGTest.java
"%JAVA%" -cp ".;..\..\external\lib\common\postgresql.jar" PGTest %1 %2 %3 %4 %5 %6 %7 %8 %9
GOTO :end

:instructions
echo.
echo Tests JDBC connection to PostgreSQL using the PostgreSQL JDBC driver.  Connects to the database using the specified
echo parameters and performs a simple SELECT.  Success results in the message "Hello PGTest"
echo.
echo Specify parameters individually:
echo    Usage:   PGTest {server} {port} {database} {username} {password}
echo    Example: PGTest localhost 5432 labkey postgres sasa
echo.

echo Specify full PostgreSQL connection URL:
echo    Usage:   PGTest {connection string} {username} {password}
echo    Example: PGTest jdbc:postgresql://localhost:5432/labkey postgres sasa
echo.

:end