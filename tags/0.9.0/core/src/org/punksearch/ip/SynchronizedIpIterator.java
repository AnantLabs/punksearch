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

public class SynchronizedIpIterator extends IpIterator implements Iterator<Ip> {
	IpIterator iterator;

	public SynchronizedIpIterator(List<IpRange> ranges) {
		super(ranges);
	}

	public synchronized boolean hasNext() {
		return super.hasNext();
	}

	public synchronized Ip next() {
		if (hasNext()) {
			return super.next();
		} else {
			return null;
		}
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

}
