package org.punksearch.web.statistics;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.punksearch.commons.SearcherConfig;
import org.punksearch.commons.SearcherConstants;
import org.punksearch.searcher.LuceneSearcher;
import org.punksearch.web.filters.TypeFilters;

public class FileTypeStatistics
{
	
	public static int count(String type)
	{
		BooleanQuery query = new BooleanQuery();
		Query smbQuery = new WildcardQuery(new Term(SearcherConstants.HOST, "smb_*"));
		Query ftpQuery = new WildcardQuery(new Term(SearcherConstants.HOST, "ftp_*"));
		query.add(smbQuery, Occur.SHOULD);
		query.add(ftpQuery, Occur.SHOULD);
		
		Filter filter = TypeFilters.get(type);
		
		try
		{
			LuceneSearcher searcher = new LuceneSearcher(SearcherConfig.getInstance().getIndexDirectory());
			searcher.search(query, 0, 1, filter);
			return searcher.overallCount();
		}
		catch (Exception e)
		{
			return 0;
		}
	}
	
	
}
