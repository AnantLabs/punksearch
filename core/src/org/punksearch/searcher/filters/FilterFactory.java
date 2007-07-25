package org.punksearch.searcher.filters;

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
		
		NumberRangeFilter<Long> filter = new NumberRangeFilter<Long>(fieldName, min, max, false, false) {
			public Long termTextToNumber(String text) {
				return Long.valueOf(text);
			}
		};
		return filter;
	}
	
}
