/***************************************************************************
 *                                                                         *
 *   PunkSearch - Searching over LAN                                       *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package org.punksearch.crawler;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.logging.Logger;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import org.punksearch.common.OnlineChecker;

/**
 * Adapter for crawling SMB hosts. Uses jCIFS library.
 * 
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class SmbAdapter implements ProtocolAdapter {

	private static Logger      __log        = Logger.getLogger(SmbAdapter.class.getName());

	public static final String SMB_DOMAIN   = "org.punksearch.crawler.smb.domain";
	public static final String SMB_USER     = "org.punksearch.crawler.smb.user";
	public static final String SMB_PASSWORD = "org.punksearch.crawler.smb.password";

	public static final int    HASH_MIN     = 1024 * 512;
	
	private SmbFile            smb;

	private String             rootPath;

	static {
		System.setProperty("jcifs.smb.client.soTimeout", System.getProperty("org.punksearch.crawler.smb.timeout", "3000"));
	}

	/**
	 * test-friendly method
	 * 
	 * @param path
	 */
	protected void setRootPath(String path) {
		rootPath = path;
	}

	public byte[] header(Object item, String path, int length) {
		SmbFile file = (SmbFile) item;
		try {
			if (file.isFile()) {
				InputStream is = file.getInputStream();
				byte[] buf = new byte[length];
				is.read(buf);
				is.close();
				return buf;
			} else {
				return null;
			}
		} catch (SmbAuthException e) {
			__log.warning("Can't read header for the file (restricted access): " + file.getServer() + path + getName(item));
			return null;
		} catch (IOException e) {
			__log.warning("Can't read header for the file (i/o error): " + file.getServer() + path + getName(item));
			//e.printStackTrace();
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.punksearch.crawler.ProtocolAdapter#getFullPath(java.lang.Object)
	 */
	/*
	public String getFullPath(Object item) {
		return getPath(item) + getName(item);
	}
	*/

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.punksearch.crawler.ProtocolAdapter#getModificationTime(java.lang.Object)
	 */
	public long getModificationTime(Object item) {
		try {
			return ((SmbFile) item).lastModified();
		} catch (SmbException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.punksearch.crawler.ProtocolAdapter#getName(java.lang.Object)
	 */
	public String getName(Object item) {
		SmbFile res = (SmbFile) item;
		try {
			if (res.getName().startsWith(res.getServer())) {
				return "";
			}
			return (res.isFile()) ? res.getName() : res.getName().substring(0, res.getName().length() - 1);
		} catch (SmbException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.punksearch.crawler.ProtocolAdapter#getPath(java.lang.Object)
	 */
	/*
	public String getPath(Object item) {
		String fullPath = ((SmbFile) item).getPath();
		int nameLength = getName(item).length();
		int endPos = (isDirectory(item)) ? fullPath.length() - 1 - nameLength : fullPath.length() - nameLength;
		return (endPos > rootPath.length()) ? fullPath.substring(rootPath.length(), endPos) : "/";
	}
	*/

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.punksearch.crawler.ProtocolAdapter#getProtocol()
	 */
	public String getProtocol() {
		return "smb";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.punksearch.crawler.ProtocolAdapter#getSize(java.lang.Object)
	 */
	public long getSize(Object item) {
		try {
			return ((SmbFile) item).length();
		} catch (SmbException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.punksearch.crawler.ProtocolAdapter#isDirectory(java.lang.Object)
	 */
	public boolean isDirectory(Object item) {
		try {
			return ((SmbFile) item).isDirectory();
		} catch (SmbException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.punksearch.crawler.ProtocolAdapter#isFile(java.lang.Object)
	 */
	public boolean isFile(Object item) {
		try {
			return ((SmbFile) item).isFile();
		} catch (SmbException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.punksearch.crawler.ProtocolAdapter#isHidden(java.lang.Object)
	 */
	public boolean isHidden(Object item) {
		try {
			return ((SmbFile) item).isHidden();
		} catch (SmbException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.punksearch.crawler.ProtocolAdapter#isLink(java.lang.Object)
	 */
	public boolean isLink(Object item) {
		return false;
	}

	public boolean connect(String ip) {
		if (!OnlineChecker.isActiveSmb(ip)) {
			return false;
		}
		try {
			smb = (getSmbAuth() == null) ? new SmbFile("smb://" + ip + "/") : new SmbFile("smb://" + ip + "/",
			        getSmbAuth());
			setRootPath("smb://" + ip);
			return true;
		} catch (RuntimeException e) {
			__log.info("smb: Exception occured: " + e.getMessage() + " during indexing host " + ip);
			return false;
		} catch (MalformedURLException e) {
			__log.info("smb: MalformedURLException occured: " + e.getMessage() + " during indexing host " + ip);
			return false;
		} finally {
			// TODO: disconnect?
		}

	}

	public void disconnect() {
	}

	public Object getRootDir() {
		if (smb == null) {
			throw new IllegalStateException("Can't get root dir since not connected to any smb host");
		}
		return smb;
	}

	public Object[] listFiles(Object dir, String path) {
		try {
			return ((SmbFile) dir).listFiles();
		} catch (SmbAuthException e) {
			__log.fine("Can't list files in restricted directory: " + ((SmbFile) dir).getPath());
			return new Object[0];
		} catch (SmbException e) {
			__log.fine("Can't list files (" + e.getMessage() + ") in directory: "
			        + ((SmbFile) dir).getPath());
			try {
				smb.listFiles(); // check if we still connected
				return new Object[0];
			} catch (SmbException e1) {
				throw new RuntimeException("Connection with host was dropped");
			}
		}
	}

	private NtlmPasswordAuthentication getSmbAuth() {
		String domain = System.getProperty(SMB_DOMAIN);
		String user = System.getProperty(SMB_USER);
		String password = System.getProperty(SMB_PASSWORD);

		if (user.length() > 0) {
			return new NtlmPasswordAuthentication(domain, user, password);
		} else {
			return null;
		}
	}

}
