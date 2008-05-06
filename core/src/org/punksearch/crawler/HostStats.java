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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.punksearch.ip.Ip;

/**
 * Statistics for a crawled host
 * 
 * @author Yury Soldak (ysoldak@gmail.com)
 * 
 */
public class HostStats implements Comparable<HostStats> {
	private static Logger      __log       = Logger.getLogger(HostStats.class.getName());

	public static final String DUMP_PREFIX = "hosts-";
	public static final String DUMP_SUFFIX = ".csv";

	private Ip                 ip;
	private String             protocol;
	private long               size;
	private long               count;

	public HostStats(String ip, String protocol, long size, long count) {
		this(new Ip(ip), protocol, size, count);
	}

	public HostStats(Ip ip, String protocol, long size, long count) {
		this.ip = ip;
		this.protocol = protocol;
		this.size = size;
		this.count = count;
	}

	public Ip getIp() {
		return ip;
	}

	public String getProtocol() {
		return protocol;
	}

	public long getSize() {
		return size;
	}

	public long getCount() {
		return count;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof HostStats) {
			return (compareTo((HostStats) obj) == 0 && size == ((HostStats) obj).size && count == ((HostStats) obj).count);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return protocol.hashCode() + ip.hashCode() + Long.valueOf(size).hashCode();
	}

	public int compareTo(HostStats obj) {
		int ips = ip.compareTo(obj.getIp());
		if (ips != 0) {
			return ips;
		} else {
			return protocol.compareTo(obj.getProtocol());
		}
	}

	@Override
	public String toString() {
		return ip + "," + protocol + "," + size + "," + count;
	}

	public static void merge(String inputDirPath, String outFilePath) {
		File dir = new File(inputDirPath);

		String[] fileNames = dir.list();
		List<String> list = new ArrayList<String>();
		for (String fileName : fileNames) {
			if (fileName.startsWith(DUMP_PREFIX) && fileName.endsWith(DUMP_SUFFIX)) {
				list.add(fileName);
			}
		}
		fileNames = list.toArray(new String[list.size()]);
		if (fileNames.length > 0) {
			Arrays.sort(fileNames);
			String dirPath = dir.getAbsolutePath() + File.separator;
			List<HostStats> result = parse(dirPath + fileNames[fileNames.length - 1]);
			if (fileNames.length > 1) {
				for (int i = fileNames.length - 2; i >= 0; i--) {
					List<HostStats> curHostStats = parse(dirPath + fileNames[i]);
					for (HostStats curHS : curHostStats) {
						boolean found = false;
						for (HostStats resultHS : result) {
							if (curHS.compareTo(resultHS) == 0) {
								found = true;
								break;
							}
						}
						if (!found) {
							result.add(curHS);
						}
					}
				}
			}
			Collections.sort(result);
			try {
				FileUtils.writeLines(new File(outFilePath), result);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static List<HostStats> parse(String hostsFilePath) {
		List<HostStats> result = new ArrayList<HostStats>();
		try {
			List<String> lines = FileUtils.readLines(new File(hostsFilePath));
			for (String line : lines) {
				String[] parts = line.trim().split(",");
				if (line.trim().startsWith("#") || parts.length < 4) {
					continue;
				}
				HostStats hs = new HostStats(parts[0], parts[1], Long.parseLong(parts[2]), Long.parseLong(parts[3]));
				result.add(hs);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public static void dump(String dirPath, List<HostStats> hostStats) {
		Collections.sort(hostStats);

		File dir = new File(dirPath);
		if (!dir.exists() && !dir.mkdir()) {
			__log.info("Can't make directory (check permissions and free space): " + dirPath);
			return;
		}

		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		String fileName = DUMP_PREFIX + df.format(new Date()) + DUMP_SUFFIX;
		File dumpFile = new File(dirPath + fileName);
		try {
			FileUtils.writeLines(dumpFile, hostStats);
		} catch (IOException e) {
			__log.warning("Can't dump host stats into (check permissions and free space): " + dirPath + fileName);
		}
	}

}
