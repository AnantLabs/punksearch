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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.punksearch.common.OnlineChecker;

/**
 * Adapter for crawling FTP hosts. Uses commons-net library.
 * 
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class FtpAdapter implements ProtocolAdapter {

	private static Logger __log = Logger.getLogger(FtpAdapter.class.getName());

	private FTPClient     ftp   = new FTPClient();

	private String        rootPath;

	public boolean connect(String ip) {
		disconnect();

		if (!OnlineChecker.isActiveFtp(ip)) {
			return false;
		}

		try {
			setupFtpClient(ip);
			ftp.connect(ip);
			ftp.login(getUser(), getPassword());
			ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
			setRootPath("/");
			return true;
		} catch (Exception e) {
			__log.info("ftp: Exception (" + e.getMessage() + ") during connecting the server: " + ip);
			disconnect();
			return false;
		}
	}

	public void disconnect() {
		try {
			if (ftp.isConnected()) {
				ftp.disconnect();
			}
		} catch (Exception e) {
			__log.info("ftp: exception during disconnect " + e.getMessage());
			ftp = new FTPClient();
		}
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
		FTPFile file = (FTPFile) item;
		try {
			if (file.isFile()) {
				InputStream is = ftp.retrieveFileStream(path + file.getName());
				if (is == null) {
					__log.fine("Can't read header for the file (" + ftp.getReplyCode() + "): " + path + getName(item));
					return null;
				}
				byte[] buf = new byte[length];
				is.read(buf);
				is.close();
				ftp.completePendingCommand();
				return buf;
			} else {
				return null;
			}
		} catch (IOException e) {
			__log.fine("Can't read header for the file (i/o error): " + path + getName(item));
			return null;
		}
	}

	public long getModificationTime(Object item) {
		return ((FTPFile) item).getTimestamp().getTime().getTime();
	}

	public String getName(Object item) {
		if (item instanceof String) {
			return (String) item;
		}
		return ((FTPFile) item).getName();
	}

	public String getProtocol() {
		return "ftp";
	}

	public Object getRootDir() {
		if (ftp == null || !ftp.isConnected()) {
			__log.warning("Can't get root dir since not connected to any ftp host");
			throw new IllegalStateException("Can't get root dir since not connected to any ftp host");
		}
		return rootPath;
	}

	public long getSize(Object item) {
		return ((FTPFile) item).getSize();
	}

	public boolean isDirectory(Object item) {
		return ((FTPFile) item).isDirectory();
	}

	public boolean isFile(Object item) {
		return (!((FTPFile) item).isDirectory() && !((FTPFile) item).isSymbolicLink());
	}

	public boolean isHidden(Object item) {
		return false;
	}

	public boolean isLink(Object item) {
		return ((FTPFile) item).isSymbolicLink();
	}

	public Object[] listFiles(Object dir, String path) {
		if (dir instanceof String) {
			return list((String) dir);
		} else {
			return list(path + getName(dir) + "/");
		}

	}

	private Object[] list(String path) {
		FTPFile[] items = {};
		try {
			items = ftp.listFiles(path);
		} catch (IOException e) {
			// host communication problem occured, rethrow the exception so crawler will give up crawling this host
			__log.warning("I/O Exception during listing of dir: " + e.getMessage());
			throw new RuntimeException(e);
		} catch (Exception e) {
			__log.info("ftp: Exception (" + e.getMessage() + ") during changing or listing directory: " + path);
		}
		return items;
	}

	public Map<String, String> parseCustomEncodings(String encString) {
		Map<String, String> result = new HashMap<String, String>();
		if (encString == null || encString.length() == 0) {
			return result;
		}

		String[] chunks = encString.split(",");
		for (String chunk : chunks) {
			String[] parts = chunk.split(":");
			result.put(parts[0], parts[1]);
		}
		return result;
	}

	public Map<String, String> parseCustomModes(String modString) {
		Map<String, String> result = new HashMap<String, String>();
		if (modString == null || modString.length() == 0) {
			return result;
		}

		String[] chunks = modString.split(",");
		for (String chunk : chunks) {
			String[] parts = chunk.split(":");
			result.put(parts[0], parts[1]);
		}
		return result;
	}

	private String getFtpEncodingForIp(String ip) {
		String defaultEnc = System.getProperty("org.punksearch.crawler.ftp.encoding.default");
		String customEnc = System.getProperty("org.punksearch.crawler.ftp.encoding.custom");
		Map<String, String> encMap = parseCustomEncodings(customEnc);
		return (encMap.containsKey(ip)) ? encMap.get(ip) : defaultEnc;
	}

	private boolean isActiveModeForIp(String ip) {
		String defaultMode = System.getProperty("org.punksearch.crawler.ftp.mode.default");
		String customMode = System.getProperty("org.punksearch.crawler.ftp.mode.custom");
		Map<String, String> customModes = parseCustomModes(customMode);
		String modeStr = (customModes.containsKey(ip)) ? customModes.get(ip) : defaultMode;
		return (modeStr.equals("active"));
	}

	private void setupFtpClient(String ip) throws IOException {
		if (ftp == null) {
			ftp = new FTPClient();
		}

		ftp.setControlEncoding(getFtpEncodingForIp(ip));
		// TODO
		// if (isActiveModeForIp(ip)) {
		// ftp.setConnectMode(FTPConnectMode.ACTIVE); } else { ftp.setConnectMode(FTPConnectMode.PASV); }
		// ftp.setRemoteHost(ip);
		ftp.setDefaultTimeout(Integer.parseInt(System.getProperty("org.punksearch.crawler.ftp.timeout")));
	}

	private String getUser() {
		String user = System.getProperty("org.punksearch.crawler.ftp.user");
		return (user.length() == 0) ? "anonymous" : user;
	}

	private String getPassword() {
		String passwd = System.getProperty("org.punksearch.crawler.ftp.password");
		return (passwd.length() == 0) ? "some@email.com" : passwd;
	}

}
