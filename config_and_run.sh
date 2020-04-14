#!/bin/bash

envsubst < /labkey.xml > /usr/local/tomcat/conf/Catalina/localhost/labkey.xml

./usr/local/tomcat/bin/catalina.sh run
