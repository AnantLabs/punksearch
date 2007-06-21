package org.punksearch.searcher;

import java.io.IOException;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Filter;

public class CompositeFilter extends Filter
{
	private Set<Filter> filters = new HashSet<Filter>();
	
	public void add(Filter filter)
	{
		if (filter == null)
		{
			throw new IllegalArgumentException("filter is null");
		}
		filters.add(filter);
	}
	
	public void remove(Filter filter)
	{
		filters.remove(filter);
	}
	
	@Override
	public BitSet bits(IndexReader reader) throws IOException
	{
		if (filters.size() == 0)
		{
			return new BitSet(reader.maxDoc());
		}
		
		BitSet result = null;
		for (Filter filter : filters)
		{
			if (result == null)
				result = filter.bits(reader);
			else
				result.and(filter.bits(reader));
		}
		return result;
	}

}
