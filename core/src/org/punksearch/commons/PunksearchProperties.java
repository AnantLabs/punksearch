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
package org.punksearch.commons;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PunksearchProperties {

	private static String     PROPERTIES_FILENAME = "punksearch.properties";
	private static Properties props;

	static {
		props = new Properties();
		InputStream inputStream = PunksearchProperties.class.getClassLoader().getResourceAsStream(PROPERTIES_FILENAME);

		if (inputStream == null) {
			throw new RuntimeException("property file '" + PROPERTIES_FILENAME + "' not found in the classpath");
		}

		try {
			props.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getProperty(String key) {
		return props.getProperty(key);
	}
	
	public static void setProperty(String key, String value) {
		props.setProperty(key, value);
	}

}
