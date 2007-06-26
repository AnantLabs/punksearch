package org.punksearch.indexer;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexModifier;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.punksearch.commons.SearcherConfig;
import org.punksearch.commons.SearcherConstants;
import org.punksearch.commons.SearcherException;


public class IndexerOperator
{
	private static IndexerOperator	singleton		= null;
	private static IndexModifier	indexModifier	= null;
	
	
	private IndexerOperator() throws IOException
	{
		indexModifier = createIndexModifier();
	}

	/**
	 * Adds documents in index
	 * @param documentList List of Document
	 * @throws org.punksearch.commons.SearcherException Failed adding documents in index
	 */
	public synchronized void addDocuments(List<Document> documentList) throws SearcherException
	{
		
		try
		{
			if (documentList == null || documentList.size() == 0)
			{
				return;
			}
			//indexModifier.setMergeFactor(100);
			//indexModifier.setMaxBufferedDocs(1000);
//			indexModifier.setUseCompoundFile(false);
			for (Document document : documentList)
			{
				indexModifier.addDocument(document);
			}
			//indexModifier.flush();
			//indexModifier.close();
		}
		catch (IOException e)
		{
			throw new SearcherException("Failed adding documents in index", e);
		}
		
	}

	/**
	 * Deletes from index all documents for given ip
	 * @param host for ex. smb://10.20.0.155
	 * @throws SearcherException Failed deleting documents
	 */
	public synchronized void deleteDocuments(String ip, String proto) throws SearcherException
	{
		try
		{
			indexModifier.deleteDocuments(new Term(SearcherConstants.HOST, proto + "_" + ip));
			//indexModifier.flush();
			//indexModifier.close();
		}
		catch (IOException e)
		{
			throw new SearcherException("Failed deleting documents for host " + ip, e);
		}
	}

	private IndexModifier createIndexModifier() throws IOException
	{
		
		boolean indexExists = IndexReader.indexExists(SearcherConfig.getInstance().getIndexDirectory());
		return new IndexModifier(SearcherConfig.getInstance().getIndexDirectory(), new KeywordAnalyzer(), !indexExists);
	}

	public synchronized static IndexerOperator getInstance() throws IOException
	{
		if (singleton == null)
		{
			singleton = new IndexerOperator();
		}
		return singleton;
	}

	public synchronized void optimizeIndex() throws IOException
	{
		indexModifier.optimize();
		//indexModifier.flush();
		//indexModifier.close();
	}
	
	public synchronized void flushIndex() throws IOException
	{
		indexModifier.flush();
	}
	
}
