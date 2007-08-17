/**
 * FtpIndexer.java
 * Created on 25.06.2006
 * Author     Evgeny Shiriaev
 * Email      arpmipg@gmail.com
 */

package org.punksearch.indexer;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.lucene.document.Document;
import org.punksearch.commons.SearcherConfig;
import org.punksearch.commons.IndexFields;
import org.punksearch.commons.SearcherException;

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
	
	private FTPClient ftp = new FTPClient();
	private String ip;

	/**
	 * Constructor
	 * @param ip - ip for indexing (for ex., 127.0.0.1)
	 * @throws IllegalArgumentException IP must not be null
	 */
	public FtpIndexer(String ip) throws IllegalArgumentException
	{
		if (ip == null)
		{
			throw new IllegalArgumentException("IP must not be null");
		}
		this.ip = ip;
	}

	/**
	 * Indexes standalone file
	 * @param file instance of FTPFile for indexing
	 * @param path
	 * @throws FTPException 
	 * @throws IOException 
	 * @throws IllegalArgumentException too big file size (see <code>NumberUtils</code> class)
	 */
	/*
	private long indexFile(FTPFile file, String path) throws IllegalArgumentException
	{
		if (! file.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION))
		{
			return 0L;
		}
		String fullFileName = file.getName().toLowerCase();
		int dotIndex = fullFileName.lastIndexOf('.');
		String fileName = (dotIndex != -1) ? fullFileName.substring(0, dotIndex) : fullFileName;
		String fileExtension = (dotIndex != -1) ? fullFileName.substring(dotIndex + 1, fullFileName.length()) : "";
		long fileSize = file.getSize();
		String fileSizeStr = NumberUtils.pad(fileSize);

		addDocumentToList(fileName, fileExtension, fileSizeStr, host + path);

		return fileSize;
	}
	*/
	
	private Document makeDirDocument(FTPFile dir, long dirSize) throws IOException, FTPException
	{
		/*
		if (!dir.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION))
		{
			return null;
		}
		*/
		String fullDirName = ftp.pwd().toLowerCase();
		//fullDirName = fullDirName.substring(0, fullDirName.length() - 1); // cut last "/"
		
		int lastSlash = fullDirName.lastIndexOf("/");
		
		String dirName = fullDirName.substring(lastSlash + 1);
		String dirPath = (lastSlash > 1)? fullDirName.substring(1, lastSlash) : "";
		
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
	private Document makeFileDocument(FTPFile file) throws IOException, FTPException
	{
		/*
		if (!file.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION))
		{
			return null;
		}
		*/

		String fullFileName = file.getName().toLowerCase(); // not from root, but both file name and file extension
		int    dotIndex     = fullFileName.lastIndexOf('.');
		
		String fileName = (dotIndex > 0) ? fullFileName.substring(0, dotIndex) : fullFileName;
		String fileExt  = (dotIndex > 0) ? fullFileName.substring(dotIndex + 1, fullFileName.length()) : "";
		String fileSize = Long.toString(file.size());

		String lastModified = Long.toString(file.lastModified().getTime());

		String fileFolderPath = ftp.pwd();
		String[] pathParts = fileFolderPath.split("/");
		float boost = 1.0f;
		for (int i = 0; i < pathParts.length; i++)
		{
			boost /= 2;
		}

		return makeDocument(fileName, fileExt, fileSize, fileFolderPath, lastModified, boost);
	}
	
	
	
	private Document processResource(String root, FTPFile file, int deep) throws SearcherException, FTPException, ParseException, IOException
	{
		Document doc = null;

		if (file.getName().startsWith("."))
		{
			return null;
		}

		if (file.isDir())
		{
			if (deep > SearcherConfig.getInstance().getIndexDeep() || file.getName().contains("$"))
			{
				return null;
			}
			long size = indexDirectoryContents(root + "/" + file.getName(), deep + 1);
			if (size != 0L)
			{
				doc = makeDirDocument(file, size);
			}
		}
		else if (!file.isLink())
		{
			doc = makeFileDocument(file);
		}

		return doc;
	}
	
	private boolean isGoodDirectory(FTPFile[] items)
	{
		boolean goodFileFound = false;
		boolean badFileFound  = false;
		for (FTPFile item : items)
		{
			try
			{
				if (!item.isDir() && !item.getName().startsWith("."))
				{
					String ext = item.getName().substring(item.getName().lastIndexOf(".")+1);
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
	
	private long indexDirectoryContents(String dir, int deep) throws SearcherException, IOException, FTPException, ParseException
	{
		try
		{
			ftp.chdir(dir);
		}
		catch (Exception e)
		{
			__log.info("Exception (" + e.toString() + ") during changing working directory to: " + dir);
			return 0L;
		}
		
		
		FTPFile[] items = {};
		
		try
		{
			items = ftp.dirDetails(dir);
		}
		catch (Exception e)
		{
			__log.info("Exception (" + e.toString() + ") during listing directory: " + dir);
			return 0L;
		}
		
		if (!isGoodDirectory(items))
		{
			return 0L;
		}
		
		// start actual indexing
		long size = 0L;
		List<Document> documentList = new ArrayList<Document>();

		for (FTPFile file : items)
		{
			Document doc = null;
			try
			{
				doc = processResource(dir, file, deep);
				ftp.chdir(dir);
			}
			catch (Exception e)
			{
				__log.info("Error processing resource: ftp://" + ip + dir + "/" + file.getName() + ". " + e.getMessage());
				e.printStackTrace();
			}

			if (doc != null)
			{
				documentList.add(doc);
				size += Long.parseLong(doc.get(IndexFields.SIZE));
			}
		}

		IndexerOperator.getInstance().addDocuments(documentList);

		return size;
	}
	
	
	public long index() throws SearcherException
	{
		long result = 0L;
		if (isActive(ip))
		{
			try
			{
				setupControlEncoding();
				setupConnectMode();
				ftp.setRemoteHost(ip);
				ftp.setTimeout(SearcherConfig.getInstance().getFtpTimeout());
				ftp.connect();
				
				ftp.login("anonymous", "arpmipg@gmail.com");
				
				result = indexDirectoryContents("/", 0);
			}
			catch (Exception e)
			{
				__log.info("FTP: Exception (" + e.toString() + ") during indexing server: " + ip);
			}
			finally
			{
				try
				{
					 if (ftp.connected()) ftp.quit();
				}
				catch (Exception e)
				{
					__log.info("Can't disconnect from ftp server: " + ip);
				}
			}
		}
		return result;
	}

	private void setupControlEncoding() throws FTPException
	{
		Map<String, String> customEncodings = SearcherConfig.getInstance().getFtpCustomEncodings();
		if (customEncodings.containsKey(ip))
		{
			ftp.setControlEncoding(customEncodings.get(ip));
		}
		else
		{
			ftp.setControlEncoding(SearcherConfig.getInstance().getFtpDefaultEncoding());
		}
	}
	
	private void setupConnectMode()
	{
		String mode;
		
		Map<String, String> customModes = SearcherConfig.getInstance().getFtpCustomModes();
		if (customModes.containsKey(ip))
		{
			mode = customModes.get(ip);
		}
		else
		{
			mode = SearcherConfig.getInstance().getFtpDefaultMode();
		}
		
		if (mode.equalsIgnoreCase("active"))
		{
			ftp.setConnectMode(FTPConnectMode.ACTIVE);
		}
		else
		{
			ftp.setConnectMode(FTPConnectMode.PASV);
		}
	}
	
	@Override
	protected String getIp()
	{
		return ip;
	}

	@Override
	protected int getPort()
	{
		return 21;
	}

	@Override
	protected String getProtocol()
	{
		return "ftp";
	}

	
}
