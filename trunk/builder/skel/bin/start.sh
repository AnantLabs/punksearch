#!/bin/sh

PORT=8180
WAR=../punksearch.war

CP=../etc
for file in ../lib/*.jar; do
	CP=$CP:$file
done;

echo $CP

DEBUG=
#DEBUG=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=y

java $DEBUG -Xmx1024m -Xbootclasspath/a:$CP -jar ../lib/punksearch-server.jar $PORT $WAR
