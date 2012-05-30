package org.punksearch.crawler;

import org.junit.Test;
import org.punksearch.crawler.selective_scan.ScannedFoldersRegistry;

import java.util.HashMap;

import static junit.framework.Assert.*;

/**
 * User: gubarkov
 * Date: 30.05.12
 * Time: 16:36
 */
public class FoldersToScanTests {
    @Test
    public void testIllegal() {
        final HashMap<String, String> protocolToFoldersStr = new HashMap<String, String>();

        protocolToFoldersStr.put("smb", "1.1.1.1:only:aaa/111:bbb|2.2.2.2:not1:ccc/3/3/3");

        try {
            new ScannedFoldersRegistry(protocolToFoldersStr);
            fail();
        } catch (IllegalArgumentException e) {
        }

        protocolToFoldersStr.put("smb", "1.1.1.1:ZZZ:aaa/111:bbb|2.2.2.2:not:ccc/3/3/3");
        try {
            new ScannedFoldersRegistry(protocolToFoldersStr);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testRegistry() {
        final HashMap<String, String> protocolToFoldersStr = new HashMap<String, String>();

        protocolToFoldersStr.put("smb", "1.1.1.1:only:aaa/111:/bbb|2.2.2.2:not:ccc/3/3/3");

        ScannedFoldersRegistry scannedFoldersRegistry = new ScannedFoldersRegistry(protocolToFoldersStr);

        assertTrue(scannedFoldersRegistry.allowedScan("smb", "3.3.3.3", "qqq"));// no info
        assertTrue(scannedFoldersRegistry.allowedScan("smb", "1.1.1.1", "aaa//111///222"));
        assertTrue(scannedFoldersRegistry.allowedScan("smb", "1.1.1.1", "aaa\\111\\\\222"));
        assertTrue(scannedFoldersRegistry.allowedScan("smb", "1.1.1.1", "/aaa//111///222"));
        assertFalse(scannedFoldersRegistry.allowedScan("smb", "1.1.1.1", "aaa//111111"));
        assertFalse(scannedFoldersRegistry.allowedScan("smb", "1.1.1.1", "aaa"));
        assertFalse(scannedFoldersRegistry.allowedScan("smb", "1.1.1.1", "cccc"));
        assertTrue(scannedFoldersRegistry.allowedScan("smb", "1.1.1.1", "bbb"));
        assertTrue(scannedFoldersRegistry.allowedScan("smb", "1.1.1.1", "/bbb"));
        assertTrue(scannedFoldersRegistry.allowedScan("smb", "1.1.1.1", "bbb/BBB"));

        assertTrue(scannedFoldersRegistry.allowedScan("smb", "2.2.2.2", "bbb/BBB"));
        assertTrue(scannedFoldersRegistry.allowedScan("smb", "2.2.2.2", "ccc/3/3"));
        assertTrue(scannedFoldersRegistry.allowedScan("smb", "2.2.2.2", "ccc\\3\\3"));
        assertTrue(scannedFoldersRegistry.allowedScan("smb", "2.2.2.2", "ccc/3/3/333"));
        assertFalse(scannedFoldersRegistry.allowedScan("smb", "2.2.2.2", "ccc/3/3/3"));
        assertFalse(scannedFoldersRegistry.allowedScan("smb", "2.2.2.2", "ccc/3/3/3/"));
        assertFalse(scannedFoldersRegistry.allowedScan("smb", "2.2.2.2", "ccc/3\\3/3\\333"));
    }
}
