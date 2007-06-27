package org.punksearch.indexer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.punksearch.commons.SearcherConfig;
import org.punksearch.ip.Ip;
import org.punksearch.ip.IpIterator;


public class Indexer implements Runnable
{
	private static Logger	__log		= Logger.getLogger(Indexer.class.getName());
	private static Indexer	instance	= null;
	
	private IpIterator ipIterator;
	private List<IndexerThread> threadList = new ArrayList<IndexerThread>();	
	
	public static Indexer getInstance()
	{
		if (null == instance)
			instance = new Indexer();
		return instance;
	}
	
	public void run()
	{
		//System.setProperty("jcifs.smb.client.responseTimeout", Integer.toString(SearcherConfig.getInstance().getSmbTimeout()));
		//System.setProperty("jcifs.smb.client.soTimeout", "6000");		
		ipIterator = new IpIterator(SearcherConfig.getInstance().getIpRanges());
		threadList.clear();
		
		try
		{
			IndexerOperator.init();
			
			long startTime = new Date().getTime();
			__log.info("Indexing process started: " + SearcherConfig.getInstance().getIpRangesString());
			
			for (int i = 0; i < SearcherConfig.getInstance().getIndexThreads(); i++)
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
			//IndexerOperator.getInstance().optimizeIndex();
			//IndexerOperator.getInstance().flushIndex();

			long finishTime = new Date().getTime();
			__log.info("Index process is finished in " + ((finishTime - startTime) / 1000) + " sec");
		}
		catch (Exception e)
		{
			__log.warning("Indexer.run(): exception occured. " + e.getMessage());
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
