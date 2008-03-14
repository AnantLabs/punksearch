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
package org.punksearch.crawler;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LengthFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;

public class FilenameAnalyzer extends Analyzer
{

	@Override
	public TokenStream tokenStream(String fieldName, Reader reader)
	{
	    TokenStream result = new FilenameTokenizer(reader);
	    result = new LengthFilter(result, 3, 1000);
	    result = new LowerCaseFilter(result);
	    return result;
	}

}
