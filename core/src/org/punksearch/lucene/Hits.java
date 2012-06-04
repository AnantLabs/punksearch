package org.punksearch.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;

/**
 * User: gubarkov
 * Date: 26.05.12
 * Time: 17:02
 */
public class Hits {
    private final IndexSearcher indexSearcher;
    private final TopDocs topDocs;

    public Hits(IndexSearcher indexSearcher, TopDocs topDocs) {

        this.indexSearcher = indexSearcher;
        this.topDocs = topDocs;
    }

    public int length() {
        return topDocs.scoreDocs.length;
    }

    public Document doc(int i) {
        try {
            return indexSearcher.doc(topDocs.scoreDocs[i].doc);
        } catch (IOException e) {
            throw new RuntimeException("doc", e);
        }
    }
}
