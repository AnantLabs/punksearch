package org.punksearch.web.filters;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryFilter;
import org.apache.lucene.search.TermQuery;
import org.punksearch.commons.IndexFields;
import org.punksearch.searcher.filters.CompositeFilter;
import org.punksearch.searcher.filters.FilterFactory;
import org.punksearch.searcher.filters.NumberRangeFilter;
import org.punksearch.web.Types;

public class TypeFilters
{

	private static Long					MB		= 1024L * 1024L;

	private static Map<String, Filter>	filters	= new HashMap<String, Filter>();

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
		filters.put(Types.FILM,    new CachedFilter(createFilter(Types.FILM)));
		filters.put(Types.CLIP,    new CachedFilter(createFilter(Types.CLIP)));
		filters.put(Types.MUSIC,   new CachedFilter(createFilter(Types.MUSIC)));
		filters.put(Types.ISO,     new CachedFilter(createFilter(Types.ISO)));
		filters.put(Types.PICTURE, new CachedFilter(createFilter(Types.PICTURE)));
		filters.put(Types.ARCHIVE, new CachedFilter(createFilter(Types.ARCHIVE)));
		filters.put(Types.EXE,     new CachedFilter(createFilter(Types.EXE)));
		filters.put(Types.DOC,     new CachedFilter(createFilter(Types.DOC)));
		filters.put(Types.DIR,     new CachedFilter(createFilter(Types.DIR)));
	}

	private static Filter createFilter(String type)
	{
		Long min = null;
		Long max = null;
		String ext = null;
		if (type.equals(Types.FILM))
		{
			min = 500 * MB;
			ext = "avi vob mpg";
		}
		else if (type.equals(Types.CLIP))
		{
			min = 3 * MB;
			max = 100 * MB;
			ext = "avi mpg mpeg wmv mov wmf";
		}
		else if (type.equals(Types.MUSIC))
		{
			min = 1 * MB;
			max = 100 * MB;
			ext = "mp3 ogg wav wma flac";
		}
		else if (type.equals(Types.ISO))
		{
			min = 10 * MB;
			ext = "iso mdf";
		}
		else if (type.equals(Types.PICTURE))
		{
			ext = "jpg jpeg gif png bmp tif";
		}
		else if (type.equals(Types.ARCHIVE))
		{
			ext = "zip rar arj gz tgz tar bz2";
		}
		else if (type.equals(Types.EXE))
		{
			ext = "exe";
		}
		else if (type.equals(Types.DOC))
		{
			ext = "rtf doc xls ppt pdf ps djvu odt ods odp odg odf txt htm html xhtml";
		}
		else if (type.equals(Types.DIR))
		{
			ext = "";
		}
		else
		{
			throw new IllegalArgumentException("unknown type");
		}

		CompositeFilter filter = new CompositeFilter();

		if (min != null || max != null)
		{
			NumberRangeFilter<Long> sizeFilter = FilterFactory.createNumberFilter(IndexFields.SIZE, min, max);
			filter.add(sizeFilter);
		}

		if (ext != null)
		{
			try
			{
				Query extQuery;
				if (ext.length() != 0)
				{
					extQuery = new QueryParser(IndexFields.EXTENSION, new StandardAnalyzer()).parse(ext);
				}
				else
				{
					extQuery = new TermQuery(new Term(IndexFields.EXTENSION, ""));
				}
				System.out.println(extQuery);
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
