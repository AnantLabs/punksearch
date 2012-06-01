/***************************************************************************
 *                                                                         *
 *   PunkSearch - Searching over LAN                                       *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package org.punksearch.ip;

import java.util.ArrayList;
import java.util.List;

public class IpRange implements Comparable<IpRange> {
    private final Ip startIp;
    private final Ip finishIp;

    /**
     * Constructs an instance.
     *
     * @param strRange String in form "n<sub>1</sub>.n<sub>2</sub>.n<sub>3</sub>.n<sub>4</sub>-n<sub>5</sub>.n<sub>6</sub>.n<sub>7</sub>.n<sub>8</sub>"
     *                 or "n<sub>1</sub>.n<sub>2</sub>.n<sub>3</sub>.n<sub>4</sub>" or "n<sub>1</sub>.n<sub>2</sub>.*",
     *                 where n<sub>i</sub> in range 0..255
     */
    public IpRange(String strRange) {
        if (!isIpRange(strRange)) {
            throw new IllegalArgumentException("Illegal IP range: " + strRange);
        }
        String[] parts = strRange.split("-");
        if (parts.length == 2) {
            startIp = new Ip(parts[0]);
            finishIp = new Ip(parts[1]);
        } else {
            if (Ip.isIp(strRange)) {
                startIp = new Ip(strRange);
                finishIp = new Ip(strRange);
            } else {
                // ipWildcardRange
                // turn n1.n2.* to n1.n2.0.0-n1.n2.255.255
                final String[] subParts = strRange.split("\\.");

                String[] startIpParts = {"0", "0", "0", "0"};
                String[] finishIpParts = {"255", "255", "255", "255"};

                System.arraycopy(subParts, 0, startIpParts, 0, subParts.length - 1);
                System.arraycopy(subParts, 0, finishIpParts, 0, subParts.length - 1);

                IpRange ipRange = new IpRange(
                        startIpParts[0] + '.' + startIpParts[1] + '.' + startIpParts[2] + '.' + startIpParts[3] + '-' +
                                finishIpParts[0] + '.' + finishIpParts[1] + '.' + finishIpParts[2] + '.' + finishIpParts[3]);

                startIp = ipRange.startIp;
                finishIp = ipRange.finishIp;
            }
        }
    }

    public Ip getFinishIp() {
        return finishIp;
    }

    public Ip getStartIp() {
        return startIp;
    }

    @Override
    public String toString() {
        if (!startIp.equals(finishIp)) {
            return startIp + "-" + finishIp;
        } else {
            return startIp.toString();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IpRange && startIp.equals(((IpRange) obj).startIp)
                && finishIp.equals(((IpRange) obj).finishIp)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return startIp.hashCode() + finishIp.hashCode();
    }

    @Override
    public int compareTo(IpRange o) {
        return startIp.compareTo(o.startIp);
    }

    /**
     * Checks whatever the candidate string can be parsed as IpRange object.
     *
     * @param candidate String to check
     * @return true if string defines an IP range
     */
    public static boolean isIpRange(String candidate) {
        String[] parts = candidate.split("-");
        if (parts.length == 2) {
            return Ip.isIp(parts[0]) && Ip.isIp(parts[1]);
        } else {
            return Ip.isIp(parts[0]) || isIpWildcardRange(parts[0]);
        }
    }

    /**
     * @param range to check
     * @return if range is of form N.* or N.N.* or N.N.N.*
     */
    private static boolean isIpWildcardRange(String range) {
        final String[] parts = range.split("\\.");

        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            if (!Ip.validIpPart(part)) {
                return false;
            }
        }

        return "*".equals(parts[parts.length - 1]);
    }

    /**
     * Converts comma-separated ranges string to a list of IpRange objects.
     * <p/>
     * Implementation skips chunks what can't be parsed. In case of empty or null argument the empty list will be
     * returned.
     *
     * @param ranges Comma-separated ranges string to be converted
     * @return List of IpRange instances, may be empty.
     */
    public static List<IpRange> parseList(String ranges) {
        List<IpRange> result = new ArrayList<IpRange>();
        if (ranges != null) {
            for (String range : ranges.split(",")) {
                if (isIpRange(range)) {
                    result.add(new IpRange(range));
                }
            }
        }
        return result;
    }
}
