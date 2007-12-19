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
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import org.apache.lucene.document.Document;
import org.punksearch.commons.IndexFields;
import org.punksearch.commons.OnlineChecker;
import org.punksearch.commons.SearcherException;

/**
 * Class for smb indexing and storing information
 */
public class SmbIndexer extends ProtocolIndexer
{
	private static Logger	__log	= Logger.getLogger(SmbIndexer.class.getName());

	private String			ip;
	private int				maxDeep	= 5;
	private NtlmPasswordAuthentication auth = null;
	
	/**
	 * Constructor
	 * @param ip - ip for indexing (for ex., 10.20.0.155)
	 * @throws IllegalArgumentException IP must not be null
	 * @throws MalformedURLException connection to smb failed
	 * @throws SmbException 
	 */
	public SmbIndexer(int maxDeep, NtlmPasswordAuthentication auth) throws IllegalArgumentException
	{
		this.maxDeep = maxDeep;
		this.auth = auth;
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
			String tempDirName = dir.getName(); //.toLowerCase();
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
			__log.warning("SMB: SmbException (" + e.toString() + ") occured on resource: " + dir.toString());
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

			String fullFileName = file.getName(); //.toLowerCase();
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
			__log.warning("SMB: SmbException (" + e.toString() + ") occured on resource: " + file.toString());
			return null;
		}
	}
	
	private boolean isGoodDirectory(SmbFile[] items) throws SmbException
	{
		boolean badFileFound = false;
		for (SmbFile item : items)
		{
			String name = item.getName();
			int    dot  = name.lastIndexOf(".");
			if ( item.isFile() && (dot > 0) )
			{
				if (isGoodExtension(name.substring(dot + 1)))
				{
					return true;
				}
				else
				{
					badFileFound = true;
				}
			}
		}
		return !badFileFound;
	}
	
	private boolean shouldIndex(SmbFile file) throws SmbException {
		if (file.getName().startsWith(".") || file.isHidden() )
		{
			return false;
		}
		if (file.isDirectory())
		{
			return true;
		}
		return isGoodExtension(getExtension(file)); // files without extension are bad
	}
	
	private String getExtension(SmbFile file)
	{
		String name = file.getName();
		int    dot  = name.lastIndexOf(".");
		if (dot > 0)
		{
			return name.substring(dot + 1);
		}
		else
		{
			return "";
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
	private long indexDirectoryContents(SmbFile dir, int deep) throws IOException
	{
		if (deep > maxDeep)
		{
			return 0L;
		}
		
		SmbFile[] items = null;
		try {
			items = dir.listFiles();
		} catch (SmbAuthException e) {
			__log.info("SMB: restricted directory: " + dir.getPath());
			return 0L;
		} catch (SmbException e) {
			__log.info("SMB: exception (" + e.getMessage() + ") occured while indexing directory: " + dir.getPath());
			return 0L;
		}

		if (!isGoodDirectory(items))
		{
			return 0L;
		}

		// start actual indexing
		long size = 0L;
		List<Document> documentList = new ArrayList<Document>();

		for (SmbFile file : items)
		{
			//__log.info("SMB: " + file.getCanonicalPath());
			if (!shouldIndex(file))
			{
				continue;
			}
			
			Document doc = processResource(file, deep);

			if (doc != null)
			{
				documentList.add(doc);
				size += Long.parseLong(doc.get(IndexFields.SIZE));
			}
		}

		IndexOperator.getInstance().addDocuments(documentList);

		return size;
	}

	private Document processResource(SmbFile file, int deep) throws IOException
	{
		Document doc = null;

		if (file.isDirectory())
		{
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
	public long index(String ip)
	{
		return index(ip, true);
	}
	
	public long index(String ip, boolean checkOnline)
	{
		if (ip == null)
		{
			throw new IllegalArgumentException("IP must not be null");
		}

		this.ip = ip;

		if (checkOnline && !OnlineChecker.isActiveSmb(ip))
		{
			return 0L;
		}
		
		SmbFile smb = null;
		try
		{
			smb = (auth == null)? new SmbFile("smb://" + ip + "/") : new SmbFile("smb://" + ip + "/", auth);
			return indexDirectoryContents(smb, 0);
		}
		catch (IOException e)
		{
			__log.info("SMB: Exception occured: " + e.getMessage() + " during indexing host " + ip);
			e.printStackTrace();
			return 0L;
		}
		finally
		{
			// TODO: disconnect?
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
