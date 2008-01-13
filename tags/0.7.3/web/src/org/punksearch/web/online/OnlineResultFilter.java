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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.punksearch.commons.IndexFields;
import org.punksearch.searcher.ResultFilter;

/**
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class OnlineResultFilter implements ResultFilter {

	// TODO: extract OnlineResultFilter.THREAD_COUNT to settings
	private static final int THREAD_COUNT = 10;
	
	public boolean matches(Document doc) {
		return CachedOnlineChecker.isOnline(doc.get(IndexFields.HOST));
	}

	public List<Integer> filter(final List<Document> docs) {

		List<String> hosts = extractDistinctHosts(docs);

		int size = hosts.size();
		int chunkSize = size / THREAD_COUNT;
		int lastChunk = size % THREAD_COUNT;

		final List<Integer> onlineHostIds = Collections.synchronizedList(new LinkedList<Integer>());

		List<Thread> threadList = new ArrayList<Thread>();
		for (int i = 0; i < THREAD_COUNT; i++) {
			final int chunkStart = i * chunkSize;
			final int chunkStop = chunkStart + chunkSize - 1;
			Thread thread = new OnlineCheckThread("OnlineCheckThread" + i, chunkStart, chunkStop, hosts, onlineHostIds);
			thread.start();
			threadList.add(thread);
		}
		if (lastChunk != 0) {
			Thread thread = new OnlineCheckThread("OnlineCheckThread" + THREAD_COUNT, THREAD_COUNT * chunkSize, size,
			        hosts, onlineHostIds);
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
		Set<String> onlineHosts = new HashSet<String>(onlineHostIds.size());
		for (Integer id : onlineHostIds) {
			onlineHosts.add(hosts.get(id));
		}
		List<Integer> docIds = new LinkedList<Integer>();
		for (int i = 0 ; i < docs.size() ; i++) {
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
	private int           start = 0;
	private int           stop  = 0;
	private List<String>  hosts = null;
	private List<Integer> out   = null;

	public OnlineCheckThread(String name, int start, int stop, List<String> hosts, List<Integer> out) {
		super(name);
		this.start = start;
		this.stop = stop;
		this.hosts = hosts;
		this.out = out;
	}

	public void run() {
		//System.out.println("> onlineCheckThread. checking " + start + ":" + stop);
		for (int j = start; j < stop; j++) {
			String host = hosts.get(j).replace("smb_", "smb://").replace("ftp_", "ftp://");
			if (CachedOnlineChecker.isOnline(host)) {
				out.add(j);
			}
		}
		//System.out.println("< onlineCheckThread. checking " + start + ":" + stop);
	}

}
