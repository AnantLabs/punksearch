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
package org.punksearch.web.online;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.punksearch.common.OnlineChecker;

public class CachedOnlineChecker {

	public static final long               timeout;

	private static Map<String, HostStatus> cache = Collections.synchronizedMap(new HashMap<String, HostStatus>());

	static {
		String timeoutStr = System.getProperty("org.punksearch.online.cache.timeout");
		timeout = (timeoutStr != null) ? Long.parseLong(timeoutStr) * 1000 : 600 * 1000;
	}

	public static boolean isOnline(String host) {
		Date recheckLimit = new Date(System.currentTimeMillis() - timeout);
		HostStatus hs = cache.get(host);

		if (hs != null && hs.date.after(recheckLimit)) {
			return hs.online;
		}

		boolean online = OnlineChecker.isOnline(host);
		cache.put(host, new HostStatus(new Date(), online));
		return online;
	}

}

class HostStatus {

	Date    date;
	boolean online;

	HostStatus(Date date, boolean online) {
		this.date = date;
		this.online = online;
	}

}