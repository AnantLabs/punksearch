package ru.spbu.dorms.arpm.indexer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import ru.spbu.dorms.arpm.commons.SearcherConstants;


public abstract class ProtocolIndexer
{
	protected String[] goodExtensions = {
			"avi", "mov", "mpg", "vob", "wmv", "wmf", 
			"mp3", "wav", "ogg",
			"exe", 
			"iso", "bin", "dmg", "mdf",
			"txt", "xml", "doc", "rtf", "xls", "pdf", "ps",
			"jpg", "gif", "png",
			"zip", "rar", "tar", "gz", "tgz"};
	
	protected abstract String getProtocol();
	protected abstract String getIp();
	protected abstract int    getPort();
	
	/**
	 * Creates <code>Document</code> instance and adds it to <code>documentList</code>
	 * @param name
	 * @param extension
	 * @param size
	 * @param path
	 */
	protected Document makeDocument(String name, String ext, String size, String path, String date, float boost)
	{
		Document document = new Document();
		document.add(new Field(SearcherConstants.HOST, getProtocol()+ "_" + getIp(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(SearcherConstants.NAME, name, Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(SearcherConstants.EXTENSION, ext, Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(SearcherConstants.SIZE, size, Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(SearcherConstants.PATH, path, Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(SearcherConstants.DATE, date, Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.setBoost(boost);
		return document;
	}
	
	protected boolean isGoodExtension(String ext)
	{
		for (String goodExt : goodExtensions)
		{
			if (ext.equalsIgnoreCase(goodExt))
				return true;
		}
		return false;
	}
	
	protected boolean isActive(String ip)
	{
		try
		{
			SocketAddress sockaddr = new InetSocketAddress(ip, getPort());
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