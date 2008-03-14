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

import junit.framework.TestCase;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPFile;

/**
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class FtpAdapterTest extends TestCase {

	private static String ip       = "127.0.0.1";
	private static String login    = "anonymous";
	private static String password = "some@email.com";

	public static enum MODE {
		active, passive
	};

	private FTPClient  ftp      = new FTPClient();

	private int        timeout;
	private MODE       mode     = MODE.passive;
	private String     encoding = "UTF8";
	private String     rootPath;

	private FtpAdapter adapter;

	protected void setUp() throws Exception {
		super.setUp();
		setupFtpClient(ip);
		ftp.connect();
		ftp.login(login, password);
		ftp.keepAlive();

		rootPath = ftp.pwd();
		
		adapter = new FtpAdapter();
		adapter.setRootPath(ftp.pwd());
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		disconnect();
		super.tearDown();
	}

	/**
	 * Test method for {@link org.punksearch.crawler.FtpAdapter#getModificationTime(java.lang.Object)}.
	 */
	public void testGetModificationTime() {
		FTPFile file = getSomeFile();
		assertEquals(file.lastModified().getTime(), adapter.getModificationTime(file));
	}

	/**
	 * Test method for {@link org.punksearch.crawler.FtpAdapter#getName(java.lang.Object)}.
	 */
	public void testGetName() {
		FTPFile file = getSomeFile();
		assertFalse(file.getName().contains("/"));
		assertEquals(file.getName(), adapter.getName(file));
		
		FTPFile dir = getSomeDir();
		assertFalse(dir.getName().contains("/"));
		assertEquals(dir.getName(), adapter.getName(dir));
	}

	/**
	 * Test method for {@link org.punksearch.crawler.FtpAdapter#getPath(java.lang.Object)}.
	 */
	public void testGetPath() {
		FTPFile file = getSomeFile();
		String path = adapter.getPath(file);
		assertTrue(path.startsWith("/"));
		assertFalse(path.endsWith("/"));
		assertEquals(file.getPath().substring(rootPath.length()), path);
	}

	/**
	 * Test method for {@link org.punksearch.crawler.FtpAdapter#getFullPath(java.lang.Object)}.
	 */
	public void testGetFullPath() {
		FTPFile file = getSomeFile();
		String expected = file.getPath().substring(rootPath.length()) + "/" + file.getName();
		assertEquals(expected, adapter.getFullPath(file));
	}

	/**
	 * Test method for {@link org.punksearch.crawler.FtpAdapter#getSize(java.lang.Object)}.
	 */
	public void testGetSize() {
		FTPFile file = getSomeFile();
		assertEquals(file.size(), adapter.getSize(file));
	}

	/**
	 * Test method for {@link org.punksearch.crawler.FtpAdapter#isDirectory(java.lang.Object)}.
	 */
	public void testIsDirectory() {
		FTPFile file = getSomeFile();
		assertEquals(file.isDir(), adapter.isDirectory(file));
		FTPFile dir = getSomeDir();
		assertEquals(dir.isDir(), adapter.isDirectory(dir));
	}

	/**
	 * Test method for {@link org.punksearch.crawler.FtpAdapter#isFile(java.lang.Object)}.
	 */
	public void testIsFile() {
		FTPFile file = getSomeFile();
		assertEquals(!file.isDir(), adapter.isFile(file));
		FTPFile dir = getSomeDir();
		assertEquals(!dir.isDir(), adapter.isFile(dir));
	}

	private boolean disconnect() {
		try {
			if (ftp.connected()) {
				ftp.quit();
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void setupFtpClient(String ip) throws FTPException, IOException {
		if (ftp == null) {
			ftp = new FTPClient();
		}
		ftp.setControlEncoding(encoding);
		if (mode == MODE.active) {
			ftp.setConnectMode(FTPConnectMode.ACTIVE);
		} else {
			ftp.setConnectMode(FTPConnectMode.PASV);
		}
		ftp.setRemoteHost(ip);
		ftp.setTimeout(timeout);
	}

	private FTPFile getSomeFile() {
		try {
			FTPFile[] items = ftp.dirDetails("/");
			for (FTPFile item : items) {
				if (item.isDir() && !item.getName().startsWith(".")) {
					// ftp.chdir(item.getName());
					FTPFile[] items2 = ftp.dirDetails(item.getName());
					if (items2 == null) {
						continue;
					}
					for (FTPFile item2 : items2) {
						if (!item2.isDir() && !item2.isLink() && !item2.getName().startsWith(".")) {
							return item2;
						}
					}
				}
			}
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private FTPFile getSomeDir() {
		try {
			FTPFile[] items = ftp.dirDetails("/");
			for (FTPFile item : items) {
				if (item.isDir() && !item.getName().startsWith(".")) {
					return item;
				}
			}
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
