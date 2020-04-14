::----------------------------------------------------------------------
:: Specify the location of Sun JDK/JRE 6.x, 5.x or 1.4.x here
::----------------------------------------------------------------------
@set JAVA_HOME=C:\jdk1.6

::----------------------------------------------------------------------
:: Specify the location of Maven 2 here
::----------------------------------------------------------------------
@set M2_HOME=C:\apache-maven-2.0.9

::----------------------------------------------------------------------
:: Setting the initial and maximum heap size allocated by JVM when 
:: running Maven.
::
:: Since DocFlex/XML generator will be executed in the same JVM, check 
:: this option when you need to process large quantities of data 
:: (e.g. to generate documentation for big XML schemas)!
::----------------------------------------------------------------------
@set MAVEN_OPTS=-Xms256m -Xmx512m

::----------------------------------------------------------------------
:: Running DocFlex/XML generator from Maven
::----------------------------------------------------------------------
call %M2_HOME%\bin\mvn docflex:docflex-xml-maven-plugin:1.0:generator

pause