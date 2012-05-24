package org.punksearch.experiments;

import org.junit.Test;

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
}
