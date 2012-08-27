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

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.punksearch.common.FileTypes;
import org.punksearch.common.PunksearchFs;
import org.punksearch.ip.IpIterator;
import org.punksearch.ip.IpRange;
import org.punksearch.ip.IpRanges;
import org.punksearch.ip.SynchronizedIpIterator;
import org.punksearch.logic.hosts_resolver.HostnameResolver;
import org.punksearch.stats.HostStats;
import org.punksearch.stats.TotalStats;
import org.punksearch.stats.TotalStatsWriter;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.punksearch.common.Settings.*;
import static org.punksearch.crawler.CrawlerKeys.*;

/**
 * The crawling process manager. It starts crawling threads, cleans target index, merges data crawled by threads into
 * the target index, cleans temporary files.
 *
 * @author Yury Soldak (ysoldak@gmail.com)
 * @see HostCrawler
 * @see IndexOperator
 * @see org.punksearch.stats.HostStats
 */
public class NetworkCrawler implements Runnable {
    private static final Log log = LogFactory.getLog(NetworkCrawler.class);

    private static final NetworkCrawler INSTANCE = new NetworkCrawler();

    private static final String THREAD_PREFIX = "HostCrawler";

    private FileTypes fileTypes;
    private String indexDirectory;
    private boolean forceUnlock;
    private int threadCount;
    private float daysToKeep;
    private int maxHours;
    private List<IpRange> ranges;

    private final List<HostCrawler> threadList = Collections.synchronizedList(new ArrayList<HostCrawler>());
    private Set<Timer> timers = new HashSet<Timer>();

    private NetworkCrawler() {
    }

    public static NetworkCrawler getInstance() {
        return INSTANCE;
    }

    /**
     * Signals all threads to stop crawling.
     */
    public void stop() {
        synchronized (threadList) {
            for (HostCrawler thread : threadList) {
                thread.requestStop();
            }
        }
    }

    /**
     * The getter method to access current running crawling threads.
     *
     * @return List of crawling threads.
     */
    public List<HostCrawler> getThreads() {
        return threadList;
    }

    /**
     * Starts the crawling process. Starts all threads, merges temp indexes into main one, clears temp files.
     */
    public synchronized void run() {
        readProperties();

        if (!prepareAllIndexDirs()) {
            log.warn("Can't start crawling. Something wrong with an index directory (check log).");
            return;
        }

        if (ranges.size() == 0) {
            log.warn("Can't start crawling. The list of IPs to crawl is empty.");
            return;
        }

        long startTime = new Date().getTime();
        log.info("Crawl process started");

        startTimers();

        IpIterator iter = new SynchronizedIpIterator(ranges);
        synchronized (threadList) {
            threadList.clear();
            for (int i = 0; i < threadCount; i++) {
                HostCrawler indexerThread = makeThread(i, iter);
                indexerThread.start();
                threadList.add(indexerThread);
            }
        }

        TotalStats totalStats = new TotalStats(System.currentTimeMillis());
        List<HostStats> hosts = new ArrayList<HostStats>();

        boolean cleaned = false;

        for (HostCrawler thread : threadList) {
            try {
                thread.join();
                // we want clean the target index just once and at the end of index process
                // also we do not want to clean the index if crawling was interrupted
                if (!cleaned) {
                    cleanTargetIndex();
                    cleaned = true;
                }
                hosts.addAll(thread.getCrawledHosts());
                totalStats.addShares(thread.getShares());
                removeHostsFromIndex(thread.getCrawledHosts());
                mergeIntoIndex(thread.getName());
                cleanTempForThread(thread.getName());
            } catch (InterruptedException e) {
                log.warn("Interrupted: " + thread.getName());
            }
            log.info("Finished: " + thread.getName());
        }

        if (hosts.size() > 0) {
            String statsDir = PunksearchFs.resolveStatsDirectory();
            HostStats.dump(statsDir, hosts);
            HostStats.merge(statsDir, PunksearchFs.resolve(statsDir + File.separator + "hosts.csv"));
            totalStats.addHostStats(hosts);
            TotalStatsWriter.dump(totalStats);
        }

        // should always optimize, since some old items could have been deleted and no one new host crawled.
        log.info("Optimizing index...");
        IndexOperator.optimize(indexDirectory);

        long finishTime = new Date().getTime();
        log.info("Crawl process finished in " + ((finishTime - startTime) / 1000) + " sec");

        synchronized (threadList) {
            threadList.clear();
        }
        cancelTimers();
    }

