#!/bin/sh

FULL_SCAN_DAY="1"
OWNER=tomcat55

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

# Determine the range of IPs to crawl (depends on day of week and if "hosts.csv" present)
DAY_OF_WEEK=$FULL_SCAN_DAY
[ -f $PUNKSEARCH_HOME/hosts.csv] && DAY_OF_WEEK=`date +"%u"`

case $DAY_OF_WEEK in
$FULL_SCAN_DAY)
        echo "Start full network scan"
        RANGE=""
;;
*)
        echo "Start update of known hosts"
        RANGE="hosts.csv"
;;
esac

# Crawl the network
cd $PUNKSEARCH_HOME
./crawler.sh $RANGE > cron-crawl.log 2>&1

# Fix permissions, so tomcat can r/w data
chown -R $OWNER logs/
chown $OWNER hosts.csv
