package org.punksearch.web;

import java.util.List;
import java.util.Map;

import org.punksearch.indexer.CrawlerConfig;
import org.punksearch.ip.IpRange;

/**
 * Class for storing indexer config
 */
public class SearcherConfig
{
	private static SearcherConfig	instance;

	private String					indexDirectory;
	private CrawlerConfig			crawlerConfig		= new CrawlerConfig();
	private String					googleAnalyticsId	= "";
	private int						maxClauseCount		= 1000000;
	private boolean					fastSearch			= true;

	private SearcherConfig()
	{
	}

	public static SearcherConfig getInstance()
	{
		if (instance == null)
		{
			instance = new SearcherConfig();
		}
		return instance;
	}
	
	public CrawlerConfig getCrawlerConfig()
	{
		return crawlerConfig;
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
		crawlerConfig.setIndexThreads(indexThreads);
	}

	public int getIndexThreads()
	{
		return crawlerConfig.getIndexThreads();
	}

	public void setIndexDeep(int indexDeep)
	{
		crawlerConfig.setIndexDeep(indexDeep);
	}

	public int getIndexDeep()
	{
		return crawlerConfig.getIndexDeep();
	}

	public void setIpRanges(String rangesString)
	{
		crawlerConfig.setIpRanges(rangesString);
	}

	public List<IpRange> getIpRanges()
	{
		return crawlerConfig.getIpRanges();
	}

	public String getIpRangesString()
	{
		return crawlerConfig.getIpRangesString();
	}

	public void setSmbLogin(String smbLogin)
	{
		crawlerConfig.setSmbLogin(smbLogin);
	}

	public String getSmbLogin()
	{
		return crawlerConfig.getSmbLogin();
	}

	public int getSmbTimeout()
	{
		return crawlerConfig.getSmbTimeout();
	}

	public void setSmbTimeout(int timeout)
	{
		crawlerConfig.setSmbTimeout(timeout);
	}

	public int getMaxClauseCount()
	{
		return maxClauseCount;
	}

	public void setMaxClauseCount(int maxClauseCount)
	{
		this.maxClauseCount = maxClauseCount;
	}

	public void setFtpDefaultEncoding(String ftpDefaultEnc)
	{
		crawlerConfig.setFtpDefaultEncoding(ftpDefaultEnc);
	}

	public String getFtpDefaultEncoding()
	{
		return crawlerConfig.getFtpDefaultEncoding();
	}

	public void setFtpCustomEncodings(String encString)
	{
		crawlerConfig.setFtpCustomEncodings(encString);
	}

	public Map<String, String> getFtpCustomEncodings()
	{
		return crawlerConfig.getFtpCustomEncodings();
	}

	public void setFtpDefaultMode(String mode)
	{
		crawlerConfig.setFtpDefaultMode(mode);
	}

	public String getFtpDefaultMode()
	{
		return crawlerConfig.getFtpDefaultMode();
	}

	public void setFtpCustomModes(String modString)
	{
		crawlerConfig.setFtpCustomModes(modString);
	}

	public Map<String, String> getFtpCustomModes()
	{
		return crawlerConfig.getFtpCustomModes();
	}

	public String getGoogleAnalyticsId()
	{
		return googleAnalyticsId;
	}

	public void setGoogleAnalyticsId(String googleAnalyticsId)
	{
		this.googleAnalyticsId = googleAnalyticsId;
	}

	public boolean isFastSearch()
	{
		return fastSearch;
	}

	public void setFastSearch(boolean fastSearch)
	{
		this.fastSearch = fastSearch;
	}

	public int getFtpTimeout()
	{
		return crawlerConfig.getFtpTimeout();
	}

	public void setFtpTimeout(int ftpTimeout)
	{
		crawlerConfig.setFtpTimeout(ftpTimeout);
	}
}
