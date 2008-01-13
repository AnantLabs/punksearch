package org.punksearch.indexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.punksearch.ip.IpRange;

public class CrawlerConfig
{
	private int						indexThreads		= 1;
	private int						indexDeep			= 5;
	private List<IpRange>			ipRanges			= new ArrayList<IpRange>();
	private String					smbDomain			= "";
	private String					smbUser				= "";
	private String					smbPassword			= "";
	private int						smbTimeout			= 5000;
	private String					ftpDefaultEnc		= "";
	private Map<String, String>		ftpCustomEnc		= new HashMap<String, String>();
	private String					ftpDefaultMode		= "";
	private Map<String, String>		ftpCustomModes		= new HashMap<String, String>();
	private int						ftpTimeout			= 2000;
	
	public int getIndexThreads()
	{
		return indexThreads;
	}
	public void setIndexThreads(int indexThreads)
	{
		this.indexThreads = indexThreads;
	}
	public int getIndexDeep()
	{
		return indexDeep;
	}
	public void setIndexDeep(int indexDeep)
	{
		this.indexDeep = indexDeep;
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
		return (ranges.length() != 0) ? ranges.substring(1) : "";
	}	
	public void setIpRanges(List<IpRange> ipRanges)
	{
		this.ipRanges = ipRanges;
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
	public void setSmbDomain(String smbDomain)
	{
		this.smbDomain = smbDomain;
	}
	public String getSmbUser()
	{
		return smbUser;
	}
	public void setSmbUser(String smbUser)
	{
		this.smbUser = smbUser;
	}
	public String getSmbPassword()
	{
		return smbPassword;
	}
	public void setSmbPassword(String smbPassword)
	{
		this.smbPassword = smbPassword;
	}
	public int getSmbTimeout()
	{
		return smbTimeout;
	}
	public void setSmbTimeout(int smbTimeout)
	{
		this.smbTimeout = smbTimeout;
	}
	public String getFtpDefaultEncoding()
	{
		return ftpDefaultEnc;
	}
	public void setFtpDefaultEncoding(String ftpDefaultEnc)
	{
		this.ftpDefaultEnc = ftpDefaultEnc;
	}
	public Map<String, String> getFtpCustomEncodings()
	{
		return ftpCustomEnc;
	}
	public void setFtpCustomEncodings(Map<String, String> ftpCustomEnc)
	{
		this.ftpCustomEnc = ftpCustomEnc;
	}
	public void setFtpCustomEncodings(String encString)
	{
		if (encString == null || encString.length() == 0)
			return;

		String[] chunks = encString.split(",");
		for (String chunk : chunks)
		{
			String[] parts = chunk.split(":");
			ftpCustomEnc.put(parts[0], parts[1]);
		}
	}	
	public String getFtpDefaultMode()
	{
		return ftpDefaultMode;
	}
	public void setFtpDefaultMode(String ftpDefaultMode)
	{
		this.ftpDefaultMode = ftpDefaultMode;
	}
	public Map<String, String> getFtpCustomModes()
	{
		return ftpCustomModes;
	}
	public void setFtpCustomModes(Map<String, String> ftpCustomModes)
	{
		this.ftpCustomModes = ftpCustomModes;
	}
	public void setFtpCustomModes(String modString)
	{
		if (modString == null || modString.length() == 0)
			return;

		String[] chunks = modString.split(",");
		for (String chunk : chunks)
		{
			String[] parts = chunk.split(":");
			ftpCustomModes.put(parts[0], parts[1]);
		}
	}	
	public int getFtpTimeout()
	{
		return ftpTimeout;
	}
	public void setFtpTimeout(int ftpTimeout)
	{
		this.ftpTimeout = ftpTimeout;
	}
}
