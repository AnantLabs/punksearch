package org.punksearch.searcher.filters;

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
	
	@Override
	public String toString()
	{
		String result = "";
		for (Filter filter : filters)
		{
			result += "; " + filter.toString();
		}
		return result;
	}
	
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (!(o instanceof CompositeFilter))
			return false;
		
		CompositeFilter other = (CompositeFilter) o;
		if (other.filters.size() != this.filters.size())
		{
			return false;
		}
		else
		{
			for (Filter myFilter : this.filters)
			{
				if (!other.filters.contains(myFilter))
				{
					return false;
				}
			}
			for (Filter hisFilter : other.filters)
			{
				if (!this.filters.contains(hisFilter))
				{
					return false;
				}
			}
			return true;
		}
		
	}

}
