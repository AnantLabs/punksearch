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
package org.punksearch.common;

public class IndexFields {
	public static final String EXTENSION     = "Extension";   // avi, mp3, ...
	public static final String HOST          = "Host";        // [smb|ftp]_10.0.3.2
	public static final String HOST_NAME     = "HostName";        // user-pc.campus
	public static final String LAST_MODIFIED = "LastModified"; // timestamp
	public static final String NAME          = "Name";        // filename without extension
	public static final String PATH          = "Path";        // /video/best/ghost/
	public static final String SIZE          = "Size";        // 000705560576
	public static final String DATE          = "Date";
	public static final String TYPE          = "Type";        // DIR or FILE

	public static final String INDEXED       = "Indexed";
	public static final String HEADER        = "Header";

	public static final String TYPE_DIR      = "DIR";
	public static final String TYPE_FILE     = "FILE";
	public static final String SORT_SUFFIX   = "_Sort";       // 000705560576

	private IndexFields() {
	}
}
