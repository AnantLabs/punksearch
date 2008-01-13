package org.punksearch.common;

import junit.framework.TestCase;

import org.punksearch.indexer.CrawlerConfig;

public class CrawlerConfigTest extends TestCase
{
	private CrawlerConfig cc;
	
	public void setUp()
	{
		cc = new CrawlerConfig();
	}
	
	
	public void testIpRanges()
	{
		cc.setIpRanges("1.2.3.4");
		assertEquals(1, cc.getIpRanges().size());
		assertEquals("1.2.3.4", cc.getIpRangesString());
		
		cc.setIpRanges("1.2.3.4-2.3.4.5");
		assertEquals(1, cc.getIpRanges().size());
		assertEquals("1.2.3.4-2.3.4.5", cc.getIpRangesString());
		
		cc.setIpRanges("1.2.3.4-2.3.4.5,10.20.30.40");
		assertEquals(2, cc.getIpRanges().size());
		assertEquals("1.2.3.4-2.3.4.5,10.20.30.40", cc.getIpRangesString());
	}

	public void testSmbLogin()
	{
		String smbLogin = "DOMAIN1\\user2:password3";
		cc.setSmbLogin(smbLogin);
		assertEquals(smbLogin, cc.getSmbLogin());
	}

	public void testGetSmbDomain()
	{
		String smbLogin = "DOMAIN1\\user2:password3";
		cc.setSmbLogin(smbLogin);
		assertEquals("DOMAIN1", cc.getSmbDomain());
	}

	public void testGetSmbUser()
	{
		String smbLogin = "DOMAIN1\\user2:password3";
		cc.setSmbLogin(smbLogin);
		assertEquals("user2", cc.getSmbUser());
	}

	public void testGetSmbPassword()
	{
		String smbLogin = "DOMAIN1\\user2:password3";
		cc.setSmbLogin(smbLogin);
		assertEquals("password3", cc.getSmbPassword());
	}

}
