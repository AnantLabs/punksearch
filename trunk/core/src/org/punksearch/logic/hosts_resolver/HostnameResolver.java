package org.punksearch.logic.hosts_resolver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.punksearch.util.RenewableMemoizer;

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
}
