package org.punksearch.cli.util;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.index.IndexReader;
import org.punksearch.common.IndexFields;
import org.punksearch.common.PunksearchFs;
import org.punksearch.common.PunksearchProperties;
import org.punksearch.util.lucene.LuceneUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * User: gubarkov
 * Date: 19.06.12
 * Time: 19:43
 */
public class ListInvalidFolders {
    public static void main(String[] args) {
        loadProps();

        String indexDirectory = PunksearchFs.resolveIndexDirectory();
        IndexReader indexReader = null;
        try {
            indexReader = IndexReader.open(LuceneUtils.dir(indexDirectory));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        final int total = indexReader.numDocs();
        final MapFieldSelector fieldSelector = new MapFieldSelector(IndexFields.HOST, IndexFields.PATH);
        for (int i = 0; i < total; i++) {
            try {
                final Document doc = indexReader.document(i, fieldSelector);
                checkDoc(doc.get(IndexFields.HOST), doc.get(IndexFields.PATH));
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    private static Set<String> suspiciousPaths = new HashSet<String>();

    private static void checkDoc(String host, String path) {
        if (isSuspiciousPath(path) && !suspiciousPaths.contains(path)) {
            System.out.println(host + " -- " + path);
            suspiciousPaths.add(path);
        }
    }

    private static Pattern SUSPICIOUS_PATH = Pattern.compile("/(.*?)(?=/).*?/\\1(?=/).*?/\\1/");

    static boolean isSuspiciousPath(String path) {
        return SUSPICIOUS_PATH.matcher(path).find();
    }

    private static void loadProps() {
        try {
            PunksearchProperties.loadDefault();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
