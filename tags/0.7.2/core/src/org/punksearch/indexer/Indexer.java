package org.punksearch.indexer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class Indexer implements Runnable
{
	private static Logger		__log		= Logger.getLogger(Indexer.class.getName());

	private String				indexDirectory;
	private CrawlerConfig		crawlerConfig;

	private List<IndexerThread>	threadList	= new ArrayList<IndexerThread>();

	public Indexer(String indexDir, CrawlerConfig crawlerConfig)
	{
		this.indexDirectory = indexDir;
		this.crawlerConfig = crawlerConfig;
	}

	public void run()
	{
		IpIteratorWrapper iter = new IpIteratorWrapper(crawlerConfig.getIpRanges());
		threadList.clear();

		try
		{
			IndexOperator.init(indexDirectory);

			long startTime = new Date().getTime();
			__log.info("Indexing process started");

			for (int i = 0; i < crawlerConfig.getIndexThreads(); i++)
			{
				IndexerThread indexerThread = new IndexerThread("IndexerThread" + i, crawlerConfig, iter);
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
		for (Thread thread : threadList)
		{
			thread.interrupt();
		}
	}

	public List<IndexerThread> getThreads()
	{
		return threadList;
	}

}
