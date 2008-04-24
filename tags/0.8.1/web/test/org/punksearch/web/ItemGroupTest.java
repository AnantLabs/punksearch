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

import junit.framework.TestCase;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.punksearch.common.IndexFields;

public class ItemGroupTest extends TestCase {

	public void testMatches() {
		Document item = newItem(100, "avi");
		ItemGroup group = new ItemGroup(item);

		assertTrue(group.matches(newItem(100, "avi")));
		assertFalse(group.matches(newItem(101, "avi")));
		assertFalse(group.matches(newItem(100, "exe")));
		assertFalse(group.matches(newItem(101, "exe")));
	}

	// TODO: different case for extensions

	public void testAdd() {
		Document item = newItem(100, "avi");
		ItemGroup group = new ItemGroup(item);

		group.add(newItem(100, "avi"));
		assertEquals(2, group.getItems().size());

		try {
			group.add(newItem(101, "avi"));
			fail("Should throw exception");
		} catch (IllegalArgumentException e) {
			// ok
		}

	}

	private Document newItem(int size, String ext) {
		Document item = new Document();
		item.add(new Field(IndexFields.SIZE, String.valueOf(size), Store.NO, Index.TOKENIZED));
		item.add(new Field(IndexFields.EXTENSION, ext, Store.NO, Index.TOKENIZED));
		return item;
	}

}
