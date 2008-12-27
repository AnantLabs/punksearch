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
package org.punksearch.web.statistics;

import java.text.NumberFormat;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.punksearch.common.FileTypes;
import org.punksearch.common.IndexFields;
import org.punksearch.common.PunksearchFs;
import org.punksearch.web.filters.TypeFilters;

public class FileTypeStatistics {

	private static Map<String, Long> countCache              = null;
	private static Map<String, Long> sizeCache               = null;
	private static Long              totalSizeCache          = null;
	private static long              countCacheTimestamp     = 0;
	private static long              sizeCacheTimestamp      = 0;
	private static long              totalSizeCacheTimestamp = 0;

	private static Thread            updater                 = new AsyncUpdater();

	private static Query makeQuery() {
		BooleanQuery query = new BooleanQuery();
		Query smbQuery = new WildcardQuery(new Term(IndexFields.HOST, "smb_*"));
		Query ftpQuery = new WildcardQuery(new Term(IndexFields.HOST, "ftp_*"));
		query.add(smbQuery, BooleanClause.Occur.SHOULD);
		query.add(ftpQuery, BooleanClause.Occur.SHOULD);
		return query;
	}

	private static Hits extractDocsForType(String type) {
		Filter filter = TypeFilters.get(type);
		try {
			IndexSearcher indexSearcher = new IndexSearcher(PunksearchFs.resolveIndexDirectory());
			Hits hits = indexSearcher.search(makeQuery(), filter);
			return hits;
		} catch (Exception e) {
			return null;
		}
	}

	public static long count(String type) {
		return extractDocsForType(type).length();
	}

	public static synchronized Map<String, Long> count() {
		if (countCache == null || indexChangedAfter(countCacheTimestamp)) {
			countCache = new TreeMap<String, Long>();

			FileTypes types = TypeFilters.getTypes();
			for (String key : types.list()) {
				countCache.put(key, count(key));
			}
			countCache.put("directory", count(TypeFilters.DIRECTORY_KEY));
			countCacheTimestamp = System.currentTimeMillis();
		}
		return countCache;
	}

	public static long size(String type) {
		System.out.println("Started FileTypeStatistics.size() for " + type);
		Hits hits = extractDocsForType(type);
		long size = 0;
		try {
			for (int i = 0; i < hits.length(); i++) {
				Document doc = hits.doc(i);
				size += Long.parseLong(doc.get(IndexFields.SIZE));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Finished FileTypeStatistics.size() for " + type + "=" + (size / (1024D * 1024)) + "MB");
		return size;
	}

	public static synchronized Map<String, Long> size() {
		if (sizeCache == null || indexChangedAfter(sizeCacheTimestamp)) {
			sizeCache = new TreeMap<String, Long>();
			FileTypes types = TypeFilters.getTypes();
			for (String key : types.list()) {
				sizeCache.put(key, size(key));
			}
			sizeCache.put("directory", Long.valueOf(0));
			sizeCacheTimestamp = System.currentTimeMillis();
		}
		return sizeCache;
	}

	public static synchronized Long totalSize() {
		if (totalSizeCache == null || indexChangedAfter(totalSizeCacheTimestamp)) {
			long size = 0;
			try {
				// Rough approximation to the root directories.
				// Obviously, non-latin1 directory names slip through the filter, we'll catch them later
				// Maybe we should use some ranges with UTF8-16 characters... TODO
				String approxQuery = "Host:ftp_* Host:smb_* -Path:{a TO Z*} -Path:{0 TO 9*}";
				QueryParser parser = new QueryParser("Host", new SimpleAnalyzer());
				Query query = parser.parse(approxQuery);
				IndexSearcher indexSearcher = new IndexSearcher(PunksearchFs.resolveIndexDirectory());
				Hits hits = indexSearcher.search(query);
				for (int i = 0; i < hits.length(); i++) {
					Document doc = hits.doc(i);
					String path = doc.get(IndexFields.PATH);
					if (!path.equals("/")) {
						continue;
					}
					size += Long.parseLong(doc.get(IndexFields.SIZE));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			totalSizeCache = size;
			totalSizeCacheTimestamp = System.currentTimeMillis();
		}
		return totalSizeCache;
	}

	public static PieDataset makePieDataset(Map<String, Long> values) {
		long total = 0;
		for (Long value : values.values()) {
			total += value;
		}
		return makePieDataset(values, total);
	}

	public static PieDataset makePieDataset(Map<String, Long> values, long total) {
		long sum = 0;
		for (Long value : values.values()) {
			sum += value;
		}
		long other = total - sum;

		NumberFormat nfPercent = NumberFormat.getPercentInstance();
		NumberFormat nfNumber = NumberFormat.getNumberInstance();
		nfPercent.setMaximumFractionDigits(2);

		DefaultPieDataset dataset = new DefaultPieDataset();
		for (Map.Entry<String, Long> entry : values.entrySet()) {
			long value = entry.getValue();
			dataset.setValue(entry.getKey() + " " + nfPercent.format(value / (total + 0.0)) + " ("
			        + nfNumber.format(value) + ")", value);
		}
		if (other > 0) {
			dataset.setValue("other " + nfPercent.format(other / (total + 0.0)) + " (" + nfNumber.format(other) + ")",
			        other);
		}

		return dataset;
	}

	private static boolean indexChangedAfter(long timestamp) {
		try {
			return (IndexReader.lastModified(PunksearchFs.resolveIndexDirectory()) > timestamp);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean isUpToDate() {
		boolean countCacheOk = !indexChangedAfter(countCacheTimestamp);
		boolean sizeCacheOk = !indexChangedAfter(sizeCacheTimestamp);
		boolean totalSizeCacheOk = !indexChangedAfter(totalSizeCacheTimestamp);
		return countCacheOk && sizeCacheOk && totalSizeCacheOk;
	}

	public static synchronized void update(boolean force) {
		if (force) {
			countCache = null;
			sizeCache = null;
			totalSizeCache = null;
		}
		count();
		size();
		totalSize();
	}

	public static void updateAsync() {
		synchronized (updater) {
			if (!updater.isAlive()) {
				updater = new AsyncUpdater();
				updater.start();
			}
		}
	}

	private static class AsyncUpdater extends Thread {
		public void run() {
			FileTypeStatistics.update(false);
		}
	}
}
