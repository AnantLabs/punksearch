package org.punksearch.crawler;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;

/**
 * User: gubarkov
 * Date: 25.05.12
 * Time: 15:40
 */
public class IndexUtils {
    public static Directory dir(String dir) throws IOException {
        return dir(new File(dir));
    }

    public static Directory dir(File dir) throws IOException {
        if (dir == null) {
            throw new NullPointerException("dir");
        }

        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Not a directory");
        }

        return FSDirectory.open(dir);
    }
}
