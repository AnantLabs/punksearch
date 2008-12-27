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
package org.punksearch.web.filters;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.punksearch.common.IndexFields;
import org.punksearch.online.OnlineStatuses;
import org.punksearch.web.ResultFilter;

/**
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class OnlineResultFilter implements ResultFilter {

	public boolean matches(Document doc) {
		return OnlineStatuses.getInstance().isOnline(doc.get(IndexFields.HOST));
	}

	public List<Integer> filter(final List<Document> docs) {
		List<String> hosts = extractDistinctHosts(docs);
		Set<String> onlineHosts = OnlineStatuses.getInstance().getOnline(hosts);
		List<Integer> docIds = new LinkedList<Integer>();
		for (int i = 0; i < docs.size(); i++) {
			Document doc = docs.get(i);
			if (onlineHosts.contains(doc.get(IndexFields.HOST))) {
				docIds.add(i);
			}
		}
		return docIds;
	}

	private List<String> extractDistinctHosts(List<Document> docs) {
		List<String> result = new ArrayList<String>();
		for (Document doc : docs) {
			if (!result.contains(doc.get(IndexFields.HOST))) {
				result.add(doc.get(IndexFields.HOST));
			}
		}
		return result;
	}

}