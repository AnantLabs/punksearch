/**
 * SmbIndexer.java
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
import java.util.ArrayList;
import java.util.List;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import ru.spbu.dorms.arpm.commons.SearcherConfig;
import ru.spbu.dorms.arpm.commons.SearcherConstants;
import ru.spbu.dorms.arpm.commons.SearcherException;

/**
 * Class for smb indexing and storing information
 */
public class SmbIndexer
{
	private static Logger	__log	= Logger.getLogger(SmbIndexer.class);

	private static int		PORT	= 139;

	private String			ip;
	private SmbFile			smb		= null;
	
	private String[] goodExtensions = {
			"avi", "mov", "mpg", "vob", "wmv", "wmf", 
			"mp3", "wav", "ogg",
			"exe", 
			"iso", "bin", "dmg", "mdf",
			"txt", "xml", "doc", "rtf", "xls", "pdf", "ps",
			"jpg", "gif", "png",
			"zip", "rar", "tar", "gz", "tgz"};

	/**
	 * Constructor
	 * @param ip - ip for indexing (for ex., 10.20.0.155)
	 * @throws IllegalArgumentException IP must not be null
	 * @throws MalformedURLException connection to smb failed
	 * @throws SmbException 
	 */
	public SmbIndexer(String ip) throws IllegalArgumentException, MalformedURLException, SmbException
	{
		if (ip == null)
		{
			throw new IllegalArgumentException("IP must not be null");
		}
		this.ip = ip;

		if (!SearcherConfig.getInstance().getSmbUser().equals(""))
		{
			NtlmPasswordAuthentication pa = new NtlmPasswordAuthentication(SearcherConfig.getInstance().getSmbDomain(), SearcherConfig.getInstance().getSmbUser(), SearcherConfig.getInstance().getSmbPassword());
			smb = new SmbFile("smb://" + ip + "/", pa);
			/*
			 try
			 {
			 smb.listFiles();
			 }
			 catch (SmbAuthException e)
			 {
			 smb = null;
			 }
			 */
		}
		else
		{
			smb = new SmbFile("smb://" + ip + "/");
		}

	}

	/**
	 * Creates <code>Document</code> instance and adds it to <code>documentList</code>
	 * @param name
	 * @param extension
	 * @param size
	 * @param path
	 */
	private Document makeDocument(String name, String ext, String size, String path, String date, float boost)
	{
		Document document = new Document();
		document.add(new Field(SearcherConstants.TYPE, "smb", Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(SearcherConstants.HOST, ip, Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(SearcherConstants.NAME, name, Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(SearcherConstants.EXTENSION, ext, Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(SearcherConstants.SIZE, size, Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(SearcherConstants.PATH, path, Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(SearcherConstants.DATE, date, Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.setBoost(boost);
		return document;
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
			String dirExtension = SearcherConstants.DIRECTORY_EXTENSION;
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
			__log.error("SmbException (" + e.toString() + ") occured on resource: " + dir.toString());
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
			__log.error("SmbException (" + e.toString() + ") occured on resource: " + file.toString());
			return null;
		}
	}

	private boolean isGoodExtension(String ext)
	{
		for (String goodExt : goodExtensions)
		{
			if (ext.equalsIgnoreCase(goodExt))
				return true;
		}
		return false;
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
		
		boolean goodFileFound = false;
		boolean badFileFound  = false;
		for (SmbFile item : items)
		{
			try
			{
				if (item.isFile() && !item.getName().startsWith("."))
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
			return 0L;
		
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
				size += Long.parseLong(doc.get(SearcherConstants.SIZE));
			}
		}

		try
		{
			IndexerOperator.getInstance().addDocuments(documentList);
		}
		catch (IOException e)
		{
			__log.error("IOException (" + e.toString() + ") during adding documents into index");
			throw new SearcherException("Can't add documents to index.", e);
		}

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
			if (deep > SearcherConfig.getInstance().getIndexDeep() || file.getName().contains("$"))
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
	 * Indexes smb
	 * @throws IllegalArgumentException too big file size (see <code>NumberUtils</code> class)
	 * @throws SearcherException Failed adding documents in index
	 * @throws SmbException error processing with dir
	 */
	public long index() throws SearcherException, SmbException
	{
		if (isActive(ip))
		{
			return indexDirectoryContents(smb, 0);
		}
		else
		{
			return 0L;
		}
	}

	public static boolean isActive(String ip)
	{
		try
		{
			SocketAddress sockaddr = new InetSocketAddress(ip, PORT);
			Socket s = new Socket();
			s.connect(sockaddr, 1000);
			s.close();
			return true;
		}
		catch (SocketException e)
		{
			return false;
		}
		catch (IOException e)
		{
			return false;
		}
	}
}
