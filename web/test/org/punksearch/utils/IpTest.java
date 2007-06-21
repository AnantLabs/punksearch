package org.punksearch.utils;

import org.punksearch.utils.Ip;

import junit.framework.TestCase;

public class IpTest extends TestCase
{

	public void testToString()
	{
		Ip ip1 = new Ip("0.0.0.0");
		assertEquals("0.0.0.0", ip1.toString());

		Ip ip2 = new Ip("1.2.3.4");
		assertEquals("1.2.3.4", ip2.toString());
	}

	public void testToLong()
	{
		Ip ip1 = new Ip("0.0.0.0");
		assertEquals(0L, ip1.toLong().longValue());

		Ip ip2 = new Ip("1.2.3.4");
		assertEquals(16909060, ip2.toLong().longValue()); // 00000001.00000010.00000011.00000100
	}
}
