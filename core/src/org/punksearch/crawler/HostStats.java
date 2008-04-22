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
package org.punksearch.crawler;

import org.punksearch.ip.Ip;

/**
 * Statistics for a crawled host
 * 
 * @author Yury Soldak (ysoldak@gmail.com)
 * 
 */
public class HostStats implements Comparable<HostStats> {

	private Ip     ip;
	private String protocol;
	private long   size;

	public HostStats(String ip, String protocol, long size) {
		this(new Ip(ip), protocol, size);
	}

	public HostStats(Ip ip, String protocol, long size) {
		this.ip = ip;
		this.protocol = protocol;
		this.size = size;
	}

	public Ip getIp() {
		return ip;
	}

	public String getProtocol() {
		return protocol;
	}

	public long getSize() {
		return size;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof HostStats) {
			return (compareTo((HostStats) obj) == 0 && size == ((HostStats) obj).size);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return protocol.hashCode() + ip.hashCode() + Long.valueOf(size).hashCode();
	}

	public int compareTo(HostStats obj) {
		int ips = ip.compareTo(obj.getIp());
		if (ips != 0) {
			return ips;
		} else {
			return protocol.compareTo(obj.getProtocol());
		}
	}

	@Override
	public String toString() {
		return ip + "," + protocol + "," + size;
	}

}
