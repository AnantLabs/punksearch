/**
 * IndexerThread.java
 * Created on 25.06.2006
 * Author     Evgeny Shiriaev
 * Email      arpmipg@gmail.com
 */

package org.punksearch.indexer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Logger;

import jcifs.smb.SmbException;

import org.punksearch.commons.SearcherException;

/**
 * Class represents indexer thread
 */
public class IndexerThread extends Thread
{
	private Logger	__log	= Logger.getLogger(IndexerThread.class.getName());

	private String	ip;

	public IndexerThread(String name)
	{
		super(name);
	}

	/**
	 * Indexes net (smb and ftp)
	 */
	public void run()
	{
		while ( (ip = Indexer.getInstance().nextIp()) != null )
		{
			try
			{
				__log.info(ip + " start indexing");

				IndexerOperator.getInstance().deleteDocuments(ip, "smb");

				try
				{
					SmbIndexer smbIndexer = new SmbIndexer(ip);
					long sizeSmb = smbIndexer.index();
					if (sizeSmb > 0)
					{
						__log.info("SMB: " + ip + " indexed: " + sizeSmb + " bytes");
					}
				}
				catch (SmbException e)
				{
					String reason = (e.getRootCause() != null) ? e.getRootCause().getMessage() : e.getMessage();

					//String reason = (e.getCause() == null)? "unknown reason" : e.getCause().getMessage();
					__log.info("Indexing of smb host " + ip + " failed due to: " + reason);
				}

				IndexerOperator.getInstance().deleteDocuments(ip, "ftp");
				FtpIndexer ftpIndexer = new FtpIndexer(ip);
				long sizeFtp = ftpIndexer.index();
				if (sizeFtp > 0)
				{
					__log.info("FTP: " + ip + " indexed: " + sizeFtp + " bytes");
				}

				//IndexerOperator.getInstance().optimizeIndex();
				IndexerOperator.getInstance().flushIndex();
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

	public String getIp()
	{
		return ip;
	}
}
