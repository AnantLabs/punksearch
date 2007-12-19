package org.punksearch.web.filters;

import java.io.IOException;
import java.util.BitSet;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Filter;


public class CachedFilter extends Filter
{
	private Filter filter;
	private BitSet bitSet = null;
	
	public CachedFilter(Filter filter)
	{
		this.filter = filter;
	}

	@Override
	public BitSet bits(IndexReader reader) throws IOException
	{
		if (bitSet != null)
			return bitSet;
		
		bitSet = new BitSet();
		synchronized (bitSet)
		{
			bitSet = filter.bits(reader);
		}
		return bitSet; 
	}
	
	
	
}
