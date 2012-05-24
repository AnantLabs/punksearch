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
package org.punksearch.online;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Probes if some particular host is online. Both SMB and FTP protocols are supported.
 * 
 * Use connect() method to test any required port for a host.
 * 
 * Use ping() method to ping the host (does not open a connection).
 * 
 * Use probe() method to use default strategy (can be adjusted with system property)
 * 
 * @author Yury Soldak (ysoldak@gmail.com)
 * 
 */
public class Probe {
	private static Log           __log             = LogFactory.getLog(Probe.class);

	/**
	 * Count of msec to wait while probing host's port. <p/> Host considered offline if socket was not opened in this
	 * period of time.
	 */
	public static final String   TIMEOUT_PROPERTY  = "org.punksearch.online.timeout";

	/**
	 * Strategy to use for determining host status: "ping" or "connect" ("connect" is used by default)
	 * 
	 * First does not open connection to remote host, but inaccurate (it does not prove smb/ftp server is running).
	 * 
	 * Second opens connection to remote host and closes it immediately, yet remote host observes connection, etc.
	 */
	public static final String   STRATEGY_PROPERTY = "org.punksearch.online.strategy";

	private static int           PROBE_TIMEOUT     = Integer.getInteger(TIMEOUT_PROPERTY, 1000);
	private static String        PROBE_STRATEGY    = System.getProperty(STRATEGY_PROPERTY, "connect");
	private static final boolean USE_OLD_SMB_PORT  = true;

	private static final int     SMB_PORT          = 445;
	private static final int     SMB_OLD_PORT      = 139;
	private static final int     FTP_PORT          = 21;

	/**
	 * Does probing. Expects host in form [protocol]://[ip]
	 * 
	 * @param host
	 *            Host to probe
	 * @return true if online, false if offline
	 */
	public boolean probe(String host) {
		String ip = host.substring(6); // strip "smb://" or "ftp://"
		if (PROBE_STRATEGY.equals("ping")) {
			return ping(ip);
		} else {
			return (host.startsWith("ftp")) ? connectFtp(ip) : connectSmb(ip);
		}
	}

	public boolean ping(String ip) {
		boolean result;
		try {
			InetAddress ia = InetAddress.getByName(ip);
			result = ia.isReachable(PROBE_TIMEOUT);
		} catch (Exception e) {
			__log.warn("Exception during ping: " + e.getMessage());
			result = false;
		}
		__log.debug("Probe (ping): " + ip + " = " + result);
		return result;
	}

	private boolean connectSmb(String ip) {
		return connect(ip, SMB_PORT) || (USE_OLD_SMB_PORT && connect(ip, SMB_OLD_PORT));
	}

	private boolean connectFtp(String ip) {
		return connect(ip, FTP_PORT);
	}

	/**
	 * Checks if custom port is open at the host
	 * 
	 * @param ip
	 *            IP of the host to check
	 * @param port
	 *            Port of the host to check
	 * @return true if port is open, false if closed
	 */
	public boolean connect(String ip, int port) {
		boolean result;
		try {
			SocketAddress sockaddr = new InetSocketAddress(ip, port);
			Socket s = new Socket();
			s.connect(sockaddr, PROBE_TIMEOUT);
			s.close();
			result = true;
		} catch (IOException e) {
			result = false;
		}
		__log.debug("Probe (connect): " + ip + ":" + port + " = " + result);
		return result;
	}
}
