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
package org.punksearch.searcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;

/**
 * Helper class to search over Lucene index. Tracks changes in the index and re-inits itself if necessary.
 * 
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class Searcher {
	private static final Logger __log                = Logger.getLogger(Searcher.class.getName());

	private IndexSearcher       indexSearcher;

	private String              indexDir;
	private long                indexSearcherCreated = 0;

	public Searcher(String dir) {
		this.indexDir = dir;
		init(dir);
	}

	private void init(String dir) {
		try {
			indexSearcher = new IndexSearcher(FSDirectory.getDirectory(dir));
		} catch (IOException e) {
			throw new IllegalArgumentException("Index directory is invalid: " + dir);
		}
		this.indexSearcherCreated = System.currentTimeMillis();
	}

	public SearcherResult search(Query query, Integer start, Integer stop, Filter filter) {

		checkIndexDirectory();

		if (null != start && null != stop && (start > stop || start < 0 || stop < 0)) {
			throw new IllegalArgumentException("First (" + start + ") and last (" + stop
			        + ") should be non-negative and first should be more than or equal to last.");
		}

		try {
			// Query query = new QueryParser(SearcherConstants.NAME, new StandardAnalyzer()).parse(text);

			// Sort sort = (null != sortFieldId)? new Sort(new SortField(sortFieldId + SearcherConstants.SORT_SUFFIX)) :
			// null;
			Hits hits = indexSearcher.search(query, filter);

			Integer first = start;
			Integer last = stop;
			if (null == first) {
				first = 0;
			}

			if (null == last || hits.length() - 1 < last) {
				last = hits.length() - 1;
			}

			List<Document> docs = new ArrayList<Document>(last - first + 1);
			for (int i = first; i <= last; i++) {
				Document doc = hits.doc(i);
				doc.setBoost(hits.score(i));
				docs.add(doc);
			}

			return new SearcherResult(hits.length(), docs);
		} catch (IOException e) {
			throw new RuntimeException("IOException during search", e);
		}
	}

	public SearcherResult search(Query query, Filter filter, Integer limit) {

		checkIndexDirectory();

		try {
			Hits hits = indexSearcher.search(query, filter);

			int myLimit = (limit == null || limit <= 0 || limit > hits.length()) ? hits.length() : limit;

			List<Document> docs = new ArrayList<Document>(myLimit);
			for (int i = 0; i < myLimit; i++) {
				Document doc = hits.doc(i);
				doc.setBoost(hits.score(i));
				docs.add(doc);
			}
			return new SearcherResult(hits.length(), docs);
		} catch (IOException e) {
			throw new RuntimeException("IOException during search", e);
		}
	}

	private void checkIndexDirectory() {
		try {
			if (IndexReader.lastModified(indexDir) > indexSearcherCreated) {
				init(indexDir);
			}
		} catch (CorruptIndexException e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1);
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1);
		}
	}
}
