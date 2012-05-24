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

import org.punksearch.crawler.adapters.SmbAdapter;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import junit.framework.TestCase;

/**
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class SmbAdapterTest extends TestCase {

	private static String ip = "10.20.111.107";
	private static String user = null;
	private static String pwd = null;

	private SmbFile       root;

	private SmbAdapter    adapter;

	protected void setUp() throws Exception {
		super.setUp();

		NtlmPasswordAuthentication auth = (user != null)? new NtlmPasswordAuthentication("", user, pwd) : null;
		
		root = new SmbFile("smb://" + ip + "/", auth);

		adapter = new SmbAdapter();
	}

	public void testGetFullPath() {
		SmbFile file = getSomeFile();
		String expected = file.getPath().substring(root.getPath().length() - 1);
		assertEquals(expected, adapter.getFullPath(file));
		
		SmbFile dir = getSomeDir();
		String expected2 = dir.getPath().substring(root.getPath().length() - 1, dir.getPath().length() - 1);
		assertEquals(expected2, adapter.getFullPath(dir));
	}

	public void testGetModificationTime() throws Exception {
		SmbFile file = getSomeFile();
		assertEquals(file.lastModified(), adapter.getModificationTime(file));
	}

	public void testGetName() {
		SmbFile file = getSomeFile();
		String fileName = adapter.getName(file);
		assertFalse(file.getName().contains("/"));
		assertFalse(fileName.contains("/"));
		assertEquals(file.getName(), adapter.getName(file));

		SmbFile dir = getSomeDir();
		String dirName = adapter.getName(dir);
		assertTrue(dir.getName().contains("/"));
		assertFalse(dirName.contains("/"));
		assertEquals(dir.getName().substring(0, dir.getName().length() - 1), dirName);
	}

	public void testGetPath() {
		SmbFile file = getSomeFile();
		String path = adapter.getPath(file);
		assertTrue(path.startsWith("/"));
		assertTrue(path.endsWith("/"));
		String expectedPath = file.getPath().substring(root.getPath().length() - 1);
		expectedPath = expectedPath.substring(0, expectedPath.length() - adapter.getName(file).length());
		assertEquals(expectedPath, path);
		
		SmbFile dir = getSomeDir();
		String path2 = adapter.getPath(dir);
		assertTrue(path2.startsWith("/"));
		assertTrue(path2.endsWith("/"));
		assertFalse(path2.endsWith(adapter.getName(dir) + "/"));
		String expectedPath2 = dir.getPath().substring(root.getPath().length() - 1);
		expectedPath2 = expectedPath2.substring(0, expectedPath2.length() - 1 - adapter.getName(dir).length());
		assertEquals(expectedPath2, path2);
	}

	public void testGetSize() throws SmbException {
		SmbFile file = getSomeFile();
		assertEquals(file.length(), adapter.getSize(file));
	}

	public void testIsDirectory() throws SmbException {
		SmbFile file = getSomeFile();
		assertEquals(file.isDirectory(), adapter.isDirectory(file));
		SmbFile dir = getSomeDir();
		assertEquals(dir.isDirectory(), adapter.isDirectory(dir));
	}

	public void testIsFile() throws SmbException {
		SmbFile file = getSomeFile();
		assertEquals(file.isFile(), adapter.isFile(file));
		SmbFile dir = getSomeDir();
		assertEquals(dir.isFile(), adapter.isFile(dir));
	}

	public void testIsHidden() throws SmbException {
		SmbFile file = getSomeFile();
		assertEquals(file.isHidden(), adapter.isHidden(file));
	}

	public void testIsLink() {
		SmbFile file = getSomeFile();
		assertEquals(false, adapter.isLink(file));
	}

	private SmbFile getSomeFile() {
		try {
			return findFile(root);
		} catch (SmbException e) {
			e.printStackTrace();
			return null;
		}
	}

	private SmbFile findFile(SmbFile rootDir) throws SmbException {
		try {
			SmbFile[] items = rootDir.listFiles();
			for (SmbFile item : items) {
				if (!item.isDirectory() && !item.getName().startsWith(".")) {
					return item;
				}
				if (item.isDirectory() && !item.getName().startsWith(".") && !item.getName().endsWith("$/")) {
					SmbFile file = findFile(item);
					if (file != null) {
						return file;
					}
				}
			}
		} catch (SmbAuthException e) {
			e.printStackTrace();
		}

		return null;
	}

	private SmbFile getSomeDir() {
		try {
			SmbFile[] items = root.listFiles();
			for (SmbFile item : items) {
				if (item.isDirectory() && !item.getName().startsWith(".") && !item.getName().endsWith("$/")) {
					return item;
				}
			}
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
