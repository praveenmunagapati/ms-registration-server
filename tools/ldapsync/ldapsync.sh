#!/bin/sh
#
#
LOG=ldapsync-stdout.log
ERROR_LOG=ldapsync-stderr.log
LDAP_USER=user
LDAP_PASSWORD=password
SERVER_NAME=https://myServer.com/labkey
DOMAIN=mydomain.wisc.edu

ssh root@ldap.primate.wisc.edu slapcat -s "cn=users,dc=primate,dc=wisc,dc=edu" > users.ldif

#check if the file exists
if [ ! -e users.ldif ]
     then
        echo "the file users.ldif does not exist"
	exit 1;
fi

# check if file size is greater than 0
if [ ! -s users.ldif ]
     then
        echo "The file size of users.ldif is 0"
	exit 1;
fi

ssh root@ldap.primate.wisc.edu slapcat -s "cn=groups,dc=primate,dc=wisc,dc=edu" > groups.ldif

#check if the file exists
if [ ! -e groups.ldif ]
     then
        echo "the file groups.ldif does not exist"
	exit 1;
fi

# check if file size is greater than 0
if [ ! -s groups.ldif ]
     then
        echo "The file size of groups.ldif is 0"
	exit 1;
fi

java -Dlog4j.configuration=file:./log4j.xml -Djavax.net.ssl.trustStore=/users/labkey/.keystore -Djavax.net.ssl.trustStorePassword=changeit -Ddomain=$DOMAIN -Duser=$LDAP_USER -Dpassword=$LDAP_PASSWORD -Dserver=$SERVER_NAME -classpath "lib/*:ldapsync.jar" org.labkey.tools.ldapsync.LDAPSync users.ldif groups.ldif 2>>$ERROR_LOG 1>>$LOG
