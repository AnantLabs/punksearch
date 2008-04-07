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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.punksearch.common.FileTypes;
import org.punksearch.ip.IpRange;
import org.punksearch.ip.SynchronizedIpIterator;

/**
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class NetworkCrawler implements Runnable {
	private static Logger     __log      = Logger.getLogger(NetworkCrawler.class.getName());

	private FileTypes         fileTypes  = new FileTypes();
	private String            indexDirectory;

	private List<HostCrawler> threadList = new ArrayList<HostCrawler>();

	public NetworkCrawler(String indexDir) {
		fileTypes.readFromDefaultFile();
		this.indexDirectory = indexDir;
	}

	public void run() {
		String range = System.getProperty("org.punksearch.crawler.range");
		int threadCount = Integer.parseInt(System.getProperty("org.punksearch.crawler.threads"));

		SynchronizedIpIterator iter = new SynchronizedIpIterator(parseRanges(range));

		threadList.clear();

		try {
			long startTime = new Date().getTime();
			__log.info("Crawl process started");

			for (int i = 0; i < threadCount; i++) {
				// FileUtils.deleteDirectory(new File(indexDirectory + "_IndexerThread" + i));
				HostCrawler indexerThread = new HostCrawler("HostCrawler" + i, iter, fileTypes, indexDirectory + "_crawler" + i);
				// indexerThread.setDaemon(true);
				indexerThread.start();
				threadList.add(indexerThread);
			}
			Set<String> crawledHosts = new HashSet<String>();
			for (HostCrawler crawlerThread : threadList) {
				crawlerThread.join();
				__log.info("Crawl thread joined: " + crawlerThread.getName());
				crawledHosts.addAll(crawlerThread.getCrawledHosts());
			}

			cleanup(indexDirectory, crawledHosts);
			__log.info("Target index directory cleaned up: " + indexDirectory);
			
			merge(indexDirectory, threadCount);
			__log.info("Temp index directories merged into target index directory");

			for (int i = 0; i < threadCount; i++) {
				FileUtils.deleteDirectory(new File(indexDirectory + "_crawler" + i));
			}
			__log.info("Temp index directories cleaned up");
			
			// IndexOperator.getInstance().optimizeIndex();
			// IndexOperator.getInstance().flushIndex();

			long finishTime = new Date().getTime();
			__log.info("Crawl process finished in " + ((finishTime - startTime) / 1000) + " sec");
		} catch (Exception e) {
			__log.warning("NetworkCrawler.run(): exception occured. " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void cleanup(String indexDirectory, Set<String> crawled) {
		if (Boolean.parseBoolean(System.getProperty("org.punksearch.crawler.fromscratch"))) {
			IndexOperator.deleteAll(indexDirectory);
		} else {
			for (String host : crawled) {
				IndexOperator.deleteByHost(indexDirectory, host);
			}
			int daysToKeep = Integer.parseInt(System.getProperty("org.punksearch.crawler.keepdays"));
			IndexOperator.deleteByAge(indexDirectory, daysToKeep);
		}
	}

	private void merge(String indexDir, int count) {
		Set<String> dirs = new HashSet<String>();
		for (int i = 0; i < count; i++) {
			dirs.add(indexDir + "_crawler" + i);
		}
		IndexOperator.merge(indexDir, dirs);
	}

	public void stop() {
		for (Thread thread : threadList) {
			thread.interrupt();
		}
	}

	public List<HostCrawler> getThreads() {
		return threadList;
	}

	public List<IpRange> parseRanges(String rangesString) {
		List<IpRange> result = new ArrayList<IpRange>();
		String[] rangeChunks = rangesString.split(",");
		for (String chunk : rangeChunks) {
			result.add(new IpRange(chunk));
		}
		return result;
	}
}
