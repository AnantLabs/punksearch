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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.punksearch.common.FileTypes;
import org.punksearch.common.IndexFields;

/**
 * Implementation of a crawler thread. Crawls one host a time.
 * 
 * @see {@link NetworkCrawler}
 * 
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class HostCrawler extends Thread {
	private static Logger      __log             = Logger.getLogger(HostCrawler.class.getName());

	public static final String DEEP              = "org.punksearch.crawler.deep";

	public static final String BOOST_CREATE_DATE = "org.punksearch.crawler.boost.createdate";
	public static final String BOOST_DEEP        = "org.punksearch.crawler.boost.deep";
	public static final String BOOST_SIZE        = "org.punksearch.crawler.boost.size";

	public static final String HEADER_USE        = "org.punksearch.crawler.header.use";
	public static final String HEADER_LENGTH     = "org.punksearch.crawler.header.length";
	public static final String HEADER_THRESHOLD  = "org.punksearch.crawler.header.threshold";

	public static final String SMB_ENABLED       = "org.punksearch.crawler.smb.enabled";
	public static final String FTP_ENABLED       = "org.punksearch.crawler.ftp.enabled";

	private int                maxDeep           = Integer.parseInt(System.getProperty(DEEP, "5"));

	private boolean            boostCreateDate   = Boolean.parseBoolean(System.getProperty(BOOST_CREATE_DATE, "true"));
	private boolean            boostDeep         = Boolean.parseBoolean(System.getProperty(BOOST_DEEP, "true"));
	private boolean            boostSize         = Boolean.parseBoolean(System.getProperty(BOOST_SIZE, "true"));

	private boolean            headerUse         = Boolean.parseBoolean(System.getProperty(HEADER_USE, "true"));
	private int                headerLength      = Integer.valueOf(System.getProperty(HEADER_LENGTH, "128"));
	private long               headerThreshold   = Long.valueOf(System.getProperty(HEADER_THRESHOLD, "500000"));

	private boolean            smbEnabled        = Boolean.parseBoolean(System.getProperty(SMB_ENABLED, "true"));
	private boolean            ftpEnabled        = Boolean.parseBoolean(System.getProperty(FTP_ENABLED, "true"));

	private String             ip;
	private ProtocolAdapter    adapter;
	private FileTypes          knownFileTypes;

	private Iterator<String>   ipIterator;

	private IndexOperator      indexOperator;

	private Set<HostStats>     crawledHosts      = new HashSet<HostStats>();
	private String             timestamp;
	private long               docCount;

	public HostCrawler(String name, Iterator<String> ipIterator, FileTypes fileTypes, String indexDirectoryPath) {
		super(name);
		this.ipIterator = ipIterator;
		this.indexOperator = new IndexOperator(indexDirectoryPath);
		this.knownFileTypes = fileTypes;
	}

	public void run() {
		Set<ProtocolAdapter> adapters = new HashSet<ProtocolAdapter>();
		if (smbEnabled) {
			adapters.add(new SmbAdapter());
		}
		if (ftpEnabled) {
			adapters.add(new FtpAdapter());
		}
		while ((ip = ipIterator.next()) != null) {

			__log.fine(getName() + ". Trying " + ip);

			for (ProtocolAdapter ad : adapters) {
				setAdapter(ad);
				crawl();
			}

			if (isInterrupted()) {
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
			connected = adapter.connect(ip);
			if (connected) {
				__log.info(getName() + ". Start crawling " + curHost());
				long size = crawlDirectory(adapter.getRootDir(), "", 0);
				if (size > 0) {
					__log.info(getName() + ". Stop crawling " + curHost() + ", crawled " + size + " bytes");
					crawledHosts.add(new HostStats(ip, adapter.getProtocol(), size, docCount));
				} else {
					__log.info(getName() + ". Stop crawling " + curHost() + ", crawled 0 bytes (ignored)");
				}
			}
		} catch (IllegalArgumentException e) {
			__log.warning(getName() + ". Illegal argument exception: " + e.getMessage());
		} catch (RuntimeException e) {
			__log.warning(getName() + ". Crawling of a host " + curHost() + " was cancelled due to: " + e.getMessage());
			// if (__log.getLevel().intValue() <= Level.FINE.intValue()) { TODO: why NPE?
			// e.printStackTrace();
			// }
		} finally {
			if (connected) {
				adapter.disconnect();
			}
		}
	}

	public String getIp() {
		return ip;
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

	protected Document makeDocument(String name, String ext, String size, String path, String date, byte[] header,
	        float boost) {
		docCount++;
		Document document = new Document();
		document.add(new Field(IndexFields.HOST, curHost(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(IndexFields.NAME, name, Field.Store.YES, Field.Index.TOKENIZED));
		document.add(new Field(IndexFields.EXTENSION, ext, Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(IndexFields.SIZE, size, Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(IndexFields.PATH, path, Field.Store.YES, Field.Index.TOKENIZED));
		document.add(new Field(IndexFields.DATE, date, Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(IndexFields.INDEXED, timestamp, Field.Store.YES, Field.Index.UN_TOKENIZED));
		if (header != null) {
			document.add(new Field(IndexFields.HEADER, header, Field.Store.YES));
		}
		document.setBoost(boost);
		return document;
	}

	private String curHost() {
		return adapter.getProtocol() + "_" + getIp();
	}

	private Document makeDirDocument(Object dir, String path, long dirSize) {
		String dirName = adapter.getName(dir);
		String dirExtension = IndexFields.DIRECTORY_EXTENSION;
		String dirSizeStr = Long.toString(dirSize);
		String dirPath = path; // adapter.getPath(dir);
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

		return makeDocument(dirName, dirExtension, dirSizeStr, dirPath, lastModified, null, boost);
	}

	private Document makeFileDocument(Object file, String path) {
		String fullName = adapter.getName(file);

		String ext = getExtension(fullName);
		String name = (ext.length() > 0) ? fullName.substring(0, fullName.length() - ext.length() - 1) : fullName;
		// String path = adapter.getPath(file);
		String lastModified = Long.toString(adapter.getModificationTime(file));

		long sizeValue = adapter.getSize(file);
		byte[] header = extractHeader(file, path, sizeValue);
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

		return makeDocument(name, ext, size, path, lastModified, header, boost);
	}

	private byte[] extractHeader(Object item, String path, long size) {
		if (headerUse && size > headerThreshold) {
			return adapter.header(item, path, headerLength);
		} else {
			return null;
		}
	}

	protected long crawlDirectory(Object dir, String path, int deep) {
		if (deep > maxDeep) {
			return 0L;
		}

		__log.fine(getName() + ": processing " + adapter.getProtocol() + "://" + getIp() + path + adapter.getName(dir));

		Object[] items = adapter.listFiles(dir, path);

		String dirPath = (path.length() != 0) ? path + adapter.getName(dir) + "/" : "/";

		// start actual crawling
		long size = 0L;
		List<Document> documentList = new ArrayList<Document>();

		for (Object item : items) {
			if (shouldProcess(item, path)) {
				Document doc = processResource(item, dirPath, deep);
				if (doc != null) {
					documentList.add(doc);
					size += Long.parseLong(doc.get(IndexFields.SIZE));
				}
			}
		}

		indexOperator.addDocuments(documentList);

		return size;
	}

	private Document processResource(Object res, String path, int deep) {
		Document doc = null;

		if (adapter.isDirectory(res)) {
			long size = crawlDirectory(res, path, deep + 1);
			if (size != 0L) {
				doc = makeDirDocument(res, path, size);
			}
		} else if (adapter.isFile(res)) {
			doc = makeFileDocument(res, path);
		}

		return doc;
	}

	/**
	 * true if at least one good file or all items are directories ("hidden" files are ignored)
	 * 
	 * @param items
	 *            list of items in the directory
	 * @return
	 */
	private boolean isGoodDirectory(Object[] items, String path) {
		if (items == null || items.length == 0) {
			return false;
		}
		boolean badFileFound = false;
		for (Object item : items) {
			if (adapter.isFile(item) && !(adapter.getName(item).startsWith(".") || adapter.isHidden(item))) {
				if (shouldProcess(item, path)) {
					return true;
				} else {
					badFileFound = true;
				}
			}
		}
		if (badFileFound && __log.getLevel() == Level.FINE) {
			__log.fine("Ignored parent dir of:" + curHost() + path); // adapter.getFullPath(items[0]));
		}
		return !badFileFound;
	}

	private boolean shouldProcess(Object item, String path) {
		// TODO: why we sometimes get "null" items? observed once for FTP with folder name in CP1251 encoding
		if (item == null || adapter.getName(item).startsWith(".") || adapter.isLink(item) || adapter.isHidden(item)) {
			return false;
		}
		if (adapter.isDirectory(item)) {
			return isGoodDirectory(adapter.listFiles(item, path), path);
		} else {
			return knownFileTypes.isExtension(getExtension(adapter.getName(item)));
		}
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

}
