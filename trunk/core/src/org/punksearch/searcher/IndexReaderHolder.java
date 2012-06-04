package org.punksearch.searcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.punksearch.util.lucene.LuceneUtils;

import java.io.IOException;

/**
 * User: gubarkov
 * Date: 04.06.12
 * Time: 18:40
 */
public class IndexReaderHolder {
    private static final Log log = LogFactory.getLog(IndexReaderHolder.class);

    private IndexReader indexReader;
    private IndexSearcher indexSearcher;
    private String indexDir;

    public IndexReaderHolder(String indexDir) throws IOException {
        this.indexDir = indexDir;
    }

    public IndexReader getCurrentReader() throws IOException {
        if (indexReader == null) {
            indexReader = IndexReader.open(LuceneUtils.dir(indexDir));
        } else {
            final IndexReader updatedReader = IndexReader.openIfChanged(indexReader);
            if (updatedReader != null) {
                log.info("Index updated. Recreating reader...");

                indexReader.close();
                indexReader = updatedReader;
            }
        }

        return indexReader;
    }

    /**
     * @return current searcher (looking at last index data)
     * @throws IOException
     */
    public IndexSearcher getCurrentSearcher() throws IOException {
        if (indexSearcher == null) {
            indexSearcher = new IndexSearcher(getCurrentReader());
        } else {
            IndexReader currentReader = getCurrentReader();

            if (indexSearcher.getIndexReader() != currentReader) {
                indexSearcher = new IndexSearcher(currentReader);
            }
        }
        return indexSearcher;
    }
}
