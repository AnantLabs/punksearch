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

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.punksearch.common.IndexFields;
import org.punksearch.common.PunksearchProperties;
import org.punksearch.searcher.EasyQueryParser;
import org.punksearch.searcher.Searcher;
import org.punksearch.searcher.SearcherResult;

/**
 * @author Yury Soldak (ysoldak@gmail.com)
 *
 */
public class SearcherMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			printUsage();
			System.exit(1);
		}
		
		if (args[0].equals("-t")) {
			//TODO
		} else {
			EasyQueryParser parser = new EasyQueryParser(10000);
			Query query = parser.makeSimpleQuery(args[0]);
			Searcher searcher = new Searcher(PunksearchProperties.getProperty("org.punksearch.index.dir"));
			SearcherResult result = searcher.search(query, null, 30);
			System.out.println("Found items: " + result.getHitCount());
			for (Document doc : result.getChunk()) {
				System.out.println(makeResultRow(doc));
			}
		}
	}
	
	private static void printUsage() {
		System.out.println("Usage: java -jar <jarfile> [-t <type>] <query>");
		System.out.println("Query is rather Lucene query like: \"Path:distr* +Extension:iso\" or simple human-oriented query like \"distr iso\"");
		System.out.println("Possible query parameters: Host, Path, Name, Extension, Size, LastModified");
	}

	private static String makeResultRow(Document doc) {
		String row = doc.get(IndexFields.HOST).replace("_", "://") + doc.get(IndexFields.PATH) + doc.get(IndexFields.NAME);
		return row;
	}
	

}
