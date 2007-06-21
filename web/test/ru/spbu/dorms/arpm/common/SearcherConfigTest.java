package ru.spbu.dorms.arpm.common;

import ru.spbu.dorms.arpm.commons.SearcherConfig;
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

	public void testSetSmbLogin()
	{
		fail("Not yet implemented");
	}

	public void testGetSmbLogin()
	{
		fail("Not yet implemented");
	}

	public void testGetSmbDomain()
	{
		fail("Not yet implemented");
	}

	public void testGetSmbUser()
	{
		fail("Not yet implemented");
	}

	public void testGetSmbPassword()
	{
		fail("Not yet implemented");
	}

}
