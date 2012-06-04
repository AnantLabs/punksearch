package org.punksearch.experiments;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.junit.Test;
import org.punksearch.common.IndexFields;
import org.punksearch.common.PunksearchFs;
import org.punksearch.common.PunksearchProperties;
import org.punksearch.lucene.LuceneUtils;
import org.punksearch.online.Probe;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * User: gubarkov
 * Date: 23.05.12
 * Time: 0:14
 */
public class Experiments {
    @Test
    public void stringEncodingConversion() throws UnsupportedEncodingException {
        String paramValue = "тест";
        System.out.println(paramValue);
        paramValue = new String(paramValue.getBytes("ISO-8859-1"), "UTF-8");
        System.out.println(paramValue);
    }

    @Test
    public void probeTest1() {
        final Probe probe = new Probe();
        final boolean res = probe.probe("smb://194.85.80.61");
        System.out.println(res);
    }

    @Test
    public void attemptToReadFieldTerms() throws IOException {
        PunksearchProperties.loadDefault();

        final String dir = PunksearchFs.resolveIndexDirectory();

        final IndexReader indexReader = IndexReader.open(LuceneUtils.dir(dir));

        final TermEnum terms = indexReader.terms(new Term(IndexFields.HOST));

        // see https://issues.apache.org/jira/browse/LUCENE-6
        System.out.println(terms.term());

        while (terms.next()) {
            final Term term = terms.term();

            if (!IndexFields.HOST.equals(term.field())) {
                break;
            }

            System.out.println(term);
        }
    }
    @Test
    public void attemptToReadFieldTerms1() throws IOException {
        PunksearchProperties.loadDefault();

        final String dir = PunksearchFs.resolveIndexDirectory();

        final IndexReader indexReader = IndexReader.open(LuceneUtils.dir(dir));

        final TermEnum terms = indexReader.terms();

        Set<String> hosts = new LinkedHashSet<String>();

        boolean print = false;
        while (terms.next()) {
            final Term term = terms.term();

            if (IndexFields.HOST.equals(term.field())) {
                print = true;
                hosts.add(term.text());
            }

            if (print) {
                System.out.println(term);
            }
        }

        for (String host : hosts) {
            System.out.println(host);
        }
    }
}
