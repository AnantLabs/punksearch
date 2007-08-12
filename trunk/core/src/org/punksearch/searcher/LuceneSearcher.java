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
import org.punksearch.commons.SearcherConfig;
import org.punksearch.commons.SearcherException;

public class LuceneSearcher
{
	private static final Logger		__log	= Logger.getLogger(LuceneSearcher.class.getName());
	private static IndexSearcher	searcher;
	
	static
	{
		try
		{
			searcher = new IndexSearcher(FSDirectory.getDirectory(SearcherConfig.getInstance().getIndexDirectory()));
		}
		catch (IOException e)
		{
			__log.severe("Problem with index directory, can't init searcher! " + e.getMessage());
		}
	}
	
	public static SearcherResult search(Query query, Integer first, Integer last, Filter filter) throws SearcherException
	{
		if (null != first && null != last && (first > last || first < 0 || last < 0))
		{
			String errorMessage = "First and last should be non-negative and first shoul be more than or equal to last.";
			throw new IllegalArgumentException(errorMessage);
		}

		try
		{
			//Query query = new QueryParser(SearcherConstants.NAME, new StandardAnalyzer()).parse(text);

			//Sort sort = (null != sortFieldId)? new Sort(new SortField(sortFieldId + SearcherConstants.SORT_SUFFIX)) : null;
			Hits hits = searcher.search(query, filter);


			if (null == first)
			{
				first = 0;
			}

			if (null == last || hits.length() - 1 < last)
			{
				last = hits.length() - 1;
			}

			List<Document> docs = new ArrayList<Document>(last-first+1);
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
			__log.warning(e.getMessage());
			throw new SearcherException("Problem with Lucene index directory.", e);
		}
		catch (RuntimeException e)
		{
			__log.warning(e.getMessage());
			throw new SearcherException("Exception during search: " + e.getMessage(), e);
		}
		finally
		{
		}
	}
}
