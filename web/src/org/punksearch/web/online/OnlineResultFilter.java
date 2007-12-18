package org.punksearch.web.online;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.punksearch.commons.IndexFields;
import org.punksearch.searcher.ResultFilter;

public class OnlineResultFilter implements ResultFilter {

	// TODO: extract to settings
	private static final int THREAD_COUNT = 10;
	
	public boolean matches(Document doc) {
		return CachedOnlineChecker.isOnline(doc.get(IndexFields.HOST));
	}

	public List<Integer> filter(final List<Document> docs) {

		List<String> hosts = extractDistinctHosts(docs);

		int size = hosts.size();
		int chunkSize = size / THREAD_COUNT;
		int lastChunk = size % THREAD_COUNT;

		final List<Integer> result = Collections.synchronizedList(new ArrayList<Integer>());

		List<Thread> threadList = new ArrayList<Thread>();
		for (int i = 0; i < THREAD_COUNT; i++) {
			final int chunkStart = i * chunkSize;
			final int chunkStop = chunkStart + chunkSize - 1;
			Thread thread = new OnlineCheckThread("OnlineCheckThread" + i, chunkStart, chunkStop, hosts, result);
			thread.start();
			threadList.add(thread);
		}
		if (lastChunk != 0) {
			Thread thread = new OnlineCheckThread("OnlineCheckThread" + THREAD_COUNT, THREAD_COUNT * chunkSize, size,
			        hosts, result);
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
		return result;
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
		System.out.println("> onlineCheckThread. checking " + start + ":" + stop);
		for (int j = start; j < stop; j++) {
			if (CachedOnlineChecker.isOnline(hosts.get(j))) {
				out.add(j);
			}
		}
		System.out.println("< onlineCheckThread. checking " + start + ":" + stop);
	}

}
