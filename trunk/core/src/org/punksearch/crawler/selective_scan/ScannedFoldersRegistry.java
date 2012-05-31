package org.punksearch.crawler.selective_scan;

import org.punksearch.common.Settings;
import org.punksearch.crawler.CrawlerKeys;

import java.util.*;

/**
 * User: gubarkov
 * Date: 30.05.12
 * Time: 15:42
 */
public class ScannedFoldersRegistry implements ScannedFolders {
    private Map<String, FolderScanInfo> folderScanInfos = new HashMap<String, FolderScanInfo>();

    private final static ScannedFoldersRegistry instance = new ScannedFoldersRegistry();

    public static ScannedFolders getInstance() {
        return instance;
    }

    ScannedFoldersRegistry() {
        final Map<String, String> protocolToFoldersStr = new HashMap<String, String>();

        protocolToFoldersStr.put("smb", Settings.get(CrawlerKeys.SMB_FOLDERS));
        protocolToFoldersStr.put("ftp", Settings.get(CrawlerKeys.FTP_FOLDERS));

        init(protocolToFoldersStr);
    }

    public ScannedFoldersRegistry(Map<String, String> protocolToFoldersStr) {
        init(protocolToFoldersStr);
    }

    private void init(Map<String, String> protocolToFoldersStr) {
        for (Map.Entry<String, String> protocolFolders : protocolToFoldersStr.entrySet()) {
            final String protocol = protocolFolders.getKey();
            final String foldersStr = protocolFolders.getValue();

            final List<FolderScanInfo> folderScanInfosList = parseFoldersString(foldersStr);

            for (FolderScanInfo folderScanInfo : folderScanInfosList) {
                folderScanInfos.put(protocolIpKey(protocol, folderScanInfo.getIp()), folderScanInfo);
            }
        }
    }

    @Override
    public boolean allowedScan(String protocol, String ip, String path) {
        final FolderScanInfo folderScanInfo = folderScanInfos.get(protocolIpKey(protocol, ip));

        return folderScanInfo == null || folderScanInfo.allowedScan(path);
    }

    @Override
    public List<String> foldersAllowedToScan(String protocol, String ip) {
        final FolderScanInfo folderScanInfo = folderScanInfos.get(protocolIpKey(protocol, ip));

        if (folderScanInfo == null
                || folderScanInfo.getScanType() != FolderScanType.ONLY_THOSE) {
            return null;
        } else {
            List<String> result = new ArrayList<String>(folderScanInfo.getFolders().size());
            for (String folder : folderScanInfo.getFolders()) {
                if (!folder.endsWith("/")) {
                    folder += "/";
                }
                result.add(folder);
            }
            return result;
        }
    }

    private String protocolIpKey(String protocol, String ip) {
        return protocol + "_" + ip;
    }

    private List<FolderScanInfo> parseFoldersString(String foldersStr) {
        // Format ip1:only:folder1:folder2|ip2:not:folder3:folder4

        if (foldersStr == null || "".equals(foldersStr.trim())) {
            return Collections.emptyList();
        }

        List<FolderScanInfo> result = new LinkedList<FolderScanInfo>();

        final String[] parts = foldersStr.split("\\|");
        for (String part : parts) {
            final String[] subParts = part.split(":");
            String ip = subParts[0];
            final String scanTypeStr = subParts[1];
            final FolderScanType folderScanType = FolderScanType.byCode(scanTypeStr);

            if (folderScanType == null) {
                throw new IllegalArgumentException("scan type: " + scanTypeStr);
            }

            String[] folders = Arrays.copyOfRange(subParts, 2, subParts.length);

            result.add(new FolderScanInfo(ip, Arrays.asList(folders), folderScanType));
        }

        return result;
    }
}
