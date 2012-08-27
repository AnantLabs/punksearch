package org.punksearch.stats;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.punksearch.common.PunksearchFs;

import java.io.File;
import java.io.IOException;

/**
 * User: gubarkov
 * Date: 27.08.12
 * Time: 16:54
 */
public class TotalStatsWriter {
    private static Log log = LogFactory.getLog(TotalStatsWriter.class);

    static String FILENAME = "stats-summary.csv";
    static String CHARSET = "UTF-8";

    public static void dump(TotalStats totalStats) {
        try {
            FileUtils.write(getFile(), totalStats.serialize(), CHARSET);
        } catch (IOException e) {
            log.error("Unable write total stats", e);
        }
    }

    static File getFile() {
        return new File(PunksearchFs.resolveStatsDirectory(), FILENAME);
    }
}
