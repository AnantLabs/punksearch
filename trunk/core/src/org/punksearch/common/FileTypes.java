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
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

/**
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class FileTypes {

	private static final String   DEFAULT_CONFIG_FILE = "filetypes.conf";

	private Map<String, FileType> types               = new HashMap<String, FileType>();

	public void readFromDefaultFile() {
		String home = System.getenv("PUNKSEARCH_HOME");
		if (home == null) {
			home = System.getProperty("user.dir");
		}
		readFromFile(new File(home + System.getProperty("file.separator") + DEFAULT_CONFIG_FILE));
	}

	public void readFromFile(File file) {
		try {
			List<String> lines = FileUtils.readLines(file);
			for (String line : lines) {
				line = line.trim();
				if (line.startsWith("#") || line.length() == 0) {
					continue;
				}
				String[] chunks = line.split(":");
				if (chunks.length != 4) {
					System.out.println("Line skipped, wrong count of columns: " + line);
					continue;
				}
				System.out.println(line);
				FileType type = makeItemType(chunks);
				types.put(type.getTitle(), type);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public FileType get(String title) {
		return types.get(title);
	}

	public Set<String> list() {
		return types.keySet();
	}

	public boolean isExtension(String ext) {
		for (FileType type : types.values()) {
			if (type.getExtensions().contains(ext.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	private FileType makeItemType(String[] chunks) {
		FileType type = new FileType();
		type.setTitle(chunks[0].trim());
		type.setExtensions(chunks[3].trim().split(","));
		int min = parseSize(chunks[1].trim());
		if (min > 0) {
			type.setMinBytes(min);
		}
		int max = parseSize(chunks[2].trim());
		if (max > 0) {
			type.setMaxBytes(max);
		}
		return type;
	}

	protected static int parseSize(String size) {
		if (size.length() == 0) {
			size = "0";
		}
		int lastChar = size.length();
		int multiplier = 1;
		if (size.endsWith("K") || size.endsWith("M") || size.endsWith("G")) {
			lastChar = size.length() - 1;
			if (size.endsWith("K")) {
				multiplier = 1024;
			} else if (size.endsWith("M")) {
				multiplier = 1024 * 1024;
			} else if (size.endsWith("G")) {
				multiplier = 1024 * 1024 * 1024;
			}
		}
		return Integer.parseInt(size.substring(0, lastChar)) * multiplier;
	}
}
