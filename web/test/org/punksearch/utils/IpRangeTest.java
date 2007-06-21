package org.punksearch.utils;

import org.punksearch.utils.Ip;
import org.punksearch.utils.IpRange;

import junit.framework.TestCase;

public class IpRangeTest extends TestCase
{

	public void testSimpleRange()
	{
		IpRange ipr = new IpRange("1.2.3.4");
		Ip ip = new Ip("1.2.3.4");
		assertEquals(ip, ipr.getStartIp());
		assertEquals(ip, ipr.getFinishIp());
	}

	public void testConventionalRange() {
		IpRange ipr = new IpRange("1.2.3.4-1.2.4.10");
		Ip ip1 = new Ip("1.2.3.4");
		Ip ip2 = new Ip("1.2.4.10");
		assertEquals(ip1, ipr.getStartIp());
		assertEquals(ip2, ipr.getFinishIp());
	}
}