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
package org.punksearch.crawler.adapters;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

/**
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class FtpAdapterTest extends TestCase {

	private static String ip       = "127.0.0.1";
	private static String login    = null;
	private static String password = null;

	public static enum MODE {
		active, passive
	};

	private FTPClient  ftp      = new FTPClient();

	private int        timeout;
	//private MODE       mode     = MODE.passive;
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

	protected void tearDown() throws Exception {
		disconnect();
		super.tearDown();
	}

	public void testGetModificationTime() {
		FTPFile file = getSomeFile();
		assertEquals(file.getTimestamp().getTimeInMillis(), adapter.getModificationTime(new FtpItem(file, null)));
	}

	public void testGetName() throws Exception {
		FTPFile file = getSomeFile();
		assertFalse(file.getName().contains("/"));
		assertEquals(file.getName(), adapter.getName(new FtpItem(file, null)));
		
		FTPFile dir = getSomeDir();
		assertFalse(dir.getName().contains("/"));
		assertEquals(dir.getName(), adapter.getName(new FtpItem(dir, null)));
	}
	
	public void testCanNavigateDeep() throws Exception {
		FTPFile dir = getSomeDir(2);
		assertNotNull(dir);
	}

	private String pathFromFile(FTPFile file) throws Exception {
		return ftp.printWorkingDirectory().substring(rootPath.length()) + "/" + file.getName();
	}
	
	public void testGetPath() throws Exception {
		FTPFile file = getSomeFile();
		FtpItem item = new FtpItem(file, pathFromFile(file));
		String path = adapter.getPath(item);
		assertTrue(path.startsWith("/"));
		assertTrue(path.endsWith("/"));
		assertFalse(path.endsWith(adapter.getName(item) + "/"));
		assertEquals(ftp.printWorkingDirectory().substring(rootPath.length()) + "/", path);
		
		FTPFile dir = getSomeDir();
		FtpItem item2 = new FtpItem(dir, pathFromFile(file));
		String path2 = adapter.getPath(item2);
		assertTrue(path2.startsWith("/"));
		assertTrue(path2.endsWith("/"));
		assertFalse(path2.endsWith(adapter.getName(item2) + "/"));
		assertEquals((ftp.printWorkingDirectory().substring(rootPath.length()) + "/").replaceAll("^/+", "/"), path2);
	}

	public void testGetFullPath() throws Exception {
		FTPFile file = getSomeFile();
		FtpItem item = new FtpItem(file, pathFromFile(file));
		assertTrue(adapter.getFullPath(item).startsWith("/"));
		assertFalse(adapter.getFullPath(item).endsWith("/"));
		String expected = pathFromFile(file); //file.getPath().substring(rootPath.length()) + "/" + adapter.getName(file);
		assertEquals(expected, adapter.getFullPath(item));
		
		FTPFile dir = getSomeDir();
		FtpItem item2 = new FtpItem(dir, pathFromFile(dir));
		assertTrue(adapter.getFullPath(item2).startsWith("/"));
		assertFalse(adapter.getFullPath(item2).endsWith("/"));
		String expected2 = pathFromFile(dir); //dir.getPath().substring(rootPath.length()) + "/" + adapter.getName(dir);
		assertEquals(expected2.replaceAll("^/+", "/"), adapter.getFullPath(item2));
	}

	public void testGetSize() {
		FTPFile file = getSomeFile();
		assertEquals(file.getSize(), adapter.getSize(new FtpItem(file, null)));
	}

	public void testIsDirectory() throws Exception {
		FTPFile file = getSomeFile();
		assertEquals(file.isDirectory(), adapter.isDirectory(new FtpItem(file, null)));
		FTPFile dir = getSomeDir();
		assertEquals(dir.isDirectory(), adapter.isDirectory(new FtpItem(dir, null)));
	}

	public void testIsFile() throws Exception {
		FTPFile file = getSomeFile();
		assertEquals(!file.isDirectory(), adapter.isFile(new FtpItem(file, null)));
		FTPFile dir = getSomeDir();
		assertEquals(!dir.isDirectory(), adapter.isFile(new FtpItem(dir, null)));
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
			FTPFile[] items = ftp.listFiles(rootPath);
			for (FTPFile item : items) {
				if (item.isDirectory() && !item.getName().startsWith(".")) {
					FTPFile[] items2 = ftp.listFiles(item.getName());
					if (items2 == null) {
						continue;
					}
					ftp.changeWorkingDirectory(rootPath + item.getName());
					for (FTPFile item2 : items2) {
						if (!item2.isDirectory() && !item2.isSymbolicLink() && !item2.getName().startsWith(".") && item2.getName().contains(".")) {
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

	private FTPFile getSomeDir() throws Exception {
		return getSomeDir(0);
	}
	
	private FTPFile getSomeDir(int level) throws Exception {
		String curPath = "/";
		FTPFile result = null;
		for (int cur = 0; cur <= level; cur++) {
    		FTPFile[] items = ftp.listFiles(curPath);
    		for (FTPFile item : items) {
    			if (item.isDirectory() && !item.getName().startsWith(".")) {
    				curPath += "/" + item.getName();
    				result = item;
    				break;
    			}
    		}
		}
		return result;
	}

}
