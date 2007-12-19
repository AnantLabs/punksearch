package org.punksearch.web;

import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.punksearch.commons.SearcherException;
import org.punksearch.searcher.ResultFilter;
import org.punksearch.searcher.Searcher;
import org.punksearch.searcher.SearcherResult;
import org.punksearch.web.online.OnlineResultFilter;

public class SearcherWrapper
{
	private static Searcher searcher;
	
	public static void init(String dir)
	{
		searcher = new Searcher(dir);
	}
	
	public static SearcherResult search(Query query, Integer first, Integer last, Filter filter) throws SearcherException
	{
		return searcher.search(query, first, last, filter);
	}
	
	public static SearcherResult search(Query query, Filter filter, Integer limit, Boolean includeOffline) throws SearcherException
	{
		ResultFilter resultFilter = null;
		if (!includeOffline) {
			resultFilter = new OnlineResultFilter();
		}
		return searcher.search(query, filter, limit, resultFilter);
	}
	
}
