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
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.punksearch.common.FileTypes;
import org.punksearch.common.IndexFields;
import org.punksearch.common.PunksearchProperties;
import org.punksearch.ip.SynchronizedIpIterator;

/**
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class HostCrawler extends Thread {
	private static Logger          __log        = Logger.getLogger(HostCrawler.class.getName());

	private String                 ip;
	private int                    maxDeep      = 5;

	private ProtocolAdapter        adapter;
	private FileTypes              knownFileTypes;

	private SynchronizedIpIterator iterator;

	private IndexOperator          indexOperator;

	private Set<String>            crawledHosts = new HashSet<String>();
	private Set<String>            skippedHosts = new HashSet<String>();
	private String                 timestamp;

	public HostCrawler(String name, SynchronizedIpIterator iter, FileTypes fileTypes, String indexDirectoryPath) {
		super(name);

		iterator = iter;
		knownFileTypes = fileTypes;

		maxDeep = Integer.parseInt(PunksearchProperties.getProperty("org.punksearch.crawler.deep"));

		indexOperator = new IndexOperator(indexDirectoryPath);
	}

	public void run() {

		SmbAdapter smbAdapter = new SmbAdapter();
		FtpAdapter ftpAdapter = new FtpAdapter();

		while ((ip = iterator.next()) != null) {
			
			__log.fine(getName() + ": trying " + ip);
			
			crawlWithAdapter(smbAdapter);
			crawlWithAdapter(ftpAdapter);

		}
		indexOperator.close();
	}

	private void crawlWithAdapter(ProtocolAdapter ad) {
		
		timestamp = Long.toString(System.currentTimeMillis());
		String protocol = ad.getProtocol();
		
		boolean connected = false;
		try {
			connected = ad.connect(ip);
			if (connected) {
				__log.info(getName() + ": start crawling " + ip);
				setAdapter(ad);
				long size = crawlDirectory(adapter.getRootDir(), 0);
				if (size > 0) {
					__log.info(getName() + ": " + protocol + ": " + ip + " crawled: " + size + " bytes");
					crawledHosts.add("smb_" + ip);
				} else {
					skippedHosts.add("smb_" + ip);
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
		document.add(new Field(IndexFields.HOST, adapter.getProtocol() + "_" + getIp(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(IndexFields.NAME, name, Field.Store.YES, Field.Index.TOKENIZED));
		document.add(new Field(IndexFields.EXTENSION, ext, Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(IndexFields.SIZE, size, Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(IndexFields.PATH, path, Field.Store.YES, Field.Index.TOKENIZED));
		document.add(new Field(IndexFields.DATE, date, Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(IndexFields.INDEXED, timestamp, Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.setBoost(boost);
		return document;
	}

	private Document makeDirDocument(Object dir, long dirSize) {
		String dirName = adapter.getName(dir);
		String dirExtension = IndexFields.DIRECTORY_EXTENSION;
		String dirSizeStr = Long.toString(dirSize);
		String dirPath = adapter.getPath(dir);
		String lastModified = Long.toString(adapter.getModificationTime(dir));

		String[] pathParts = dirPath.split("/");
		float boost = 1.0f;
		for (int i = 0; i < pathParts.length; i++) {
			boost /= 2;
		}
		boost *= dirSize / 1000.0f;

		return makeDocument(dirName, dirExtension, dirSizeStr, dirPath, lastModified, boost);
	}

	private Document makeFileDocument(Object file) {
		String fullName = adapter.getName(file);

		String fileExt = getExtension(fullName);
		String fileName = (fileExt.length() > 0) ? fullName.substring(0, fullName.length() - fileExt.length() - 1) : fullName;
		String fileSize = Long.toString(adapter.getSize(file));
		String filePath = adapter.getPath(file);
		String lastModified = Long.toString(adapter.getModificationTime(file));

		String[] pathParts = filePath.split("/");
		float boost = 1.0f;
		for (int i = 0; i < pathParts.length; i++) {
			boost /= 2;
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
	 * true if at least one good file or all items are directories
	 * 
	 * @param items
	 * @return
	 */
	private boolean isGoodDirectory(Object[] items) {
		if (items == null || items.length == 0) {
			return false;
		}
		boolean badFileFound = false;
		for (Object item : items) {
			if (adapter.isFile(item)) {
				if (shouldProcess(item)) {
					return true;
				} else {
					badFileFound = true;
				}
			}
		}
		return !badFileFound;
	}

	private boolean shouldProcess(Object item) {
		if (adapter.getName(item).startsWith(".") || adapter.isLink(item) || adapter.isHidden(item)) {
			return false;
		}
		if (adapter.isDirectory(item)) {
			// return isGoodDirectory(adapter.listFiles(item));
			return true;
		} else {
			// System.out.println(getExtension(adapter.getName(item)));
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
