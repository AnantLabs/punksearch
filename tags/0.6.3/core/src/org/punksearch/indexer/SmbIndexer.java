/**
 * SmbIndexer.java Created on 25.06.2006 Author Evgeny Shiriaev Email
 * arpmipg@gmail.com
 */

package org.punksearch.indexer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import org.apache.lucene.document.Document;
import org.punksearch.commons.IndexFields;
import org.punksearch.commons.SearcherException;

/**
 * Class for smb indexing and storing information
 */
public class SmbIndexer extends ProtocolIndexer
{
	private static Logger	__log	= Logger.getLogger(SmbIndexer.class.getName());

	private String			ip;
	private int				maxDeep	= 5;

	/**
	 * Constructor
	 * @param ip - ip for indexing (for ex., 10.20.0.155)
	 * @throws IllegalArgumentException IP must not be null
	 * @throws MalformedURLException connection to smb failed
	 * @throws SmbException 
	 */
	public SmbIndexer() throws IllegalArgumentException
	{
	}

	/**
	 * Indexes directory (without parsing)
	 * @param dir
	 * @param dirSize
	 * @throws SmbException error processing with directory
	 */
	private Document makeDirDocument(SmbFile dir, long dirSize)
	{
		try
		{
			if (!dir.canRead())
			{
				return null;
			}
			String tempDirName = dir.getName().toLowerCase();
			String dirName = tempDirName.substring(0, tempDirName.length() - 1);
			String dirExtension = IndexFields.DIRECTORY_EXTENSION;
			String dirSizeStr = Long.toString(dirSize);
			String tempDirPath = dir.getPath().toLowerCase();
			String dirPath = tempDirPath.substring(ip.length() + 7, tempDirPath.length() - 1 - dirName.length());
			String lastModified = Long.toString(dir.getLastModified());

			String[] pathParts = dirPath.split("/");
			float boost = 1.0f;
			for (int i = 0; i < pathParts.length; i++)
			{
				boost /= 2;
			}
			boost *= dirSize / 1000.0f;
			//float boost = 100000/(pathParts.length*50 + dirName.length()*100) + dirSize/1000000;

			return makeDocument(dirName, dirExtension, dirSizeStr, dirPath, lastModified, boost);
		}
		catch (SmbException e)
		{
			__log.warning("SmbException (" + e.toString() + ") occured on resource: " + dir.toString());
			return null;
		}
	}

	/**
	 * Indexes standalone file
	 * @param file instance of SmbFile for indexing
	 * @return size of indexed file
	 * @throws IllegalArgumentException too big file size (see <code>NumberUtils</code> class)
	 * @throws SmbException error processing with file
	 */
	private Document makeFileDocument(SmbFile file)
	{
		try
		{
			if (!file.canRead())
			{
				return null;
			}

			String fullFileName = file.getName().toLowerCase();
			int dotIndex = fullFileName.lastIndexOf('.');

			String fileName = (dotIndex != -1) ? fullFileName.substring(0, dotIndex) : fullFileName;
			String fileExt = (dotIndex != -1) ? fullFileName.substring(dotIndex + 1, fullFileName.length()) : "";
			String fileSize = Long.toString(file.length());

			String filePath = file.getPath().toLowerCase();
			int lastSlashIndex = filePath.lastIndexOf('/');
			String fileFolderPath = filePath.substring(ip.length() + 7, lastSlashIndex + 1);

			String lastModified = Long.toString(file.getLastModified());

			String[] pathParts = fileFolderPath.split("/");
			float boost = 1.0f;
			for (int i = 0; i < pathParts.length; i++)
			{
				boost /= 2;
			}

			return makeDocument(fileName, fileExt, fileSize, fileFolderPath, lastModified, boost);
		}
		catch (SmbException e)
		{
			__log.warning("SmbException (" + e.toString() + ") occured on resource: " + file.toString());
			return null;
		}
	}

