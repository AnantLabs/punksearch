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
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.punksearch.common.FileTypes;
import org.punksearch.common.PunksearchProperties;
import org.punksearch.ip.IpRange;
import org.punksearch.ip.SynchronizedIpIterator;

/**
 * The crawling process manager. It starts crawling threads, cleans target index, merges data crawled by threads into
 * the target index, cleans temporary files.
 * 
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class NetworkCrawler implements Runnable {
	private static Logger       __log             = Logger.getLogger(NetworkCrawler.class.getName());

	public static final String  TMP_DIR_PROPERTY  = "org.punksearch.crawler.tmpdir";
	public static final String  UNLOCK_PROPERTY   = "org.punksearch.crawler.forceunlock";
	public static final String  THREADS_PROPERTY  = "org.punksearch.crawler.threads";
	public static final String  RANGE_PROPERTY    = "org.punksearch.crawler.range";
	public static final String  KEEPDAYS_PROPERTY = "org.punksearch.crawler.keepdays";

	private static final String THREAD_PREFIX     = "HostCrawler";
	private static final String HOSTS_DUMP        = "hosts.csv";

	private FileTypes           fileTypes;
	private String              indexDirectory;
	private boolean             forceUnlock;
	private List<IpRange>       ranges;
	private int                 threadCount;
	private float               daysToKeep;

	private List<HostCrawler>   threadList        = new ArrayList<HostCrawler>();

	public NetworkCrawler() {
		this.indexDirectory = PunksearchProperties.resolveIndexDirectory();
		this.forceUnlock = Boolean.valueOf(System.getProperty(UNLOCK_PROPERTY));
		this.threadCount = Integer.parseInt(System.getProperty(THREADS_PROPERTY));
		this.ranges = parseRanges(System.getProperty(RANGE_PROPERTY));
		this.fileTypes = FileTypes.readFromDefaultFile();
		this.daysToKeep = Float.parseFloat(System.getProperty(KEEPDAYS_PROPERTY));
	}

	public NetworkCrawler(String indexDir, boolean unlock, int threads, String ranges, FileTypes fileTypes, float days) {
		this.indexDirectory = indexDir;
		this.forceUnlock = unlock;
		this.threadCount = threads;
		this.ranges = parseRanges(ranges);
		this.fileTypes = fileTypes;
		this.daysToKeep = days;
	}

	public void run() {

		if (!prepareIndex(indexDirectory)) {
			__log.warning("Can't start crawling. Something wrong with index directory (check log).");
			return;
		}
		if (ranges.size() == 0) {
			__log.warning("Can't start crawling. The list of IPs to crawl is empty.");
			return;
		}

		SynchronizedIpIterator iter = new SynchronizedIpIterator(ranges);
		threadList.clear();

		try {

			long startTime = new Date().getTime();
			__log.info("Crawl process started");

			for (int i = 0; i < threadCount; i++) {
				if (!prepareIndex(getThreadDirectory(i))) {
					__log.warning("Cancel crawling. Can't make directory for crawl thread: " + getThreadDirectory(i));
					stop();
					return;
				}
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
					__log.info(thread.getName() + " finished");
				} catch (InterruptedException e) {
					__log.warning(thread.getName() + " was interrupted");
				}
			}

			if (hosts.size() > 0) {
				dumpHosts(hosts);
				IndexOperator.optimize(indexDirectory);
			}

			threadList.clear();

			long finishTime = new Date().getTime();
			__log.info("Crawl process finished in " + ((finishTime - startTime) / 1000) + " sec");
		} catch (Exception e) {
			__log.warning("NetworkCrawler.run(): exception occured. " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void removeHostsFromIndex(Set<HostStats> hosts) {
		__log.fine("Start cleaning target index directory from set of indexed hosts");
		for (HostStats host : hosts) {
			__log.info("Cleaning target index directory from indexed host: " + host);
			IndexOperator.deleteByHost(indexDirectory, host.getProtocol() + "_" + host.getIp());
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

	public void stop() {
		for (Thread thread : threadList) {
			thread.interrupt();
		}
	}

	public List<HostCrawler> getThreads() {
		return threadList;
	}

	/**
	 * Parses the IP ranges string.
	 * 
	 * The string can be either path to a file with IP ranges or comma-separated list of string representation of IP
	 * ranges.
	 * 
	 * @param rangesString
	 *            either path to a file with IP ranges or comma-separated list of string representation of IP ranges.
	 * @return list of IP ranges. May return empty list, never null.
	 */
	private static List<IpRange> parseRanges(String rangesString) {
		String[] parts = rangesString.split(",");
		if (IpRange.isIpRange(parts[0])) {
			List<IpRange> result = new ArrayList<IpRange>();
			String[] ranges = rangesString.split(",");
			for (String range : ranges) {
				result.add(new IpRange(range));
			}
			return result;
		} else {
			String path;
			if (PunksearchProperties.isAbsolutePath(rangesString)) {
				path = rangesString;
			} else {
				path = PunksearchProperties.resolveHome() + System.getProperty("file.separator") + rangesString;
			}
			File file = new File(path);
			if (file.exists()) {
				return loadRangesFromFile(file);
			} else {
				__log.warning("Can't find IP ranges file: '" + file.getAbsolutePath() + "'");
				return new ArrayList<IpRange>();
			}
		}
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
	 *            either absolute or relative (to punksearch home) path to the file
	 * @return list of IpRanage objects
	 */
	private static List<IpRange> loadRangesFromFile(File file) {
		List<IpRange> result = new ArrayList<IpRange>();
		try {
			List<String> lines = FileUtils.readLines(file);
			for (String line : lines) {
				line = line.trim();
				if (line.startsWith("#")) {
					continue;
				}
				String[] chunks = line.split(",");
				if (IpRange.isIpRange(chunks[0].trim())) {
					result.add(new IpRange(chunks[0].trim()));
				}
			}
		} catch (IOException e) {
			__log.warning("Can't load ranges from file: " + file.getAbsolutePath());
		}
		return result;
	}

	private String getThreadDirectory(int index) {
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
		HostCrawler indexerThread = new HostCrawler(THREAD_PREFIX + index, iter, fileTypes, getThreadDirectory(index));
		return indexerThread;
	}

	private void cleanTempForThread(String threadName) throws IOException {
		int index = Integer.valueOf(threadName.substring(THREAD_PREFIX.length()));
		FileUtils.deleteDirectory(new File(getThreadDirectory(index)));
	}

	private static void dumpHosts(List<HostStats> crawledHosts) {
		Collections.sort(crawledHosts);
		File dumpFile = new File(PunksearchProperties.resolveHome() + System.getProperty("file.separator") + HOSTS_DUMP);
		try {
			FileUtils.writeLines(dumpFile, crawledHosts);
		} catch (IOException e) {
			__log.warning("Can't dump crawled hosts into '" + dumpFile.getAbsolutePath() + "': " + e.getMessage());
		}
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

}
