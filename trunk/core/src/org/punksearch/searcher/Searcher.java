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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to search over Lucene index. Tracks changes in the index and re-inits itself if necessary.
 * 
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class Searcher {
	private static final Log log = LogFactory.getLog(Searcher.class);

    public static final int MAX_COUNT = 100000;
    private IndexReaderHolder indexReaderHolder;

    public Searcher(String dir) {
        try {
            indexReaderHolder = new IndexReaderHolder(dir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Searcher(IndexReaderHolder indexReaderHolder) {
        this.indexReaderHolder = indexReaderHolder;
    }

    /*public SearcherResult search(Query query, Integer start, Integer stop, Filter filter) {
		log.trace("Search for: " + query);

		checkIndexDirectory();

		if (null != start && null != stop && (start > stop || start < 0 || stop < 0)) {
			throw new IllegalArgumentException("First (" + start + ") and last (" + stop
			        + ") should be non-negative and first should be more than or equal to last.");
		}

		try {
			// Query query = new QueryParser(SearcherConstants.NAME, new StandardAnalyzer()).parse(text);

			// Sort sort = (null != sortFieldId)? new Sort(new SortField(sortFieldId + SearcherConstants.SORT_SUFFIX)) :
			// null;

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
			log.error("IOException during search", e);
			throw new RuntimeException("IOException during search", e);
		}
	}*/

	public SearcherResult search(Query query, Filter filter, final int limit) {
		log.trace("Search for: " + query);

        try {
            IndexSearcher indexSearcher = indexReaderHolder.getCurrentSearcher();

            int myLimit = (limit <= 0 || limit > MAX_COUNT) ? MAX_COUNT : limit;

            final TopDocs topDocs = indexSearcher.search(query, filter, myLimit);

            final int totalHitsCount = Math.min(topDocs.totalHits, myLimit);

            final ScoreDoc[] scoreDocs = topDocs.scoreDocs;
            final int resultLen = scoreDocs.length;

			List<Document> docs = new ArrayList<Document>(resultLen);

            for (final ScoreDoc scoreDoc : scoreDocs) {
                Document doc = indexSearcher.doc(scoreDoc.doc);
                doc.setBoost(scoreDoc.score);// TODO: is this necessary?
                docs.add(doc);
            }

			return new SearcherResult(totalHitsCount, docs);
		} catch (IOException e) {
			log.error("IOException during search", e);
			throw new RuntimeException("IOException during search", e);
		}
	}
}
