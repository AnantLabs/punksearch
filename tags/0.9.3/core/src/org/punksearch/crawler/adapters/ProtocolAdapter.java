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

/**
 * General interface any network protocol adapter must implement.
 * 
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public interface ProtocolAdapter {

	/**
	 * In form: name.ext
	 */
	public String getName(Object item);

	/**
	 * Full absolute path w/o last part (i.e. name), ends with "/"
	 */
	public String getPath(Object item);

	/**
	 * Full absolute path w/ last part, last char is NOT "/"
	 */
	public String getFullPath(Object item);

	/**
	 * In milliseconds
	 */
	public long getModificationTime(Object item);

	/**
	 * In bytes
	 */
	public long getSize(Object item);

	public boolean isDirectory(Object item);

	public boolean isFile(Object item);

	public boolean isHidden(Object item);

	public boolean isLink(Object item);

	/**
	 * smb or ftp
	 */
	public String getProtocol();

	/**
	 * @param ip
	 *            IP address of remote host to connect to
	 * @return true in case of success
	 */
	public boolean connect(String ip);

	public void disconnect();

	/**
	 * The first, root directory object
	 */
	public Object getRootDir();

	public Object[] list(Object dir);

	/**
	 * Read some data from start of a file
	 * 
	 * @param item
	 *            File to read data from
	 * @param length
	 *            How much data to read (in bytes)
	 * @return Data read
	 */
	public byte[] header(Object item, int length);

}
