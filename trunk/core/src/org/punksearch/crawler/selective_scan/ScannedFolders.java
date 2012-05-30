package org.punksearch.crawler.selective_scan;

import java.util.List;

/**
 * User: gubarkov
 * Date: 30.05.12
 * Time: 17:32
 */
public interface ScannedFolders {
    boolean allowedScan(String protocol, String ip, String path);

    List<String> foldersAllowedToScan(String protocol, String ip);
}
