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
package org.punksearch.ip;

import java.util.Iterator;
import java.util.List;

public class SynchronizedIpIterator implements Iterator<String> {
	IpIterator iterator;

	public SynchronizedIpIterator(List<IpRange> ranges) {
		iterator = new IpIterator(ranges);
	}

	public synchronized boolean hasNext() {
		if (iterator == null) {
			return false;
		}
		return iterator.hasNext();
	}

	public synchronized String next() {
		if (iterator == null || !iterator.hasNext()) {
			return null;
		}
		return iterator.next().toString();
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

}
