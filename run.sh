#!/bin/bash

JAVA_HOME=/usr/lib/jvm/zulu11
WORKDIR=/opt/modbusparser/
CLASSPATH="/opt/modbusparser/modbusparser-0.0.1-SNAPSHOT.jar:/opt/modbusparser/deps/gson-2.9.0.jar:/opt/modbusparser/deps/jsr305-3.0.2.jar:/opt/modbusparser/deps/org.eclips$
CLASSNAME="modbusparser.TCPClient"
JAVA_OPTIONS=" -Xms256m -Xmx512m -server -cp $CLASSPATH"
APP_OPTIONS=" /opt/modbusparser/config.yaml"

cd $WORKDIR
"${JAVA_HOME}/bin/java" $JAVA_OPTIONS $CLASSNAME $APP_OPTIONS
