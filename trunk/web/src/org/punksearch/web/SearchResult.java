package org.punksearch.web;

import java.text.DateFormat;
import java.text.DecimalFormat;

import org.apache.lucene.document.Document;
import org.punksearch.commons.IndexFields;

public class SearchResult {
	
	public boolean isAvailable = true; //TODO Implement
	
	public String host = null;
	public String path = null;
	public String name = null;
	public String ext = null;
	public String date = null;
	public String size = null;
	
	public float score = 0;
	
	public SearchResult(Document doc)
	{
		host = doc.get(IndexFields.HOST).replace("smb_", "smb://").replace("ftp_", "ftp://");
		path = doc.get(IndexFields.PATH).replaceAll("&", "&amp;");
		name = doc.get(IndexFields.NAME).replaceAll("&", "&amp;");
		ext   = doc.get(IndexFields.EXTENSION);
		if (ext.length() != 0)
			name += "." + ext;
		
		size = doc.get(IndexFields.SIZE);
		date = doc.get(IndexFields.DATE);
		
		//size = NumberFormat.getNumberInstance().format((Double.valueOf(size))/(1024*1024));
		size = new DecimalFormat("###,##0.00").format((Double.valueOf(size))/(1024*1024));			
		date = DateFormat.getDateInstance().format(Long.valueOf(date));
		
		score = doc.getBoost();
	}
}
