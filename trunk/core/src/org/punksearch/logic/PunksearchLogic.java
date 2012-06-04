package org.punksearch.logic;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.punksearch.common.IndexFields;
import org.punksearch.common.PunksearchHost;
import org.punksearch.searcher.IndexReaderHolder;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * User: gubarkov
 * Date: 04.06.12
 * Time: 18:34
 */
public class PunksearchLogic {
    private IndexReaderHolder indexReaderHolder;

    public PunksearchLogic(IndexReaderHolder indexReaderHolder) {
        this.indexReaderHolder = indexReaderHolder;
    }

    public List<PunksearchHost> listIndexedHosts() throws IOException {
        final TermEnum terms = indexReaderHolder.getCurrentReader().terms(new Term(IndexFields.HOST));

        List<PunksearchHost> hosts = new LinkedList<PunksearchHost>();

        // see https://issues.apache.org/jira/browse/LUCENE-6
        hosts.add(new PunksearchHost(terms.term().text()));

        while (terms.next()) {
            final Term term = terms.term();

            if (!IndexFields.HOST.equals(term.field())) {
                break;
            }

            hosts.add(new PunksearchHost(term.text()));
        }

        Collections.sort(hosts);

        return hosts;
    }
}
