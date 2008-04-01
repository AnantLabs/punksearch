#!/bin/sh

CP=../punksearch-core.jar:../etc
for file in ../lib/*.jar; do
	CP=$CP:$file
done;

echo $CP

java -cp $CP org.punksearch.cli.SearcherMain $1
