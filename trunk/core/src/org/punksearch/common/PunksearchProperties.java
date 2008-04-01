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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PunksearchProperties {

	private static String PROPERTIES_FILENAME = "punksearch.properties";

	public static void loadDefault() throws FileNotFoundException {
		String home = System.getenv("PUNKSEARCH_HOME");
		if (home == null) {
			home = System.getProperty("user.dir");
		}
		loadFromFile(home + System.getProperty("file.separator") + PROPERTIES_FILENAME);
	}

	public static void loadFromFile(String path) throws FileNotFoundException {
		Properties props = new Properties();
		InputStream inputStream = new FileInputStream(path);
		try {
			props.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (Object key : props.keySet()) {
			System.setProperty((String) key, (String) props.get(key));
		}
	}

	public static String resolveIndexDirectory() {
		String indexDir = System.getProperty("org.punksearch.index.dir");
		if (!isAbsolutePath(indexDir)) {
			String home = System.getenv("PUNKSEARCH_HOME");
			if (home == null) {
				home = System.getProperty("user.dir");
			}
			indexDir = home + System.getProperty("file.separator") + indexDir;
		}
		return indexDir;
	}

	private static boolean isAbsolutePath(String path) {
		return path.startsWith("/");
	}

}
