package org.punksearch.crawler.selective_scan;

import javax.annotation.Nullable;

/**
 * User: gubarkov
 * Date: 30.05.12
 * Time: 15:56
 */
public enum FolderScanType {
    ONLY_THOSE("only"),
    NOT_THOSE("not");
    private String keyCode;

    FolderScanType(String keyCode) {
        this.keyCode = keyCode;
    }

    public String getKeyCode() {
        return keyCode;
    }

    @Nullable
    public static FolderScanType byCode(String keyCode) {
        for (FolderScanType folderScanType : values()) {
            if (folderScanType.getKeyCode().equals(keyCode)) {
                return folderScanType;
            }
        }

        return null;
    }
}
