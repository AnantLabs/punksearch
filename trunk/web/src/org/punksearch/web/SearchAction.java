package org.punksearch.web;

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
import org.punksearch.searcher.Searcher;
import org.punksearch.searcher.SearcherResult;
import org.punksearch.searcher.filters.CompositeFilter;
import org.punksearch.searcher.filters.FilterFactory;
import org.punksearch.searcher.filters.NumberRangeFilter;
import org.punksearch.web.filters.TypeFilters;

public class SearchAction
{

	private static final int	MIN_TERM_LENGTH	= 3;

	private static Logger		__log			= Logger.getLogger(SearchParams.class.getName());

	private SearchParams		params;
	private SearcherConfig		config			= SearcherConfig.getInstance();

	private long				searchTime		= 0;
	private int					overallCount	= 0;

	public SearchAction(SearchParams params)
	{
		this.params = params;
	}

	public List<SearchResult> doSearch()
	{
		Query query = null;
		Filter filter = null;

		if (params.type.equals("advanced"))
		{
			query = makeAdvancedQuery();

			NumberRangeFilter<Long> sizeFilter = null;
			if (params.minSize != null || params.maxSize != null)
			{
				sizeFilter = FilterFactory.createNumberFilter(IndexFields.SIZE, params.minSize, params.maxSize);
			}

			NumberRangeFilter<Long> dateFilter = null;
			if (params.fromDate != null || params.toDate != null)
			{
				dateFilter = FilterFactory.createNumberFilter(IndexFields.DATE, params.fromDate, params.toDate);
			}

			if (sizeFilter != null || dateFilter != null)
			{
				CompositeFilter resultFilter = new CompositeFilter();
				if (sizeFilter != null)
					resultFilter.add(sizeFilter);
				if (dateFilter != null)
					resultFilter.add(dateFilter);
				filter = resultFilter;
			}
		}
		else
		{
			query = makeSimpleQuery();
			filter = TypeFilters.get(params.type);
		}

		List<SearchResult> searchResults = null;

		if (query != null)
		{
			__log.info("query constructed: " + query.toString());
			try
			{
				//LuceneSearcher searcher = new LuceneSearcher(config.getIndexDirectory());
				Date startDate = new Date();
				SearcherResult result = SearcherWrapper.search(query, params.first, params.last, filter);
				Date stopDate = new Date();
				searchTime = stopDate.getTime() - startDate.getTime();
				overallCount = result.getHitCount();
				searchResults = prepareResults(result.getChunk());
			}
			catch (SearcherException se)
			{
				__log.warning(se.getMessage());
			}

		}

		return searchResults;
	}

	public int getOverallCount()
	{
		return overallCount;
	}

	public long getSearchTime()
	{
		return searchTime;
	}

	private static List<String> prepareQueryParameter(String str)
	{
		List<String> result = new LinkedList<String>();
		if (str != null)
		{
			str = str.replaceAll("\\*|_|-|\\.|,|\\:|\\[|\\]|#|\\(|\\)|'|/|&", " ");
			String[] terms = str.toLowerCase().split(" ");
			for (String term : terms)
			{
				term = term.trim();
				if (term.length() >= MIN_TERM_LENGTH)
				{
					result.add(term);
				}
			}
		}
		return result;
	}

	private static List<SearchResult> prepareResults(List<Document> results)
	{
		if (results == null)
			return null;

		List<SearchResult> searchResults = new LinkedList<SearchResult>();
		for (Document doc : results)
		{
			searchResults.add(new SearchResult(doc));
		}
		return searchResults;
	}

