package org.punksearch.searcher.filters;

import org.apache.lucene.search.Filter;
import org.punksearch.common.FileType;
import org.punksearch.common.FileTypes;
import org.punksearch.common.IndexFields;

public class FilterFactory
{

	public static NumberRangeFilter<Long> createNumberFilter(String fieldName, Long min, Long max)
	{
		if (min == null && max == null)
		{
			throw new IllegalArgumentException("Both min and max can't be null at the same time");
		}
		if (min != null && max != null && min > max)
		{
			throw new IllegalArgumentException("min > max");
		}
		
		boolean includeLower = (min != null);
		boolean includeUpper = (max != null);
		
		NumberRangeFilter<Long> filter = new NumberRangeFilter<Long>(fieldName, min, max, includeLower, includeUpper) {
			public Long termTextToNumber(String text) {
				return Long.valueOf(text);
			}
		};
		return filter;
	}
	
	public Filter makeSizeFilter(FileTypes types, String typeName) {
		FileType type = types.get(typeName);
		Long min = (type == null || type.getMinBytes() <= 0) ? null : type.getMinBytes();
		Long max = (type == null || type.getMaxBytes() <= 0) ? null : type.getMaxBytes();
		NumberRangeFilter<Long> sizeFilter = createNumberFilter(IndexFields.SIZE, min, max);
		return sizeFilter;
	}
	
}
