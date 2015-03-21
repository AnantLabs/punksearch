**The results are outdated. The test was conducted on some old version of the application. We are going to repeat the test using new version soon. Stay tuned.**

# Introduction #
It was interesting to check the application throughput on heavy load setting.

# Environment #
| **Index** | 944429 items including folders and files (average local network), 160Mb on disk |
|:----------|:--------------------------------------------------------------------------------|
| **Hardware** | AMD Duron 1000Gz, 256Mb, 7200 rpm |
| **Software** | Debian GNU/Linux, Java 1.5, Tomcat 5.5 |

# Tools #
[Apache JMeter](http://jakarta.apache.org/jmeter/)

# Experiments #
10 users, 100 requests for each, 1 sec ramp-up period.

I searched for "potter" in _everything_. It was found, that actual query means nothing for load test results. The single exception is query, which returns no results -- it is evaluated faster. This can be explained by additional routines used by case with non-zero results to represent them and send the _big_ page to the user.

## Resulting (main) graph ##
![![](http://punksearch.googlecode.com/files/everything-potter_graph_thumb.png)](http://punksearch.googlecode.com/files/everything-potter_graph.png)

## Distribution graph ##
![![](http://punksearch.googlecode.com/files/everything-potter_distribution_thumb.png)](http://punksearch.googlecode.com/files/everything-potter_distribution.png)

# Conclusions #
PUNKSearch shows acceptable performance (even on old hardware). It takes only 100 msec in average to answer a query when 10 users search something simultaneously. Distribution graph reveals good behaviour, since major part of samples are closely grouped.

# Extra #
  * [JMeter test plan used](http://punksearch.googlecode.com/files/PunkSearch_2007-08-13.jmx)
  * [List of load test tools](http://www.softwareqatest.com/qatweb1.html#LOAD)