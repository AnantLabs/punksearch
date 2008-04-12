#!/bin/sh

CP=.
for file in lib/*.jar; do
	CP=$CP:$file
done;

echo $CP

java -Xmx256m -cp $CP org.punksearch.cli.CrawlerMain $1
