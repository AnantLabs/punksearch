/**
 * IndexerThread.java
 * Created on 25.06.2006
 * Author     Evgeny Shiriaev
 * Email      arpmipg@gmail.com
 */

package ru.spbu.dorms.arpm.indexer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

import jcifs.smb.SmbException;

import org.apache.log4j.Logger;

import ru.spbu.dorms.arpm.commons.SearcherException;

/**
 * Class represents indexer thread
 */
public class IndexerThread extends Thread
{
	private Logger __log = Logger.getLogger(IndexerThread.class);

	private String ip;
	
	public IndexerThread(String name)
	{
		super(name);
	}
	
	/**
	 * Indexes net (smb and ftp)
	 */
	public void run()
	{
		while ((ip = Indexer.getInstance().nextIp()) != null)
		{
			try
			{
				__log.debug(ip + " start indexing");
				
				IndexerOperator.getInstance().deleteDocuments(ip);
				SmbIndexer smbIndexer = new SmbIndexer(ip);
				long size = smbIndexer.index();
				
				if (size > 0)
				{
					__log.debug("SMB: " + ip + " indexed: " + size + " bytes");
				}
				/*
				FtpIndexer ftpIndexer = new FtpIndexer(ip);
				String ftpHost = "ftp://" + ip;
				__log.debug(ftpHost + " is indexing");
				IndexerOperator.getInstance().deleteDocuments(ftpHost);
				ftpIndexer.performIndexing();
				__log.debug(ftpHost + " was indexed");
				*/
				//IndexerOperator.getInstance().optimizeIndex();
				IndexerOperator.getInstance().flushIndex();
			}
			catch (IllegalArgumentException e)
			{
				__log.error("IAE: " + e);
			}
			catch (MalformedURLException e)
			{
				__log.error("MUE: " + e);
			}
			catch (SearcherException e)
			{
				__log.error("SE: " + e);
			}
			catch (SmbException e)
			{
				String reason = e.getMessage();
				
				//String reason = (e.getCause() == null)? "unknown reason" : e.getCause().getMessage();
				__log.info("Indexing of smb host " + ip + " failed due to: " + reason);
			}
			catch (IOException e)
			{
				__log.error("Can't flush index! "+e.getMessage(), e);
			}
		}
	}
	
	public String getIp()
	{
		return ip;
	}
}
