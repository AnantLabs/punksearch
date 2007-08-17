package org.punksearch.indexer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.punksearch.ip.Ip;
import org.punksearch.ip.IpIterator;
import org.punksearch.ip.IpRange;

public class Indexer implements Runnable
{
	private static Logger		__log		= Logger.getLogger(Indexer.class.getName());
	private static Indexer		instance	= null;

	private String				indexDirectory;
	private List<IpRange>		ipRanges;
	private int					threadCount;

	private IpIterator			ipIterator;
	private List<IndexerThread>	threadList	= new ArrayList<IndexerThread>();

	public static Indexer getInstance()
	{
		if (instance == null)
		{
			instance = new Indexer();
		}
		return instance;
	}

	private Indexer()
	{
	};

	public void init(String indexDir, List<IpRange> ipRanges, int threadCount)
	{
		this.indexDirectory = indexDir;
		this.ipRanges = ipRanges;
		this.threadCount = threadCount;
	}

	public void run()
	{
		//System.setProperty("jcifs.smb.client.responseTimeout", Integer.toString(SearcherConfig.getInstance().getSmbTimeout()));
		//System.setProperty("jcifs.smb.client.soTimeout", "6000");		
		ipIterator = new IpIterator(ipRanges);
		threadList.clear();

		try
		{
			IndexOperator.init(indexDirectory);

			long startTime = new Date().getTime();
			__log.info("Indexing process started");

			for (int i = 0; i < threadCount; i++)
			{
				IndexerThread indexerThread = new IndexerThread("IndexerThread" + i);
				//indexerThread.setDaemon(true);
				indexerThread.start();
				threadList.add(indexerThread);
			}
			for (Thread indexerThread : threadList)
			{
				indexerThread.join();
			}

			IndexOperator.getInstance().optimizeIndex();
			IndexOperator.getInstance().flushIndex();

			long finishTime = new Date().getTime();
			__log.info("Index process is finished in " + ((finishTime - startTime) / 1000) + " sec");
		}
		catch (Exception e)
		{
			__log.warning("Indexer.run(): exception occured. " + e.getMessage());
		}
		finally
		{
			IndexOperator.close();
		}
	}

	public void stop()
	{
		ipIterator = null;
	}

	public synchronized String nextIp()
	{
		if (ipIterator != null && ipIterator.hasNext())
		{
			Ip ip = ipIterator.next();
			return ip.toString();
		}
		return null;
	}

	public List<IndexerThread> getThreads()
	{
		return threadList;
	}

	/*
	public float getProgress()
	{
		List<Long> startIps  = IndexerConfig.getInstance().getStartIpList();
		List<Long> finishIps = IndexerConfig.getInstance().getFinishIpList();
		
		long total = 0;
		long passed = 0;
		for (int i = 0; i < startIps.size(); i++)
		{
			if (startIps.get(i) <= currentIp && currentIp <= finishIps.get(i))
			{
				passed = total + currentIp - startIps.get(i) + 1;
			}
			total += finishIps.get(i) - startIps.get(i) + 1;
		}
		return (passed + 0.0f) / total;
	}
	*/

}
