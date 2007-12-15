package org.punksearch.indexer;

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
