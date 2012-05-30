package org.punksearch.crawler;

import org.junit.Test;

import java.util.HashMap;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * User: gubarkov
 * Date: 30.05.12
 * Time: 16:36
 */
public class FoldersToScanTests {
    @Test
    public void test1() {
        final HashMap<String, String> protocolToFoldersStr = new HashMap<String, String>();

        protocolToFoldersStr.put("smb", "1.1.1.1:only:aaa/111:bbb|2.2.2.2:not:ccc/3/3/3");

        FoldersToScanRegistry foldersToScanRegistry = new FoldersToScanRegistry(protocolToFoldersStr);

        assertTrue(foldersToScanRegistry.allowedScan("smb", "3.3.3.3", "qqq"));// no info
        assertTrue(foldersToScanRegistry.allowedScan("smb", "1.1.1.1", "aaa//111///222"));
        assertFalse(foldersToScanRegistry.allowedScan("smb", "1.1.1.1", "aaa//111111"));
        assertFalse(foldersToScanRegistry.allowedScan("smb", "1.1.1.1", "aaa"));
        assertFalse(foldersToScanRegistry.allowedScan("smb", "1.1.1.1", "cccc"));
        assertTrue(foldersToScanRegistry.allowedScan("smb", "1.1.1.1", "bbb"));
        assertTrue(foldersToScanRegistry.allowedScan("smb", "1.1.1.1", "bbb/BBB"));

        assertTrue(foldersToScanRegistry.allowedScan("smb", "2.2.2.2", "bbb/BBB"));
        assertTrue(foldersToScanRegistry.allowedScan("smb", "2.2.2.2", "ccc/3/3"));
        assertTrue(foldersToScanRegistry.allowedScan("smb", "2.2.2.2", "ccc/3/3/333"));
        assertFalse(foldersToScanRegistry.allowedScan("smb", "2.2.2.2", "ccc/3/3/3"));
        assertFalse(foldersToScanRegistry.allowedScan("smb", "2.2.2.2", "ccc/3/3/3/"));
        assertFalse(foldersToScanRegistry.allowedScan("smb", "2.2.2.2", "ccc/3/3/3/333"));
    }
}
