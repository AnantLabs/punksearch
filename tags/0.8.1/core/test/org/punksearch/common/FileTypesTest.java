package org.punksearch.common;

import junit.framework.TestCase;

public class FileTypesTest extends TestCase {

	public void testParseSize() {
		String zero = "";
		String b = "10";
		String kb = "10K";
		String mb = "10M";
		String gb = "10G";
		
		assertEquals(0, FileTypes.parseSize(zero));
		assertEquals(10, FileTypes.parseSize(b));
		assertEquals(10 * 1024, FileTypes.parseSize(kb));
		assertEquals(10 * 1024 * 1024, FileTypes.parseSize(mb));
		assertEquals(10 * 1024 * 1024 * 1024, FileTypes.parseSize(gb));
	}

}
