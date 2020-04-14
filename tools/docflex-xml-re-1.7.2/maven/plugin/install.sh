#!/bin/sh

#----------------------------------------------------------------------
# Specify the location of Sun JDK/JRE 6.x, 5.x or 1.4.x here
#----------------------------------------------------------------------
JAVA_HOME=/usr/java/jdk1.6.0_02

#----------------------------------------------------------------------
# Specify the location of Maven 2 here
#----------------------------------------------------------------------
M2_HOME=/usr/local/apache-maven/apache-maven-2.0.9

#----------------------------------------------------------------------
# Other variables
#----------------------------------------------------------------------

DOCFLEX_XML_HOME=../..
DOCFLEX_XML_VER=1.7.2

#----------------------------------------------------------------------
# Installing DocFlex/XML RE Java library
#----------------------------------------------------------------------

${M2_HOME}/bin/mvn install:install-file -Dfile=${DOCFLEX_XML_HOME}/lib/docflex-xml-re.jar -DgroupId=docflex -DartifactId=docflex-xml -Dversion=${DOCFLEX_XML_VER} -Dpackaging=jar -DgeneratePom=true

#----------------------------------------------------------------------
# Compiling and installing DocFlex/XML Maven Plugin
#----------------------------------------------------------------------

${M2_HOME}/bin/mvn install

sleep 10
