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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.punksearch.common.FileTypes;
import org.punksearch.common.IndexFields;
import org.punksearch.crawler.adapters.ProtocolAdapter;
import org.punksearch.crawler.adapters.ProtocolAdapterFactory;
import org.punksearch.ip.Ip;

/**
 * Implementation of a crawler thread. Crawls one host a time.
 * 
 * @see NetworkCrawler
 * @see ProtocolAdapter
 * @see IndexOperator
 * 
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class HostCrawler extends Thread {
	private static Log      __log           = LogFactory.getLog(HostCrawler.class);

	private int             maxDeep         = Integer.getInteger(Settings.DEEP, 7);

	private boolean         boostCreateDate = Boolean.parseBoolean(System.getProperty(Settings.BOOST_CREATE_DATE, "true"));
	private boolean         boostDeep       = Boolean.parseBoolean(System.getProperty(Settings.BOOST_DEEP, "true"));
	private boolean         boostSize       = Boolean.parseBoolean(System.getProperty(Settings.BOOST_SIZE, "true"));

	private boolean         headerUse       = Boolean.parseBoolean(System.getProperty(Settings.HEADER_USE, "true"));
	private int             headerLength    = Integer.getInteger(Settings.HEADER_LENGTH, 128);
	private long            headerThreshold = Long.getLong(Settings.HEADER_THRESHOLD, 50000000L);

	private Iterator<Ip>    ipIterator;
	private FileTypes       knownFileTypes;
	private IndexOperator   indexOperator;

	private Ip              ip;
	private ProtocolAdapter adapter;
	private boolean         stopRequested   = false;
	private Set<HostStats>  crawledHosts    = new HashSet<HostStats>();
	private String          timestamp;
	private long            docCount;

	public HostCrawler(String name, Iterator<Ip> ipIterator, FileTypes fileTypes, String indexDirectoryPath) {
		super(name);
		this.ipIterator = ipIterator;
		this.indexOperator = new IndexOperator(indexDirectoryPath);
		this.knownFileTypes = fileTypes;
	}

	public void run() {
		Set<ProtocolAdapter> adapters = ProtocolAdapterFactory.createAll();
		while ((ip = ipIterator.next()) != null) {

			__log.debug("Trying " + ip);

			for (ProtocolAdapter ad : adapters) {
				setAdapter(ad);
				crawl();
			}

			if (isStopRequested() || isInterrupted()) {
				__log.info("Thread was stopped");
				break;
			}
		}
		indexOperator.close();
	}

	private void crawl() {
		timestamp = Long.toString(System.currentTimeMillis());
		docCount = 0;

		boolean connected = false;
		try {
			connected = adapter.connect(ip.toString());
			if (connected) {
				__log.info("Start crawling " + currentHostUrl());
				long size = crawlDirectory(adapter.getRootDir(), 0);
				if (size > 0) {
					__log.info("Stop crawling " + currentHostUrl() + ", crawled " + size + " bytes");
					crawledHosts.add(new HostStats(ip, adapter.getProtocol(), size, docCount));
				} else {
					__log.info("Stop crawling " + currentHostUrl() + ", crawled 0 bytes (ignored)");
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			__log.warn("Crawling of a host " + currentHostUrl() + " was cancelled due to: " + e.getMessage());
			// delete files of failed host from temp index
			indexOperator.deleteDocuments(ip.toString(), adapter.getProtocol());
		} finally {
			if (connected) {
				adapter.disconnect();
			}
		}
	}

	public String getIp() {
		return (ip != null) ? ip.toString() : null;
	}

	public void setMaxDeep(int deep) {
		maxDeep = deep;
	}

	public int getMaxDeep() {
		return maxDeep;
	}

	public void setAdapter(ProtocolAdapter adapter) {
		this.adapter = adapter;
	}

	public void setKnownFileTypes(FileTypes fileTypes) {
		this.knownFileTypes = fileTypes;
	}

	protected Document makeDocument(String name, String ext, String size, String path, String date, String type, byte[] header, float boost) {
		docCount++;
		Document document = new Document();
		document.add(new Field(IndexFields.HOST, currentHost(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(IndexFields.NAME, name, Field.Store.YES, Field.Index.TOKENIZED));
		document.add(new Field(IndexFields.EXTENSION, ext.toLowerCase(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(IndexFields.SIZE, size, Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(IndexFields.PATH, path, Field.Store.YES, Field.Index.TOKENIZED));
		document.add(new Field(IndexFields.DATE, date, Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(IndexFields.INDEXED, timestamp, Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(IndexFields.TYPE, type, Field.Store.NO, Field.Index.UN_TOKENIZED));
		if (header != null) {
			document.add(new Field(IndexFields.HEADER, header, Field.Store.YES));
		}
		document.setBoost(boost);
		return document;
	}

	private String currentHost() {
		return adapter.getProtocol() + "_" + getIp();
	}

	private String currentHostUrl() {
		return adapter.getProtocol() + "://" + getIp();
	}

	private Document makeDirDocument(Object dir, long dirSize) {
		String dirName = adapter.getName(dir);
		// String dirExtension = IndexFields.DIRECTORY_EXTENSION;
		String dirSizeStr = Long.toString(dirSize);
		String dirPath = adapter.getPath(dir);
		String lastModified = Long.toString(adapter.getModificationTime(dir));

		float boost = 1.0f;

		// boosting by deep (we want directories closer to root to pop up)
		// the closer a file to the root -- the more boost it to receive
		if (boostDeep) {
			String[] pathParts = dirPath.split("/");
			for (int i = 0; i < pathParts.length; i++) {
				boost /= 2;
			}
		}

		// boost size (we want big directories to pop up)
		if (boostSize) {
			boost *= dirSize / 1000.0f;
		}

		return makeDocument(dirName, "", dirSizeStr, dirPath, lastModified, IndexFields.TYPE_DIR, null, boost);
	}

	private Document makeFileDocument(Object file) {
		String fullName = adapter.getName(file);

		String ext = getExtension(fullName);
		String name = (ext.length() > 0) ? fullName.substring(0, fullName.length() - ext.length() - 1) : fullName;
		String path = adapter.getPath(file);
		String lastModified = Long.toString(adapter.getModificationTime(file));

		long sizeValue = adapter.getSize(file);
		byte[] header = extractHeader(file, sizeValue);
		String size = Long.toString(sizeValue);

		// default boost value
		float boost = 1.0f;

		// boosting by deep (we want files closer to root to pop up)
		// the closer a file to the root -- the more boost it to receive
		if (boostDeep) {
			String[] pathParts = path.split("/");
			for (int i = 0; i < pathParts.length; i++) {
				boost /= 2;
			}
		}

		// boosting by create date (we want most recent files to pop up)
		// the effect is similar to sorting by create date (in reverse order),
		// but this is true only for several most recent files
		// the rest is "sorted" according to other boost strategies
		if (boostCreateDate) {
			long modified = adapter.getModificationTime(file);
			long now = System.currentTimeMillis();
			long hour = 1000 * 3600;
			float ageBoostMultiplier = (now > modified) ? 1 + (1000 * hour * 24) / (now - modified) : 1;
			boost *= ageBoostMultiplier;
		}

		return makeDocument(name, ext, size, path, lastModified, IndexFields.TYPE_FILE, header, boost);
	}

	private byte[] extractHeader(Object item, long size) {
		if (headerUse && size > headerThreshold) {
			return adapter.header(item, headerLength);
		} else {
			return null;
		}
	}

	protected long crawlDirectory(Object dir, int deep) {
		if (deep > maxDeep || isStopRequested()) {
			return 0L;
		}

		if (__log.isTraceEnabled()) {
			__log.trace("Crawling directory " + adapter.getProtocol() + "://" + getIp() + adapter.getFullPath(dir));
		}

		Object[] items = adapter.list(dir);

		// start actual crawling
		long size = 0L;
		List<Document> documentList = new ArrayList<Document>();

		for (Object item : items) {
			if (!isStopRequested() && shouldProcess(item)) {
				Document doc = processResource(item, deep);
				if (doc != null) {
					documentList.add(doc);
					size += Long.parseLong(doc.get(IndexFields.SIZE));
				}
			}
		}

		indexOperator.addDocuments(documentList);

		return size;
	}

	private Document processResource(Object res, int deep) {
		Document doc = null;

		if (adapter.isDirectory(res)) {
			long size = crawlDirectory(res, deep + 1);
			if (size != 0L) {
				doc = makeDirDocument(res, size);
			}
		} else if (adapter.isFile(res)) {
			doc = makeFileDocument(res);
		}

		return doc;
	}

	/**
	 * Checks if the directory should be indexed
	 * 
	 * @param dir
	 *            Directory to check
	 * @return true if at least one good file or all items are directories ("hidden" files are ignored)
	 */
	private boolean isGoodDirectory(Object dir) {
		Object[] items = adapter.list(dir);
		if (items == null || items.length == 0) {
			return false;
		}
		boolean badFileFound = false;
		for (Object item : items) {
			if (adapter.isFile(item) && isIndexableResource(item)) {
				if (isGoodFile(item)) {
					return true;
				} else {
					badFileFound = true;
				}
			}
		}
		if (badFileFound && __log.isTraceEnabled()) {
			__log.trace("Ignored: " + currentHostUrl() + adapter.getFullPath(dir));
		}
		return !badFileFound;
	}

	/**
	 * @return true if file has known extension
	 */
	private boolean isGoodFile(Object item) {
		return knownFileTypes.isExtension(getExtension(adapter.getName(item)));
	}

	/**
	 * The first, sanity, check if resource should be indexed
	 * 
	 * @return true if resource should be indexed (at a first glance)
	 */
	private boolean isIndexableResource(Object item) {
		return (item != null && !adapter.getName(item).startsWith(".") && !adapter.isLink(item) && !adapter.isHidden(item));
	}

	/**
	 * Checks if resource is good enough to be added to index.<br/>
	 * If resource passes this check it for sure will be added to the index.
	 * 
	 * @param item
	 *            Resource to check
	 * @return true if resource is good and should be added to the index
	 */
	private boolean shouldProcess(Object item) {
		if (!isIndexableResource(item)) {
			return false;
		}
		return (adapter.isDirectory(item)) ? isGoodDirectory(item) : isGoodFile(item);
	}

	protected String getExtension(String filename) {
		int dot = filename.lastIndexOf(".");
		if (dot > 0) {
			return filename.substring(dot + 1);
		} else {
			return "";
		}
	}

	public Set<HostStats> getCrawledHosts() {
		return crawledHosts;
	}

	public synchronized void requestStop() {
		stopRequested = true;
	}

	public synchronized boolean isStopRequested() {
		return stopRequested;
	}

}
