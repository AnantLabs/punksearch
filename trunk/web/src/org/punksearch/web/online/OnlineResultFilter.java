package org.punksearch.web.online;

import org.apache.lucene.document.Document;
import org.punksearch.commons.IndexFields;
import org.punksearch.searcher.ResultFilter;

public class OnlineResultFilter implements ResultFilter {

	public boolean matches(Document doc) {
		return OnlineChecker.isOnline(doc.get(IndexFields.HOST));
	}

}
