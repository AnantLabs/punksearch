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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.TermQuery;
import org.punksearch.common.FileType;
import org.punksearch.common.FileTypes;
import org.punksearch.common.IndexFields;
import org.punksearch.searcher.filters.CompositeFilter;
import org.punksearch.searcher.filters.FilterFactory;
import org.punksearch.searcher.filters.NumberRangeFilter;

public class TypeFilters {

	public static final String         DIRECTORY_KEY = "directory";

	private static Map<String, Filter> filters       = new HashMap<String, Filter>();
	private static FileTypes           types         = FileTypes.readFromDefaultFile();

	static {
		init();
	}

	public static Filter get(String type) {
		return filters.get(type);
	}

	private static void init() {

		for (String key : types.list()) {
			filters.put(key, create(key));
		}
		filters.put(DIRECTORY_KEY, createByItemType(null, null, IndexFields.TYPE_DIR));
	}

	/**
	 * 
	 * @param min
	 * @param max
	 * @param itemType
	 *            "DIR" or "FILE"
	 * @return
	 */
	private static Filter createByItemType(Long min, Long max, String itemType) {
		CompositeFilter filter = new CompositeFilter();

		if (min != null || max != null) {
			NumberRangeFilter<Long> sizeFilter = FilterFactory.createNumberFilter(IndexFields.SIZE, min, max);
			filter.add(sizeFilter);
		}

		Query typeQuery = new TermQuery(new Term(IndexFields.TYPE, itemType));
		QueryWrapperFilter typeFilter = new QueryWrapperFilter(typeQuery);
		filter.add(typeFilter);

		return new CachingWrapperFilter(filter);
	}

	private static Filter createByExt(Long min, Long max, Set<String> extensions) {
		CompositeFilter filter = new CompositeFilter();

		if (min != null || max != null) {
			NumberRangeFilter<Long> sizeFilter = FilterFactory.createNumberFilter(IndexFields.SIZE, min, max);
			filter.add(sizeFilter);
		}

		String extStr = "";
		for (String tmp : extensions) {
			extStr += " " + tmp;
		}
		extStr = extStr.trim();

		try {
			Query extQuery;
			if (extStr.length() != 0) {
				extQuery = new QueryParser(IndexFields.EXTENSION, new StandardAnalyzer()).parse(extStr);
			} else {
				extQuery = new TermQuery(new Term(IndexFields.EXTENSION, ""));
			}
			QueryWrapperFilter extFilter = new QueryWrapperFilter(extQuery);
			filter.add(extFilter);
		} catch (ParseException pe) {
			// dummy
		}

		return new CachingWrapperFilter(filter);
	}

	private static Filter create(String typeName) {
		FileType type = types.get(typeName);
		if (type != null) {
			Long min = type.getMinBytes();
			Long max = type.getMaxBytes();
			Set<String> ext = type.getExtensions();
			return createByExt(min, max, ext);
		} else {
			throw new IllegalArgumentException("File type not found: " + typeName);
		}
	}

	public static FileTypes getTypes() {
		return types;
	}

}
