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
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class CrawlerMain {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) {
		try {
	        PunksearchProperties.loadDefault();
        } catch (FileNotFoundException e) {
        	System.err.println("Can't find the properties file: " + e.getMessage());
        	System.exit(1);
        }
		if (args.length > 0) {
			System.setProperty("org.punksearch.crawler.range", args[0]);
		}
		NetworkCrawler crawler = new NetworkCrawler();
		crawler.run();
	}

}
