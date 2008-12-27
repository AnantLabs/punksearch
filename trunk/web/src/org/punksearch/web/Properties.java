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
package org.punksearch.web;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Yury Soldak (ysoldak@gmail.com)
 *
 */
public class Properties {

	public static Map<String, String> getPunksearchProperties() {
		Map<String, String> result = new TreeMap<String, String>();
		for (Object key : System.getProperties().keySet()) {
			if (key.toString().startsWith("org.punksearch") && System.getProperty(key.toString()).length() > 0) {
				result.put(key.toString(), System.getProperty(key.toString()));
			}
		}
		return result;
	}
	
}
