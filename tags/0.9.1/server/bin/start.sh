#!/bin/sh

PORT=8180

CP=.
for file in ../lib/*.jar; do
	CP=$CP:$file
done;

echo $CP

java -Xbootclasspath/a:$CP -jar ../punksearch-server.jar $PORT $1