package org.punksearch.common;

import org.punksearch.logic.hosts_resolver.HostnameResolver;
import org.punksearch.logic.online.OnlineStatuses;

/**
 * Encapsulates protocol + ip
 * <p/>
 * User: gubarkov
 * Date: 04.06.12
 * Time: 17:56
 */
public class PunksearchHost {
    private final String protocol;
    private final String ip;

    /**
     * @param protoIp like smb_1.1.1.1
     */
    public PunksearchHost(String protoIp) {
        final String[] parts = protoIp.split("_");

        protocol = parts[0];
        ip = parts[1];
    }

    public String getProtocol() {
        return protocol;
    }

    public String getIp() {
        return ip;
    }

    public String getHostName() {
        return HostnameResolver.getInstance().resolveByIp(ip);
    }

    public boolean isOnline() {
        return OnlineStatuses.getInstance().isOnline(protocol + "_" + ip);
    }
}