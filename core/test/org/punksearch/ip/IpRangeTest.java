package org.punksearch.ip;

import junit.framework.TestCase;

public class IpRangeTest extends TestCase {

    public void testSimpleRange() {
        IpRange ipr = new IpRange("1.2.3.4");
        Ip ip = new Ip("1.2.3.4");
        assertEquals(ip, ipr.getStartIp());
        assertEquals(ip, ipr.getFinishIp());
    }

    public void testConventionalRange() {
        IpRange ipr = new IpRange("1.2.3.4-1.2.4.10");
        Ip ip1 = new Ip("1.2.3.4");
        Ip ip2 = new Ip("1.2.4.10");
        assertEquals(ip1, ipr.getStartIp());
        assertEquals(ip2, ipr.getFinishIp());
    }

    public void testWildcardRange() {
        IpRange ipr = new IpRange("1.2.*");
        assertEquals(new Ip("1.2.0.0"), ipr.getStartIp());
        assertEquals(new Ip("1.2.255.255"), ipr.getFinishIp());
        assertEquals(new IpRange("1.2.0.0-1.2.255.255"), ipr);

        IpRange allIps = new IpRange("*");
        assertEquals(new IpRange("0.0.0.0-255.255.255.255"), allIps);
    }

    public void testWildcardRangeValidity() {
        new IpRange("*");
        new IpRange("1.*");
        new IpRange("1.2.*");
        new IpRange("1.2.3.*");
        try {
            new IpRange("1.2.3.4.*");
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            new IpRange("1.2.3.4.5.*");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testRangeContains() {
        final IpRange range_1_2_3 = new IpRange("1.2.3.*");

        assertTrue(range_1_2_3.contains(new Ip("1.2.3.4")));
        assertTrue(range_1_2_3.contains(new Ip("1.2.3.0")));
        assertTrue(range_1_2_3.contains(new Ip("1.2.3.255")));
        assertFalse(range_1_2_3.contains(new Ip("3.3.3.3")));

        IpRange ipr = new IpRange("1.2.3.4-1.2.4.10");

        assertTrue(ipr.contains(ipr.getStartIp()));
        assertTrue(ipr.contains(ipr.getFinishIp()));
        assertTrue(ipr.contains(new Ip("1.2.3.4")));
        assertTrue(ipr.contains(new Ip("1.2.4.0")));
        assertFalse(ipr.contains(new Ip("1.2.3.0")));
        assertFalse(ipr.contains(new Ip("1.2.5.0")));
    }
}