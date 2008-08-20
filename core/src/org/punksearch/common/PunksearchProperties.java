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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * The loader of PUNKSearch's system properties.
 * 
 * 
 * @author Yury Soldak (ysoldak@gmail.com)
 * 
 */
public class PunksearchProperties {

	public static final String PROPERTIES_FILENAME = "punksearch.properties";

	public static void loadDefault() throws FileNotFoundException {
		loadFromFile(PunksearchFs.resolve("conf" + File.separator + PROPERTIES_FILENAME));
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

}
