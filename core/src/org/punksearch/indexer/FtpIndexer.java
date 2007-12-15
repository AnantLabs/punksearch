/**
 * FtpIndexer.java Created on 25.06.2006 Author Evgeny Shiriaev Email
 * arpmipg@gmail.com
 */

package org.punksearch.indexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.lucene.document.Document;
import org.punksearch.commons.IndexFields;
import org.punksearch.commons.OnlineChecker;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPFile;

/**
 * Class for ftp indexing and storing information
 */
public class FtpIndexer extends ProtocolIndexer
{
	private static Logger	__log	= Logger.getLogger(FtpIndexer.class.getName());

	public static enum MODE {active, passive};
	
	private FTPClient		ftp		= new FTPClient();
	
	private int				maxDeep;
	private int				timeout;
	private MODE			mode = MODE.passive;
	private String			encoding;

	/**
	 * Constructor
	 */
	public FtpIndexer(int maxDeep, int timeout) throws IllegalArgumentException
	{
		this.maxDeep = maxDeep;
		this.timeout = timeout;
	}
	
	public void setMode(MODE mode) {
		this.mode = mode;
	}
	
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
	private Document makeDirDocument(FTPFile dir, long dirSize) throws IOException
	{
		String fullDirName;
		try
		{
			fullDirName = ftp.pwd();//.toLowerCase();
		}
		catch (FTPException e)
		{
			__log.info("FTPException in makeDirDocument " + e.getMessage());
			return null;
		}
		//fullDirName = fullDirName.substring(0, fullDirName.length() - 1); // cut last "/"

		int lastSlash = fullDirName.lastIndexOf("/");

		String dirName = fullDirName.substring(lastSlash + 1);
		String dirPath = (lastSlash > 1) ? fullDirName.substring(1, lastSlash) : "";

		String dirExtension = IndexFields.DIRECTORY_EXTENSION;
		String dirSizeStr = Long.toString(dirSize);

		//String tempDirPath = dir.getPath().toLowerCase();
		String lastModified = Long.toString(dir.lastModified().getTime());

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

	/**
	 * Indexes standalone file
	 * @param file instance of SmbFile for indexing
	 * @return size of indexed file
	 * @throws FTPException 
	 * @throws IOException 
	 * @throws IllegalArgumentException too big file size (see <code>NumberUtils</code> class)
	 */
	private Document makeFileDocument(FTPFile file) throws IOException
	{
		String fileFolderPath;
		try
		{
			fileFolderPath = ftp.pwd();
		}
		catch (FTPException e)
		{
			__log.info("FTPException in makeFileDocument " + e.getMessage());
			return null;
		}

		String fullFileName = file.getName(); //.toLowerCase(); // not from root, but both file name and file extension
		int dotIndex = fullFileName.lastIndexOf('.');

		String fileName = (dotIndex > 0) ? fullFileName.substring(0, dotIndex) : fullFileName;
		String fileExt = (dotIndex > 0) ? fullFileName.substring(dotIndex + 1) : "";
		String fileSize = Long.toString(file.size());

		String lastModified = Long.toString(file.lastModified().getTime());

		String[] pathParts = fileFolderPath.split("/");
		float boost = 1.0f;
		for (int i = 0; i < pathParts.length; i++)
		{
			boost /= 2;
		}

		return makeDocument(fileName, fileExt, fileSize, fileFolderPath, lastModified, boost);
	}

	private Document processResource(String root, FTPFile file, int deep) throws IOException
	{
		Document doc = null;

		if (file.isDir())
		{
			long size = indexDirectoryContents(root + "/" + file.getName(), deep + 1);
			if (size != 0L)
			{
				doc = makeDirDocument(file, size);
			}
		}
		else
		{
			doc = makeFileDocument(file);
		}

		return doc;
	}

	private boolean isGoodDirectory(FTPFile[] items)
	{
		boolean badFileFound = false;
		for (FTPFile item : items)
		{
			String name = item.getName();
			int    dot  = name.lastIndexOf(".");
			if ( !item.isDir() && (dot > 0) )
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
	
	private boolean shouldIndex(FTPFile file) {
		if (file.getName().startsWith(".") || file.isLink())
		{
			return false;
		}
		if (file.isDir())
		{
			return true;
		}
		return isGoodExtension(getExtension(file)); // files without extension are bad
	}
	
	private String getExtension(FTPFile file)
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

	private long indexDirectoryContents(String dir, int deep) throws IOException
	{
		if (deep > maxDeep)
		{
			return 0L;
		}
		
		FTPFile[] items = {};

		try
		{
			ftp.chdir(dir); // required for pwd() in make* methods
			items = ftp.dirDetails(dir);
			if (!isGoodDirectory(items))
			{
				return 0L;
			}
		}
		catch (IOException e) {
			// host communication problem occured, rethrow the exception so indexer will give up indexing this host
			throw e;
		}
		catch (Exception e)
		{
			__log.info("Exception (" + e.getMessage() + ") during changing or listing directory: " + dir);
			return 0L;
		}

		// start actual indexing
		long size = 0L;
		List<Document> documentList = new ArrayList<Document>();

		for (FTPFile file : items)
		{
			if (!shouldIndex(file))
			{
				continue;
			}
			
			try
			{
				Document doc = processResource(dir, file, deep);
				if (doc != null)
				{
					documentList.add(doc);
					size += Long.parseLong(doc.get(IndexFields.SIZE));
				}
				ftp.chdir(dir); // rollback. required for pwd() in make* methods
			}
			catch (IOException e) {
				// host communication problem occured, rethrow the exception so indexer will give up indexing this host
				throw e;
			}
			catch (Exception e)
			{
				__log.info("Error processing resource: ftp://" + getIp() + dir + "/" + file.getName() + ". " + e.getMessage());
			}
		}

		IndexOperator.getInstance().addDocuments(documentList);

		return size;
	}
	
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
		
		if (checkOnline && !OnlineChecker.isActiveFtp(ip))
		{
			return 0L;
		}
		
		try
		{
			setupFtpClient(ip);
			ftp.connect();
			ftp.login("anonymous", "some@ema.il");
			return indexDirectoryContents("/", 0);
		}
		catch (Exception e)
		{
			__log.info("FTP: Exception (" + e.getMessage() + ") during indexing server: " + ip);
			return 0L;
		}
		finally
		{
			if (!disconnect())
			{
				__log.warning("Can't disconnect from ftp server: " + ip);
			}
		}
	}

	private boolean disconnect()
	{
		try
		{
			if (ftp.connected())
			{
				ftp.quit();
			}
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	private void setupFtpClient(String ip) throws FTPException, IOException {
		if (ftp == null)
		{
			ftp = new FTPClient();
		}
		ftp.setControlEncoding(encoding);
		if (mode == MODE.active)
		{
			ftp.setConnectMode(FTPConnectMode.ACTIVE);
		}
		else
		{
			ftp.setConnectMode(FTPConnectMode.PASV);
		}
		ftp.setRemoteHost(ip);
		ftp.setTimeout(timeout);
	}

	@Override
	protected String getIp()
	{
		return ftp.getRemoteHost();
	}

	@Override
	protected String getProtocol()
	{
		return "ftp";
	}

}
