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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.punksearch.online.OnlineStatuses;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.punksearch.crawler.CrawlerKeys.*;

/**
 * Adapter for crawling FTP hosts. Uses commons-net library.
 * 
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class FtpAdapter implements ProtocolAdapter {

	private static Log __log = LogFactory.getLog(FtpAdapter.class);

    private FTPClient  ftp   = new FTPClient();

	private String     rootPath;

	public boolean connect(String ip) {
		disconnect();

		__log.trace("Check if server has active ftp: " + ip);
		if (!OnlineStatuses.getInstance().isOnline("ftp://" + ip)) {
			return false;
		}

		try {
			__log.trace("Connecting to server: " + ip);
			setupFtpClient(ip);
			ftp.connect(ip);
			ftp.login(getUser(), getPassword());
			ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
			setRootPath(ftp.printWorkingDirectory());
			return true;
		} catch (Exception e) {
			__log.warn("Exception (" + e.getMessage() + ") during connecting the server: " + ip);
			disconnect();
			return false;
		}
	}

	public void disconnect() {
		try {
			if (ftp.isConnected()) {
				__log.trace("Disconnectiong from server: " + ftp.getRemoteAddress().getHostAddress());
				ftp.disconnect();
			}
		} catch (Exception e) {
			__log.warn("Exception (" + e.getMessage() + ") during disconnecting a server");
			ftp = new FTPClient();
		}
	}

	/**
	 * test-friendly method
	 */
	protected void setRootPath(String path) {
		rootPath = path;
	}
	
	public byte[] header(Object item, int length) {
		FtpItem file = (FtpItem) item;
		try {
			if (file.isFile()) {
				String filePath = file.getPath();//path + file.getName();
				InputStream is = ftp.retrieveFileStream(filePath);
				if (is == null) {
					__log.debug("Can't read header for the file (" + ftp.getReplyCode() + "): " + filePath);
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
			__log.debug("Can't read header for the file (i/o error): " + getFullPath(item));
			return null;
		}
	}

	public long getModificationTime(Object item) {
		return ((FtpItem) item).getModificationTime();
	}

	public String getName(Object item) {
		return ((FtpItem) item).getName();
	}

	public String getPath(Object item) {
		return ((FtpItem) item).getPath();
	}
	
	public String getFullPath(Object item) {
		return ((FtpItem) item).getFullPath();
	}

	public String getProtocol() {
		return "ftp";
	}

	public Object getRootDir() {
		if (ftp == null || !ftp.isConnected()) {
			__log.error("Can't get root dir since not connected to any ftp host");
			throw new IllegalStateException("Can't get root dir since not connected to any ftp host");
		}
		return new FtpItem(null, "");
	}
	
	public long getSize(Object item) {
		return ((FtpItem) item).getSize();
	}

	public boolean isDirectory(Object item) {
		return ((FtpItem) item).isDirectory();
	}

	public boolean isFile(Object item) {
		//return (!((FtpItem) item).isDirectory() && !((FtpItem) item).isLink());
		return ((FtpItem) item).isFile();
	}

	public boolean isHidden(Object item) {
		return false;
	}

	public boolean isLink(Object item) {
		return ((FtpItem) item).isLink();
	}

	public Object[] list(Object dir) {
		FtpItem item = (FtpItem) dir;
		try {
			FTPFile[] files = ftp.listFiles(rootPath + item.getFullPath());
			FtpItem[] result = new FtpItem[files.length];
			for (int i = 0; i < files.length; i++) {
				result[i] = new FtpItem(files[i], item.getFullPath() + "/" + files[i].getName());
			}
			return result;
		} catch (IOException e) {
			// host communication problem occured, rethrow the exception so crawler will give up crawling this host
			__log.warn("I/O Exception during listing of dir: " + e.getMessage());
			throw new RuntimeException(e);
		} catch (Exception e) {
			__log.info("Exception (" + e.getMessage() + ") during listing directory: " + item.getFullPath());
			return new FtpItem[0];
		}
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
		String defaultEnc = System.getProperty(FTP_ENCODING_DEFAULT, "UTF-8");
		String customEnc = System.getProperty(FTP_ENCODING_CUSTOM);
		Map<String, String> encMap = parseCustomEncodings(customEnc);
		return (encMap.containsKey(ip)) ? encMap.get(ip) : defaultEnc;
	}

	/*
	private boolean isActiveModeForIp(String ip) {
		String defaultMode = System.getProperty("org.punksearch.crawler.ftp.mode.default");
		String customMode = System.getProperty("org.punksearch.crawler.ftp.mode.custom");
		Map<String, String> customModes = parseCustomModes(customMode);
		String modeStr = (customModes.containsKey(ip)) ? customModes.get(ip) : defaultMode;
		return (modeStr.equals("active"));
	}
	*/

	private void setupFtpClient(String ip) throws IOException {
		if (ftp == null) {
			ftp = new FTPClient();
		}

		ftp.setControlEncoding(getFtpEncodingForIp(ip));
		// TODO
		// if (isActiveModeForIp(ip)) {
		// ftp.setConnectMode(FTPConnectMode.ACTIVE); } else { ftp.setConnectMode(FTPConnectMode.PASV); }
		// ftp.setRemoteHost(ip);
		ftp.setDefaultTimeout(Integer.parseInt(System.getProperty(FTP_TIMEOUT)));
	}

	private String getUser() {
		String user = System.getProperty(FTP_USER);
		return (user.length() == 0) ? "anonymous" : user;
	}

	private String getPassword() {
		String passwd = System.getProperty(FTP_PASSWORD);
		return (passwd.length() == 0) ? "some@email.com" : passwd;
	}

}


class FtpItem {

	private FTPFile file;
	private String fullpath;
	
	FtpItem(FTPFile file, String fullpath) {
		this.file = file;
		this.fullpath = fullpath;
	}
	
	// last part of path
	String getName() {
		if (file != null) {
			return file.getName();
		} else {
			return "";
		}
	}
	
	// full absolute path w/o last part (name)
	String getPath() {
		if (fullpath.length() > 1) { // "/a" -> "/", "/a/b/c" -> "/a/b/"
			return fullpath.substring(0, fullpath.lastIndexOf("/")+1);
		} else {
			return "";
		}
	}
	
	// full absolute path w/o trailing "/" 
	String getFullPath() {
		return fullpath;
	}
	
	long getModificationTime() {
		if (file != null) {
			return file.getTimestamp().getTime().getTime();
		}
		return 0;
	}
	
	boolean isDirectory() {
		if (file != null) {
			return file.isDirectory();
		}
		return true;
	}

	boolean isFile() {
		if (file != null) {
			return file.isFile();
		}
		return false;
	}
	
	boolean isLink() {
		if (file != null) {
			return file.isSymbolicLink();
		} else {
			return false;
		}
	}
	
	boolean isHidden() {
		return false;
	}
	
	long getSize() {
		if (file != null) {
			return file.getSize();
		}
		return 0L;
	}
}