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
import java.util.Map;
import java.util.Properties;

public class PunksearchProperties {

	public static final String PROPERTIES_FILENAME = "punksearch.properties";

	public static void loadDefault() throws FileNotFoundException {
		loadFromFile(resolveHome() + System.getProperty("file.separator") + PROPERTIES_FILENAME);
	}

	public static void loadFromFile(String path) throws FileNotFoundException {
		Properties props = new Properties();
		InputStream inputStream = new FileInputStream(path);
		try {
			props.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		for (Map.Entry<Object, Object> entry : props.entrySet()) {
			System.setProperty((String) entry.getKey(), (String) entry.getValue());
		}
	}

	public static String resolveIndexDirectory() {
		String indexDir = System.getProperty("org.punksearch.index.dir");
		if (!isAbsolutePath(indexDir)) {
			indexDir = resolveHome() + System.getProperty("file.separator") + indexDir;
		}
		return indexDir;
	}

	public static boolean isAbsolutePath(String path) {
		if (System.getProperty("os.name").contains("Windows")) {
			return path.substring(1).startsWith(":");
		} else {
			return path.startsWith("/");
		}
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

}
