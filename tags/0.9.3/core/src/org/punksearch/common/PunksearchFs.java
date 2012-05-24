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
package org.punksearch.common;

import java.io.File;

/**
 * Utility class to deal with Punksearch's filesystem artifacts. Helps to resolve path to index directory, etc.
 * 
 * @author Yury Soldak (ysoldak@gmail.com)
 * 
 */
public class PunksearchFs {
	
	public static String resolveIndexDirectory() {
		return resolve(System.getProperty("org.punksearch.index.dir"));
	}

	public static String resolveStatsDirectory() {
		return resolve("stats");
	}

	public static String resolveHome() {
		String home = System.getProperty("org.punksearch.home");
		if (home == null) {
			home = System.getenv("PUNKSEARCH_HOME");
			if (home == null) {
				home = System.getProperty("user.dir");
			}
		}
		return home;
	}

	/**
	 * Leaves absolute paths as is and resolves relative paths using punksearch's home directory as root.
	 * 
	 * @param path Path to resolve, absolute of relative
	 * @return Resolved absolute path
	 */
	public static String resolve(String path) {
		if (isAbsolutePath(path)) {
			return path;
		} else {
			return resolveHome() + File.separator + path;
		}
	}

	public static boolean isAbsolutePath(String path) {
		if (System.getProperty("os.name").contains("Windows")) {
			return path.substring(1).startsWith(":");
		} else {
			return path.startsWith("/");
		}
	}
}
