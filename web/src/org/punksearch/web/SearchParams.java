package org.punksearch.web;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

public class SearchParams 
{
	private static Logger __log	= Logger.getLogger(SearchParams.class.getName());
	
	private HttpServletRequest request = null;
		
	public String type = "everything";
	public String query = null;
	
	public String dir = null;	
	public String file = null;
	public String ext = null;

	public Long minSize =  null;
	public Long maxSize = null;
	
	public Date fromDate = null;
	public Date toDate = null;

	public Boolean ftp = true;
	public Boolean smb = true;
	
	public Integer first = 0;
	public Integer last = SearchPager.PAGE_SIZE-1;
		
	public SearchParams(HttpServletRequest request)
	{
		this.request = request;
	
		String typeParam = request.getParameter("type");
		if (typeParam != null) this.type = typeParam;

		this.query = getStringValue("query");

		this.dir = getStringValue("dir");
		this.file = getStringValue("file");
		this.ext = getStringValue("ext");
		
		try
		{			
			this.maxSize = (Long)Math.round(Double.valueOf(getStringValue("maxSize"))*1024*1024);
			this.minSize = (Long)Math.round(Double.valueOf(getStringValue("minSize"))*1024*1024);
			this.fromDate = DateFormat.getDateInstance().parse(getStringValue("fromDate"));
			this.toDate = DateFormat.getDateInstance().parse(getStringValue("toDate"));
			
			this.first = Integer.valueOf(getStringValue("first"));
			this.last = Integer.valueOf(getStringValue("last"));
		}
		catch (NumberFormatException nfe)
		{
			__log.warning(nfe.getMessage());
		}
		catch (ParseException pe) 
		{
			__log.warning(pe.getMessage());
		}

		this.smb = (request.getParameter("smb") != null);
		this.ftp = (request.getParameter("ftp") != null);	
		
		if (this.minSize != null && this.maxSize != null && this.minSize > this.minSize)
		{
			this.minSize = this.maxSize = null;
		}
		if (this.fromDate != null && this.toDate != null && this.fromDate.getTime() > this.toDate.getTime())
		{
			this.fromDate = this.toDate = null;
		}
	}
	
	public String getStringValue(String paramName)
	{
		String paramValue = this.request.getParameter(paramName);
		if (paramValue == null) return "";
		
		try
		{
			paramValue = new String(paramValue.getBytes("ISO-8859-1"), "UTF-8");
		}
		catch (UnsupportedEncodingException uee)
		{
			__log.warning(uee.getMessage());
		}
		
		return paramValue;
	}
}