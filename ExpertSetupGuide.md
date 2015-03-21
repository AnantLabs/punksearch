**NB: the description below is for version 0.8.1, the home directory structure changed as from 0.9.0. The major thoughts and advices are still valid, but keep in mind home directory changes! (and look into latest .deb file to see how author prefers to spread the application across a linux installation)**

# Introduction #

This page describes in details one of possible approaches to install (setup) the PUNKSeach on your linux server.
We cover here servlet container configuration (Tomcat), Cron setup and arrangement of the PUNKSearch files to enable easy upgrades in the future (so you will not lost your configuration on upgrade).

# Details #

## Overview ##
![http://punksearch.googlecode.com/files/expert_setup.png](http://punksearch.googlecode.com/files/expert_setup.png)

Please note we exploit convenient symbolic links in the proposed setup.

## Separate configs from other stuff ##
First of all we are lazy and want upgrades to go smoothly and do not erase our custom configuration files (namely, punksearch.properties and filetypes.conf).

So we create 2 directories:
  1. /opt/punksearch,
  1. /opt/punksearch\_dist.

In the second directory we download punksearch-

&lt;version&gt;

-quickstart.zip and unzip it into the directory.

Then we copy 2 configuration files  to /opt/punksearch and midify them to satisfy our needs.

The major thing we should setup in punksearch.properties is path to the index directory.
We prefer to store index outside the configuration directory (for example in /opt/punksearch\_index/index/). Please, note the extra "index" directory at the end. This was done to isolate crawler-generated junk. Yeah, crawler generates some junk during crawling process -- the small temporary index directories (in out case expect to see directories like /opt/punksearch\_index/index\_crawler3 from time to time). The crawler cleans the junk after it is finished to crawl the network, but still...

The next thing we want to do is to make symbolic links to the crawler.sh and lib directory (see figure). So these artifacts will be upgraded easily and we can use the crawler.sh script to trigger crawling from command line (useful for cron).

## Tomcat ##

Both bundles (WAR and QuickStart) include context descriptor file proven to work with Apache Tomcat (tested on 5.5.26). In order to setup your tomcat installation to host the PUNKSearch, you should symlink the context descriptor file (punksearch.xml) to TOMCAT\_HOME/conf/Catalina/localhost/punksearch.xml. The Tomcat then handle the descriptor and does the magic.

We suggest to copy the descriptor from /opt/punksearch\_dist to /opt/punksearch, adjust it and do symlink. Do not forget to setup the punksearch\_home and admin\_password properties, as well as path to the war file.

## Cron ##

And finally, you'd like to setup the system to re-crawl the network periodically. Okay, no problem, just create tiny bash script (see Figure) and make symlink to it from /etc/cron.daily and you are done!

Good luck and watch the updates.