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

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.punksearch.common.IndexFields;

/**
 * Helper class to create Lucene queries easily.
 * 
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class EasyQueryParser {
	private static final Log   __log                = LogFactory.getLog(Searcher.class);

	public static final String CLAUSES_PROPERTY     = "org.punksearch.search.clauses";
	public static final String TERM_LENGTH_PROPERTY = "org.punksearch.search.termlength";
	public static final String FAST_SEARCH_PROPERTY = "org.punksearch.search.fast";

	private int                maxClauseCount       = 10000;
	private int                minTermLength        = 3;
	private boolean            isFastSearch         = true;

	public EasyQueryParser() {
		if (System.getProperty(CLAUSES_PROPERTY) != null) {
			this.maxClauseCount = Integer.valueOf(System.getProperty(CLAUSES_PROPERTY));
		}
		if (System.getProperty(TERM_LENGTH_PROPERTY) != null) {
			this.minTermLength = Integer.valueOf(System.getProperty(TERM_LENGTH_PROPERTY));
		}
		if (System.getProperty(FAST_SEARCH_PROPERTY) != null) {
			this.isFastSearch = Boolean.valueOf(System.getProperty(FAST_SEARCH_PROPERTY));
		}
	}

	public Query makeSimpleQuery(String userQuery) {
		if (__log.isDebugEnabled()) {
			__log.debug("Query to parse: " + userQuery);
		}
		List<String> terms = prepareQueryParameter(userQuery);

		if (terms.size() == 0) {
			return null;
		}

		BooleanQuery query = new BooleanQuery(false);
		BooleanQuery.setMaxClauseCount(maxClauseCount);

		for (String item : terms) {
			BooleanQuery itemQuery = new BooleanQuery();

			BooleanClause.Occur occurItem = occurItem(item);

			Query nameQuery = new WildcardQuery(new Term(IndexFields.NAME, prepareItem(item)));
			itemQuery.add(nameQuery, BooleanClause.Occur.SHOULD);

			Query pathQuery = new WildcardQuery(new Term(IndexFields.PATH, prepareItem(item)));
			itemQuery.add(pathQuery, BooleanClause.Occur.SHOULD);

			query.add(itemQuery, occurItem);
		}

		return query;
	}

	public Query makeAdvancedQuery(String dir, String file, String ext) {
		if (__log.isDebugEnabled()) {
			__log.debug("Query (advanced) to parse: dir(" + dir + ") file(" + file + ") ext(" + ext + ")");
		}

		BooleanQuery query = new BooleanQuery(false);
		BooleanQuery.setMaxClauseCount(maxClauseCount);

		List<String> dirTerms = prepareQueryParameter(dir);
		List<String> fileTerms = prepareQueryParameter(file);
		List<String> extTerms = prepareQueryParameter(ext);

		if (fileTerms.size() != 0 || extTerms.size() != 0) {
			if (__log.isDebugEnabled()) {
				__log.debug("Search for files");
			}
			if (fileTerms.size() != 0) {
				BooleanQuery fileQuery = new BooleanQuery();
				for (String item : fileTerms) {
					BooleanClause.Occur occurItem = occurItem(item);
					Query nameQuery = new WildcardQuery(new Term(IndexFields.NAME, prepareItem(item)));
					fileQuery.add(nameQuery, occurItem);
				}
				query.add(fileQuery, BooleanClause.Occur.MUST);
			}

			if (extTerms.size() != 0) {
				BooleanQuery extQuery = new BooleanQuery();
				for (String item : extTerms) {
					Query termQuery = new TermQuery(new Term(IndexFields.EXTENSION, item));
					extQuery.add(termQuery, BooleanClause.Occur.SHOULD);
				}
				query.add(extQuery, BooleanClause.Occur.MUST);
			} else {
				Query extensionQuery = new TermQuery(new Term(IndexFields.EXTENSION, IndexFields.DIRECTORY_EXTENSION));
				query.add(extensionQuery, BooleanClause.Occur.MUST_NOT);
			}

			// restrict files to occur in specified directories only
			if (dirTerms.size() != 0) {
				BooleanQuery dirQuery = new BooleanQuery();
				int negations = 0;

				for (String item : dirTerms) {
					BooleanClause.Occur occurItem = occurItem(item);
					if (occurItem == BooleanClause.Occur.MUST_NOT) {
						negations++;
					}
					Query pathQuery = new WildcardQuery(new Term(IndexFields.PATH, prepareItem(item)));
					dirQuery.add(pathQuery, occurItem);
				}
				// it must be at least one positive clause in query to be executed.
				// so add one if all user clauses are negative.
				if (dirTerms.size() == negations) {
					Query pathQuery = new WildcardQuery(new Term(IndexFields.PATH, "*"));
					dirQuery.add(pathQuery, BooleanClause.Occur.SHOULD);
				}
				query.add(dirQuery, BooleanClause.Occur.MUST);
			}
		} else if (dirTerms.size() != 0) {
			if (__log.isDebugEnabled()) {
				__log.debug("Search for directories only, since file name was not specified");
			}
			for (String item : dirTerms) {
				BooleanQuery dirQuery = new BooleanQuery();

				BooleanClause.Occur occurItem = occurItem(item);

				Query nameQuery = new WildcardQuery(new Term(IndexFields.NAME, prepareItem(item)));
				dirQuery.add(nameQuery, BooleanClause.Occur.MUST);

				Query extensionQuery = new TermQuery(new Term(IndexFields.EXTENSION, IndexFields.DIRECTORY_EXTENSION));
				dirQuery.add(extensionQuery, BooleanClause.Occur.MUST);

				query.add(dirQuery, occurItem);
			}
		} else {
			return null;
		}
		return query;
	}

	private List<String> prepareQueryParameter(String str) {
		List<String> result = new LinkedList<String>();
		if (str != null) {
			str = str.replaceAll("\\*|_|!|\\.|,|\\:|\\[|\\]|#|\\(|\\)|'|/|&", " ");
			String[] terms = str.toLowerCase().split(" ");
			for (String term : terms) {
				term = term.trim();
				if (term.length() >= minTermLength) {
					result.add(term);
				}
			}
		}
		return result;
	}

	private String prepareItem(String item) {
		if (item.startsWith("+") || item.startsWith("-")) {
			item = item.substring(1);
		}
		return (isFastSearch) ? item + "*" : "*" + item + "*";
	}

	private BooleanClause.Occur occurItem(String item) {
		BooleanClause.Occur result = BooleanClause.Occur.SHOULD;
		if (item.startsWith("+")) {
			result = BooleanClause.Occur.MUST;
		} else if (item.startsWith("-")) {
			result = BooleanClause.Occur.MUST_NOT;
		}
		return result;
	}

}
