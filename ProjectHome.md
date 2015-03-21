# Summary #
Yet another LAN indexing solution. Indexes SMB and FTP hosts. Has web interface. Written in Java. GPL.

Leveraging Lucene's scoring system, the PUNKSearch returns results (to the end user) sorted by relevance like all modern _web_ search engines do. Also it exploits a set of heuristics for efficient indexing of really big local networks.  The web interface reports the online/offline status of hosts, groups results by size and extension and does some other cool things.

  * The [Lucene](http://lucene.apache.org/java/docs/index.html) indexing library is used to store crawled data and search over it then;
  * The [jCIFS Client Library](http://jcifs.samba.org/) is used to crawl SMB hosts;
  * The [commons-net](http://commons.apache.org/net/) library is used to crawl FTP hosts ([edtFTPj](http://www.enterprisedt.com/products/edtftpj/) was used in versions < 0.9.0).

Please, take a look at QuickStart guide and/or ExpertSetupGuide to start using the PUNKSearch.

You may also want to check the ChangeList, welcome :)

And do not forget to see at [issues](http://code.google.com/p/punksearch/issues/list) we have, maybe you are the one to help us?

# Screenshots #
## Public (Search) Interface ##
![![](http://punksearch.googlecode.com/files/screenshot_search_0.8.0_thumb.gif)](http://punksearch.googlecode.com/files/screenshot_search_0.8.0.gif)

## Private (Admin) Interface, Statistics Tab ##
![![](http://punksearch.googlecode.com/files/screenshot_statistics_0.8.0_thumb.gif)](http://punksearch.googlecode.com/files/screenshot_statistics_0.8.0.gif)

# Contacts #
  * icq: 76336206
  * msn: ysoldak/hotmail/com
  * gtalk: ysoldak/gmail/com

# Similar Applications #
  * [sharehound](http://sharehound.org/) very similar to PUNKSearch in intensions and core technologies, but has much more features! I advice you to take a look.