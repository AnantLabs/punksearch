package org.punksearch.searcher;

import org.apache.lucene.document.Document;

public interface ResultFilter {

	public boolean matches(Document doc);
	
}
