package org.punksearch.ip;

public class IpRange
{
	private Ip	startIp;
	private Ip	finishIp;

	public IpRange(String strRange)
	{
		String[] parts = strRange.split("-");
		if (parts.length == 2)
		{
			startIp = new Ip(parts[0]);
			finishIp = new Ip(parts[1]);
		}
		else
		{
			startIp = new Ip(strRange);
			finishIp = new Ip(strRange);
		}
	}

	public Ip getFinishIp()
	{
		return finishIp;
	}

	public Ip getStartIp()
	{
		return startIp;
	}

	public String toString()
	{
		if (!startIp.equals(finishIp))
		{
			return startIp + "-" + finishIp;
		}
		else
		{
			return startIp.toString();
		}
	}
	
}
