# Introduction #

You may prefer not to launch application server (like tomcat) on port 80. For various reasons, for example you already have apache server running on 80 port. You still may want use default http port (80) for punksearch.

Additional benefit of directing traffic through apache is possibility to use well known and useful statistics analysis tools like AWStats, etc.

This page explains how you can achieve this using different techniques.

All advices are for Ubuntu 8.04 regarding paths and commands used, but of course you may adapt them for your linux distribution or completely different operating system.

Two ways are known right now:
  1. mod\_jk
  1. mod\_proxy + mod\_rewrite

# mod\_jk #
The advised way is to use apache's module mod\_jk.

## Steps in a glance ##
  1. **install mod\_jk**<br />apt-get install libapache2-mod-jk
  1. **create file /etc/apache2/workers.properties** (see listing below)
  1. **create file /etc/apache2/mods-available/jk.conf** (see listing below)
  1. **enable redirect**<br />ln -s /etc/apache2/mods-available/jk.conf /etc/apache2/mods-enabled/jk.conf
  1. **relaunch apache**<br />/etc/init.d/apache2 restart

## workers.properties ##
```
worker.list=localhost
worker.localhost.port=8009
worker.localhost.host=localhost
worker.localhost.type=ajp13
worker.localhost.lbfactor=1
workers.tomcat_home=/usr/share/tomcat5.5/
workers.java_home=/usr/lib/jvm/java-1.5.0-sun/
```

## jk.conf (tested with tomcat) ##
```
# Sample mod_jk configuration for Apache 2
#
# for all commands/options available see the manual provided in libapache-mod-jk-doc package. 

# The location where mod_jk will find the workers definitions
JkWorkersFile   /etc/apache2/workers.properties

# The location where mod_jk is going to place its log file
JkLogFile       /var/log/apache2/mod_jk.log

# The log level:
# - info log will contain standard mod_jk activity (default).
# - warn log will contain non fatal error reports.
# - error log will contain also error reports.
# - debug log will contain all information on mod_jk activity
# - trace log will contain all tracing information on mod_jk activity
JkLogLevel      info

# Send all requests ending in .jsp to ajp13_worker
JkMount /*.jsp localhost
```

# mod\_proxy + mod\_rewrite #
  1. **place .htaccess file** (see listing below) in some subdirectory of your apache served site (somewhere inside /var/www)
  1. **ensure you can use .htaccess**<br />look for AllowOverride and set in to "All" in site's config file, like /etc/apache2/sites-enables/001-default

## .htaccess ##
```
RewriteEngine   on
RewriteRule     (.*) http://localhost:8180/punksearch/$1 [P]
```