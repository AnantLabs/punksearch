package org.punksearch.commons;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class OnlineChecker {

	// TODO: extract to settings
	private static final int     TIMEOUT          = 1000;
	private static final boolean USE_OLD_SMB_PORT = false;

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
