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
 * Wrapper class for FileType objects. Consider using static factory methods to instantiate this class.
 * 
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class FileTypes {

	public static final String    DEFAULT_CONFIG_FILE = "filetypes.conf";

	private Map<String, FileType> types               = new HashMap<String, FileType>();

	public FileTypes(Map<String, FileType> map) {
		types = map;
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

	private static FileType makeItemType(String[] chunks) {
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

	public static FileTypes readFromDefaultFile() {
		String path = PunksearchFs.resolve("conf" + File.separator + DEFAULT_CONFIG_FILE);
		return readFromFile(new File(path));
	}

	@SuppressWarnings("unchecked")
    public static FileTypes readFromFile(File file) {
		try {
			Map<String, FileType> map = new HashMap<String, FileType>();
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
				FileType type = makeItemType(chunks);
				map.put(type.getTitle(), type);
			}
			FileTypes result = new FileTypes(map);
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
