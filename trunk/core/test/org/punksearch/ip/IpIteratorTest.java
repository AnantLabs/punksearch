package org.punksearch.ip;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class IpIteratorTest extends TestCase
{
	public void testSimpleRange() {
		List<IpRange> ranges = new ArrayList<IpRange>();
		ranges.add(new IpRange("1.2.3.4"));
		IpIterator it = new IpIterator(ranges);
		assertTrue(it.hasNext());
		Ip ip = it.next();
		assertEquals("1.2.3.4", ip.toString());
		assertFalse(it.hasNext());
	}
	
	public void testConventionalSmallRange() {
		List<IpRange> ranges = new ArrayList<IpRange>();
		ranges.add(new IpRange("1.2.3.4-1.2.3.10"));
		IpIterator it = new IpIterator(ranges);
		assertTrue(it.hasNext());
		Ip ip1 = it.next();
		assertEquals("1.2.3.4", ip1.toString());
		assertTrue(it.hasNext());
		Ip ip2 = it.next();
		assertEquals("1.2.3.5", ip2.toString());
		assertTrue(it.hasNext());
	}
	
	public void testConventionalBigRange() {
		List<IpRange> ranges = new ArrayList<IpRange>();
		ranges.add(new IpRange("1.2.3.255-1.2.4.10"));
		IpIterator it = new IpIterator(ranges);
		assertTrue(it.hasNext());
		Ip ip1 = it.next();
		assertEquals("1.2.3.255", ip1.toString());
		assertTrue(it.hasNext());
		Ip ip2 = it.next();
		assertEquals("1.2.4.0", ip2.toString());
		assertTrue(it.hasNext());
	}
	
	public void testMultiRange() {
		List<IpRange> ranges = new ArrayList<IpRange>();
		ranges.add(new IpRange("1.2.3.1-1.2.3.100"));
		ranges.add(new IpRange("1.2.30.10-1.2.30.200"));
		IpIterator it = new IpIterator(ranges);
		Ip ip = it.next();
		while (!ip.toString().equals("1.2.3.100")) {
			ip = it.next();
		}
		assertTrue(it.hasNext());
		ip = it.next();
		assertEquals("1.2.30.10", ip.toString());
		assertTrue(it.hasNext());
	}
	
	public void testUnsupportedRemove() {
		List<IpRange> ranges = new ArrayList<IpRange>();
		ranges.add(new IpRange("1.2.3.255-1.2.4.10"));
		IpIterator it = new IpIterator(ranges);
		try {
			it.remove();
			fail("Should fail since remove() is not supported");
		} catch (UnsupportedOperationException success) {
		}
	}
}
