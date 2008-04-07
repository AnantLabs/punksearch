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
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.punksearch.common.IndexFields;
import org.punksearch.common.SearcherException;
import org.punksearch.searcher.EasyQueryParser;
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
	private static final int MAX_DOCS         = 10000;

	private static Logger    __log            = Logger.getLogger(SearchParams.class.getName());

	private SearchParams     params;
	// private SearcherConfig config = SearcherConfig.getInstance();

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
		EasyQueryParser parser = new EasyQueryParser();
		if (params.type.equals("advanced")) {
			return parser.makeAdvancedQuery(params.dir, params.file, params.ext);
		} else {
			return parser.makeSimpleQuery(params.query);
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
				overallCount = result.count();
				searchResults = prepareResults(result.items());
			} catch (SearcherException se) {
				__log.warning(se.getMessage());
			}
		}

		return searchResults;
	}

	public List<ItemGroup> doSearchGroupped() {
		return doSearchGroupped(0, MAX_DOCS);
	}

	public List<ItemGroup> doSearchGroupped(int start, int stop) {
		Query query = makeQuery();
		if (query == null) {
			return new ArrayList<ItemGroup>(0);
		}
		__log.info("query constructed: " + query.toString());

		Filter filter = makeFilter();

		List<ItemGroup> searchResults = new ArrayList<ItemGroup>();

		try {
			Date startDate = new Date();
			SearcherResult result = SearcherWrapper.search(query, filter, MAX_DOCS);
			Date stopDate = new Date();

			List<Document> docs = sortDocsOnlineFirst(result.items());
			searchResults = makeGroupsFromDocs(docs);

			searchTime = stopDate.getTime() - startDate.getTime();
			presentationTime = new Date().getTime() - stopDate.getTime();
			overallCount = searchResults.size();
		} catch (SearcherException se) {
			__log.warning(se.getMessage());
		}

		int min = (start > 0) ? Math.max(0, start) : 0;
		int max = (stop > 0) ? Math.min(overallCount, stop) : 0;
		if (min > max || min > overallCount) {
			min = 0;
			max = 0;
		}
		return searchResults.subList(min, max);
	}

	// following should be effective while list is linked and not array-based
	// profile this anyway, since removal still can be expensive (must traverse list until index met)
	private static List<Document> sortDocsOnlineFirst(List<Document> docs) {
		List<Document> result = new ArrayList<Document>(docs.size());

		ResultFilter resultFilter = new OnlineResultFilter();
		List<Integer> idxs = resultFilter.filter(docs);
		// __log.info("Online docs: " + idxs.size());
		Collections.sort(idxs);

		int count = 0;
		for (Integer idx : idxs) {
			Document onlineDoc = docs.get(idx);
			// docs.remove(idx.intValue());
			result.add(onlineDoc);
			count++;
		}
		for (int i = 0; i < docs.size(); i++) {
			if (!idxs.contains(i)) {
				result.add(docs.get(i));
			}
		}
		return result;
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
}
