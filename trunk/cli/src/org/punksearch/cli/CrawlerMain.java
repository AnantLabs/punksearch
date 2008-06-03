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

		final NetworkCrawler crawler = new NetworkCrawler();
		Thread crawlerThread = new Thread(crawler);
		crawlerThread.start();

		TimerTask dumpStatus = new TimerTask() {
			public void run() {
				List<HostCrawler> threads = crawler.getThreads();
				String dump = "";
				for (HostCrawler thread : threads) {
					String ip = (thread.getIp() == null) ? "inactive" : thread.getIp();
					dump += thread.getName() + " : " + ip + " : " + thread.getCrawledHosts().size() + "\n";
				}
				try {
					String path = PunksearchFs.resolve("crawler.status");
					FileUtils.writeStringToFile(new File(path), dump);
				} catch (IOException e) {
					System.out.println("Can't write crawler status");
				}
			}
		};

		Timer timer = new Timer();
		timer.scheduleAtFixedRate(dumpStatus, 60000, 60000);

		crawlerThread.join();
		timer.cancel();
	}

}
