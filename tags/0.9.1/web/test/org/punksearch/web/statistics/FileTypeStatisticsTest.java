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
package org.punksearch.web.statistics;

import java.util.Map;

import junit.framework.TestCase;

import org.punksearch.common.PunksearchFs;
import org.punksearch.common.PunksearchProperties;

/**
 * Not unit-test. Just for debugging and profiling.
 * 
 * @author Yury Soldak (ysoldak@gmail.com)
 *
 */
public class FileTypeStatisticsTest extends TestCase {

	/**
	 * Test method for {@link org.punksearch.web.statistics.FileTypeStatistics#count()}.
	 * @throws Exception 
	 */
	public void testCount() throws Exception {
		PunksearchProperties.loadDefault();
		System.out.println(PunksearchFs.resolveIndexDirectory());
		Map<String, Long> counts = FileTypeStatistics.count();
		System.out.println(counts.size());
	}

	/**
	 * Test method for {@link org.punksearch.web.statistics.FileTypeStatistics#size()}.
	 * @throws Exception 
	 */
	public void testSize() throws Exception {
		PunksearchProperties.loadDefault();
		System.out.println(PunksearchFs.resolveIndexDirectory());
		Map<String, Long> sizes = FileTypeStatistics.size();
		System.out.println(sizes.size());
	}

	/**
	 * Test method for {@link org.punksearch.web.statistics.FileTypeStatistics#totalSize()}.
	 * @throws Exception 
	 */
	public void testTotalSize() throws Exception {
		PunksearchProperties.loadDefault();
		System.out.println(PunksearchFs.resolveIndexDirectory());
		Long totalSize = FileTypeStatistics.totalSize();
		System.out.println(totalSize);
	}

}