    /**
     * Extracts configuration from system properties.
     * <p/>
     * The system property names are defined by static final fields of CrawlerKeys class.
     */
    private void readProperties() {
        indexDirectory = PunksearchFs.resolveIndexDirectory();

        forceUnlock = getBool(UNLOCK_PROPERTY, false);
        threadCount = getInt(THREADS_PROPERTY, 5);

        fileTypes = FileTypes.readFromDefaultFile();

        daysToKeep = getFloat(KEEPDAYS_PROPERTY, 7);
        maxHours = getInt(MAXHOURS_PROPERTY, 12);

        ranges = parseRanges(get(RANGE_PROPERTY));
    }

    private void startTimers() {
        Timer processTimer = new Timer();
        processTimer.schedule(new MaxRunWatchDog(), maxHours * 3600 * 1000L);

        Timer statusDumpTimer = new Timer();
        long dumpPeriod = Long.getLong(DUMP_STATUS_PERIOD, 10L) * 1000;
        statusDumpTimer.scheduleAtFixedRate(new ThreadStatusDump(), dumpPeriod, dumpPeriod);

        timers.add(processTimer);
        timers.add(statusDumpTimer);
    }

    private void cancelTimers() {
        for (Timer timer : timers) {
            timer.cancel();
        }
        timers.clear();
    }

    private boolean prepareAllIndexDirs() {
        if (!prepareIndex(indexDirectory)) {
            log.warn("Can't prepare main index directory (check log).");
            return false;
        }
        for (int i = 0; i < threadCount; i++) {
            final String threadDirectory = getThreadDirectory(i);
            if (!prepareIndex(threadDirectory)) {
                log.warn("Can't prepare directory for crawl thread: " + threadDirectory);
                return false;
            }
        }
        return true;
    }

    private void removeHostsFromIndex(Set<HostStats> hosts) {
        log.trace("Start cleaning target index directory from set of indexed hosts");
        for (HostStats host : hosts) {
            final String hostTerm = host.getProtocol() + "_" + host.getIp();
            final String hostName = HostnameResolver.getInstance().resolveByIp(host.getIp().toString());

            log.debug("Cleaning target index directory from indexed host: " + hostTerm.replace("_", "://") +
                    ", hostname: " + hostName);

            IndexOperator.deleteByHost(indexDirectory, hostTerm, hostName);
        }
        log.trace("Finished cleaning target index directory from set of indexed hosts");
    }

    private void cleanTargetIndex() {
        log.trace("Start cleaning target index directory: " + indexDirectory);
        if (daysToKeep == 0) {
            IndexOperator.deleteAll(indexDirectory);
            log.trace("Target index directory wiped out");
        } else {
            log.trace("Start cleaning target index directory from old items");
            IndexOperator.deleteByAge(indexDirectory, daysToKeep);
            log.trace("Finished cleaning target index directory from old items");
        }
        log.trace("Target index directory cleaned up.");
    }

    private void mergeIntoIndex(String threadName) {
        int index = Integer.valueOf(threadName.substring(THREAD_PREFIX.length()));
        Set<String> dirs = new HashSet<String>();
        dirs.add(getThreadDirectory(index));
        IndexOperator.merge(indexDirectory, dirs);
    }

    /**
     * Parses the IP ranges string.
     * <p/>
     * The string can be either path to a file with IP ranges or comma-separated list of string representation of IP
     * ranges.
     *
     * @param rangesString Either path to a file with IP ranges or comma-separated list of string representation of IP ranges.
     * @return list of IP ranges. May return empty list, never null.
     */
    private static List<IpRange> parseRanges(String rangesString) {
        List<IpRange> result = IpRanges.parseList(rangesString);
        if (result.isEmpty()) {
            File file = new File(PunksearchFs.resolve(rangesString));
            if (file.exists()) {
                result = loadRangesFromFile(file);
            } else {
                log.warn("Can't find IP ranges file: '" + file.getAbsolutePath() + "'");
            }
        }
        return result;
    }

