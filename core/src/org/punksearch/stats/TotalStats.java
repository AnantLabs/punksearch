package org.punksearch.stats;

import java.util.Date;
import java.util.List;

/**
 * User: gubarkov
 * Date: 27.08.12
 * Time: 16:54
 */
public class TotalStats {
    private long scanTimestamp;
    private int scannedHosts = 0;
    private int scannedShares = 0;
    private long scannedFiles = 0;
    private long scannedBytes = 0;

    public TotalStats(long scanTimestamp) {
        this.scanTimestamp = scanTimestamp;
    }

    public void addShares(int shares) {
        scannedShares += shares;
    }

    public void addHostStats(List<HostStats> hosts) {
        scannedHosts += hosts.size(); // TODO ? same host may have smb + ftp

        for (HostStats host : hosts) {
            scannedFiles += host.getCount();
            scannedBytes += host.getSize();
        }
    }

    public String serialize() {
        return "" +
                scanTimestamp + ',' +
                scannedHosts + ',' +
                scannedShares + ',' +
                scannedFiles + ',' +
                scannedBytes;
    }

    public static TotalStats deserialize(String str) {
        final String[] parts = str.split(",");
        final TotalStats totalStats = new TotalStats(Long.parseLong(parts[0]));
        totalStats.scannedHosts = Integer.parseInt(parts[1]);
        totalStats.scannedShares = Integer.parseInt(parts[2]);
        totalStats.scannedFiles = Long.parseLong(parts[3]);
        totalStats.scannedBytes = Long.parseLong(parts[4]);
        return totalStats;
    }

    public long getScanTimestamp() {
        return scanTimestamp;
    }

    public Date getScanDate() {
        return new Date(scanTimestamp);
    }

    public int getScannedHosts() {
        return scannedHosts;
    }

    public int getScannedShares() {
        return scannedShares;
    }

    public long getScannedFiles() {
        return scannedFiles;
    }

    public long getScannedBytes() {
        return scannedBytes;
    }
}
