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
import java.util.List;

import junit.framework.TestCase;

/**
 * @author Yury Soldak (ysoldak@gmail.com)
 *
 */
public class HostStatsTest extends TestCase {

	public void testMerge() throws IOException {
		File tmp = File.createTempFile("punksearch-test-", ".csv");
		tmp.deleteOnExit();
		HostStats.merge("test-data/hoststats", tmp.getAbsolutePath());
		List<HostStats> list = HostStats.parse(tmp.getAbsolutePath());
		assertEquals(3, list.size());
	}

	public void testParse() {
		List<HostStats> list = HostStats.parse("test-data/hoststats/hosts-20080102030405.csv");
		assertEquals(2, list.size());
	}

}
