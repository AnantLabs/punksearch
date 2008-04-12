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

import java.util.HashSet;
import java.util.Set;

/**
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class FileType {

	private String      title      = "undefined";
	private Set<String> extensions = new HashSet<String>();
	private long         minBytes   = 0;
	private long         maxBytes   = Long.MAX_VALUE;

	public void addExtensions(String... exts) {
		for (String ext : exts) {
			extensions.add(ext.toLowerCase());
		}
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Set<String> getExtensions() {
		return extensions;
	}

	public void setExtensions(Set<String> extensions) {
		this.extensions = extensions;
	}

	public void setExtensions(String... extensions) {
		this.extensions.clear();
		this.addExtensions(extensions);
	}

	public long getMinBytes() {
		return minBytes;
	}

	public void setMinBytes(int minBytes) {
		this.minBytes = minBytes;
	}

	public long getMaxBytes() {
		return maxBytes;
	}

	public void setMaxBytes(int maxBytes) {
		this.maxBytes = maxBytes;
	}

}
