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

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

/**
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class FtpAdapterTest extends TestCase {

	private static String ip       = "10.0.0.21";
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
		ftp.connect(ip);
		ftp.login(login, password);
		//ftp.keepAlive();

		rootPath = ftp.printWorkingDirectory();
		
		adapter = new FtpAdapter();
		adapter.setRootPath(ftp.printWorkingDirectory());
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
		assertEquals(file.getTimestamp().getTimeInMillis(), adapter.getModificationTime(file));
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
	/*
	public void testGetPath() {
		FTPFile file = getSomeFile();
		String path = adapter.getPath(file);
		assertTrue(path.startsWith("/"));
		assertTrue(path.endsWith("/"));
		assertFalse(path.endsWith(adapter.getName(file) + "/"));
		//assertEquals(file.getPath().substring(rootPath.length()) + "/", path);
		
		FTPFile dir = getSomeDir();
		String path2 = adapter.getPath(dir);
		assertTrue(path2.startsWith("/"));
		assertTrue(path2.endsWith("/"));
		assertFalse(path2.endsWith(adapter.getName(dir) + "/"));
		//assertEquals((dir.getPath().substring(rootPath.length()) + "/").replaceAll("^/+", "/"), path2);
	}
	*/


	/**
	 * Test method for {@link org.punksearch.crawler.FtpAdapter#getFullPath(java.lang.Object)}.
	 */
	/*
	public void testGetFullPath() {
		FTPFile file = getSomeFile();
		assertTrue(adapter.getFullPath(file).startsWith("/"));
		assertFalse(adapter.getFullPath(file).endsWith("/"));
		//String expected = file.getPath().substring(rootPath.length()) + "/" + adapter.getName(file);
		//assertEquals(expected, adapter.getFullPath(file));
		
		FTPFile dir = getSomeDir();
		assertTrue(adapter.getFullPath(dir).startsWith("/"));
		assertFalse(adapter.getFullPath(dir).endsWith("/"));
		//String expected2 = dir.getPath().substring(rootPath.length()) + "/" + adapter.getName(dir);
		//assertEquals(expected2.replaceAll("^/+", "/"), adapter.getFullPath(dir));
	}
	*/
	/**
	 * Test method for {@link org.punksearch.crawler.FtpAdapter#getSize(java.lang.Object)}.
	 */
	public void testGetSize() {
		FTPFile file = getSomeFile();
		assertEquals(file.getSize(), adapter.getSize(file));
	}

	/**
	 * Test method for {@link org.punksearch.crawler.FtpAdapter#isDirectory(java.lang.Object)}.
	 */
	public void testIsDirectory() {
		FTPFile file = getSomeFile();
		assertEquals(file.isDirectory(), adapter.isDirectory(file));
		FTPFile dir = getSomeDir();
		assertEquals(dir.isDirectory(), adapter.isDirectory(dir));
	}

	/**
	 * Test method for {@link org.punksearch.crawler.FtpAdapter#isFile(java.lang.Object)}.
	 */
	public void testIsFile() {
		FTPFile file = getSomeFile();
		assertEquals(!file.isDirectory(), adapter.isFile(file));
		FTPFile dir = getSomeDir();
		assertEquals(!dir.isDirectory(), adapter.isFile(dir));
	}

	private boolean disconnect() {
		try {
			if (ftp.isConnected()) {
				ftp.quit();
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void setupFtpClient(String ip) throws IOException {
		if (ftp == null) {
			ftp = new FTPClient();
		}
		ftp.setControlEncoding(encoding);
		/*
		if (mode == MODE.active) {
			ftp.setConnectMode(FTPConnectMode.ACTIVE);
		} else {
			ftp.setConnectMode(FTPConnectMode.PASV);
		}
		ftp.setRemoteHost(ip);
		*/
		ftp.setDefaultTimeout(timeout);
	}

	private FTPFile getSomeFile() {
		try {
			FTPFile[] items = ftp.listFiles("/");
			for (FTPFile item : items) {
				if (item.isDirectory() && !item.getName().startsWith(".")) {
					// ftp.chdir(item.getName());
					FTPFile[] items2 = ftp.listFiles(item.getName());
					if (items2 == null) {
						continue;
					}
					for (FTPFile item2 : items2) {
						if (!item2.isDirectory() && !item2.isSymbolicLink() && !item2.getName().startsWith(".")) {
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
			FTPFile[] items = ftp.listFiles("/");
			for (FTPFile item : items) {
				if (item.isDirectory() && !item.getName().startsWith(".")) {
					return item;
				}
			}
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
