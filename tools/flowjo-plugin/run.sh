#!/bin/sh

# Must compile with JDK 6 (FlowJo doesn't support JDK 7 yet)
JAVA_HOME=/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home

FJ_LIB="/Applications/FlowJo Java/FlowJo 10.0.5.app/Contents/Resources/Java"
FJLIB_JAR="$FJ_LIB/fjlib.jar"
ENGINE_JAR="$FJ_LIB/engine.jar"

CLIENTAPI_JARS="../../build/client-api/java/jar/*"
CLIENTAPI_LIB_JARS="../../remoteapi/java/lib/*"

CP=$FJLIB_JAR
CP=$CP:$ENGINE_JAR
CP=$CP:"$CLIENTAPI_JARS"
CP=$CP:"$CLIENTAPI_LIB_JARS"
CP=$CP:build/classes

java -cp "$CP" org.labkey.flow.flowjo.LabKeyServer $*

