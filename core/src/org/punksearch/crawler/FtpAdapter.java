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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.punksearch.common.OnlineChecker;
import org.punksearch.common.PunksearchProperties;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPFile;

/**
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class FtpAdapter implements ProtocolAdapter {

	private static Logger __log   = Logger.getLogger(FtpAdapter.class.getName());
	
	private FTPClient     ftp     = new FTPClient();

	private String        rootPath;

	public boolean connect(String ip) {
		if (!OnlineChecker.isActiveFtp(ip)) {
			return false;
		}

		try {
			setupFtpClient(ip);
			ftp.connect();
			ftp.login(getUser(), getPassword());
			ftp.keepAlive();
			setRootPath(ftp.pwd());
			return true;
		} catch (Exception e) {
			__log.info("ftp: Exception (" + e.getMessage() + ") during connecting the server: " + ip);
			return false;
		}
	}

	public void disconnect() {
		String ip = "unknown-ip";
		try {
			if (ftp.connected()) {
				ip = ftp.getRemoteHost();
				ftp.quit();
			}
		} catch (Exception e) {
			__log.info("ftp: exception during disconnect from " + ip + ": " + e.getMessage());
		}
	}

	/**
	 * test-friendly method
	 * @param path
	 */
	protected void setRootPath(String path) {
		rootPath = path;
	}
	
	public String getFullPath(Object item) {
		return getPath(item) + getName(item);
	}

	public long getModificationTime(Object item) {
		return ((FTPFile) item).lastModified().getTime();
	}

	public String getName(Object item) {
		return ((FTPFile) item).getName();
	}

	public String getPath(Object item) {
		String path = ((FTPFile) item).getPath().replaceAll("^/+", "/");
		String suffix = (path.length() == 1)? "" : "/";
		return path.substring(rootPath.length() - 1) + suffix;
	}

	public String getProtocol() {
		return "ftp";
	}

	public Object getRootDir() {
		if (ftp == null || !ftp.connected()) {
			throw new IllegalStateException("can't get root dir since not connected to any ftp host");
		}
		try {
			return rootPath;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	public long getSize(Object item) {
		return ((FTPFile) item).size();
	}

	public boolean isDirectory(Object item) {
		return ((FTPFile) item).isDir();
	}

	public boolean isFile(Object item) {
		return (!((FTPFile) item).isDir() && !((FTPFile) item).isLink());
	}

	public boolean isHidden(Object item) {
		return false;
	}

	public boolean isLink(Object item) {
		return ((FTPFile) item).isLink();
	}

	public Object[] listFiles(Object dir) {
		if (dir instanceof String) {
			return list((String)dir);
		} else {
    		return list(getFullPath(dir));
		}
	}
	
	private Object[] list(String path) {
		FTPFile[] items = {};
		try {
			items = ftp.dirDetails(path);
		} catch (IOException e) {
			// host communication problem occured, rethrow the exception so crawler will give up crawling this host
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
		String defaultEnc = PunksearchProperties.getProperty("org.punksearch.crawler.ftp.encoding.default");
		String customEnc = PunksearchProperties.getProperty("org.punksearch.crawler.ftp.encoding.custom");
		Map<String, String> encMap = parseCustomEncodings(customEnc);
		return (encMap.containsKey(ip)) ? encMap.get(ip) : defaultEnc;
	}

	private boolean isActiveModeForIp(String ip) {
		String defaultMode = PunksearchProperties.getProperty("org.punksearch.crawler.ftp.mode.default");
		String customMode = PunksearchProperties.getProperty("org.punksearch.crawler.ftp.mode.custom");
		Map<String, String> customModes = parseCustomModes(customMode);
		String modeStr = (customModes.containsKey(ip)) ? customModes.get(ip) : defaultMode;
		return (modeStr.equals("active"));
	}

	private void setupFtpClient(String ip) throws FTPException, IOException {
		if (ftp == null) {
			ftp = new FTPClient();
		}
		ftp.setControlEncoding(getFtpEncodingForIp(ip));
		if (isActiveModeForIp(ip)) {
			ftp.setConnectMode(FTPConnectMode.ACTIVE);
		} else {
			ftp.setConnectMode(FTPConnectMode.PASV);
		}
		ftp.setRemoteHost(ip);
		ftp.setTimeout(Integer.parseInt(PunksearchProperties.getProperty("org.punksearch.crawler.ftp.timeout")));
	}
	
	private String getUser() {
		String user = PunksearchProperties.getProperty("org.punksearch.crawler.ftp.user");
		return (user.length() == 0)? "anonymous" : user;
	}
	
	private String getPassword() {
		String passwd = PunksearchProperties.getProperty("org.punksearch.crawler.ftp.password");
		return (passwd.length() == 0)? "some@email.com" : passwd;
	}

}
