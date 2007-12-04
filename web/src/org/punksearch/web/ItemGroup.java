package org.punksearch.web;

import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.punksearch.commons.IndexFields;

public class ItemGroup {

	private List<Document> items = new LinkedList<Document>();
	private long            size  = 0;
	private String         ext   = "";

	public ItemGroup(Document item) {
		items.add(item);
		size = Long.valueOf(item.get(IndexFields.SIZE));
		ext = item.get(IndexFields.EXTENSION);
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
		return (itemSize == size && itemExt.equals(ext));
	}

	public List<Document> getItems() {
		return items;
	}
	
}
