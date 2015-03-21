# Introduction #

It is natural you want to recrawl network periodically. It would be great if application do it automatically.
While application itself can't schedule crawling yet, it is possible to use cron (or something similar) for that.
We are planning to use Quartz to enable app with scheduling. And we definitely will implement that.
But we'll also keep the "workaround" described here, since it may look more handy and natural for experts.

# Details #
Currently (starting from version 0.8.0), you have 2 ways to start crawling from cron: run crawler.sh bundled in quickstart zip or request special admin page of web app.

## Script (new and preferred way) ##
PUNKSearch has crawler.sh script included in quickstart bundle starting from version 0.8.0. You can point the cron to execute it in order to recrawl the network. The web app monitors changes to the index directory and will recreate caches on first search query after crawling process is finished.

## HTTP GET request (old way) ##
In order to trigger indexing task from a script (for example _bash_) you should use following line:
```
wget --spider "http://<host>/<path>/admin.jsp?action=start&ip=<ip list>&threads=<thread
count>&deep=<index deep>&indexDir=<path to index directory>&smbLogin=<login to smb shares, if
any>&password=<admin password>"
```

Example:
```
wget --spider "http://localhost:8080/punksearch/admin.jsp?action=start&ip=192.168.4.1-
192.168.5.255,192.168.8.1-
192.168.8.255&threads=5&deep=5&indexDir=/var/local/PunkSearchIndex&smbLogin=&password=123_456"
```

We use _wget_ here to send GET request to the application. _--spider_ says that we do not want page to be downloaded.
Please note, that we use _password_ parameter. It will not work if you omit it. This ensures no one will schedule reindexing if you do not want that.