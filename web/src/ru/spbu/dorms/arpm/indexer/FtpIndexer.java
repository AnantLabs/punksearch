/**
 * FtpIndexer.java
 * Created on 25.06.2006
 * Author     Evgeny Shiriaev
 * Email      arpmipg@gmail.com
 */

package ru.spbu.dorms.arpm.indexer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.commons.net.ftp.*;

import java.util.List;
import java.util.ArrayList;
import java.io.*;

import ru.spbu.dorms.arpm.commons.SearcherConstants;
import ru.spbu.dorms.arpm.commons.SearcherException;
import ru.spbu.dorms.arpm.utils.NumberUtils;

/**
 * Class for ftp indexing and storing information
 */
public class FtpIndexer
{
	private List<Document> documentList = new ArrayList<Document>();
	private FTPClient ftp = new FTPClient();
	private String host;
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
		host = "ftp://" + ip;
	}

	/**
	 * Creates <code>Document</code> instance and adds it to <code>documentList</code>
	 * @param name
	 * @param extension
	 * @param size
	 * @param path
	 */
	private void addDocumentToList(String name, String extension, String size, String path)
	{
		Document document = new Document();
		document.add(new Field(SearcherConstants.HOST,      host,      Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(SearcherConstants.NAME,      name,      Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(SearcherConstants.EXTENSION, extension, Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(SearcherConstants.SIZE,      size,      Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(SearcherConstants.PATH,      path,      Field.Store.YES, Field.Index.UN_TOKENIZED));
		documentList.add(document);
	}

	/**
	 * Indexes standalone file
	 * @param file instance of FTPFile for indexing
	 * @param path
	 * @throws IllegalArgumentException too big file size (see <code>NumberUtils</code> class)
	 */
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

	/**
	 * Indexes directory
	 * @param path
	 * @return size of indexed directory
	 * @throws IllegalArgumentException too big file or directory size (see <code>NumberUtils</code> class)
	 * @throws IOException error occurs while either sending a command to the server or receiving a reply from the server
	 * @throws SearcherException Failed adding documents in index
	 */
	private long parseDirectory(String path) throws IllegalArgumentException, IOException, SearcherException
	{
		// class FTPFile uses Telnet protocol. It doubles letter "ÿ" (code 255). For "ß" it's ok
		String pathInUpperCase = path.toUpperCase(); // TODO: think about it
		if (! ftp.changeWorkingDirectory(pathInUpperCase))
		{
			return 0L;
		}
		FTPFile files[] = ftp.listFiles();
		long dirSize = 0L;
		for (FTPFile file : files)
		{
			if (file.isDirectory())
			{
				String dirName = file.getName().toLowerCase();
				if (dirName.equals(".") || dirName.equals(".."))
				{
					continue;
				}

				long curDirSize = parseDirectory(path + dirName + "/");
				dirSize += curDirSize;

				String dirExtension = SearcherConstants.DIRECTORY_EXTENSION;
				String dirSizeStr   = NumberUtils.pad(curDirSize);

				addDocumentToList(dirName, dirExtension, dirSizeStr, host + path);
			}
			else if (file.isFile())
			{
				dirSize += indexFile(file, path);
			}
		}
		if (documentList.size() > 1000)
		{
			IndexerOperator.getInstance().addDocuments(documentList);
			documentList.clear();
		}
		return dirSize;
	}

	/**
	 * Indexes ftp
	 * @throws IllegalArgumentException too big file size (see <code>NumberUtils</code> class)
	 * @throws IOException error occurs while either sending a command to the server or receiving a reply from the server
	 * @throws SearcherException Failed adding documents in index
	 */
	public void performIndexing() throws IllegalArgumentException, IOException, SearcherException
	{
		documentList.clear();
		try
		{
			ftp.setControlEncoding("Cp1251"); // TODO: think about this
			ftp.connect(ip);
			int reply = ftp.getReplyCode();
			if (! FTPReply.isPositiveCompletion(reply))
			{
				ftp.disconnect();
				return;
			}
		}
		catch (IOException e)
		{
            if (ftp.isConnected())
            {
                try
                {
                    ftp.disconnect();
                }
                catch (IOException ex)
                {
					// do nothing
                }
            }
			return;
		}
		try
		{
			if (! ftp.login("Anonymous", "arpmipg@gmail.com"))
			{
				ftp.logout();
				return;
			}
			parseDirectory("/");
		}
		catch (IOException e)
		{
			// do nothing
		}
		finally
		{
			if (ftp.isConnected())
			{
				try
				{
					ftp.disconnect();
				}
				catch (IOException e)
				{
					// do nothing
				}
			}
		}
		IndexerOperator.getInstance().addDocuments(documentList);
	}

/*	public static void main(String[] args)
	{
		try
		{
			new FtpIndexer("195.19.254.184").performIndexing();
		}
		catch (Exception e)
		{
			int a = 1;
		}
	} */
}
