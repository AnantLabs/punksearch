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

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexModifier;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.punksearch.common.IndexFields;
import org.punksearch.common.SearcherException;

// TODO: think about this wrapper class. it seems it can be full static or entirely refactored
public class IndexOperator
{
	private static Logger	__log	= Logger.getLogger(IndexOperator.class.getName());
	
	private static IndexOperator	singleton		= null;
	private static IndexModifier	indexModifier	= null;
	
	
	private IndexOperator()
	{
	}

	public static synchronized void init(String dir) throws IOException
	{
		if (singleton == null)
		{
			singleton = new IndexOperator();
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
	 * @throws org.punksearch.common.SearcherException Failed adding documents in index
	 */
	public boolean addDocuments(List<Document> documentList)
	{
		if (documentList == null || documentList.size() == 0)
		{
			return true;
		}
		
		try
		{
			//indexModifier.setMergeFactor(100);
			//indexModifier.setMaxBufferedDocs(1000);
			//indexModifier.setUseCompoundFile(false);
			for (Document document : documentList)
			{
				indexModifier.addDocument(document);
			}
			return true;
		}
		catch (IOException e)
		{
			__log.warning("Failed adding documents into index. " + e.getMessage());
			return false;
		}
		
	}

	/**
	 * Deletes from index all documents for given ip
	 * @param host for ex. smb://10.20.0.155
	 * @throws SearcherException Failed deleting documents
	 */
	public boolean deleteDocuments(String ip, String proto)
	{
		try
		{
			indexModifier.deleteDocuments(new Term(IndexFields.HOST, proto + "_" + ip));
			return true;
		}
		catch (IOException e)
		{
			__log.warning("Failed deleting documents for host '" + proto + "://" + ip + "'. " + e.getMessage());
			return false;
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

	public static IndexOperator getInstance()
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
