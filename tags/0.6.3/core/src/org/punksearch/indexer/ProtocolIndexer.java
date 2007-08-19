package org.punksearch.indexer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.punksearch.commons.IndexFields;



public abstract class ProtocolIndexer
{
	protected String[] goodExtensions =
	{
		"avi", "mov", "mpeg", "mpg", "vob", "wmv", "wmf", 
		"flac", "mp3", "ogg", "wav", "wma",
		"exe", 
		"iso", "dmg", "mdf", "nrg", "img", "daa", "pqi",
		"djvu", "doc", "htm", "html", "rtf", "odg", "odp", "ods", "odt", "pdf", "ppt", "ps", "txt", "xhtml", "xls",
		"bmp", "gif", "jpeg", "jpg", "png", "tif", "tiff",
		"7z", "arj", "bz2", "gz", "rar", "tar", "tgz", "zip"
	};
	
	protected abstract String getProtocol();
	protected abstract String getIp();
	//protected abstract int    getPort();
	
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
		document.add(new Field(IndexFields.HOST, getProtocol()+ "_" + getIp(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(IndexFields.NAME, name, Field.Store.YES, Field.Index.TOKENIZED));
		document.add(new Field(IndexFields.EXTENSION, ext, Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(IndexFields.SIZE, size, Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(IndexFields.PATH, path, Field.Store.YES, Field.Index.TOKENIZED));
		document.add(new Field(IndexFields.DATE, date, Field.Store.YES, Field.Index.UN_TOKENIZED));
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
	/*
	protected boolean isActive(String ip)
	{
		return isActive(ip, getPort());
	}
	*/
	protected boolean isActive(String ip, int port)
	{
		try
		{
			SocketAddress sockaddr = new InetSocketAddress(ip, port);
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