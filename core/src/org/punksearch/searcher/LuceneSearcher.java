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
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.punksearch.commons.SearcherException;
import org.punksearch.searcher.filters.NumberRangeFilter;


public class LuceneSearcher
{
	private static final Logger		__log 	= Logger.getLogger(LuceneSearcher.class.getName());
	
	private Directory	luceneDirectory;
	private int			overallCount	= 0;
	//private String		sortFieldId	= null;
	
	public LuceneSearcher(String path) throws IOException
	{
		this(FSDirectory.getDirectory(path));
	}
	
	public LuceneSearcher(Directory dir)
	{
		luceneDirectory = dir;
	}
	
	public List<Document> search(Query query, Integer first, Integer last, Filter filter) throws SearcherException
	{
		IndexSearcher searcher = null;
		
		if (null != first && null != last && (first > last || first < 0 || last < 0))
		{
			String errorMessage = "First and last should be non-negative and first shoul be more than or equal to last.";
			throw new IllegalArgumentException(errorMessage);
		}
		
		try
		{
			searcher = new IndexSearcher(luceneDirectory);
			//Query query = new QueryParser(SearcherConstants.NAME, new StandardAnalyzer()).parse(text);

			//Sort sort = (null != sortFieldId)? new Sort(new SortField(sortFieldId + SearcherConstants.SORT_SUFFIX)) : null;
			Hits hits = searcher.search(query, filter);

			List<Document> result = new ArrayList<Document>();
			
			if (null == first)
			{
				first = 0;
			}
			
			if (null == last || hits.length() - 1 < last)
			{
				last = hits.length() - 1;
			}
			
			for (int i = first; i <= last; i++)
			{
				Document doc = hits.doc(i);
				doc.setBoost(hits.score(i));
				result.add(doc);
			}	
			overallCount = hits.length();
			
			return result;
		}
		catch(IOException e)
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
			if (null != searcher)
			{
				try
				{
					searcher.close();
				}
				catch (IOException e)
				{
					__log.warning(e.getMessage());
					throw new SearcherException("Unable to close Lucene index directory.", e);
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.navio.search.Searcher#lastCount()
	 */	
	public int overallCount()
	{
		return overallCount;
	}
	/*
	public void setSortField(String fieldId)
	{
		if (null == fieldId || fieldId.equals(""))
		{
			this.sortFieldId = null;
		}
		else
		{
			this.sortFieldId	= fieldId;
		}
	}
	*/
	
}
