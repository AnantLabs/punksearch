#!/bin/sh

CP=.
for file in lib/*.jar; do
	CP=$CP:$file
done;

echo $CP

java -Xmx1024m -Djava.util.logging.config.file=log.properties -cp $CP org.punksearch.cli.CrawlerMain $1
