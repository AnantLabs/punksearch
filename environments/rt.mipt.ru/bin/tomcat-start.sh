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

export PUNKSEARCH_HOME

$PRGDIR/../tomcat/bin/startup.sh