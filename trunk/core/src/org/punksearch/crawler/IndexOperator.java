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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.punksearch.common.IndexFields;
import org.punksearch.common.SearcherException;
import org.punksearch.searcher.filters.FilterFactory;
import org.punksearch.searcher.filters.NumberRangeFilter;

public class IndexOperator {
	private static Logger __log = Logger.getLogger(IndexOperator.class.getName());

	private IndexWriter   indexWriter;

	public IndexOperator(String indexDirectory) {
		try {
			this.indexWriter = createIndexWriter(indexDirectory);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Adds documents in index
	 * 
	 * @param documentList
	 *            List of Document
	 * @throws org.punksearch.common.SearcherException
	 *             Failed adding documents in index
	 */
	public boolean addDocuments(List<Document> documentList) {
		if (documentList == null || documentList.size() == 0) {
			return true;
		}

		try {
			for (Document document : documentList) {
				indexWriter.addDocument(document);
			}
			return true;
		} catch (IOException e) {
			__log.warning("Failed adding documents into index. " + e.getMessage());
			return false;
		}

	}

	/**
	 * Deletes from index all documents for given ip
	 * 
	 * @param host
	 *            for ex. smb://10.20.0.155
	 * @throws SearcherException
	 *             Failed deleting documents
	 */
	public boolean deleteDocuments(String ip, String proto) {
		try {
			indexWriter.deleteDocuments(new Term(IndexFields.HOST, proto + "_" + ip));
			return true;
		} catch (IOException e) {
			__log.warning("Failed deleting documents for host '" + proto + "://" + ip + "'. " + e.getMessage());
			return false;
		}
	}

	public static void deleteByHost(String dir, String host) {
		try {
			IndexWriter iw = createIndexWriter(dir);
			iw.deleteDocuments(new Term(IndexFields.HOST, host));
			iw.close();
		} catch (IOException ex) {
			__log.log(Level.SEVERE, "Exception during merging index directories", ex);
			throw new RuntimeException(ex);
		}
	}

	public static void deleteAll(String dir) {
		try {
			File indexDir = new File(dir);
			if (indexDir.exists()) {
				FileUtils.cleanDirectory(indexDir);
				IndexWriter iw = createIndexWriter(dir);
				iw.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void deleteByAge(String dir, int age) {
		boolean indexExists = IndexReader.indexExists(dir);
		if (!indexExists) {
			return;
		}
		try {
			IndexSearcher is = new IndexSearcher(FSDirectory.getDirectory(dir));
			long max = System.currentTimeMillis() - age * 1000 * 3600 * 24;
			NumberRangeFilter<Long> oldDocs = FilterFactory.createNumberFilter(IndexFields.INDEXED, null, max);
			Query query = new WildcardQuery(new Term(IndexFields.HOST, "*"));
			Hits hits = is.search(query, oldDocs);
			IndexReader ir = IndexReader.open(dir);
			for (int i = 0; i < hits.length(); i++) {
				ir.deleteDocument(hits.id(i));
			}
			ir.close();
		} catch (IOException ex) {
			__log.log(Level.SEVERE, "Exception during merging index directories", ex);
			throw new RuntimeException(ex);
		}
	}

	public static void merge(String targetDir, Set<String> sourceDirs) {
		try {
			IndexWriter iw = createIndexWriter(targetDir);
			Directory[] dirs = new Directory[sourceDirs.size()];
			int i = 0;
			for (String source : sourceDirs) {
				dirs[i] = FSDirectory.getDirectory(source);
				i++;
			}
			iw.addIndexesNoOptimize(dirs);
			iw.optimize();
			iw.flush();
			iw.close();
		} catch (IOException ex) {
			__log.log(Level.SEVERE, "Exception during merging index directories", ex);
			throw new RuntimeException(ex);
		}
	}

	private static IndexWriter createIndexWriter(String dir) throws IOException {
		boolean indexExists = IndexReader.indexExists(dir);
		return new IndexWriter(dir, createAnalyzer(), !indexExists);
	}

	public static boolean isLocked(String dir) {
		try {
			return IndexReader.isLocked(dir);
		} catch (IOException e) {
			__log.warning("IOException during checking if index directory is locked, "
			        + "assuming it is not (maybe index directory just does not exist?)");
			return false;
		}
	}

	public static void unlock(String dir) {
		try {
			if (IndexReader.isLocked(dir)) {
				IndexReader.unlock(FSDirectory.getDirectory(dir));
			}
		} catch (IOException e) {
			__log.warning("IOException during unlocking of index directory "
			        + "(maybe index directory just does not exist?)");
		}
	}

	private static Analyzer createAnalyzer() {
		PerFieldAnalyzerWrapper paw = new PerFieldAnalyzerWrapper(new KeywordAnalyzer());
		paw.addAnalyzer(IndexFields.NAME, new FilenameAnalyzer());
		paw.addAnalyzer(IndexFields.PATH, new FilenameAnalyzer());
		paw.addAnalyzer(IndexFields.EXTENSION, new LowerCaseAnalyzer());
		return paw;
	}

	public void optimize() {
		try {
			indexWriter.optimize();
		} catch (CorruptIndexException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void flush() {
		try {
			indexWriter.flush();
		} catch (CorruptIndexException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void close() {
		try {
			indexWriter.close();
		} catch (CorruptIndexException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
