package org.punksearch.searcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;

public class Searcher
{
	private static final Logger		__log	= Logger.getLogger(Searcher.class.getName());

	private IndexSearcher			indexSearcher;

	public Searcher(String dir)
	{
		try
		{
			indexSearcher = new IndexSearcher(FSDirectory.getDirectory(dir));
		}
		catch (IOException e)
		{
			throw new IllegalArgumentException("Index directory is invalid: " + dir);
		}
	}

	public SearcherResult search(Query query, Integer first, Integer last, Filter filter)
	{
		if (null != first && null != last && (first > last || first < 0 || last < 0))
		{
			throw new IllegalArgumentException("First (" + first + ") and last (" + last + ") should be non-negative and first should be more than or equal to last.");
		}

		try
		{
			//Query query = new QueryParser(SearcherConstants.NAME, new StandardAnalyzer()).parse(text);

			//Sort sort = (null != sortFieldId)? new Sort(new SortField(sortFieldId + SearcherConstants.SORT_SUFFIX)) : null;
			Hits hits = indexSearcher.search(query, filter);

			if (null == first)
			{
				first = 0;
			}

			if (null == last || hits.length() - 1 < last)
			{
				last = hits.length() - 1;
			}

			List<Document> docs = new ArrayList<Document>(last - first + 1);
			for (int i = first; i <= last; i++)
			{
				Document doc = hits.doc(i);
				doc.setBoost(hits.score(i));
				docs.add(doc);
			}

			return new SearcherResult(hits.length(), docs);
		}
		catch (IOException e)
		{
			throw new RuntimeException("IOException during search", e);
		}
	}
	
	public SearcherResult search(Query query, Filter filter, Integer limit)
	{
		
		try
		{
			Hits hits = indexSearcher.search(query, filter);
			
			int myLimit = (limit == null || limit <= 0 || limit > hits.length())? hits.length() : limit;
			
			List<Document> docs = new ArrayList<Document>(myLimit);
			for (int i = 0; i < myLimit; i++)
			{
				Document doc = hits.doc(i);
				doc.setBoost(hits.score(i));
				docs.add(doc);
			}
			return new SearcherResult(hits.length(), docs);
		}
		catch (IOException e)
		{
			throw new RuntimeException("IOException during search", e);
		}
	}
}
