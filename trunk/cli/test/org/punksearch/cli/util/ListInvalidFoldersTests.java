package org.punksearch.cli.util;

import junit.framework.Assert;
import org.junit.Test;

/**
 * User: gubarkov
 * Date: 19.06.12
 * Time: 20:03
 */
public class ListInvalidFoldersTests {
    @Test
    public void testSuspiciousPath() {
        Assert.assertTrue(ListInvalidFolders.isSuspiciousPath("/aaaa/aaaa/aaaa/"));
        Assert.assertTrue(ListInvalidFolders.isSuspiciousPath("/aaaa/bbb/aaaa/aaaa/"));
        Assert.assertTrue(ListInvalidFolders.isSuspiciousPath("/aaaa/bbb/aaaa/ccc/aaaa/aa/"));

        Assert.assertFalse(ListInvalidFolders.isSuspiciousPath("/aaaQa/aaaa/"));
        Assert.assertFalse(ListInvalidFolders.isSuspiciousPath("/aaaQa/aaaa/aaaa/"));
        Assert.assertFalse(ListInvalidFolders.isSuspiciousPath("/aaaa/bbb/aaAaa/ccc/"));
    }
}
