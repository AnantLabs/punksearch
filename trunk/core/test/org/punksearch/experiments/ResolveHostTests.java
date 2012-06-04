package org.punksearch.experiments;

import org.junit.Assert;
import org.junit.Test;
import org.punksearch.logic.hosts_resolver.HostnameByIpComputable;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * User: gubarkov
 * Date: 21.05.12
 * Time: 15:44
 */
public class ResolveHostTests {
    @Test
    public void t1() throws UnknownHostException {
        InetAddress addr = InetAddress.getByName("194.85.80.61");
        String host = addr.getHostName();
        System.out.println(host);
    }

    @Test
    public void resolveHostTest() throws InterruptedException {
        HostnameByIpComputable hostnameByIpComputable = new HostnameByIpComputable();

        Assert.assertEquals("localhost", hostnameByIpComputable.compute("127.0.0.1"));
        Assert.assertEquals("google-public-dns-a.google.com", hostnameByIpComputable.compute("8.8.8.8"));
        Assert.assertEquals(null, hostnameByIpComputable.compute("999.999.999.999"));
    }

}
