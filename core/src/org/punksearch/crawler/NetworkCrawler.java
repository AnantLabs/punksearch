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

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.punksearch.common.FileTypes;
import org.punksearch.common.PunksearchFs;
import org.punksearch.ip.IpIterator;
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
	private static Log                  __log         = LogFactory.getLog(NetworkCrawler.class);

	private static final NetworkCrawler INSTANCE      = new NetworkCrawler();

	private static final String         THREAD_PREFIX = "HostCrawler";

	private FileTypes                   fileTypes;
	private String                      indexDirectory;
	private boolean                     forceUnlock;
	private int                         threadCount;
	private float                       daysToKeep;
	private int                         maxHours;
	private List<IpRange>               ranges;

	private List<HostCrawler>           threadList    = Collections.synchronizedList(new ArrayList<HostCrawler>());
	private Set<Timer>                  timers        = new HashSet<Timer>();

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
			__log.warn("Can't start crawling. Something wrong with an index directory (check log).");
			return;
		}

		if (ranges.size() == 0) {
			__log.warn("Can't start crawling. The list of IPs to crawl is empty.");
			return;
		}

		long startTime = new Date().getTime();
		__log.info("Crawl process started");

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
				__log.warn("Interrupted: " + thread.getName());
			}
			__log.info("Finished: " + thread.getName());
		}
		if (hosts.size() > 0) {
			String statsDir = PunksearchFs.resolveStatsDirectory();
			HostStats.dump(statsDir, hosts);
			HostStats.merge(statsDir, PunksearchFs.resolve(statsDir + File.separator + "hosts.csv"));
		}

		// should always optimize, since some old items could have been deleted and no one new host crawled.
		IndexOperator.optimize(indexDirectory);

		long finishTime = new Date().getTime();
		__log.info("Crawl process finished in " + ((finishTime - startTime) / 1000) + " sec");

		synchronized (threadList) {
			threadList.clear();
		}
		cancelTimers();
	}

	/**
	 * Extracts configuration from system properties.
	 * 
	 * The system property names are defined by static final fields of Settings class.
	 */
	private void readProperties() {
		this.indexDirectory = PunksearchFs.resolveIndexDirectory();
		this.forceUnlock = Boolean.valueOf(System.getProperty(Settings.UNLOCK_PROPERTY, "false"));
		this.threadCount = Integer.getInteger(Settings.THREADS_PROPERTY, 5);
		this.fileTypes = FileTypes.readFromDefaultFile();
		this.daysToKeep = Float.parseFloat(System.getProperty(Settings.KEEPDAYS_PROPERTY, "7"));
		this.maxHours = Integer.getInteger(Settings.MAXHOURS_PROPERTY, 12);
		this.ranges = parseRanges(System.getProperty(Settings.RANGE_PROPERTY));
	}

	private void startTimers() {
		Timer processTimer = new Timer();
		processTimer.schedule(new MaxRunWatchDog(), maxHours * 3600 * 1000L);

		Timer statusDumpTimer = new Timer();
		long dumpPeriod = Long.getLong(Settings.DUMP_STATUS_PERIOD, 10L) * 1000;
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
			__log.warn("Can't prepare main index directory (check log).");
			return false;
		}
		for (int i = 0; i < threadCount; i++) {
			if (!prepareIndex(getThreadDirectory(i))) {
				__log.warn("Can't prepare directory for crawl thread: " + getThreadDirectory(i));
				return false;
			}
		}
		return true;
	}

	private void removeHostsFromIndex(Set<HostStats> hosts) {
		__log.trace("Start cleaning target index directory from set of indexed hosts");
		for (HostStats host : hosts) {
			String hostTerm = host.getProtocol() + "_" + host.getIp();
			__log.debug("Cleaning target index directory from indexed host: " + hostTerm.replace("_", "://"));
			IndexOperator.deleteByHost(indexDirectory, hostTerm);
		}
		__log.trace("Finished cleaning target index directory from set of indexed hosts");
	}

	private void cleanTargetIndex() {
		__log.trace("Start cleaning target index directory: " + indexDirectory);
		if (daysToKeep == 0) {
			IndexOperator.deleteAll(indexDirectory);
			__log.trace("Target index directory wiped out");
		} else {

			__log.trace("Start cleaning target index directory from old items");
			IndexOperator.deleteByAge(indexDirectory, daysToKeep);
			__log.trace("Finished cleaning target index directory from old items");

		}
		__log.trace("Target index directory cleaned up.");
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
				__log.warn("Can't find IP ranges file: '" + file.getAbsolutePath() + "'");
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
			__log.warn("Can't load ranges from file: " + file.getAbsolutePath());
		}
		ArrayList<IpRange> list = new ArrayList<IpRange>(result);
		Collections.sort(list);
		return list;
	}

	private static String getThreadDirectory(int index) {
		String tempDir = System.getProperty(Settings.TMP_DIR_PROPERTY);
		if (tempDir == null || tempDir.length() == 0) {
			tempDir = System.getProperty("java.io.tmpdir");
		}
		if (!tempDir.endsWith(System.getProperty("file.separator"))) {
			tempDir += System.getProperty("file.separator");
		}
		return tempDir + "punksearch_crawler" + index;
	}

	private HostCrawler makeThread(int index, IpIterator iter) {
		return new HostCrawler(THREAD_PREFIX + index, iter, fileTypes, getThreadDirectory(index));
	}

	private static void cleanTempForThread(String threadName) {
		int index = Integer.valueOf(threadName.substring(THREAD_PREFIX.length()));
		try {
			FileUtils.deleteDirectory(new File(getThreadDirectory(index)));
		} catch (IOException e) {
			__log.warn("Temp directory '" + getThreadDirectory(index) + "' was not cleaned up. Check permissions");
		}
	}

	private boolean prepareIndex(String dir) {
		if (!IndexOperator.indexExists(dir)) {
			try {
				IndexOperator.createIndex(dir);
			} catch (IOException e) {
				__log.error("Can't create index directory: '" + dir + "'!");
				return false;
			}
		}

		if (IndexOperator.isLocked(dir)) {
			if (forceUnlock) {
				IndexOperator.unlock(dir);
			} else {
				__log.warn("Index directory is locked: '" + dir + "' "
				        + "Consider to set \"*.crawler.forceunlock=true\" in punksearch.properties");
				return false;
			}
		}

		return true;
	}

	private class MaxRunWatchDog extends TimerTask {
		public void run() {
			__log.info("Stopping crawling due to time limit");
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
			String path = System.getProperty("java.io.tmpdir") + File.separator + STATUS_FILENAME;
			try {
				FileUtils.writeStringToFile(new File(path), dump);
			} catch (IOException e) {
				__log.warn("Can't write crawler status to file: " + path);
			}
		}
	}

}
