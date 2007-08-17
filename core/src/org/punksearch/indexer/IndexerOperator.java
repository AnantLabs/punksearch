package org.punksearch.indexer;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexModifier;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.punksearch.commons.IndexFields;
import org.punksearch.commons.SearcherException;

// TODO: think about this wrapper class. it seems it can be full static or entirely refactored
public class IndexerOperator
{
	private static IndexerOperator	singleton		= null;
	private static IndexModifier	indexModifier	= null;
	
	
	private IndexerOperator()
	{
	}

	public static synchronized void init(String dir) throws IOException
	{
		if (singleton == null)
		{
			singleton = new IndexerOperator();
		}
		indexModifier = createIndexModifier(dir);
	}
	
	public static synchronized void close()
	{
		singleton = null;
		try
		{
			indexModifier.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds documents in index
	 * @param documentList List of Document
	 * @throws org.punksearch.commons.SearcherException Failed adding documents in index
	 */
	public void addDocuments(List<Document> documentList) throws SearcherException
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
	public void deleteDocuments(String ip, String proto) throws SearcherException
	{
		try
		{
			indexModifier.deleteDocuments(new Term(IndexFields.HOST, proto + "_" + ip));
			//indexModifier.flush();
			//indexModifier.close();
		}
		catch (IOException e)
		{
			throw new SearcherException("Failed deleting documents for host " + ip, e);
		}
	}

	private static IndexModifier createIndexModifier(String dir) throws IOException
	{
		
		boolean indexExists = IndexReader.indexExists(dir);
		return new IndexModifier(dir, createAnalyzer(), !indexExists);
	}
	
	private static Analyzer createAnalyzer()
	{
		PerFieldAnalyzerWrapper paw = new PerFieldAnalyzerWrapper(new KeywordAnalyzer());
		paw.addAnalyzer(IndexFields.NAME, new FilenameAnalyzer());
		paw.addAnalyzer(IndexFields.PATH, new FilenameAnalyzer());
		return paw;
	}

	public static IndexerOperator getInstance()
	{
		if (singleton == null)
		{
			throw new IllegalStateException("Not initialized");
		}
		return singleton;
	}

	public void optimizeIndex() throws IOException
	{
		indexModifier.optimize();
		//indexModifier.flush();
		//indexModifier.close();
	}
	
	public void flushIndex() throws IOException
	{
		indexModifier.flush();
	}
	
}