    /**
     * Reads a file and creates list of IpRanges from it.
     * <p/>
     * The file may be of random format, the single restriction is IP should be in the first column. Each row of the
     * file must be either comment (starts with "#") or start with IP or IP range.
     * <p/>
     * Example:
     * <p/>
     * <pre>
     * # this is a comment before single ip
     * 10.20.30.40
     * # another comment before ip range
     * 11.22.33.44-11.22.33.55
     * # comment before long row, the tail after first comma is ignored
     * 22.33.44.55, smth else, foo
     * </pre>
     *
     * @param file file to get ip ranges from
     * @return list of IpRanage objects
     */
    @SuppressWarnings("unchecked")
    private static List<IpRange> loadRangesFromFile(File file) {
        Set<IpRange> result = new HashSet<IpRange>();
        try {
            List<String> lines = FileUtils.readLines(file);
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split(",");
                if (IpRange.isIpRange(parts[0].trim())) {
                    result.add(new IpRange(parts[0].trim()));
                }
            }
        } catch (IOException e) {
            log.warn("Can't load ranges from file: " + file.getAbsolutePath());
        }
        ArrayList<IpRange> list = new ArrayList<IpRange>(result);
        Collections.sort(list);
        return list;
    }

    private static String getThreadDirectory(int index) {
        return getTempDir() + "punksearch_crawler" + index;
    }

    /**
     * @return temp dir ending by file.separator
     */
    private static String getTempDir() {
        String tempDir = System.getProperty(TMP_DIR_PROPERTY);

        if (tempDir == null || tempDir.length() == 0) {
            tempDir = System.getProperty("java.io.tmpdir");
        }

        if (!tempDir.endsWith(File.separator)) {
            tempDir += File.separator;
        }

        return tempDir;
    }

    private HostCrawler makeThread(int index, IpIterator iter) {
        return new HostCrawler(THREAD_PREFIX + index, iter, fileTypes, getThreadDirectory(index));
    }

    private static void cleanTempForThread(String threadName) {
        int index = Integer.valueOf(threadName.substring(THREAD_PREFIX.length()));
        try {
            FileUtils.deleteDirectory(new File(getThreadDirectory(index)));
        } catch (IOException e) {
            log.warn("Temp directory '" + getThreadDirectory(index) + "' was not cleaned up. Check permissions");
        }
    }

    private boolean prepareIndex(String dir) {
        if (!IndexOperator.indexExists(dir)) {
            try {
                IndexOperator.createIndex(dir);
            } catch (IOException e) {
                log.error("Can't create index directory: '" + dir + "'!");
                return false;
            }
        }

        if (IndexOperator.isLocked(dir)) {
            if (forceUnlock) {
                IndexOperator.unlock(dir);
            } else {
                log.warn("Index directory is locked: '" + dir + "' "
                        + "Consider to set \"*.crawler.forceunlock=true\" in punksearch.properties");
                return false;
            }
        }

        return true;
    }

    private class MaxRunWatchDog extends TimerTask {
        public void run() {
            log.info("Stopping crawling due to time limit");
            NetworkCrawler.getInstance().stop();
        }
    }

    private class ThreadStatusDump extends TimerTask {

        public static final String STATUS_FILENAME = "punksearch-crawl.status";

        public void run() {
            List<HostCrawler> threads = NetworkCrawler.getInstance().getThreads();
            String dump = "";
            for (HostCrawler thread : threads) {
                boolean stop = thread.isStopRequested();
                String status = "unknown";
                if (stop) {
                    if (thread.getIp() != null) {
                        status = "stopping";
                    } else {
                        status = "stopped manually";
                    }
                } else {
                    if (thread.getIp() != null) {
                        status = "crawling " + thread.getIp();
                    } else {
                        status = "finished successfully";
                    }
                }
                dump += thread.getName() + " : " + status + " : " + thread.getCrawledHosts().size() + "\n";
            }
            String path = getTempDir() + STATUS_FILENAME;
            try {
                FileUtils.writeStringToFile(new File(path), dump);
            } catch (IOException e) {
                log.warn("Can't write crawler status to file: " + path);
            }
        }
    }
}
