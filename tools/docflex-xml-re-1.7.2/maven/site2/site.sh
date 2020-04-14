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
# Setting the initial and maximum heap size allocated by JVM when
# running Maven.
#
# Since DocFlex/XML generator will be executed in the same JVM, check
# this option when you need to process large quantities of data
# (e.g. to generate documentation for big XML schemas)!
#----------------------------------------------------------------------
export MAVEN_OPTS="-Xms256m -Xmx512m"

#----------------------------------------------------------------------
# Run the site generation
#----------------------------------------------------------------------
${M2_HOME}/bin/mvn site:site

sleep 10
