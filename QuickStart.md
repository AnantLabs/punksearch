# Quickstart Bundle #
The bundle includes:
  * Web application (war file)
  * Jetty server
  * Example index (it was extracted from real index, so it somewhat inconsistent in terms of statistics page)
  * Default configuration files (punksearch.properties, filetypes.conf)
  * Example of tomcat's context descriptor (punksearch.xml)
  * Bash script to start the Jetty server with punksearch web app (start.sh)
  * Bash script to crawl the network from console, which can be used to schedule crawling with cron (crawler.sh)
  * All necessary libraries for this console-based crawling engine

Setup:
  * Download the bundle (punksearch-

&lt;version&gt;

-quickstart.zip)
  * Extract it into some directory (for example: /opt/punksearch)
  * Run start.sh (or start.bat)
  * Open http://localhost:8180/punksearch/search.jsp and try find something (the example index contains free linux-related stuff, so search "linux" or "ubuntu" or "debian", etc)
  * Open http://localhost:8180/punksearch/admin.jsp and login (password: "123\_456" by default)
  * Check settings at "config" tab, modify punksearch.properties if needed (restart the server if you did)
  * Crawl your LAN ("status" tab or using bash script)
  * Enjoy! :)

# War bundle #
The bundle includes:
  * Web application (war file) with all required libraries included into war's WEB-INF
  * Default configuration files (punksearch.properties, filetypes.conf)
  * Example of tomcat's context descriptor (punksearch.xml)

Setup:
  * Download the bundle (punksearch-

&lt;version&gt;

-war.zip)
  * Extract it into some directory (for example: /opt/punksearch)
  * Modify the default configuration files (do not forget to set the path to the index directory!)
  * Deploy war file into your servlet container
    * Tomcat first way: put WAR into webapps directory, setup PUNKSEARCH\_HOME environment variable
    * Tomcat second way: put punksearch.xml file into CATALINA\_HOME/conf/Catalina/localhost and modify it to meet your needs (set punksearch\_home and admin\_password, location of war file)
  * Open http://localhost:<servlet container port>/punksearch/admin.jsp and login (password: "123\_456" by default)
  * Check settings at "config" tab, modify punksearch.properties if needed (redeploy web app if you did)
  * Crawl your LAN ("status" tab or using bash script)
  * Open http://localhost:<servlet container port>/punksearch/search.jsp and try find something
  * Enjoy! :)