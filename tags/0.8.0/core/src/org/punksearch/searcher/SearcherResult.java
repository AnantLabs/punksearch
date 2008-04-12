package org.punksearch.searcher;

import java.util.List;

import org.apache.lucene.document.Document;

/**
 * Date: 17.06.2006
 * 
 * @author arPm
 */
public class SearcherResult {
	private int            hits;
	private List<Document> items;

	public SearcherResult(int allHits, List<Document> items) {
		this.hits = allHits;
		this.items = items;
	}

	public int count() {
		return hits;
	}

	public List<Document> items() {
		return items;
	}
}
