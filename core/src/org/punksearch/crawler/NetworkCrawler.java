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
import java.util.List;
import java.util.logging.Logger;

import org.punksearch.commons.FileTypes;
import org.punksearch.commons.PunksearchProperties;
import org.punksearch.ip.SynchronizedIpIterator;
import org.punksearch.ip.IpRange;

/**
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class NetworkCrawler implements Runnable {
	private static Logger     __log      = Logger.getLogger(NetworkCrawler.class.getName());

	private FileTypes         fileTypes  = new FileTypes();
	private String            indexDirectory;

	private List<HostCrawler> threadList = new ArrayList<HostCrawler>();

	public NetworkCrawler(String indexDir) {
		String file = NetworkCrawler.class.getClassLoader().getResource("standard.types").getFile();
		File standardTypes = new File(file);
		fileTypes.readFromFile(standardTypes);
		this.indexDirectory = indexDir;
	}

	public void run() {
		String range = PunksearchProperties.getProperty("org.punksearch.crawler.range");
		int threadCount = Integer.parseInt(PunksearchProperties.getProperty("org.punksearch.crawler.threads"));

		SynchronizedIpIterator iter = new SynchronizedIpIterator(parseRanges(range));

		threadList.clear();

		try {
			IndexOperator.init(indexDirectory);

			long startTime = new Date().getTime();
			__log.info("Indexing process started");

			for (int i = 0; i < threadCount; i++) {
				HostCrawler indexerThread = new HostCrawler("IndexerThread" + i, iter, fileTypes);
				// indexerThread.setDaemon(true);
				indexerThread.start();
				threadList.add(indexerThread);
			}
			for (Thread indexerThread : threadList) {
				indexerThread.join();
			}

			IndexOperator.getInstance().optimizeIndex();
			IndexOperator.getInstance().flushIndex();

			long finishTime = new Date().getTime();
			__log.info("Index process is finished in " + ((finishTime - startTime) / 1000) + " sec");
		} catch (Exception e) {
			__log.warning("Indexer.run(): exception occured. " + e.getMessage());
			e.printStackTrace();
		} finally {
			IndexOperator.close();
		}
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
