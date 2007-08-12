package org.punksearch.common;

import org.punksearch.commons.SearcherConfig;

import junit.framework.TestCase;

public class SearcherConfigTest extends TestCase
{

	public void testIpRanges()
	{
		SearcherConfig.getInstance().setIpRanges("1.2.3.4");
		assertEquals(1, SearcherConfig.getInstance().getIpRanges().size());
		assertEquals("1.2.3.4", SearcherConfig.getInstance().getIpRangesString());
		
		SearcherConfig.getInstance().setIpRanges("1.2.3.4-2.3.4.5");
		assertEquals(1, SearcherConfig.getInstance().getIpRanges().size());
		assertEquals("1.2.3.4-2.3.4.5", SearcherConfig.getInstance().getIpRangesString());
		
		SearcherConfig.getInstance().setIpRanges("1.2.3.4-2.3.4.5,10.20.30.40");
		assertEquals(2, SearcherConfig.getInstance().getIpRanges().size());
		assertEquals("1.2.3.4-2.3.4.5,10.20.30.40", SearcherConfig.getInstance().getIpRangesString());
	}

	public void testSmbLogin()
	{
		String smbLogin = "DOMAIN1\\user2:password3";
		SearcherConfig.getInstance().setSmbLogin(smbLogin);
		assertEquals(smbLogin, SearcherConfig.getInstance().getSmbLogin());
	}

	public void testGetSmbDomain()
	{
		String smbLogin = "DOMAIN1\\user2:password3";
		SearcherConfig.getInstance().setSmbLogin(smbLogin);
		assertEquals("DOMAIN1", SearcherConfig.getInstance().getSmbDomain());
	}

	public void testGetSmbUser()
	{
		String smbLogin = "DOMAIN1\\user2:password3";
		SearcherConfig.getInstance().setSmbLogin(smbLogin);
		assertEquals("user2", SearcherConfig.getInstance().getSmbUser());
	}

	public void testGetSmbPassword()
	{
		String smbLogin = "DOMAIN1\\user2:password3";
		SearcherConfig.getInstance().setSmbLogin(smbLogin);
		assertEquals("password3", SearcherConfig.getInstance().getSmbPassword());
	}

}
