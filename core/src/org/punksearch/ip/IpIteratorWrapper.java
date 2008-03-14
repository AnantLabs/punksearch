package org.punksearch.ip;

import java.util.Iterator;
import java.util.List;


public class IpIteratorWrapper implements Iterator<String>
{
	IpIterator iterator;
	
	public IpIteratorWrapper(List<IpRange> ranges)
	{
		iterator = new IpIterator(ranges);
	}
	
	public boolean hasNext()
	{
		throw new UnsupportedOperationException();
	}

	public synchronized String next()
	{
		if (iterator != null && iterator.hasNext())
		{
			return iterator.next().toString();
		}
		return null;
	}

	public void remove()
	{
		throw new UnsupportedOperationException();
	}

}
