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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.punksearch.common.IndexFields;
import org.punksearch.searcher.EasyQueryParser;
import org.punksearch.searcher.SearcherResult;
import org.punksearch.searcher.filters.CompositeFilter;
import org.punksearch.searcher.filters.FilterFactory;
import org.punksearch.searcher.filters.NumberRangeFilter;
import org.punksearch.web.filters.OnlineResultFilter;
import org.punksearch.web.filters.TypeFilters;

import java.util.*;

/**
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class SearchAction {

    private static final int MAX_DOCS = 10000;

    private static Log log = LogFactory.getLog(SearchParams.class);

    private SearchParams params;
    // private SearcherConfig config = SearcherConfig.getInstance();

    private long searchTime = 0;
    private long presentationTime = 0;
    private int overallCount = 0;

    public SearchAction(SearchParams params) {
        this.params = params;
    }

    private Filter makeFilter() {
        if (SearchParams.TYPE_ADVANCED.equals(params.type)) {
            List<Filter> filters = new LinkedList<Filter>();

            if (params.minSize != null || params.maxSize != null) {
                Filter sizeFilter = FilterFactory.createNumberFilter(IndexFields.SIZE, params.minSize, params.maxSize);
                filters.add(sizeFilter);
            }

            if (params.fromDate != null || params.toDate != null) {
                Filter dateFilter = FilterFactory.createNumberFilter(IndexFields.DATE, params.fromDate, params.toDate);
                filters.add(dateFilter);
            }

            if (filters.isEmpty()) {
                return null;
            } else if (filters.size() == 1) {
                return filters.get(0);
            } else {
                return new CompositeFilter(filters);
            }
        } else {
            return TypeFilters.get(params.type);
        }
    }

    private Query makeQuery() {
        EasyQueryParser parser = EasyQueryParser.getInstance();
        if (SearchParams.TYPE_ADVANCED.equals(params.type)) {
            return parser.makeAdvancedQuery(params.dir, params.file, params.ext);
        } else {
            return parser.makeSimpleQuery(params.query);
        }
    }

    /*@Deprecated
     public List<SearchResult> doSearch() {
         Query query = makeQuery();
         Filter filter = makeFilter();

         List<SearchResult> searchResults = new ArrayList<SearchResult>();

         if (query != null) {
             log.info("query constructed: " + query.toString());
             Date startDate = new Date();
             SearcherResult result = SearcherWrapper.search(query, params.first, params.last, filter);
             Date stopDate = new Date();
             searchTime = stopDate.getTime() - startDate.getTime();
             overallCount = result.count();
             searchResults = prepareResults(result.items());
         }

         return searchResults;
     }*/

    public List<ItemGroup> doSearchGroupped() {
        return doSearchGroupped(0, MAX_DOCS);
    }

    public List<ItemGroup> doSearchGroupped(int start, int stop) {
        Query query = makeQuery();
        if (query == null) {
            return Collections.emptyList();
        }
        log.info("query constructed: " + query.toString());

        Filter filter = makeFilter();

        long startTime = System.currentTimeMillis();
        SearcherResult result = SearcherWrapper.search(query, filter, MAX_DOCS);
        long stopTime = System.currentTimeMillis();

        List<Document> docs = sortDocsOnlineFirst(result.items());
        List<ItemGroup> searchResults = makeGroupsFromDocs(docs);

        searchTime = stopTime - startTime;
        presentationTime = System.currentTimeMillis() - stopTime;
        overallCount = searchResults.size();

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
        // log.info("Online docs: " + idxs.size());
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

    /**
     * Groups items (lucene documents) into groups by size and extension (see ItemGroup for details)
     * <p/>
     * Returns groups sorted according to the order of first group items sort order
     *
     * @param docs the sorted list of lucene documents (sorted by relevance and by online/offline status)
     * @return list of item groups
     */
    private static List<ItemGroup> makeGroupsFromDocs(List<Document> docs) {
        List<ItemGroup> result = new LinkedList<ItemGroup>();

        // following hash was created just in order to speedup the grouping
        // this enables us to geach potential group quickly and check if item matches it
        // the hash key is the item size
        Map<String, Set<ItemGroup>> hash = new HashMap<String, Set<ItemGroup>>();

        for (Document doc : docs) {
            String size = doc.get(IndexFields.SIZE);
            Set<ItemGroup> groups = hash.get(size);
            if (groups == null) {
                groups = new HashSet<ItemGroup>();
                hash.put(size, groups);
            }
            boolean added = false;
            for (ItemGroup group : groups) {
                if (group.matches(doc)) {
                    group.add(doc);
                    added = true;
                    break;
                }
            }
            if (!added) {
                ItemGroup group = new ItemGroup(doc);
                groups.add(group);
                result.add(group); // we have to put the group in result here, since we want to preserve order of items
            }
        }
        return result;
    }

    /*private static List<SearchResult> prepareResults(List<Document> results) {
        List<SearchResult> searchResults = new ArrayList<SearchResult>(results.size());
        for (Document doc : results) {
            searchResults.add(new SearchResult(doc));
        }
        return searchResults;
    }*/
}
