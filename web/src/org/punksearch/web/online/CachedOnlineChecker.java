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
import java.util.HashMap;
import java.util.Map;

import org.punksearch.common.OnlineChecker;

public class CachedOnlineChecker {

	public static final long               TIMEOUT;

	private static Map<String, HostStatus> cache = Collections.synchronizedMap(new HashMap<String, HostStatus>());

	static {
		String timeoutStr = System.getProperty("org.punksearch.online.cache.timeout");
		TIMEOUT = (timeoutStr != null) ? Long.parseLong(timeoutStr) * 1000 : 600 * 1000;
	}

	public static boolean isOnline(String host) {
		long now = System.currentTimeMillis();
		HostStatus hs = cache.get(host);

		if ((hs != null) && (hs.date + TIMEOUT > now)) {
			return hs.online;
		}

		// TODO: avoid active check several times if several threads want to check simultaneously
		boolean online = OnlineChecker.isOnline(host);
		if (hs != null) {
			hs.date = now;
			hs.online = online;
		} else {
			hs = new HostStatus(now, online);
		}
		
		// TODO: clean very old items to avoid potential memory leak
		cache.put(host, hs);
		return online;
	}

	private static class HostStatus {

		long    date;
		boolean online;

		HostStatus(long date, boolean online) {
			this.date = date;
			this.online = online;
		}

	}
}
