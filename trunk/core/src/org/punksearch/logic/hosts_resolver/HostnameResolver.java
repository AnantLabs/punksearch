package org.punksearch.logic.hosts_resolver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.punksearch.util.RenewableMemoizer;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * User: gubarkov
 * Date: 21.05.12
 * Time: 15:56
 */
public class HostnameResolver {
    private static Log log = LogFactory.getLog(HostnameResolver.class);

    private static final HostnameResolver hostnameResolver = new HostnameResolver();

    private RenewableMemoizer<String, String> cache;

    public HostnameResolver() {
        cache = new RenewableMemoizer<String, String>(new HostnameByIpComputable(), 60 * 60 * 1000, true);
    }

    public static HostnameResolver getInstance() {
        return hostnameResolver;
    }

    /**
     * @param ip ip to resove (either "1.1.1.1" or "smb://1.1.1.1" form)
     * @return either resolved hostname or the ip given
     */
    public String resolveByIp(String ip) {
        String hostname;
        try {
            hostname = cache.compute(ip);
        } catch (InterruptedException e) {
            log.warn(e);
            hostname = ip;
        }

        if (hostname == null) {
            hostname = ip;
        }

        return hostname;
    }

    /**
     * @param hostname to get ip of
     * @return ip or null if host unknown
     */
    public static String getIpByHostname(String hostname) {
        try {
            InetAddress inetAddress = InetAddress.getByName(hostname);
            return inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }
}
