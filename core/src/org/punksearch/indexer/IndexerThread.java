/**
 * IndexerThread.java Created on 25.06.2006 Author Evgeny Shiriaev Email
 * arpmipg@gmail.com
 */

package org.punksearch.indexer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Logger;

import org.punksearch.commons.SearcherException;

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
		SmbIndexer smbIndexer = new SmbIndexer();
		FtpIndexer ftpIndexer = new FtpIndexer();

		while ((ip = iterator.next()) != null)
		{
			try
			{
				__log.info(ip + " start indexing");

				IndexOperator.getInstance().deleteDocuments(ip, "smb");
				IndexOperator.getInstance().deleteDocuments(ip, "ftp");

				long sizeSmb = smbIndexer.index(ip, crawlerConfig);
				if (sizeSmb > 0)
				{
					__log.info("SMB: " + ip + " indexed: " + sizeSmb + " bytes");
				}

				long sizeFtp = ftpIndexer.index(ip, crawlerConfig);
				if (sizeFtp > 0)
				{
					__log.info("FTP: " + ip + " indexed: " + sizeFtp + " bytes");
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
			catch (SearcherException e)
			{
				__log.warning("SE: " + e);
			}
			catch (IOException e)
			{
				__log.warning("Can't flush index! " + e.getMessage());
			}
		}
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
