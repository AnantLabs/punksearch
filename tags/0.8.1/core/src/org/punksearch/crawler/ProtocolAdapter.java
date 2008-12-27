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

/**
 * @author Yury Soldak (ysoldak@gmail.com)
 *
 */
public interface ProtocolAdapter {

	/**
	 * In form: name.ext
	 * @param item
	 * @return
	 */
	public String getName(Object item);
	
	/**
	 * In form: /long/path/to
	 * !note: not from filesystem root at the server
	 * @param item
	 * @return
	 */
	public String getPath(Object item);
	
	/**
	 * In form: /long/path/to/name.ext
	 * @param item
	 * @return
	 */
	public String getFullPath(Object item);
	
	public long getModificationTime(Object item);
	public long getSize(Object item);
	
	public boolean isDirectory(Object item);
	public boolean isFile(Object item);
	public boolean isHidden(Object item);
	public boolean isLink(Object item);
	
	public String getProtocol();
	
	public boolean connect(String ip);
	public void disconnect();
	
	public Object getRootDir();
	
	public Object[] listFiles(Object item);
	
}