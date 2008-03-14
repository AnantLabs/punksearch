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

import org.punksearch.commons.PunksearchProperties;
import org.punksearch.crawler.NetworkCrawler;

/**
 * @author Yury Soldak (ysoldak@gmail.com)
 *
 */
public class CrawlerMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 0) {
			PunksearchProperties.setProperty("org.punksearch.crawler.range", args[0]);
		}
		NetworkCrawler crawler = new NetworkCrawler(PunksearchProperties.getProperty("org.punksearch.index.dir"));
		crawler.run();
	}

}
