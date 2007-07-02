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

	public Integer minSize =  null;
	public Integer maxSize = null;
	
	public Date fromDate = null;
	public Date toDate = null;

	public Boolean ftp = true;
	public Boolean smb = true;
		
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
			this.maxSize = Integer.valueOf(getStringValue("maxSize"));
			this.minSize = Integer.valueOf(getStringValue("mixSize"));			
			this.fromDate = DateFormat.getDateInstance().parse(getStringValue("fromDate"));
			this.toDate = DateFormat.getDateInstance().parse(getStringValue("toDate"));
		}
		catch (NumberFormatException nfe)
		{
			__log.warning(nfe.getMessage());
		}
		catch (ParseException pe) 
		{
			__log.warning(pe.getMessage());
		}

		String smbParam = request.getParameter("smb");
		if (smbParam == null) this.smb = false;		
		String ftpParam = request.getParameter("ftp");
		if (ftpParam == null) this.ftp = false;
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