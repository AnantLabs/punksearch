package org.punksearch.searcher;

import java.util.List;

import org.apache.lucene.document.Document;

/**
 * Date: 17.06.2006
 *
 * @author arPm
 */
public class SearcherResult
{
	private int				hits;
	private List<Document>	chunk;

	public SearcherResult(int allHits, List<Document> thisChunk)
	{
		this.hits = allHits;
		this.chunk = thisChunk;
	}

	public int getHitCount()
	{
		return hits;
	}

	public List<Document> getChunk()
	{
		return chunk;
	}
}
