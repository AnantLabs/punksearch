package org.punksearch.commons;

import java.util.ArrayList;
import java.util.List;

import org.punksearch.ip.IpRange;


/**
 * Class for storing indexer config
 */
public class SearcherConfig
{
	private static SearcherConfig	singleton;

	private String					indexDirectory;
	private int						indexThreads	= 1;
	private int						indexDeep		= 5;
	private String					smbDomain		= "";
	private String					smbUser			= "";
	private String					smbPassword		= "";
	private int						smbTimeout		= 5000;
	private int						maxClauseCount  = 1000000;
	private List<IpRange>			ipRanges		= new ArrayList<IpRange>();

	private SearcherConfig()
	{
	}

	public void setIndexDirectory(String indexDirectory)
	{
		this.indexDirectory = indexDirectory;
	}

	public String getIndexDirectory()
	{
		return indexDirectory;
	}

	public void setIndexThreads(int indexThreads)
	{
		this.indexThreads = indexThreads;
	}

	public int getIndexThreads()
	{
		return indexThreads;
	}

	public void setIndexDeep(int indexDeep)
	{
		this.indexDeep = indexDeep;
	}

	public int getIndexDeep()
	{
		return indexDeep;
	}

	public void setIpRanges(String rangesString)
	{
		ipRanges.clear();
		String[] rangeChunks = rangesString.split(",");
		for (String chunk : rangeChunks)
		{
			ipRanges.add(new IpRange(chunk));
		}
	}

	public List<IpRange> getIpRanges()
	{
		return ipRanges;
	}

	public String getIpRangesString()
	{
		String ranges = "";
		for (IpRange range : ipRanges)
		{
			ranges += "," + range.toString();
		}
		return (ranges.length() != 0)? ranges.substring(1) : "";
	}
	
	public void setSmbLogin(String smbLogin)
	{
		if (smbLogin.length() != 0 && smbLogin.contains("\\") && smbLogin.contains(":"))
		{
			this.smbDomain = smbLogin.substring(0, smbLogin.indexOf("\\"));
			this.smbUser = smbLogin.substring(smbLogin.indexOf("\\") + 1, smbLogin.indexOf(":"));
			this.smbPassword = smbLogin.substring(smbLogin.indexOf(":") + 1);
		}
		else
		{
			this.smbDomain = "";
			this.smbUser = "";
			this.smbPassword = "";
		}
	}

	public String getSmbLogin()
	{
		String result = "";
		if (getSmbDomain().length() != 0)
			result += getSmbDomain() + "\\";
		if (getSmbUser().length() != 0)
			result += getSmbUser() + ":" + getSmbPassword();
		return result;
	}

	public String getSmbDomain()
	{
		return smbDomain;
	}

	public String getSmbUser()
	{
		return smbUser;
	}

	public String getSmbPassword()
	{
		return smbPassword;
	}

	public static SearcherConfig getInstance()
	{
		if (null == singleton)
			singleton = new SearcherConfig();
		return singleton;
	}

	public int getSmbTimeout()
	{
		return smbTimeout;
	}

	public void setSmbTimeout(int timeout)
	{
		this.smbTimeout = timeout;
	}
	
	public int getMaxClauseCount()
	{
		return maxClauseCount;
	}

	public void setMaxClauseCount(int maxClauseCount)
	{
		this.maxClauseCount = maxClauseCount;
	}	
}
