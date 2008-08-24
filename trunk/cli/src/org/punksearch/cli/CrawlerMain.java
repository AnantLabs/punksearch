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

import java.io.FileNotFoundException;

import org.punksearch.common.PunksearchProperties;
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

		CrawlerMain cm = new CrawlerMain();
		cm.start();
	}

	public void start() throws InterruptedException {
		Thread crawlerThread = new Thread(NetworkCrawler.getInstance());
		crawlerThread.start();
		crawlerThread.join();
	}

}