	private boolean isGoodDirectory(SmbFile[] items)
	{
		boolean goodFileFound = false;
		boolean badFileFound = false;
		for (SmbFile item : items)
		{
			try
			{
				if (item.isFile() && !item.getName().startsWith("."))
				{
					String ext = item.getName().substring(item.getName().lastIndexOf(".") + 1);
					if (isGoodExtension(ext))
					{
						goodFileFound = true;
						break;
					}
					else
					{
						badFileFound = true;
					}
				}
			}
			catch (Exception e)
			{
				__log.info("Error processing resource: " + item.toString() + ". " + e.getMessage());
			}
		}

		if (!goodFileFound && badFileFound)
		{
			return false;
		}
		else
		{
			return true;
		}

	}

	/**
	 * Processes a directory
	 * @param dir instance of SmbFile for parsing
	 * @return size of processed directory.
	 * @throws IllegalArgumentException too big file or directory size (see <code>NumberUtils</code> class)
	 * @throws SearcherException Failed adding documents in index
	 * @throws SmbException 
	 * @throws NumberFormatException 
	 * @throws IOException 
	 */
	private long indexDirectoryContents(SmbFile dir, int deep) throws SearcherException, SmbException
	{
		SmbFile[] items = dir.listFiles();

		if (!isGoodDirectory(items))
		{
			return 0L;
		}

		// start actual indexing
		long size = 0L;
		List<Document> documentList = new ArrayList<Document>();

		for (SmbFile file : items)
		{
			//__log.debug(file.getPath());
			Document doc = null;
			try
			{
				doc = processResource(file, deep);
			}
			catch (Exception e)
			{
				__log.info("Error processing resource: " + file.toString() + ". " + e.getMessage());
				if (!dir.canRead())
				{
					break;
				}
			}

			if (doc != null)
			{
				documentList.add(doc);
				size += Long.parseLong(doc.get(IndexFields.SIZE));
			}
		}

		IndexOperator.getInstance().addDocuments(documentList);

		return size;
	}

	private Document processResource(SmbFile file, int deep) throws SearcherException, SmbException
	{
		Document doc = null;

		if (file.getName().startsWith("."))
		{
			return null;
		}

		if (file.isDirectory())
		{
			if (deep > maxDeep || file.getName().contains("$"))
			{
				return null;
			}
			long size = indexDirectoryContents(file, deep + 1);
			if (size != 0L)
			{
				doc = makeDirDocument(file, size);
			}
		}
		else if (file.isFile())
		{
			doc = makeFileDocument(file);
		}

		return doc;
	}

	/**
	 * Indexes particular smb host
	 * 
	 * @throws IllegalArgumentException too big file size (see <code>NumberUtils</code> class)
	 * @throws SearcherException Failed adding documents in index
	 * @throws SmbException error processing with dir
	 * @throws MalformedURLException 
	 */
	public long index(String ip, CrawlerConfig config) throws SearcherException, MalformedURLException
	{
		if (ip == null)
		{
			throw new IllegalArgumentException("IP must not be null");
		}

		this.ip = ip;
		this.maxDeep = config.getIndexDeep();

		if (isActive(ip, 445) || isActive(ip, 139))
		{
			SmbFile smb;

			if (!config.getSmbUser().equals(""))
			{
				NtlmPasswordAuthentication pa = new NtlmPasswordAuthentication(config.getSmbDomain(), config.getSmbUser(), config.getSmbPassword());
				smb = new SmbFile("smb://" + ip + "/", pa);
			}
			else
			{
				smb = new SmbFile("smb://" + ip + "/");
			}

			try
			{
				return indexDirectoryContents(smb, 0);
			}
			catch (SmbException se)
			{
				throw new SearcherException(se);
			}
		}
		else
		{
			return 0L;
		}
	}

	@Override
	protected String getIp()
	{
		return ip;
	}

	@Override
	protected String getProtocol()
	{
		return "smb";
	}
}
