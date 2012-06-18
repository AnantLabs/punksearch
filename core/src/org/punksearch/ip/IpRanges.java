package org.punksearch.ip;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.punksearch.logic.hosts_resolver.HostnameResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * User: gubarkov
 * Date: 01.06.12
 * Time: 18:03
 */
public class IpRanges {
    private static final Log log = LogFactory.getLog(IpRanges.class);

    private List<IpRange> ipRanges;

    /**
     * @param rangesString comma-separated ranges string
     */
    public IpRanges(String rangesString) {
        ipRanges = parseList(rangesString, false);
    }

    public void add(IpRange ipRange) {
        ipRanges.add(ipRange);
    }

    public boolean contains(Ip ip) {
        for (IpRange ipRange : ipRanges) {
            if (ipRange.contains(ip)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Converts comma-separated ranges string to a list of IpRange objects.
     * <p/>
     * Implementation skips chunks what can't be parsed. In case of empty or null argument the empty list will be
     * returned.
     *
     * @param ranges Comma-separated ranges string to be converted
     * @param silent parse mode. When true - throws no errors while parsing
     * @return List of IpRange instances, may be empty.
     */
    public static List<IpRange> parseList(String ranges, boolean silent) {
        List<IpRange> result = new ArrayList<IpRange>();
        if (ranges != null) {
            for (String range : ranges.split(",")) {
                if (IpRange.isIpRange(range)) {
                    result.add(new IpRange(range));
                } else {
                    String ip = HostnameResolver.getIpByHostname(range);
                    if (ip != null) {
                        result.add(new IpRange(ip));
                    } else if (!silent) {
                        throw new IllegalArgumentException("Invalid ranges: " + ranges + " because of part: " + range);
                    } else {
                        log.warn("Not an ip range: " + range);
                    }
                }
            }
        }
        return result;
    }

    public static List<IpRange> parseList(String ranges) {
        return parseList(ranges, true);
    }

    @Override
    public String toString() {
        return ipRanges.toString();
    }
}
