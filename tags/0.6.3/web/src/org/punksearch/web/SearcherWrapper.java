package org.punksearch.web;

import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.punksearch.commons.SearcherException;
import org.punksearch.searcher.Searcher;
import org.punksearch.searcher.SearcherResult;

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
	
}
