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

# Get standard environment variables
PRGDIR=`dirname "$PRG"`

# Only set PUNKSEARCH_HOME if not already set
[ -z "$PUNKSEARCH_HOME" ] && PUNKSEARCH_HOME=`cd "$PRGDIR/.." ; pwd`

TOMCAT=$PUNKSEARCH_HOME/tomcat

# cleaning

# turn on verbosity
set -v
rm -rf $PUNKSEARCH_HOME/logs/*
rm -rf $TOMCAT/logs/*
rm -rf $TOMCAT/work/*
rm -rf $TOMCAT/webapps/ROOT
set +v

