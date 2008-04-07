package org.punksearch.web;

import java.util.List;

import org.apache.lucene.document.Document;

public interface ResultFilter {

	public boolean matches(Document doc);
	
	public List<Integer> filter(List<Document> docs);
}
