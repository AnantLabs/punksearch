@echo off

set PORT=8180
set WAR=punksearch.war

set CLASSPATH=.
set CLASSPATH=%CLASSPATH%;lib\commons-io-1.3.1.jar
set CLASSPATH=%CLASSPATH%;lib\commons-logging-1.1.jar
set CLASSPATH=%CLASSPATH%;lib\core-3.1.1.jar
set CLASSPATH=%CLASSPATH%;lib\edtftpj-2.0.1.jar
set CLASSPATH=%CLASSPATH%;lib\jcifs-1.2.18.jar
set CLASSPATH=%CLASSPATH%;lib\jetty-6.1.4.jar
set CLASSPATH=%CLASSPATH%;lib\jetty-util-6.1.4.jar
set CLASSPATH=%CLASSPATH%;lib\jsp-2.1.jar
set CLASSPATH=%CLASSPATH%;lib\jsp-api-2.1.jar
set CLASSPATH=%CLASSPATH%;lib\lucene-core-2.3.1.jar
set CLASSPATH=%CLASSPATH%;lib\punksearch-cli.jar
set CLASSPATH=%CLASSPATH%;lib\punksearch-core.jar
set CLASSPATH=%CLASSPATH%;lib\punksearch-server.jar
set CLASSPATH=%CLASSPATH%;lib\servlet-api-2.5-6.1.4.jar

set DEBUG=
rem set DEBUG=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=y

@echo on
java %DEBUG% -Xmx1024m -Djava.util.logging.config.file=log.properties -Xbootclasspath/a:%CLASSPATH% -jar lib\punksearch-server.jar %PORT% %WAR%