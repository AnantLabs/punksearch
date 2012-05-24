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
package org.punksearch.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.FileUtils;
import org.punksearch.common.PunksearchFs;
import org.punksearch.common.PunksearchProperties;
import org.punksearch.crawler.HostCrawler;
import org.punksearch.crawler.NetworkCrawler;

/**
 * Utility class to kick the crawling process from command-line
 * 
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class CrawlerMain {

	public static String DUMP_STATUS_PERIOD = "org.punksearch.cli.dump.status.period";

	private static long  dumpPeriodSec      = Long.getLong(DUMP_STATUS_PERIOD, 10L);

	public static void main(String[] args) throws InterruptedException {
		try {
			PunksearchProperties.loadDefault();
		} catch (FileNotFoundException e) {
			System.err.println("Can't find the properties file: " + e.getMessage());
			System.exit(1);
		}
		if (args.length > 0) {
			System.setProperty("org.punksearch.crawler.range", args[0]);
		}

		CrawlerMain cm = new CrawlerMain();
		cm.start();
	}

	public void start() throws InterruptedException {
		Thread crawlerThread = new Thread(NetworkCrawler.getInstance());
		crawlerThread.start();

		TimerTask dumpStatus = new StatusDumpTask();

		Timer timer = new Timer();
		timer.scheduleAtFixedRate(dumpStatus, dumpPeriodSec * 1000, dumpPeriodSec * 1000);

		crawlerThread.join();
		timer.cancel();
	}

	private class StatusDumpTask extends TimerTask {

		public static final String STATUS_FILENAME = "crawler.status";
		
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
			try {
				String path = PunksearchFs.resolve(STATUS_FILENAME);
				FileUtils.writeStringToFile(new File(path), dump);
			} catch (IOException e) {
				System.err.println("Can't write crawler status");
			}
		}
	}
}