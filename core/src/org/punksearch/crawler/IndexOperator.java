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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.punksearch.common.IndexFields;
import org.punksearch.crawler.analysis.FilenameAnalyzer;
import org.punksearch.lucene.LuceneUtils;
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
	private static final Log log = LogFactory.getLog(IndexOperator.class);

	private IndexWriter     indexWriter;
	private static Analyzer analyzer = createAnalyzer();

    public static final String WRITE_LOCK_FILE = "write.lock";


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
			log.warn("Failed adding documents into index. " + e.getMessage());
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
			log.warn("Failed deleting documents for host '" + proto + "://" + ip + "'. " + e.getMessage());
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

	/* *
	 * Ensures all data was merged into the current index on disk.
	 *
	 * @throws IllegalStateException
	 *             In the case "close" method was called before.
	 */
	/*public void flush() {
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
	}*/

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
			log.error("Exception during merging index directories", ex);
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

	public static void deleteByAge(String dirPath, float days) {
		try {
            final Directory dir = LuceneUtils.dir(dirPath);
            boolean indexExists = IndexReader.indexExists(dir);
            if (!indexExists) {
                return;
            }

            final IndexReader indexReader = IndexReader.open(dir);
            IndexSearcher is = new IndexSearcher(indexReader);
			long max = System.currentTimeMillis() - Math.round(days * 1000 * 3600 * 24);
			NumberRangeFilter<Long> oldDocs = FilterFactory.createNumberFilter(IndexFields.INDEXED, null, max);
			Query query = new WildcardQuery(new Term(IndexFields.HOST, "*"));
            final TopDocs topDocs = is.search(query, oldDocs, indexReader.numDocs());
            log.info("Deleting by age from index directory. Items to delete: " + topDocs.totalHits);

            final ScoreDoc[] scoreDocs = topDocs.scoreDocs;

            for (int i = 0; i < scoreDocs.length; i++) {
				indexReader.deleteDocument(scoreDocs[i].doc); // TODO!!!
			}
			indexReader.close();
		} catch (IOException ex) {
			log.error("Exception during deleting by age from index directory", ex);
			throw new RuntimeException(ex);
		}
	}

	public static void merge(String targetDir, Set<String> sourceDirs) {
		try {
			IndexWriter iw = createIndexWriter(targetDir);
			Directory[] dirs = new Directory[sourceDirs.size()];
			int i = 0;
			for (String source : sourceDirs) {
				dirs[i] = LuceneUtils.dir(source);
				i++;
			}
			iw.addIndexes(dirs);
			iw.commit();
			iw.close();
		} catch (IOException ex) {
			log.error("Exception during merging index directories", ex);
			throw new RuntimeException(ex);
		}
	}

	private static IndexWriter createIndexWriter(String dir) throws IOException {
//		boolean indexExists = IndexReader.indexExists(dir);
//		return new IndexWriter(dir, analyzer, !indexExists);
        return new IndexWriter(LuceneUtils.dir(dir),
                new IndexWriterConfig(LuceneVersion.VERSION, analyzer));
	}

	public static boolean isLocked(String dir) {
		try {
			return LuceneUtils.dir(dir).fileExists(WRITE_LOCK_FILE);
		} catch (IOException e) {
			log.warn("IOException during checking if index directory is locked, "
                    + "assuming it is not (maybe index directory just does not exist?)");
			return false;
		}
	}

	public static void unlock(String dir) {
        try {
            log.info("Clearing lock: " + dir);

            final Directory d = LuceneUtils.dir(dir);
            d.clearLock(WRITE_LOCK_FILE);
            d.close();
        } catch (IOException e) {
            log.error("Error clearing lock", e);
        }
	}

	public static boolean indexExists(String dir) {
        try {
            return IndexReader.indexExists(LuceneUtils.dir(dir));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

	public static void createIndex(String dir) throws IOException {
		IndexWriter iw = createIndexWriter(dir);
		iw.close();
	}

	private static Analyzer createAnalyzer() {
        final FilenameAnalyzer filenameAnalyzer = new FilenameAnalyzer();

        Map<String, Analyzer> analyzerMap = new HashMap<String, Analyzer>();
        analyzerMap.put(IndexFields.NAME, filenameAnalyzer);
        analyzerMap.put(IndexFields.PATH, filenameAnalyzer);

        return new PerFieldAnalyzerWrapper(new KeywordAnalyzer(), analyzerMap);
	}

	/*public static void optimize(String dir) {
		IndexWriter iw;
		try {
			iw = createIndexWriter(dir);
			iw.optimize();
			iw.flush();
			iw.close();
		} catch (IOException e) {
			log.error("Exception during optimizing index directory '" + dir + "': " + e.getMessage());
		}
	}*/
}
