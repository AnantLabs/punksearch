package org.punksearch.web.online;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.punksearch.commons.OnlineChecker;

public class CachedOnlineChecker {

	private static int timeout = 600; // 10 min
	
	private static Map<String, HostStatus> cache = Collections.synchronizedMap(new HashMap<String, HostStatus>());
	
	public static void setTimeout(int timeout)
	{
		CachedOnlineChecker.timeout = timeout; 
	}
	
	public static boolean isOnline(String host)
	{
		Date recheckLimit = new Date(System.currentTimeMillis() - timeout * 1000);
		HostStatus hs = cache.get(host);
		
		if (hs != null && hs.date.after(recheckLimit)) {
			return hs.online;
		}
		
		boolean online = OnlineChecker.isOnline(host);
		cache.put(host, new HostStatus(new Date(), online));
		return online;
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