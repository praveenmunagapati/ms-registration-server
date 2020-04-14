@echo off
IF "%1" == "" GOTO :instructions

"%JAVA_HOME%\bin\javac" -cp "..\..\build\staging\tomcat-lib\jtds.jar" JTDSTest.java
"%JAVA_HOME%\bin\java" -cp ".;..\..\build\staging\tomcat-lib\jtds.jar" JTDSTest %1 %2 %3 %4 %5 %6 %7 %8 %9
GOTO :end

:instructions
echo.
echo Tests JDBC connection to SQL Server using jTDS driver.  Connects to the database using the specified
echo parameters and performs a simple SELECT.  Success results in the message "Hello JTDSTest"
echo.
echo Specify parameters individually:
echo    Usage:   JTDSTest {server} {port} {database} {username} {password}
echo    Example: JTDSTest localhost 1433 labkey sa sa
echo.

echo Specify full jTDS connection URL:
echo    Usage:   JTDSTest {connection string} {username} {password}
echo    Example: JTDSTest jdbc:jtds:sqlserver://localhost/labkey:1433 sa sa
echo.

:end