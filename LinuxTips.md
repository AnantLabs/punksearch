# Ubuntu #

I've experienced an issue with Sun's JDK (sun-java5-jdk) installed on Ubuntu (8.04) and punksearch 0.9.3 running on Tomcat 5.5.

The problem is with displaying statistics.jsp page.
Since we have charts there it needs awt libraries and "libawt.so" somehow can't find "libmlib\_image.so".

To make statistics.jsp page work do following (for more details see [here](https://bugs.launchpad.net/debian/+source/sun-java5/+bug/162232)):
```
sudo ln -s /usr/lib/jvm/java-1.5.0-sun/jre/lib/i386/libmlib_image.so /usr/lib
sudo ldconfig
```
Under AMD64 system this can be solved by creating the /etc/ld.so.conf.d/java.conf with the following contents:
```
/usr/lib/jvm/java-1.5.0-sun/jre/lib/i386
```

And restart Tomcat.