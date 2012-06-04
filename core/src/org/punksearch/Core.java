package org.punksearch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.punksearch.common.PunksearchFs;
import org.punksearch.logic.PunksearchLogic;
import org.punksearch.searcher.IndexReaderHolder;

import java.io.IOException;

/**
 * User: gubarkov
 * Date: 04.06.12
 * Time: 18:37
 */
public final class Core {
    private static final Log log = LogFactory.getLog(Core.class);

    private final static PunksearchLogic punksearchLogic = new PunksearchLogic();
    private final static String indexDirectory = PunksearchFs.resolveIndexDirectory();
    private final static IndexReaderHolder indexReaderHolder = initIndexHolder(indexDirectory);

    private static IndexReaderHolder initIndexHolder(String indexDirectory) {
        try {
            return new IndexReaderHolder(indexDirectory);
        } catch (IOException e) {
            log.error("Unable to open index for reading", e);
            throw new RuntimeException(e);
        }
    }

    public static PunksearchLogic getPunksearchLogic() {
        return punksearchLogic;
    }

    public static String getIndexDirectory() {
        return indexDirectory;
    }

    public static IndexReaderHolder getIndexReaderHolder() {
        return indexReaderHolder;
    }

    private Core() {
    }
}
