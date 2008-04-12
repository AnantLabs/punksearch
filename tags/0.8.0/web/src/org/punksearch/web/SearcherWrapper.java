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
package org.punksearch.web;

import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.punksearch.common.PunksearchProperties;
import org.punksearch.common.SearcherException;
import org.punksearch.searcher.Searcher;
import org.punksearch.searcher.SearcherResult;

/**
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class SearcherWrapper {
	private static Searcher searcher;

	public static void init() {
		System.out.println("Using index directory: " + PunksearchProperties.resolveIndexDirectory());
		searcher = new Searcher(PunksearchProperties.resolveIndexDirectory());
	}

	public static SearcherResult search(Query query, Integer first, Integer last, Filter filter)
	        throws SearcherException {
		return searcher.search(query, first, last, filter);
	}

	public static SearcherResult search(Query query, Filter filter, Integer limit) throws SearcherException {
		return searcher.search(query, filter, limit);
	}

}
