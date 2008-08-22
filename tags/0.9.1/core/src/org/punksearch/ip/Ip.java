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

public class Ip implements Comparable<Ip> {
	private Long value;

	public Ip(String ipStr) {
		if (!isIp(ipStr)) {
			throw new IllegalArgumentException("Illegal IP: " + ipStr);
		}
		String[] parts = ipStr.split("\\.");
		long d1 = Long.parseLong(parts[0]) << 24;
		long d2 = Long.parseLong(parts[1]) << 16;
		long d3 = Long.parseLong(parts[2]) << 8;
		long d4 = Long.parseLong(parts[3]);
		value = d1 | d2 | d3 | d4;
	}

	public Ip(Long value) {
		this.value = value;
	}

	public String toString() {
		long d4 = value & 255;
		long d3 = (value >> 8) & 255;
		long d2 = (value >> 16) & 255;
		long d1 = (value >> 24) & 255;
		return d1 + "." + d2 + "." + d3 + "." + d4;
	}

	public Long toLong() {
		return value;
	}

	public int compareTo(Ip obj) {
		return value.compareTo(obj.toLong());
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Ip)) {
			return false;
		}
		return (compareTo((Ip) obj) == 0);
	}

	public int hashCode() {
		return value.intValue();
	}

	public static boolean isIp(String candidate) {
		String[] parts = candidate.split("\\.");
		if (parts.length != 4) {
			return false;
		}
		for (String part : parts) {
			int value = Integer.parseInt(part);
			if (value < 0 || value > 255) {
				return false;
			}
		}
		return true;
	}

}