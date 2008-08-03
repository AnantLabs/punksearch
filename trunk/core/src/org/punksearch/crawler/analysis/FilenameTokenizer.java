/***************************************************************************
 *                                                                         *
 *   PunkSearch - Searching over LAN                                       *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package org.punksearch.crawler.analysis;

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
		
		if (CHARS_TO_DROP.indexOf(c) >= 0)
			return false;
		
		return true;
	}

}
