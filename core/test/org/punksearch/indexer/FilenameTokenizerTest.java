package org.punksearch.indexer;

import junit.framework.TestCase;

public class FilenameTokenizerTest extends TestCase
{
	private static String CHARS_TO_DROP = "_-.,:[]#!()'/&";

	public void testIsTokenChar()
	{
		FilenameTokenizer t = new FilenameTokenizer(null);

		for (int i = 0; i < CHARS_TO_DROP.length(); i++)
		{
			assertFalse(t.isTokenChar(CHARS_TO_DROP.charAt(i)));
		}
		
		assertTrue(t.isTokenChar("a".charAt(0)));
		
	}

}
