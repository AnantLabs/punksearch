package org.punksearch.web.online;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class OnlineChecker {

	private static int timeout = 600; // 10 min
	
	private static Map<String, HostStatus> cache = new HashMap<String, HostStatus>();
	
	public static void setTimeout(int timeout)
	{
		OnlineChecker.timeout = timeout; 
	}
	
	public static boolean isOnline(String host)
	{
		if ( !(host.startsWith("smb") || host.startsWith("ftp")) )
		{
			throw new IllegalArgumentException("Unknown protocol: " + host);
		}
		
		Date bound = new Date(System.currentTimeMillis() - timeout * 1000);
		HostStatus hs = cache.get(host);
		
		if (hs != null && hs.date.after(bound)) {
			return hs.online;
		}
		
		int ipStart = (host.indexOf(":") == 3)? 6 : 4;
		String ip = host.substring(ipStart);
		
		boolean online = (host.startsWith("ftp"))? isActiveFtp(ip) : isActiveSmb(ip);
		cache.put(host, new HostStatus(new Date(), online));
		return online;
	}
	
	private static boolean isActiveSmb(String ip) {
		return isActive(ip, 445) || isActive(ip, 139);
	}
	
	private static boolean isActiveFtp(String ip) {
		return isActive(ip, 21);
	}
	
	private static boolean isActive(String ip, int port)
	{
		System.out.println("isActive:" + ip + ":" + String.valueOf(port));
		try
		{
			SocketAddress sockaddr = new InetSocketAddress(ip, port);
			Socket s = new Socket();
			s.connect(sockaddr, 1000);
			s.close();
			return true;
		}
		catch (SocketException e)
		{
			return false;
		}
		catch (IOException e)
		{
			return false;
		}
	}

}

class HostStatus
{
	
	Date date;
	boolean online;
	
	HostStatus(Date date, boolean online)
	{
		this.date = date;
		this.online = online;
	}
	
}