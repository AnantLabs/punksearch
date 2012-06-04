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
package org.punksearch.online;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.punksearch.util.Computable;
import org.punksearch.util.RenewableMemoizer;

/**
 * @author Yury Soldak (ysoldak@gmail.com)
 * 
 */
public class OnlineStatuses {
	private static Log log = LogFactory.getLog(OnlineStatuses.class);

	public static final String                 PROBE_THREADS_PROPERTY = "org.punksearch.online.threads";

	/**
	 * Whatever to use cache for online/offline statuses
	 */
	public static final String                 CACHE_PROPERTY         = "org.punksearch.online.cache";

	/**
	 * Whatever to use aggressive self-updating strategy for the cache
	 */
	public static final String                 CACHE_ACTIVE_PROPERTY  = "org.punksearch.online.cache.active";

	/**
	 * Item in the cache considered out of date if it was in the cache more than this period of time (in msec).
	 */
	public static final String                 CACHE_TIMEOUT_PROPERTY = "org.punksearch.online.cache.timeout";

	private static final boolean               CACHE                  = Boolean.valueOf(System.getProperty(CACHE_PROPERTY, "true"));

	private static final boolean               CACHE_ACTIVE           = Boolean.getBoolean(CACHE_ACTIVE_PROPERTY);
	private static final int                   CACHE_TIMEOUT          = Integer.getInteger(CACHE_TIMEOUT_PROPERTY, 600);

	private static final int                   PROBE_THREAD_COUNT     = Integer.getInteger(PROBE_THREADS_PROPERTY, 10);

	private static final OnlineStatuses        INSTANCE               = new OnlineStatuses();

	private RenewableMemoizer<String, Boolean> cache;
	private Probe                              probe;

	private OnlineStatuses() {
		probe = new Probe();
		cache = new RenewableMemoizer<String, Boolean>(new ProbeAdapter(probe), CACHE_TIMEOUT * 1000, CACHE_ACTIVE);
	}

	public static OnlineStatuses getInstance() {
		return INSTANCE;
	}

	/**
	 * Checks if host is online.
	 * 
	 * @param host
	 *            Host to check.
	 * @return true if online, false if offline or some internal exception occured
	 */
	public boolean isOnline(String host) {
		boolean result;
		if (CACHE) {
			try {
				result = cache.compute(normalize(host));
			} catch (InterruptedException e) {
				log.warn("isOnline", e);
				result = false;
			}
		} else {
			result = probe.probe(normalize(host));
		}
		log.debug("isOnline: " + host + " = " + result);
		return result;
	}

	public Set<String> getOnline(Collection<String> hosts) {
		Iterator<String> iter = hosts.iterator();
		Set<String> onlineHosts = Collections.synchronizedSet(new HashSet<String>());
		List<Thread> threadList = new ArrayList<Thread>();
		for (int i = 0; i < PROBE_THREAD_COUNT; i++) {
			Thread thread = new OnlineCheckThread("OnlineCheckThread" + i, iter, onlineHosts);
			thread.start();
			threadList.add(thread);
		}
		try {
			for (Thread thread : threadList) {
				thread.join();
			}
		} catch (InterruptedException e) {
			// TODO: log
			onlineHosts = new HashSet<String>();
		}
		log.debug("getOnline: " + hosts.size() + " -> " + onlineHosts.size());
		return onlineHosts;
	}

	private static String normalize(String host) {
		return host.replace("_", "://");
	}

}

class ProbeAdapter implements Computable<String, Boolean> {

	private Probe probe;

	public ProbeAdapter(Probe probe) {
		this.probe = probe;
	}

	public Boolean compute(String arg) throws InterruptedException {
		return probe.probe(arg);
	}
}

class OnlineCheckThread extends Thread {
	private final Set<String>      out;
	private final Iterator<String> iter;

	public OnlineCheckThread(String name, Iterator<String> iter, Set<String> out) {
		super(name);
		this.iter = iter;
		this.out = out;
	}

	public void run() {
		String host;
		while (true) {
			synchronized (iter) {
				if (iter.hasNext()) {
					host = iter.next();
				} else {
					break;
				}
			}
			if (OnlineStatuses.getInstance().isOnline(host)) {
				out.add(host);
			}
		}
	}

}
