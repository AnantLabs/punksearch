/**
 * IndexerThread.java Created on 25.06.2006 Author Evgeny Shiriaev Email
 * arpmipg@gmail.com
 */

package org.punksearch.indexer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.logging.Logger;

import jcifs.smb.NtlmPasswordAuthentication;

import org.punksearch.commons.OnlineChecker;

/**
 * Class represents indexer thread
 */
public class IndexerThread extends Thread
{
	private Logger				__log	= Logger.getLogger(IndexerThread.class.getName());

	private String				ip;
	private CrawlerConfig		crawlerConfig;
	private IpIteratorWrapper	iterator;

	public IndexerThread(String name, CrawlerConfig conf, IpIteratorWrapper iter)
	{
		super(name);
		crawlerConfig = conf;
		this.iterator = iter;
	}

	/**
	 * Indexes net (smb and ftp)
	 */
	public void run()
	{
		SmbIndexer smbIndexer = new SmbIndexer(crawlerConfig.getIndexDeep(), getSmbAuth());
		FtpIndexer ftpIndexer = new FtpIndexer(crawlerConfig.getIndexDeep(), crawlerConfig.getFtpTimeout());

		while ((ip = iterator.next()) != null)
		{
			try
			{
				__log.info(ip + " start indexing");

				IndexOperator.getInstance().deleteDocuments(ip, "smb");
				IndexOperator.getInstance().deleteDocuments(ip, "ftp");
				
				if (canIndexSmb(ip))
				{
    				long sizeSmb = smbIndexer.index(ip, false);
    				if (sizeSmb > 0)
    				{
    					__log.info("SMB: " + ip + " indexed: " + sizeSmb + " bytes");
    				}
				}
				
				if (canIndexFtp(ip))
				{
    				ftpIndexer.setMode(getFtpModeForIp(ip));
    				ftpIndexer.setEncoding(getFtpEncodingForIp(ip));
    				long sizeFtp = ftpIndexer.index(ip, false);
    				if (sizeFtp > 0)
    				{
    					__log.info("FTP: " + ip + " indexed: " + sizeFtp + " bytes");
    				}
				}

				IndexOperator.getInstance().flushIndex();
			}
			catch (IllegalArgumentException e)
			{
				__log.warning("IAE: " + e);
			}
			catch (MalformedURLException e)
			{
				__log.warning("MUE: " + e);
			}
			catch (IOException e)
			{
				__log.info("IOException: " + e.getMessage() + " on ip='" + ip + "'");
			}
		}
	}

	private NtlmPasswordAuthentication getSmbAuth()
	{
		if (crawlerConfig.getSmbUser().length() > 0)
		{
			return new NtlmPasswordAuthentication(crawlerConfig.getSmbDomain(), crawlerConfig.getSmbUser(), crawlerConfig.getSmbPassword());
		}
		else
		{
			return null;
		}
	}
	
	private String getFtpEncodingForIp(String ip)
	{
		Map<String, String> customEncodings = crawlerConfig.getFtpCustomEncodings();
		return (customEncodings.containsKey(ip))? customEncodings.get(ip) : crawlerConfig.getFtpDefaultEncoding();
	}

	private FtpIndexer.MODE getFtpModeForIp(String ip)
	{
		Map<String, String> customModes = crawlerConfig.getFtpCustomModes();
		String modeStr = (customModes.containsKey(ip))? customModes.get(ip) : crawlerConfig.getFtpDefaultMode();
		return (modeStr.equals("active"))? FtpIndexer.MODE.active : FtpIndexer.MODE.passive;
	}	
	
	private boolean canIndexSmb(String ip)
	{
		return OnlineChecker.isActiveSmb(ip);
	}
	
	private boolean canIndexFtp(String ip)
	{
		return OnlineChecker.isActiveFtp(ip);
	}
	
	public void finish()
	{
		iterator = null;
	}

	public String getIp()
	{
		return ip;
	}
}