	private Query makeSimpleQuery()
	{
		BooleanQuery query = new BooleanQuery(false);
		BooleanQuery.setMaxClauseCount(config.getMaxClauseCount());

		List<String> terms = prepareQueryParameter(params.query);

		for (String item : terms)
		{
			BooleanQuery itemQuery = new BooleanQuery();

			BooleanClause.Occur occurItem = BooleanClause.Occur.MUST;
			if (item.startsWith("!"))
			{
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

	private Query makeAdvancedQuery()
	{
		BooleanQuery query = new BooleanQuery(false);
		BooleanQuery.setMaxClauseCount(config.getMaxClauseCount());

		List<String> dirTerms = prepareQueryParameter(params.dir);
		List<String> fileTerms = prepareQueryParameter(params.file);
		List<String> extTerms = prepareQueryParameter(params.ext);

		if (fileTerms.size() != 0 || extTerms.size() != 0) // search for files
		{
			if (fileTerms.size() != 0)
			{
				BooleanQuery fileQuery = new BooleanQuery();
				for (String item : fileTerms)
				{
					BooleanClause.Occur occurItem = BooleanClause.Occur.SHOULD;
					if (item.startsWith("!"))
					{
						item = item.substring(1);
						occurItem = BooleanClause.Occur.MUST_NOT;
					}
					Query nameQuery = new WildcardQuery(new Term(IndexFields.NAME, prepareItem(item)));
					fileQuery.add(nameQuery, occurItem);
				}
				query.add(fileQuery, BooleanClause.Occur.MUST);
			}

			if (extTerms.size() != 0)
			{
				BooleanQuery extQuery = new BooleanQuery();
				for (String item : extTerms)
				{
					Query termQuery = new TermQuery(new Term(IndexFields.EXTENSION, item));
					extQuery.add(termQuery, BooleanClause.Occur.SHOULD);
				}
				query.add(extQuery, BooleanClause.Occur.MUST);
			}
			else
			{
				Query extensionQuery = new TermQuery(new Term(IndexFields.EXTENSION, IndexFields.DIRECTORY_EXTENSION));
				query.add(extensionQuery, BooleanClause.Occur.MUST_NOT);
			}

			if (dirTerms.size() != 0) // restrict files to occur in specified directories only
			{
				BooleanQuery dirQuery = new BooleanQuery();
				int negations = 0;

				for (String item : dirTerms)
				{
					BooleanClause.Occur occurItem = BooleanClause.Occur.SHOULD;
					if (item.startsWith("!"))
					{
						item = item.substring(1);
						occurItem = BooleanClause.Occur.MUST_NOT;
						negations++;
					}

					Query pathQuery = new WildcardQuery(new Term(IndexFields.PATH, prepareItem(item)));
					dirQuery.add(pathQuery, occurItem);
				}
				if (dirTerms.size() == negations) // it must be at least one positive clause in query to be executed. so add one if all user clauses are nagative.
				{
					Query pathQuery = new WildcardQuery(new Term(IndexFields.PATH, "*"));
					dirQuery.add(pathQuery, BooleanClause.Occur.SHOULD);
				}
				query.add(dirQuery, BooleanClause.Occur.MUST);
			}
		}
		else if (dirTerms.size() != 0) // search for directories only, since file name was not specified
		{
			for (String item : dirTerms)
			{
				BooleanQuery dirQuery = new BooleanQuery();

				BooleanClause.Occur occurItem = BooleanClause.Occur.SHOULD;
				if (item.startsWith("!"))
				{
					item = item.substring(1);
					occurItem = BooleanClause.Occur.MUST_NOT;
				}

				Query nameQuery = new WildcardQuery(new Term(IndexFields.NAME, prepareItem(item)));
				dirQuery.add(nameQuery, BooleanClause.Occur.MUST);

				Query extensionQuery = new TermQuery(new Term(IndexFields.EXTENSION, IndexFields.DIRECTORY_EXTENSION));
				dirQuery.add(extensionQuery, BooleanClause.Occur.MUST);

				query.add(dirQuery, occurItem);
			}
		}
		else
		{
			return null;
		}
		return query;
	}

	private String prepareItem(String item)
	{
		return (config.isFastSearch()) ? item + "*" : "*" + item + "*";
	}

}
