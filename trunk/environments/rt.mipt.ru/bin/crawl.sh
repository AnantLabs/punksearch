#!/bin/sh

# Resolve links - $0 may be a softlink
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

echo "PUNKSEARCH_HOME: $PUNKSEARCH_HOME"
echo

CLIDIR=$(cd "$PRGDIR/../modules/cli"; pwd)

CP=.:$PUNKSEARCH_HOME/conf
for file in $CLIDIR/*.jar $CLIDIR/lib/*.jar; do
	CP=$CP:$file
done;

#echo $CP

DEBUG=
#DEBUG="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=5005,suspend=n"

java $DEBUG -Xmx512m -Dorg.punksearch.home=$PUNKSEARCH_HOME -cp $CP org.punksearch.cli.CrawlerMain $1
