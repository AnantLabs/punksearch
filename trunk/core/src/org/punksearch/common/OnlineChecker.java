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
package org.punksearch.common;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Tests if some particular host is online. Both SMB and FTP protocols are supported.
 * 
 * This is active implementation, no caching is used.
 * 
 * Hosts are described like: [protocol]://[ip] or [protocol]_[ip]
 * 
 * Examples: smb://10.20.30.40 or smb_10.20.30.40
 * 
 * Alternatively isActive() method may be used to test any required port for a host. 
 * 
 * @author Yury Soldak (ysoldak@gmail.com)
 * 
 */
public class OnlineChecker {

	public static final String   TIMEOUT_PROPERTY = "org.punksearch.online.timeout";

	private static int           TIMEOUT          = Integer.valueOf(System.getProperty(TIMEOUT_PROPERTY, "5000"));
	private static final boolean USE_OLD_SMB_PORT = true;

	private static final int     SMB_PORT         = 445;
	private static final int     SMB_OLD_PORT     = 139;
	private static final int     FTP_PORT         = 21;

	public static boolean isOnline(String host) {
		if (!(host.startsWith("smb") || host.startsWith("ftp"))) {
			throw new IllegalArgumentException("Unknown protocol: " + host);
		}

		int ipStart = (host.indexOf(":") == 3) ? 6 : 4; // cut smb_ or smb://
		String ip = host.substring(ipStart);

		return (host.startsWith("ftp")) ? isActiveFtp(ip) : isActiveSmb(ip);
	}

	public static boolean isActiveSmb(String ip) {
		return isActive(ip, SMB_PORT) || (USE_OLD_SMB_PORT && isActive(ip, SMB_OLD_PORT));
	}

	public static boolean isActiveFtp(String ip) {
		return isActive(ip, FTP_PORT);
	}

	public static boolean isActive(String ip, int port) {
		try {
			SocketAddress sockaddr = new InetSocketAddress(ip, port);
			Socket s = new Socket();
			s.connect(sockaddr, TIMEOUT);
			s.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

}
