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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.punksearch.commons.IndexFields;
import org.punksearch.commons.SearcherException;
import org.punksearch.searcher.ResultFilter;
import org.punksearch.searcher.SearcherResult;
import org.punksearch.searcher.filters.CompositeFilter;
import org.punksearch.searcher.filters.FilterFactory;
import org.punksearch.searcher.filters.NumberRangeFilter;
import org.punksearch.web.filters.TypeFilters;
import org.punksearch.web.online.OnlineResultFilter;

/**
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class SearchAction {

	// TODO: extract SearchAction.MIN_TERM_LENGTH to settings
	private static final int MIN_TERM_LENGTH  = 3;

	private static Logger    __log            = Logger.getLogger(SearchParams.class.getName());

	private SearchParams     params;
	private SearcherConfig   config           = SearcherConfig.getInstance();

	private long             searchTime       = 0;
	private long             presentationTime = 0;
	private int              overallCount     = 0;

	public SearchAction(SearchParams params) {
		this.params = params;
	}

	private Filter makeFilter() {
		if (params.type.equals("advanced")) {

			NumberRangeFilter<Long> sizeFilter = null;
			if (params.minSize != null || params.maxSize != null) {
				sizeFilter = FilterFactory.createNumberFilter(IndexFields.SIZE, params.minSize, params.maxSize);
			}

			NumberRangeFilter<Long> dateFilter = null;
			if (params.fromDate != null || params.toDate != null) {
				dateFilter = FilterFactory.createNumberFilter(IndexFields.DATE, params.fromDate, params.toDate);
			}

			if (sizeFilter != null || dateFilter != null) {
				CompositeFilter resultFilter = new CompositeFilter();
				if (sizeFilter != null)
					resultFilter.add(sizeFilter);
				if (dateFilter != null)
					resultFilter.add(dateFilter);
				return resultFilter;
			} else {
				return null;
			}
		} else {
			return TypeFilters.get(params.type);
		}
	}

	private Query makeQuery() {
		if (params.type.equals("advanced")) {
			return makeAdvancedQuery(params.dir, params.file, params.ext);
		} else {
			return makeSimpleQuery(params.query);
		}
	}

	@Deprecated
	public List<SearchResult> doSearch() {
		Query query = makeQuery();
		Filter filter = makeFilter();

		List<SearchResult> searchResults = new ArrayList<SearchResult>();

		if (query != null) {
			__log.info("query constructed: " + query.toString());
			try {
				Date startDate = new Date();
				SearcherResult result = SearcherWrapper.search(query, params.first, params.last, filter);
				Date stopDate = new Date();
				searchTime = stopDate.getTime() - startDate.getTime();
				overallCount = result.getHitCount();
				searchResults = prepareResults(result.getChunk());
			} catch (SearcherException se) {
				__log.warning(se.getMessage());
			}
		}

		return searchResults;
	}

	public List<ItemGroup> doSearchGroupped() {
		Query query = makeQuery();
		Filter filter = makeFilter();

		List<ItemGroup> searchResults = new ArrayList<ItemGroup>();

		if (query != null) {
			__log.info("query constructed: " + query.toString());
			try {

				Date startDate = new Date();
				SearcherResult result = SearcherWrapper.search(query, filter, 1000);
				Date stopDate = new Date();

				searchTime = stopDate.getTime() - startDate.getTime();

				List<Document> docs = result.getChunk();

				ResultFilter resultFilter = new OnlineResultFilter();
				List<Integer> idxs = resultFilter.filter(docs);
				Collections.sort(idxs);
				int count = 0;
				for (Integer idx : idxs) {
					// following should be effective while list is linked and
					// not array-based
					// profile this anyway, since removal still can be expensive
					// (must traverse list until index met)
					Document onlineDoc = docs.get(idx);
					docs.remove(idx.intValue());
					docs.add(count, onlineDoc);
					count++;
				}

				searchResults = makeGroupsFromDocs(docs);

				presentationTime = new Date().getTime() - stopDate.getTime();

				overallCount = searchResults.size();

			} catch (SearcherException se) {
				__log.warning(se.getMessage());
			}
		}

		return searchResults;
	}

	public int getOverallCount() {
		return overallCount;
	}

	public long getSearchTime() {
		return searchTime;
	}

	public long getPresentationTime() {
		return presentationTime;
	}

	private static List<String> prepareQueryParameter(String str) {
		List<String> result = new LinkedList<String>();
		if (str != null) {
			str = str.replaceAll("\\*|_|-|\\.|,|\\:|\\[|\\]|#|\\(|\\)|'|/|&", " ");
			String[] terms = str.toLowerCase().split(" ");
			for (String term : terms) {
				term = term.trim();
				if (term.length() >= MIN_TERM_LENGTH) {
					result.add(term);
				}
			}
		}
		return result;
	}

	private static List<ItemGroup> makeGroupsFromDocs(List<Document> docs) {
		List<ItemGroup> groups = new LinkedList<ItemGroup>();
		for (Document doc : docs) {
			boolean added = false;
			for (ItemGroup group : groups) {
				if (group.matches(doc)) {
					group.add(doc);
					added = true;
					break;
				}
			}
			if (!added) {
				groups.add(new ItemGroup(doc));
			}
		}
		return groups;
	}

	private static List<SearchResult> prepareResults(List<Document> results) {
		List<SearchResult> searchResults = new ArrayList<SearchResult>(results.size());
		for (Document doc : results) {
			searchResults.add(new SearchResult(doc));
		}
		return searchResults;
	}

	private Query makeSimpleQuery(String userQuery) {
		List<String> terms = prepareQueryParameter(userQuery);

		if (terms.size() == 0) {
			return null;
		}

		BooleanQuery query = new BooleanQuery(false);
		BooleanQuery.setMaxClauseCount(config.getMaxClauseCount());

		for (String item : terms) {
			BooleanQuery itemQuery = new BooleanQuery();

			BooleanClause.Occur occurItem = BooleanClause.Occur.SHOULD;
			if (item.startsWith("!")) {
				item = item.substring(1);
				occurItem = BooleanClause.Occur.MUST_NOT;
			}

			Query nameQuery = new WildcardQuery(new Term(IndexFields.NAME, prepareItem(item)));
			itemQuery.add(nameQuery, BooleanClause.Occur.SHOULD);

			Query pathQuery = new WildcardQuery(new Term(IndexFields.PATH, prepareItem(item)));
			itemQuery.add(pathQuery, BooleanClause.Occur.SHOULD);

			query.add(itemQuery, occurItem);
		}

		return query;
	}

	private Query makeAdvancedQuery(String dir, String file, String ext) {
		BooleanQuery query = new BooleanQuery(false);
		BooleanQuery.setMaxClauseCount(config.getMaxClauseCount());

		List<String> dirTerms = prepareQueryParameter(dir);
		List<String> fileTerms = prepareQueryParameter(file);
		List<String> extTerms = prepareQueryParameter(ext);

		if (fileTerms.size() != 0 || extTerms.size() != 0) // search for files
		{
			if (fileTerms.size() != 0) {
				BooleanQuery fileQuery = new BooleanQuery();
				for (String item : fileTerms) {
					BooleanClause.Occur occurItem = BooleanClause.Occur.SHOULD;
					if (item.startsWith("!")) {
						item = item.substring(1);
						occurItem = BooleanClause.Occur.MUST_NOT;
					}
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
					BooleanClause.Occur occurItem = BooleanClause.Occur.SHOULD;
					if (item.startsWith("!")) {
						item = item.substring(1);
						occurItem = BooleanClause.Occur.MUST_NOT;
						negations++;
					}

					Query pathQuery = new WildcardQuery(new Term(IndexFields.PATH, prepareItem(item)));
					dirQuery.add(pathQuery, occurItem);
				}
				// it must be at least one positive clause in query to be executed.
				// so add one if all user clauses are nagative.
				if (dirTerms.size() == negations) {
					Query pathQuery = new WildcardQuery(new Term(IndexFields.PATH, "*"));
					dirQuery.add(pathQuery, BooleanClause.Occur.SHOULD);
				}
				query.add(dirQuery, BooleanClause.Occur.MUST);
			}
		} else if (dirTerms.size() != 0) { // search for directories only, since file name was not specified
			for (String item : dirTerms) {
				BooleanQuery dirQuery = new BooleanQuery();

				BooleanClause.Occur occurItem = BooleanClause.Occur.SHOULD;
				if (item.startsWith("!")) {
					item = item.substring(1);
					occurItem = BooleanClause.Occur.MUST_NOT;
				}

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

	private String prepareItem(String item) {
		return (config.isFastSearch()) ? item + "*" : "*" + item + "*";
	}

}
