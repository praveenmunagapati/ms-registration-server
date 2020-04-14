FROM tomcat:9-jdk14-openjdk-slim-buster

COPY . /tmp/files/
WORKDIR /tmp/files/
RUN apt-get update && \
	apt-get install -y gettext && \
	apt-get install -y r-base 

#WORKDIR /tmp/files/dist/Registration/
RUN ./gradlew wrapper --gradle-version 6.3-rc-3 && \
	./gradlew && \
	./gradlew deployApp -PdeployMode=prod && \
	./gradlew -PdeployMode=prod :server:modules:UserReg-WS:distributions:Registration:distribution && \
	mv /tmp/files/dist/Registration/Registration-1.34-bin.tar.gz /tmp/files/ && \
	tar xvf Registration-1.34-bin.tar.gz && \
	rm Registration-1.34-bin.tar.gz && \
	cp Registration-1.34-bin/tomcat-lib/*.jar /usr/local/tomcat/lib/ && \
	cp -R Registration-1.34-bin/bin/ /usr/local/labkey/ && \
	cp -R Registration-1.34-bin/labkeywebapp/ /usr/local/labkey/ && \
	cp -R Registration-1.34-bin/modules/ /usr/local/labkey/ && \
	cp -R Registration-1.34-bin/pipeline-lib/ /usr/local/labkey/ && \
	rm -rf /tmp/files && \
	rm -rf /root/.gradle && \
	rm -rf /root/.npm

WORKDIR /
COPY labkey.xml .
COPY config_and_run.sh .

CMD ["./config_and_run.sh"]

