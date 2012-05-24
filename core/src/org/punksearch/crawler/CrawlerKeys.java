/***************************************************************************
 *                                                                         *
 *   PunkSearch - Searching over LAN                                       *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package org.punksearch.crawler;

/**
 * Utility class holding all possible crawler system properties
 *
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class CrawlerKeys {

    /**
     * Use this property to customize directory for temporary indexes
     */
    public static final String TMP_DIR_PROPERTY = "org.punksearch.crawler.tmpdir";
    /**
     * Whatever to unlock main and temporary index directories
     */
    public static final String UNLOCK_PROPERTY = "org.punksearch.crawler.forceunlock";
    /**
     * Number of threads to use for crawling the network (use values between 1 and 10)
     */
    public static final String THREADS_PROPERTY = "org.punksearch.crawler.threads";
    /**
     * The comma separated list of IP ranges or path to the file with IPs.
     */
    public static final String RANGE_PROPERTY = "org.punksearch.crawler.range";
    /**
     * Lifetime of old items in the index (may be real number).
     */
    public static final String KEEPDAYS_PROPERTY = "org.punksearch.crawler.keepdays";
    /**
     * Maximum hours to wait until a crawling thread to finish, then interrupt it.
     */
    public static final String MAXHOURS_PROPERTY = "org.punksearch.crawler.maxhours";

    public static final String DEEP = "org.punksearch.crawler.deep";

    public static final String BOOST_CREATE_DATE = "org.punksearch.crawler.boost.createdate";
    public static final String BOOST_DEEP = "org.punksearch.crawler.boost.deep";
    public static final String BOOST_SIZE = "org.punksearch.crawler.boost.size";

    public static final String HEADER_USE = "org.punksearch.crawler.data.header";
    public static final String HEADER_LENGTH = "org.punksearch.crawler.data.header.length";
    public static final String HEADER_THRESHOLD = "org.punksearch.crawler.data.header.threshold";

    public static final String DUMP_STATUS_PERIOD = "org.punksearch.crawler.dump.status.period";
}
