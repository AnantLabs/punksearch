package org.punksearch.commons;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class OnlineChecker {
	
	public static boolean isOnline(String host)
	{
		if ( !(host.startsWith("smb") || host.startsWith("ftp")) )
		{
			throw new IllegalArgumentException("Unknown protocol: " + host);
		}
		
		int ipStart = (host.indexOf(":") == 3)? 6 : 4; // cut smb_ or smb://
		String ip = host.substring(ipStart);
		
		return (host.startsWith("ftp"))? isActiveFtp(ip) : isActiveSmb(ip);
	}
	
	public static boolean isActiveSmb(String ip) {
		return isActive(ip, 445) || isActive(ip, 139);
	}
	
	public static boolean isActiveFtp(String ip) {
		return isActive(ip, 21);
	}
	
	public static boolean isActive(String ip, int port)
	{
		try
		{
			SocketAddress sockaddr = new InetSocketAddress(ip, port);
			Socket s = new Socket();
			s.connect(sockaddr, 1000);
			s.close();
			return true;
		}
		catch (IOException e)
		{
			return false;
		}
	}

}
