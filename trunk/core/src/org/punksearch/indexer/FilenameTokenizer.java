package org.punksearch.indexer;

import java.io.Reader;

import org.apache.lucene.analysis.CharTokenizer;

public class FilenameTokenizer extends CharTokenizer
{
	private static String CHARS_TO_DROP = "_-.,:[]#!()'/&";
	
	public FilenameTokenizer(Reader input)
	{
		super(input);
	}

	@Override
	protected boolean isTokenChar(char c)
	{
		if (Character.isWhitespace(c))
			return false;
		
		if (CHARS_TO_DROP.indexOf(c) > 0)
			return false;
		
		return true;
	}

}
