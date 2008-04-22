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
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class HostCrawler extends Thread {
	private static Logger      __log             = Logger.getLogger(HostCrawler.class.getName());

	public static final String BOOST_CREATE_DATE = "org.punksearch.crawler.boost.createdate";
	public static final String BOOST_DEEP        = "org.punksearch.crawler.boost.deep";
	public static final String BOOST_SIZE        = "org.punksearch.crawler.boost.size";

	public static final String DEEP_PROPERTY     = "org.punksearch.crawler.deep";

	private String             ip;
	private int                maxDeep           = 5;
	private boolean            boostCreateDate   = true;
	private boolean            boostDeep         = true;
	private boolean            boostSize         = true;

	private ProtocolAdapter    adapter;
	private FileTypes          knownFileTypes;

	private Iterator<String>   ipIterator;

	private IndexOperator      indexOperator;

	private Set<String>        crawledHosts      = new HashSet<String>();
	private Set<String>        skippedHosts      = new HashSet<String>();
	private String             timestamp;

	public HostCrawler(String name, Iterator<String> ipIterator, FileTypes fileTypes, String indexDirectoryPath) {
		super(name);

		this.ipIterator = ipIterator;
		this.indexOperator = new IndexOperator(indexDirectoryPath);
		this.knownFileTypes = fileTypes;

		if (System.getProperty(DEEP_PROPERTY) != null) {
			this.maxDeep = Integer.parseInt(System.getProperty(DEEP_PROPERTY));
		}

		if (System.getProperty(BOOST_CREATE_DATE) != null) {
			this.boostCreateDate = Boolean.parseBoolean(System.getProperty(BOOST_CREATE_DATE));
		}

		if (System.getProperty(BOOST_DEEP) != null) {
			this.boostDeep = Boolean.parseBoolean(System.getProperty(BOOST_DEEP));
		}

		if (System.getProperty(BOOST_SIZE) != null) {
			this.boostSize = Boolean.parseBoolean(System.getProperty(BOOST_SIZE));
		}
	}

	public void run() {

		SmbAdapter smbAdapter = new SmbAdapter();
		FtpAdapter ftpAdapter = new FtpAdapter();

		while ((ip = ipIterator.next()) != null) {

			__log.fine(getName() + ": trying " + ip);

			crawlWithAdapter(smbAdapter);
			crawlWithAdapter(ftpAdapter);

			if (isInterrupted()) {
				break;
			}
		}
		indexOperator.close();
	}

	private void crawlWithAdapter(ProtocolAdapter ad) {
		timestamp = Long.toString(System.currentTimeMillis());

		boolean connected = false;
		try {
			connected = ad.connect(ip);
			if (connected) {
				__log.info(getName() + ": start crawling " + ip);
				setAdapter(ad);
				long size = crawlDirectory(adapter.getRootDir(), 0);
				if (size > 0) {
					__log.info(getName() + ": " + ad.getProtocol() + ": " + ip + " crawled: " + size + " bytes");
					crawledHosts.add(curHost());
				} else {
					skippedHosts.add(curHost());
				}
			}
		} catch (IllegalArgumentException e) {
			__log.warning("IAE: " + e);
		} catch (RuntimeException e) {
			__log.warning(getName() + ": Runtime exception occured");
			e.printStackTrace();
		} finally {
			if (connected) {
				ad.disconnect();
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

	protected Document makeDocument(String name, String ext, String size, String path, String date, float boost) {
		Document document = new Document();
		document.add(new Field(IndexFields.HOST, curHost(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(IndexFields.NAME, name, Field.Store.YES, Field.Index.TOKENIZED));
		document.add(new Field(IndexFields.EXTENSION, ext, Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(IndexFields.SIZE, size, Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(IndexFields.PATH, path, Field.Store.YES, Field.Index.TOKENIZED));
		document.add(new Field(IndexFields.DATE, date, Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(IndexFields.INDEXED, timestamp, Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.setBoost(boost);
		return document;
	}

	private String curHost() {
		return adapter.getProtocol() + "_" + getIp();
	}

	private Document makeDirDocument(Object dir, long dirSize) {
		String dirName = adapter.getName(dir);
		String dirExtension = IndexFields.DIRECTORY_EXTENSION;
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

		return makeDocument(dirName, dirExtension, dirSizeStr, dirPath, lastModified, boost);
	}

	private Document makeFileDocument(Object file) {
		String fullName = adapter.getName(file);

		String fileExt = getExtension(fullName);
		String fileName = (fileExt.length() > 0) ? fullName.substring(0, fullName.length() - fileExt.length() - 1)
		        : fullName;
		String fileSize = Long.toString(adapter.getSize(file));
		String filePath = adapter.getPath(file);
		String lastModified = Long.toString(adapter.getModificationTime(file));

		// default boost value
		float boost = 1.0f;

		// boosting by deep (we want files closer to root to pop up)
		// the closer a file to the root -- the more boost it to receive
		if (boostDeep) {
			String[] pathParts = filePath.split("/");
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

		return makeDocument(fileName, fileExt, fileSize, filePath, lastModified, boost);
	}

	protected long crawlDirectory(Object dir, int deep) {
		if (deep > maxDeep) {
			return 0L;
		}

		// __log.finest(getName() + ": crawling: " + adapter.getProtocol() + "://" + getIp() +
		// adapter.getFullPath(dir));

		Object[] items = adapter.listFiles(dir);

		// start actual crawling
		long size = 0L;
		List<Document> documentList = new ArrayList<Document>();

		for (Object item : items) {
			if (shouldProcess(item)) {
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
	 * true if at least one good file or all items are directories ("hidden" files are ignored)
	 * 
	 * @param items
	 *            list of items in the directory
	 * @return
	 */
	private boolean isGoodDirectory(Object[] items) {
		if (items == null || items.length == 0) {
			return false;
		}
		boolean badFileFound = false;
		for (Object item : items) {
			if (adapter.isFile(item) && !(adapter.getName(item).startsWith(".") || adapter.isHidden(item))) {
				if (shouldProcess(item)) {
					return true;
				} else {
					badFileFound = true;
				}
			}
		}
		if (badFileFound && __log.getLevel() == Level.FINE) {
			__log.fine("Ignored parent dir of:" + curHost() + adapter.getFullPath(items[0]));
		}
		return !badFileFound;
	}

	private boolean shouldProcess(Object item) {
		if (adapter.getName(item).startsWith(".") || adapter.isLink(item) || adapter.isHidden(item)) {
			return false;
		}
		if (adapter.isDirectory(item)) {
			return isGoodDirectory(adapter.listFiles(item));
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

	public Set<String> getCrawledHosts() {
		return crawledHosts;
	}

	public Set<String> getSkippedHosts() {
		return skippedHosts;
	}

}
