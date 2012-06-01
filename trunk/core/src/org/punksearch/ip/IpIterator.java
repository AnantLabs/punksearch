package org.punksearch.ip;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class IpIterator implements Iterator<Ip>
{
	private List<IpRange>		ranges;
	private Iterator<IpRange>	rangesIterator;
	private IpRange				currentIpRange	= null;
	private Ip					nextIp			= null;

	public IpIterator(List<IpRange> ranges)
	{
		this.ranges = ranges;
		rangesIterator = this.ranges.iterator();
		currentIpRange = rangesIterator.next();
		nextIp = currentIpRange.getStartIp();
	}

	public boolean hasNext()
	{
		return (nextIp != null);
	}

	public Ip next()
	{
		if (nextIp == null)
		{
			throw new NoSuchElementException();
		}

		Ip result = nextIp;

		nextIp = new Ip(nextIp.toLong() + 1);
		if (nextIp.toLong() > currentIpRange.getFinishIp().toLong())
		{
			if (rangesIterator.hasNext())
			{
				currentIpRange = rangesIterator.next();
				nextIp = currentIpRange.getStartIp();
			}
			else
			{
				nextIp = null;
			}
		}
		return result;
	}

	public void remove()
	{
		throw new UnsupportedOperationException();
	}
}
