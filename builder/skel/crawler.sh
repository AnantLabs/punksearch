#!/bin/sh

# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

# Get standard environment variables
PRGDIR=`dirname "$PRG"`

# Only set PUNKSEARCH_HOME if not already set
[ -z "$PUNKSEARCH_HOME" ] && PUNKSEARCH_HOME=`cd "$PRGDIR/.." ; pwd`

CP=.
for file in $PUNKSEARCH_HOME/lib/*.jar; do
	CP=$CP:$file
done;

echo $CP

java -Xmx1024m -Djava.util.logging.config.file=log.properties -cp $CP org.punksearch.cli.CrawlerMain $1
