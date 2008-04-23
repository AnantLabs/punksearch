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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.punksearch.common.IndexFields;
import org.punksearch.web.ResultFilter;

/**
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class OnlineResultFilter implements ResultFilter {

	private static final int THREAD_COUNT;
	
	static {
		String onlineThreads = System.getProperty("org.punksearch.online.threads");
		THREAD_COUNT = (onlineThreads != null)? Integer.valueOf(onlineThreads) : 10;
	}

	public boolean matches(Document doc) {
		return CachedOnlineChecker.isOnline(doc.get(IndexFields.HOST));
	}

	public List<Integer> filter(final List<Document> docs) {
		List<String> hosts = extractDistinctHosts(docs);
		Iterator<String> iter = hosts.iterator();
		Set<String> onlineHosts = Collections.synchronizedSet(new HashSet<String>());

		List<Thread> threadList = new ArrayList<Thread>();
		for (int i = 0; i < THREAD_COUNT; i++) {
			Thread thread = new OnlineCheckThread("OnlineCheckThread" + i, iter, onlineHosts);
			thread.start();
			threadList.add(thread);
		}

		try {
			for (Thread thread : threadList) {
				thread.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			return new LinkedList<Integer>();
		}

		List<Integer> docIds = new LinkedList<Integer>();
		for (int i = 0; i < docs.size(); i++) {
			Document doc = docs.get(i);
			if (onlineHosts.contains(doc.get(IndexFields.HOST))) {
				docIds.add(i);
			}
		}
		return docIds;
	}

	private List<String> extractDistinctHosts(List<Document> docs) {
		List<String> result = new ArrayList<String>();
		for (Document doc : docs) {
			if (!result.contains(doc.get(IndexFields.HOST))) {
				result.add(doc.get(IndexFields.HOST));
			}
		}
		return result;
	}

}

class OnlineCheckThread extends Thread {
	private Set<String>      out;
	private Iterator<String> iter;

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
			String url = host.replace("smb_", "smb://").replace("ftp_", "ftp://");
			if (CachedOnlineChecker.isOnline(url)) {
				out.add(host);
			}
		}
	}

}
