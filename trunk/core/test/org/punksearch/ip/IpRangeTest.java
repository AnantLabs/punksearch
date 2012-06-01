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
    }
}