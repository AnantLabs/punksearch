package org.punksearch.web.filters;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryFilter;
import org.punksearch.commons.SearcherConstants;
import org.punksearch.searcher.filters.CompositeFilter;
import org.punksearch.searcher.filters.FilterFactory;
import org.punksearch.searcher.filters.NumberRangeFilter;

public class TypeFilters
{
	public static String	TYPE_FILM	= "film";
	public static String	TYPE_MUSIC	= "music";
	public static String	TYPE_ISO	= "iso";

	private static Long		MB			= 1024L * 1024L;

	private static Map<String, Filter> filters = new HashMap<String, Filter>();
	
	static
	{
		init();
	}
	
	public static void reset()
	{
		filters.clear();
		init();
	}
	
	public static Filter get(String type)
	{
		return filters.get(type);
	}

	private static void init()
	{
		filters.put(TYPE_FILM,  new CachedFilter(createFilter(TYPE_FILM)));
		filters.put(TYPE_MUSIC, new CachedFilter(createFilter(TYPE_MUSIC)));
		filters.put(TYPE_ISO,   new CachedFilter(createFilter(TYPE_ISO)));
	}
	
	private static Filter createFilter(String type)
	{
		Long min = null;
		Long max = null;
		String ext = null;
		if (type.equals(TYPE_FILM))
		{
			min = 500 * MB;
			ext = "avi vob mpg";
		}
		else if (type.equals(TYPE_MUSIC))
		{
			min = 1 * MB;
			max = 100 * MB;
			ext = "mp3 ogg wav flac";
		}
		else if (type.equals(TYPE_ISO))
		{
			min = 10 * MB;
			ext = "iso mdf";
		}
		else
		{
			throw new IllegalArgumentException("unknown type");
		}

		CompositeFilter filter = new CompositeFilter();

		if (min != null || max != null)
		{
			NumberRangeFilter<Long> sizeFilter = FilterFactory.createNumberFilter(SearcherConstants.SIZE, min, max);
			filter.add(sizeFilter);
		}

		if (ext != null && ext.length() != 0)
		{
			try
			{
				Query extQuery = new QueryParser(SearcherConstants.EXTENSION, new StandardAnalyzer()).parse(ext);
				QueryFilter extFilter = new QueryFilter(extQuery);
				filter.add(extFilter);
			}
			catch (ParseException pe)
			{
				// dummy
			}
		}

		return filter;

	}
	
}
