package org.punksearch.stats;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;

/**
 * User: gubarkov
 * Date: 27.08.12
 * Time: 16:54
 */
public class TotalStatsReader {
    private static Log log = LogFactory.getLog(TotalStatsReader.class);

    private static long lastModified = 0;
    private static TotalStats totalStats = new TotalStats(0);

    public static TotalStats getCurrent() {
        rereadTotalStats();

        return totalStats;
    }

    private static void rereadTotalStats() {
        File totalStatsFile = TotalStatsWriter.getFile();

        if (totalStatsFile.isFile() && totalStatsFile.lastModified() > lastModified) {
            try {
                log.info("Rereading totalStats file...");

                totalStats = TotalStats.deserialize(
                        FileUtils.readFileToString(totalStatsFile, TotalStatsWriter.CHARSET));

                lastModified = totalStatsFile.lastModified();
            } catch (IOException e) {
                log.error("Can't read totalStats file", e);
            }
        }
    }
}
