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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.punksearch.common.FileTypes;
import org.punksearch.common.PunksearchFs;
import org.punksearch.common.PunksearchProperties;
import org.punksearch.ip.IpRange;
import org.punksearch.ip.SynchronizedIpIterator;

/**
 * The crawling process manager. It starts crawling threads, cleans target index, merges data crawled by threads into
 * the target index, cleans temporary files.
 * 
 * @see HostCrawler
 * @see IndexOperator
 * @see HostStats
 * 
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class NetworkCrawler implements Runnable {
	private static Logger       __log             = Logger.getLogger(NetworkCrawler.class.getName());

	/**
	 * Use this property to customize directory for temporary indexes
	 */
	public static final String  TMP_DIR_PROPERTY  = "org.punksearch.crawler.tmpdir";
	/**
	 * Whatever to unlock main and temporary index directories
	 */
	public static final String  UNLOCK_PROPERTY   = "org.punksearch.crawler.forceunlock";
	/**
	 * Number of threads to use for crawling the network (use values between 1 and 10)
	 */
	public static final String  THREADS_PROPERTY  = "org.punksearch.crawler.threads";
	/**
	 * The comma separated list of IP ranges or path to the file with IPs.
	 */
	public static final String  RANGE_PROPERTY    = "org.punksearch.crawler.range";
	/**
	 * Lifetime of old items in the index (may be real number).
	 */
	public static final String  KEEPDAYS_PROPERTY = "org.punksearch.crawler.keepdays";
	/**
	 * Maximum hours to wait until a crawling thread to finish, then interrupt it.
	 */
	public static final String  MAXHOURS_PROPERTY = "org.punksearch.crawler.maxhours";

	private static final String THREAD_PREFIX     = "HostCrawler";

	private FileTypes           fileTypes;
	private String              indexDirectory;
	private boolean             forceUnlock;
	private List<IpRange>       ranges;
	private int                 threadCount;
	private float               daysToKeep;
	private int                 maxHours;

	private List<HostCrawler>   threadList        = new ArrayList<HostCrawler>();

	/**
	 * The default constructor. Extracts configuration from system properties. The system property names are defined by
	 * static final fields of this class.
	 */
	public NetworkCrawler() {
		this.indexDirectory = PunksearchFs.resolveIndexDirectory();
		this.forceUnlock = Boolean.valueOf(System.getProperty(UNLOCK_PROPERTY, "false"));
		this.threadCount = Integer.parseInt(System.getProperty(THREADS_PROPERTY, "5"));
		this.ranges = parseRanges(System.getProperty(RANGE_PROPERTY));
		this.fileTypes = FileTypes.readFromDefaultFile();
		this.daysToKeep = Float.parseFloat(System.getProperty(KEEPDAYS_PROPERTY, "7"));
		this.maxHours = Integer.parseInt(System.getProperty(MAXHOURS_PROPERTY, "12"));
	}

	/**
	 * The custom constructor. Must specify all the configuration properties.
	 * 
	 * @param indexDir
	 *            Path to directory where main index should be located.
	 * @param unlock
	 *            Whatever to unlock main and temporary index directories.
	 * @param threads
	 *            Number of threads to use for crawling the network (use values between 1 and 10).
	 * @param ranges
	 *            Comma separated list of IP ranges or path to the file with IPs.
	 * @param fileTypes
	 *            Collection of known file types.
	 * @param days
	 *            Lifetime of old items in the index.
	 */
	public NetworkCrawler(String indexDir, boolean unlock, int threads, String ranges, FileTypes fileTypes, float days) {
		this.indexDirectory = indexDir;
		this.forceUnlock = unlock;
		this.threadCount = threads;
		this.ranges = parseRanges(ranges);
		this.fileTypes = fileTypes;
		this.daysToKeep = days;
		this.maxHours = 12;
	}

	/**
	 * Signals all threads to stop crawling.
	 */
	public void stop() {
		for (Thread thread : threadList) {
			thread.interrupt();
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
	public void run() {

		if (!prepareAllIndexDirs()) {
			__log.warning("Can't start crawling. Something wrong with an index directory (check log).");
			return;
		}

		if (ranges.size() == 0) {
			__log.warning("Can't start crawling. The list of IPs to crawl is empty.");
			return;
		}

		long startTime = new Date().getTime();
		__log.info("Crawl process started");

		Timer processTimer = new Timer();
		processTimer.schedule(new MaxRunWatchDog(this), maxHours * 3600 * 1000L);

		SynchronizedIpIterator iter = new SynchronizedIpIterator(ranges);
		threadList.clear();
		for (int i = 0; i < threadCount; i++) {
			HostCrawler indexerThread = makeThread(i, iter);
			indexerThread.start();
			threadList.add(indexerThread);
		}

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
				removeHostsFromIndex(thread.getCrawledHosts());
				mergeIntoIndex(thread.getName());
				cleanTempForThread(thread.getName());
			} catch (InterruptedException e) {
				__log.warning(thread.getName() + " was interrupted");
			} catch (IOException e) {
				__log.warning("Temp directory for thread '" + thread.getName()
				        + "' was not cleaned up. Check permissions");
			}
			__log.info(thread.getName() + " finished");
		}
		if (hosts.size() > 0) {
			HostStats.dump(PunksearchFs.resolveStatsDirectory(), hosts);
			HostStats.merge(PunksearchFs.resolveStatsDirectory(), PunksearchFs.resolve("hosts.csv"));
		}

		// should always optimize, since some old items could have been deleted and no one new host crawled.
		IndexOperator.optimize(indexDirectory);

		long finishTime = new Date().getTime();
		__log.info("Crawl process finished in " + ((finishTime - startTime) / 1000) + " sec");

		threadList.clear();
		processTimer.cancel();
	}

	private boolean prepareAllIndexDirs() {
		if (!prepareIndex(indexDirectory)) {
			__log.warning("Can't prepare main index directory (check log).");
			return false;
		}
		for (int i = 0; i < threadCount; i++) {
			if (!prepareIndex(getThreadDirectory(i))) {
				__log.warning("Can't prepare directory for crawl thread: " + getThreadDirectory(i));
				return false;
			}
		}
		return true;
	}

	private void removeHostsFromIndex(Set<HostStats> hosts) {
		__log.fine("Start cleaning target index directory from set of indexed hosts");
		for (HostStats host : hosts) {
			String hostTerm = host.getProtocol() + "_" + host.getIp();
			__log.info("Cleaning target index directory from indexed host: " + hostTerm);
			IndexOperator.deleteByHost(indexDirectory, hostTerm);
		}
		__log.fine("Finished cleaning target index directory from set of indexed hosts");
	}

	private void cleanTargetIndex() {
		__log.fine("Start cleaning target index directory: " + indexDirectory);
		if (daysToKeep == 0) {
			IndexOperator.deleteAll(indexDirectory);
			__log.fine("Target index directory wiped out");
		} else {

			__log.fine("Start cleaning target index directory from old items");
			IndexOperator.deleteByAge(indexDirectory, daysToKeep);
			__log.fine("Finished cleaning target index directory from old items");

		}
		__log.fine("Target index directory cleaned up.");
	}

	private void mergeIntoIndex(String threadName) {
		int index = Integer.valueOf(threadName.substring(THREAD_PREFIX.length()));
		Set<String> dirs = new HashSet<String>();
		dirs.add(getThreadDirectory(index));
		IndexOperator.merge(indexDirectory, dirs);
	}

	/**
	 * Parses the IP ranges string.
	 * 
	 * The string can be either path to a file with IP ranges or comma-separated list of string representation of IP
	 * ranges.
	 * 
	 * @param rangesString
	 *            Either path to a file with IP ranges or comma-separated list of string representation of IP ranges.
	 * @return list of IP ranges. May return empty list, never null.
	 */
	private static List<IpRange> parseRanges(String rangesString) {
		List<IpRange> result = IpRange.parseList(rangesString);
		if (result.isEmpty()) {
			File file = new File(PunksearchFs.resolve(rangesString));
			if (file.exists()) {
				result = loadRangesFromFile(file);
			} else {
				__log.warning("Can't find IP ranges file: '" + file.getAbsolutePath() + "'");
			}
		}
		return result;
	}

	/**
	 * Reads a file and creates list of IpRanges from it.
	 * 
	 * The file may be of random format, the single restriction is IP should be in the first column. Each row of the
	 * file must be either comment (starts with "#") or start with IP or IP range.
	 * 
	 * Example:
	 * 
	 * <pre>
	 * # this is a comment before single ip
	 * 10.20.30.40
	 * # another comment before ip range
	 * 11.22.33.44-11.22.33.55
	 * # comment before long row, the tail after first comma is ignored
	 * 22.33.44.55, smth else, foo
	 * </pre>
	 * 
	 * @param path
	 *            Either absolute or relative (to punksearch home) path to the file
	 * @return list of IpRanage objects
	 */
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
			__log.warning("Can't load ranges from file: " + file.getAbsolutePath());
		}
		ArrayList<IpRange> list = new ArrayList<IpRange>(result);
		Collections.sort(list);
		return list;
	}

	private static String getThreadDirectory(int index) {
		String tempDir = System.getProperty(TMP_DIR_PROPERTY);
		if (tempDir == null || tempDir.length() == 0) {
			tempDir = System.getProperty("java.io.tmpdir");
		}
		if (!tempDir.endsWith(System.getProperty("file.separator"))) {
			tempDir += System.getProperty("file.separator");
		}
		return tempDir + "punksearch_crawler" + index;
	}

	private HostCrawler makeThread(int index, SynchronizedIpIterator iter) {
		return new HostCrawler(THREAD_PREFIX + index, iter, fileTypes, getThreadDirectory(index));
	}

	private static void cleanTempForThread(String threadName) throws IOException {
		int index = Integer.valueOf(threadName.substring(THREAD_PREFIX.length()));
		FileUtils.deleteDirectory(new File(getThreadDirectory(index)));
	}

	private boolean prepareIndex(String dir) {
		if (!IndexOperator.indexExists(dir)) {
			try {
				IndexOperator.createIndex(dir);
			} catch (IOException e) {
				__log.severe("Can't create index directory: '" + dir + "'!");
				return false;
			}
		}

		if (IndexOperator.isLocked(dir)) {
			if (forceUnlock) {
				IndexOperator.unlock(dir);
			} else {
				__log.info("Index directory is locked: '" + dir + "' "
				        + "Consider to set \"*.crawler.forceunlock=true\" in punksearch.properties");
				return false;
			}
		}

		return true;
	}

	private class MaxRunWatchDog extends TimerTask {

		private NetworkCrawler crawler;

		public MaxRunWatchDog(NetworkCrawler crawler) {
			this.crawler = crawler;
		}

		public void run() {
			crawler.stop();
		}
	}
}
