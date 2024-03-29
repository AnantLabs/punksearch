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
package org.punksearch.web;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.punksearch.common.IndexFields;

/**
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class ItemGroup {

	public static final int HASH_MIN  = 1024 * 512;

	private List<Document>  items     = new LinkedList<Document>();
	private long            size      = 0;
	private String          ext       = "";
	private byte[]          header;
	//private String          timestamp = "";

	public ItemGroup(Document item) {
		items.add(item);
		size = Long.valueOf(item.get(IndexFields.SIZE));
		ext = item.get(IndexFields.EXTENSION);
		header = item.getBinaryValue(IndexFields.HEADER);
		//timestamp = item.get(IndexFields.LAST_MODIFIED);
	}

	public void add(Document item) {
		if (!matches(item)) {
			throw new IllegalArgumentException("Item does not match: " + item);
		}
		items.add(item);
	}

	public boolean matches(Document item) {
		long itemSize = Long.valueOf(item.get(IndexFields.SIZE));
		String itemExt = item.get(IndexFields.EXTENSION);
		boolean sizeExtMatch = (itemSize == size && itemExt.equalsIgnoreCase(ext));
		if (itemSize > HASH_MIN && sizeExtMatch) {
			byte[] itemHeader = item.getBinaryValue(IndexFields.HEADER);
			if (itemHeader != null) {
				return Arrays.equals(itemHeader, header);
			} else {
				return sizeExtMatch;
			}
			/*
			String itemTimestamp = item.get(IndexFields.LAST_MODIFIED);
			if (itemTimestamp != null) {
				return itemTimestamp.equals(timestamp);
			} else {
				return sizeExtMatch;
			}
			*/
		} else { // TODO: group < HASH_MIN ?
			return sizeExtMatch;
		}
	}

	public List<Document> getItems() {
		return items;
	}
}
