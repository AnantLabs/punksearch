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

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.punksearch.logic.online.OnlineStatuses;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import static org.punksearch.crawler.CrawlerKeys.*;

/**
 * Adapter for crawling SMB hosts. Uses jCIFS library.
 *
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class SmbAdapter implements ProtocolAdapter {

    private static final Log log = LogFactory.getLog(SmbAdapter.class);

    private SmbFile smb;

    static {
        System.setProperty("jcifs.smb.client.soTimeout", System.getProperty(SMB_TIMEOUT, "3000"));
    }

    public byte[] header(Object item, int length) {
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
            log.debug("Can't read file header (restricted access): " + file.getServer() + getFullPath(item));
            return null;
        } catch (IOException e) {
            log.debug("Can't read file header (i/o error): " + file.getServer() + getFullPath(item));
            return null;
        }
    }

    public long getModificationTime(Object item) {
        try {
            return ((SmbFile) item).lastModified();
        } catch (SmbException e) {
            throw new RuntimeException(e);
        }
    }

    public String getName(Object item) {
        SmbFile res = (SmbFile) item;
        if (res.getName().startsWith(res.getServer())) {
            return "";
        }
        return (!res.getName().endsWith("/")) ? res.getName() : res.getName().substring(0, res.getName().length() - 1);
    }

    public String getPath(Object item) {
        SmbFile res = (SmbFile) item;
        String rawPath = res.getPath();
        return rawPath.substring(rawPath.indexOf("/", 7), rawPath.lastIndexOf(getName(item))); // skip "smb://<ip>" and strip name
    }

    public String getFullPath(Object item) {
        SmbFile res = (SmbFile) item;
        String rawPath = res.getPath();
        String result = rawPath.substring(rawPath.indexOf("/", 7));
        if (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    public String getProtocol() {
        return "smb";
    }

    public long getSize(Object item) {
        try {
            return ((SmbFile) item).length();
        } catch (SmbException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isDirectory(Object item) {
        try {
            return ((SmbFile) item).isDirectory();
        } catch (SmbException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isFile(Object item) {
        try {
            return ((SmbFile) item).isFile();
        } catch (SmbException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isHidden(Object item) {
        try {
            return ((SmbFile) item).isHidden();
        } catch (SmbException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isLink(Object item) {
        return false;
    }

    public boolean connect(String ip) {
        log.trace("Check if server has active smb: " + ip);
        if (!OnlineStatuses.getInstance().isOnline("smb://" + ip)) {
            return false;
        }
        try {
            log.trace("Connecting to server: " + ip);
            smb = (getSmbAuth() == null) ? new SmbFile("smb://" + ip + "/") : new SmbFile("smb://" + ip + "/", getSmbAuth());
            return true;
        } catch (RuntimeException e) {
            log.info("Exception (" + e.getMessage() + ") during connecting the server " + ip);
            return false;
        } catch (MalformedURLException e) {
            log.info("MalformedURLException (" + e.getMessage() + ") during connecting the server " + ip);
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

    @Override
    public Object resolvePath(String path) {
        try {
            return new SmbFile(smb, path);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public Object[] list(Object dir) {
        try {
            return ((SmbFile) dir).listFiles();
        } catch (SmbAuthException e) {
            log.debug("Can't list files in restricted directory: " + ((SmbFile) dir).getPath());
            return new Object[0];
        } catch (SmbException e) {
            log.debug("Can't list files (" + e.getMessage() + ") in directory: " + ((SmbFile) dir).getPath());
            if (e.getMessage().indexOf("timeout") > 0) {
                throw new RuntimeException("Timeout occured");
            }
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
