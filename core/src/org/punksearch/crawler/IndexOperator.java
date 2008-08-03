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
import org.punksearch.crawler.analysis.FilenameAnalyzer;
import org.punksearch.crawler.analysis.LowerCaseAnalyzer;
import org.punksearch.searcher.filters.FilterFactory;
import org.punksearch.searcher.filters.NumberRangeFilter;

/**
 * Utility class to work with an index. Hides all Lucene's read/write logics.
 * 
 * It has both instance and static methods. The instance methods affect the index directory IndexOperator was
 * instantiated with. The static methods are for utility tasks -- they open and close an index themselves.
 * 
 * @author Yury Soldak (ysoldak@gmail.com)
 * 
 */
public class IndexOperator {
	private static Logger   __log    = Logger.getLogger(IndexOperator.class.getName());

	private IndexWriter     indexWriter;
	private static Analyzer analyzer = createAnalyzer();

	/**
	 * Creates an instance of the class and opens index directory.
	 * 
	 * @param indexDirectory
	 *            Index directory to operate under.
	 */
	public IndexOperator(String indexDirectory) {
		try {
			this.indexWriter = createIndexWriter(indexDirectory);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Adds documents to the current index.
	 * 
	 * @param documentList
	 *            List of Document. Null value is synonymous to an empty list.
	 * @return Boolean result. False in the case of internal I/O exception.
	 * 
	 * @throws IllegalStateException
	 *             In the case "close" method was called before.
	 */
	public boolean addDocuments(List<Document> documentList) {
		if (isClosed()) {
			throw new IllegalStateException("Index was closed already.");
		}

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
	 * Deletes all documents (what match IP and protocol) from the current index.
	 * 
	 * @param ip
	 *            IP of a host to delete documents for.
	 * @param proto
	 *            Protocol to match for documents to be deleted. "smb" or "ftp".
	 * @return Boolean result. False in the case of internal I/O exception.
	 * 
	 * @throws IllegalStateException
	 *             In the case "close" method was called before.
	 */
	public boolean deleteDocuments(String ip, String proto) {
		if (isClosed()) {
			throw new IllegalStateException("Index was closed already.");
		}
		try {
			indexWriter.deleteDocuments(new Term(IndexFields.HOST, proto + "_" + ip));
			return true;
		} catch (IOException e) {
			__log.warning("Failed deleting documents for host '" + proto + "://" + ip + "'. " + e.getMessage());
			return false;
		}
	}

	/**
	 * Optimizes the current index.
	 * 
	 * @throws IllegalStateException
	 *             In the case "close" method was called before.
	 */
	public void optimize() {
		if (isClosed()) {
			throw new IllegalStateException("Index was closed already.");
		}
		try {
			indexWriter.optimize();
		} catch (CorruptIndexException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Ensures all data was merged into the current index on disk.
	 * 
	 * @throws IllegalStateException
	 *             In the case "close" method was called before.
	 */
	public void flush() {
		if (isClosed()) {
			throw new IllegalStateException("Index was closed already.");
		}
		try {
			indexWriter.flush();
		} catch (CorruptIndexException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Closes the index, unlocks it. It is not possible to use any instance methods except "isClosed" after this method
	 * was called.
	 * 
	 * @throws IllegalStateException
	 *             In the case "close" method was called before.
	 */
	public void close() {
		if (isClosed()) {
			throw new IllegalStateException("Index was closed already.");
		}
		try {
			indexWriter.close();
			indexWriter = null;
		} catch (CorruptIndexException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Checks if index was closed. Use this method to check (if unsure) if you can call instance methods.
	 * 
	 * @return True if index was closed (i.e. "close" method called before).
	 */
	public boolean isClosed() {
		return indexWriter == null;
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

	public static void deleteByAge(String dir, float days) {
		boolean indexExists = IndexReader.indexExists(dir);
		if (!indexExists) {
			return;
		}
		try {
			IndexSearcher is = new IndexSearcher(FSDirectory.getDirectory(dir));
			long max = System.currentTimeMillis() - Math.round(days * 1000 * 3600 * 24);
			NumberRangeFilter<Long> oldDocs = FilterFactory.createNumberFilter(IndexFields.INDEXED, null, max);
			Query query = new WildcardQuery(new Term(IndexFields.HOST, "*"));
			Hits hits = is.search(query, oldDocs);
			__log.info("Deleting by age from index directory. Items to delete: " + hits.length());
			IndexReader ir = IndexReader.open(dir);
			for (int i = 0; i < hits.length(); i++) {
				ir.deleteDocument(hits.id(i));
			}
			ir.close();
		} catch (IOException ex) {
			__log.log(Level.SEVERE, "Exception during deleting by age from index directory", ex);
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
			// iw.optimize();
			iw.flush();
			iw.close();
		} catch (IOException ex) {
			__log.log(Level.SEVERE, "Exception during merging index directories", ex);
			throw new RuntimeException(ex);
		}
	}

	private static IndexWriter createIndexWriter(String dir) throws IOException {
		boolean indexExists = IndexReader.indexExists(dir);
		return new IndexWriter(dir, analyzer, !indexExists);
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

	public static boolean indexExists(String dir) {
		return IndexReader.indexExists(dir);
	}

	public static void createIndex(String dir) throws IOException {
		IndexWriter iw = createIndexWriter(dir);
		iw.close();
	}

	private static Analyzer createAnalyzer() {
		PerFieldAnalyzerWrapper paw = new PerFieldAnalyzerWrapper(new KeywordAnalyzer());
		FilenameAnalyzer analyzer = new FilenameAnalyzer();
		paw.addAnalyzer(IndexFields.NAME, analyzer);
		paw.addAnalyzer(IndexFields.PATH, analyzer);
		paw.addAnalyzer(IndexFields.EXTENSION, new LowerCaseAnalyzer());
		return paw;
	}

	public static void optimize(String dir) {
		IndexWriter iw;
		try {
			iw = createIndexWriter(dir);
			iw.optimize();
			iw.flush();
			iw.close();
		} catch (IOException e) {
			__log.severe("Exception during optimizing index directory '" + dir + "': " + e.getMessage());
		}
	}
}
