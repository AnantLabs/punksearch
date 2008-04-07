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
import org.punksearch.common.FileTypes;
import org.punksearch.common.IndexFields;
import org.punksearch.searcher.filters.CompositeFilter;
import org.punksearch.searcher.filters.FilterFactory;
import org.punksearch.searcher.filters.NumberRangeFilter;

public class TypeFilters {

	private static Long                MB      = 1024L * 1024L;

	private static Map<String, Filter> filters = new HashMap<String, Filter>();

	private static FileTypes           types   = new FileTypes();

	static {
		init();
	}

	public static void reset() {
		filters.clear();
		init();
	}

	public static Filter get(String type) {
		return filters.get(type);
	}

	private static void init() {
		types.readFromDefaultFile();

		for (String key : types.list()) {
			filters.put(key, new CachedFilter(createFilter(key)));
		}
	}

	private static Filter createFilter(String type) {
		Long min = types.get(type).getMinBytes();
		Long max = types.get(type).getMaxBytes();
		String ext = "";
		for (String tmp : types.get(type).getExtensions()) {
			ext += " " + tmp;
		}
		System.out.println("Type: " + type + " = " + min + ":" + max + " -> " + ext);
		CompositeFilter filter = new CompositeFilter();

		if (min != null || max != null) {
			NumberRangeFilter<Long> sizeFilter = FilterFactory.createNumberFilter(IndexFields.SIZE, min, max);
			filter.add(sizeFilter);
		}

		if (ext != null) {
			try {
				Query extQuery;
				if (ext.length() != 0) {
					extQuery = new QueryParser(IndexFields.EXTENSION, new StandardAnalyzer()).parse(ext);
				} else {
					extQuery = new TermQuery(new Term(IndexFields.EXTENSION, ""));
				}
				System.out.println(extQuery);
				QueryFilter extFilter = new QueryFilter(extQuery);
				filter.add(extFilter);
			} catch (ParseException pe) {
				// dummy
			}
		}

		return filter;

	}

	public static FileTypes getTypes() {
		return types;
	}

}
