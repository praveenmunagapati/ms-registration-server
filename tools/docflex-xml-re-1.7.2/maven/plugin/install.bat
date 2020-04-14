::----------------------------------------------------------------------
:: Specify the location of Sun JDK/JRE 6.x, 5.x or 1.4.x here
::----------------------------------------------------------------------
@set JAVA_HOME=C:\jdk1.6

::----------------------------------------------------------------------
:: Specify the location of Maven 2 here
::----------------------------------------------------------------------
@set M2_HOME=C:\apache-maven-2.0.9

::----------------------------------------------------------------------
:: Other variables
::----------------------------------------------------------------------

@set DOCFLEX_XML_HOME=..\..
@set DOCFLEX_XML_VER=1.7.2

::----------------------------------------------------------------------
:: Installing DocFlex/XML RE Java library
::----------------------------------------------------------------------

call %M2_HOME%\bin\mvn install:install-file -Dfile=%DOCFLEX_XML_HOME%\lib\docflex-xml-re.jar -DgroupId=docflex -DartifactId=docflex-xml -Dversion=%DOCFLEX_XML_VER% -Dpackaging=jar -DgeneratePom=true

::----------------------------------------------------------------------
:: Compiling and installing DocFlex/XML Maven Plugin
::----------------------------------------------------------------------

call %M2_HOME%\bin\mvn install

pause