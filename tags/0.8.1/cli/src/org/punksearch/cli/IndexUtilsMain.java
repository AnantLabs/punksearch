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
package org.punksearch.cli;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.LockObtainFailedException;
import org.punksearch.crawler.IndexOperator;
import org.punksearch.searcher.EasyQueryParser;
import org.punksearch.searcher.Searcher;
import org.punksearch.searcher.SearcherResult;

/**
 * Index manipulation utils. For now it can extract portion of an index to
 * separate index using some simple query
 * 
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class IndexUtilsMain {

	public static void main(String[] args) throws CorruptIndexException, LockObtainFailedException, IOException {

		String queryStr = System.getProperty("org.punksearch.query");
		String indexDir = args[0];
		String newIndexDir = args[1];

		File dir = new File(newIndexDir);
		if (dir.exists()) {
			FileUtils.cleanDirectory(dir);
		}

		IndexUtilsMain utils = new IndexUtilsMain();
		utils.extract(queryStr, indexDir, newIndexDir);
	}

	public void extract(String queryStr, String indexDir, String newIndexDir) throws CorruptIndexException,
	        LockObtainFailedException, IOException {
		System.out.println("Query: " + queryStr);

		Searcher searcher = new Searcher(indexDir);
		Query query = new EasyQueryParser().makeSimpleQuery(queryStr);
		SearcherResult result = searcher.search(query, null, Integer.MAX_VALUE);

		System.out.println("Found: " + result.count());

		IndexOperator indexOperator = new IndexOperator(newIndexDir);
		indexOperator.addDocuments(result.items());
		indexOperator.optimize();
		indexOperator.close();

		System.out.println("Finished");
	}

}