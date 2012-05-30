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

import java.text.DateFormat;
import java.text.DecimalFormat;

import org.apache.lucene.document.Document;
import org.punksearch.common.IndexFields;
import org.punksearch.web.utils.BrowserOS;

public class SearchResult {

    public final String host;
    public final String protocol;
    public final String ip;

	public final String path;
	public String name;
	public String ext;
	public String date;
	public String size;

	public float  score = 0;

	public SearchResult(Document doc, BrowserOS os) {
        host = doc.get(IndexFields.HOST); // like smb_1.1.1.1
        String[] parts = host.split("_");

        String protocol = parts[0];
        ip = parts[1];

        if ("smb".equals(protocol) && os != BrowserOS.UNIX_LIKE) {
            protocol = "file";
        }

        this.protocol = protocol;

		path = doc.get(IndexFields.PATH).replaceAll("&", "&amp;");
		name = doc.get(IndexFields.NAME).replaceAll("&", "&amp;");
		ext = doc.get(IndexFields.EXTENSION);
		if (ext.length() != 0)
			name += "." + ext;

		size = doc.get(IndexFields.SIZE);
		date = doc.get(IndexFields.DATE);

		// size = NumberFormat.getNumberInstance().format((Double.valueOf(size))/(1024*1024));
		size = new DecimalFormat("###,##0.00").format((Double.valueOf(size)) / (1024 * 1024));
		date = DateFormat.getDateInstance().format(Long.valueOf(date));

		score = doc.getBoost();
	}
}
