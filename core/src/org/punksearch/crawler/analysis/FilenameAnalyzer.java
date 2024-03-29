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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LengthFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.punksearch.crawler.CrawlerKeys;
import org.punksearch.crawler.LuceneVersion;

/**
 * Analyzes file names.
 * 
 * Tokenizes the file name using {@link FilenameTokenizer}, drops all parts with length less than MIN_TERM_LENGTH and
 * lowercases all other parts.
 * 
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class FilenameAnalyzer extends Analyzer {

	public static final int MIN_TERM_LENGTH;

    static {
		String termLength = System.getProperty(CrawlerKeys.CRAWLER_TERMLENGTH);
		MIN_TERM_LENGTH = (termLength != null) ? Integer.valueOf(termLength) : 3;
	}

	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		TokenStream result = new FilenameTokenizer(reader);

        // TODO: what is boolean enablePositionIncrements for?
		result = new LengthFilter(false, result, MIN_TERM_LENGTH, 1000);
		result = new LowerCaseFilter(LuceneVersion.VERSION, result);
		return result;
	}

}
