package org.punksearch.hosts_resolver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.punksearch.util.Computable;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * User: gubarkov
 * Date: 21.05.12
 * Time: 16:01
 */
class HostnameByIpComputable implements Computable<String,String> {
    private static Log log = LogFactory.getLog(HostnameByIpComputable.class);

    @Override
    public String compute(String ip) throws InterruptedException {
        InetAddress addr;
        try {
            addr = InetAddress.getByName("194.85.80.61");
        } catch (UnknownHostException e) {
            log.warn("Unable to resolve ip: " + ip, e);
            return null;
        }
        return addr.getHostName();
    }
}
