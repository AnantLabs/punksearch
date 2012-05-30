package org.punksearch.crawler.selective_scan;

import org.apache.commons.io.FilenameUtils;

import java.util.List;

/**
 * User: gubarkov
 * Date: 30.05.12
 * Time: 15:54
 */
public class FolderScanInfo {
    private String ip;
    private List<String> folders;
    private FolderScanType scanType;

    FolderScanInfo(String ip, List<String> folders, FolderScanType scanType) {
        this.ip = ip;
        this.folders = folders;
        this.scanType = scanType;
    }

    boolean allowedScan(String path) {
        boolean pathInList = pathInList(path);

        switch (scanType) {
            case ONLY_THOSE:
                return pathInList;
            case NOT_THOSE:
                return !pathInList;
            default:
                throw new IllegalStateException();
        }
    }

    private boolean pathInList(String path) {
        for (String folder : folders) {
            if (pathInsideFolder(path, folder)) {
                return true;
            }
        }

        return false;
    }

    private boolean pathInsideFolder(String path, String folder) {
        final String pathNormalized = norm(path);
        final String folderNormalized = norm(folder);

        return pathNormalized.startsWith(folderNormalized);
    }

    private String norm(String path) {
        String norm = FilenameUtils.normalize(path, true);

        if (!norm.startsWith("/")) {
            norm = "/" + norm;
        }

        if (!norm.endsWith("/")) {
            norm += "/";
        }

        return norm;
    }

    String getIp() {
        return ip;
    }
}
