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

import java.util.HashSet;
import java.util.Set;


/**
 * @author Yury Soldak (ysoldak@gmail.com)
 * 
 */
public class ProtocolAdapterFactory {

	public static final String SMB_ENABLED = "org.punksearch.crawler.smb";
	public static final String FTP_ENABLED = "org.punksearch.crawler.ftp";

	private static boolean     smbEnabled  = Boolean.parseBoolean(System.getProperty(SMB_ENABLED, "true"));
	private static boolean     ftpEnabled  = Boolean.parseBoolean(System.getProperty(FTP_ENABLED, "true"));

	public static Set<ProtocolAdapter> createAll() {
		Set<ProtocolAdapter> adapters = new HashSet<ProtocolAdapter>();
		if (smbEnabled) {
			adapters.add(new SmbAdapter());
		}
		if (ftpEnabled) {
			adapters.add(new FtpAdapter());
		}
		return adapters;
	}

}
