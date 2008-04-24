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

public class IpRange implements Comparable<IpRange> {
	private Ip startIp;
	private Ip finishIp;

	public IpRange(String strRange) {
		if (!isIpRange(strRange)) {
			throw new IllegalArgumentException("Illegal IP range: " + strRange);
		}
		String[] parts = strRange.split("-");
		if (parts.length == 2) {
			startIp = new Ip(parts[0]);
			finishIp = new Ip(parts[1]);
		} else {
			startIp = new Ip(strRange);
			finishIp = new Ip(strRange);
		}
	}

	public Ip getFinishIp() {
		return finishIp;
	}

	public Ip getStartIp() {
		return startIp;
	}

	public String toString() {
		if (!startIp.equals(finishIp)) {
			return startIp + "-" + finishIp;
		} else {
			return startIp.toString();
		}
	}

	public static boolean isIpRange(String candidate) {
		String[] parts = candidate.split("-");
		if (parts.length == 2) {
			return Ip.isIp(parts[0]) && Ip.isIp(parts[1]);
		} else {
			return Ip.isIp(parts[0]);
		}
	}

	public boolean equals(Object obj) {
		if (obj instanceof IpRange && startIp.equals(((IpRange) obj).startIp)
		        && finishIp.equals(((IpRange) obj).finishIp)) {
			return true;
		}
		return false;
	}

	public int hashCode() {
		return startIp.hashCode() + finishIp.hashCode();
	}

	public int compareTo(IpRange o) {
		return startIp.compareTo(o.startIp);
	}

}
