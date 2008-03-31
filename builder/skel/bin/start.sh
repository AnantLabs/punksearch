#!/bin/sh

CP=../etc
for file in ../lib/*.jar; do
	CP=$CP:$file
done;

echo $CP

java -Xbootclasspath/a:$CP -jar ../lib/punksearch-server.jar 8180 ../punksearch.war

#java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=y -Xbootclasspath/a:$CP -jar ../lib/punksearch-server.jar ../web
