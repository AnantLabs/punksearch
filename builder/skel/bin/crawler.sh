#!/bin/sh

CP=../etc
for file in ../lib/*.jar; do
	CP=$CP:$file
done;

echo $CP

java -Xmx1024m -cp $CP org.punksearch.cli.CrawlerMain $1
