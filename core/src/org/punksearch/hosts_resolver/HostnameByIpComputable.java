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
public class HostnameByIpComputable implements Computable<String, String> {
    private static Log log = LogFactory.getLog(HostnameByIpComputable.class);

    @Override
    public String compute(String ip) throws InterruptedException {
        // hmmm.. now ip is of form smb://1.1.1.1
        ip = fixIp(ip);

        InetAddress addr;
        try {
            addr = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            log.warn("Unable to resolve ip: " + ip, e);
            return null;
        }
        return addr.getHostName();
    }

    private String fixIp(String ip) {
        int pos = ip.indexOf("://");
        if (pos >= 0) {
            return ip.substring(pos + 3);
        } else {
            return ip;
        }
    }
}
