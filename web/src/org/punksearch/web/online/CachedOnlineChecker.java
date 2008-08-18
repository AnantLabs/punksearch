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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.punksearch.common.OnlineChecker;

public class CachedOnlineChecker {

	public static final String                           TIMEOUT_PROPERTY = "org.punksearch.online.cache.timeout";
	public static final long                             TIMEOUT          = Long.getLong(TIMEOUT_PROPERTY, 600) * 1000;

	private static ConcurrentHashMap<String, HostStatus> cache            = new ConcurrentHashMap<String, HostStatus>();
	private static Map<String, String>                   cacheKeys        = new HashMap<String, String>();
	private static final HostStatus                      INIT_STATUS      = new HostStatus();

	public static boolean isOnline(String host) {
		long now = System.currentTimeMillis();
		String lock = "";

		// sync on the whole cache to: 1) get cached data, 2) insert init_status if necessary
		synchronized (cache) {
			HostStatus hs = cache.putIfAbsent(host, INIT_STATUS);
			if ((hs != null) && (hs != INIT_STATUS) && (hs.date + TIMEOUT > now)) { // "null" only if this is the first time we see this host
				return hs.online;
			}
			if (hs == null) { // we see the host for the first time, store the key (host name)
				cacheKeys.put(host, host);
			}
			// init lock with the stored reference to the key for required host status
			lock = cacheKeys.get(host);
		}

		// grab the lock only on the part of the cache map, so online checks for different hosts can go simultaneously
		// can't use "host" as lock here, since we want to sync on cache's object
		synchronized (lock) {
			HostStatus hs = cache.get(host);
			// maybe it was already updated by other thread while we were waiting for lock? go into if was not
			if ((hs == INIT_STATUS) || (hs.date + TIMEOUT > now)) {
				boolean online = OnlineChecker.isOnline(host);
				// create new object if it is init status in the cache now (i.e. this is first time case)
				if (hs == INIT_STATUS) {
					hs = new HostStatus(now, online);
					cache.put(host, hs);
				} else { // otherwise reuse existing object (i.e. this is timeout case)
					hs.date = now;
					hs.online = online;
				}
			}
			return hs.online;
		}
	}

	@Deprecated
	/*
	 * not thread-safe
	 */
	public static boolean isOnlineOld(String host) {
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

		HostStatus() {
			this(0L, false);
		}

		HostStatus(long date, boolean online) {
			this.date = date;
			this.online = online;
		}

	}
}
