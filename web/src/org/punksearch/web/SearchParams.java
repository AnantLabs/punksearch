package org.punksearch.web;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

public class SearchParams 
{
	private static Logger __log	= Logger.getLogger(SearchParams.class.getName());
	
	public HttpServletRequest request = null;
		
	public String type = "everything";
	public String query = null;
	
	public String dir = null;	
	public String file = null;
	public String ext = null;

	public Long minSize =  null;
	public Long maxSize = null;
	
	public Long fromDate = null;
	public Long toDate = null;

	public Boolean ftp = true;
	public Boolean smb = true;
	
	public Integer first = 0;
	public Integer last = SearchPager.PAGE_SIZE-1;
		
	public SearchParams(HttpServletRequest request)
	{
		this.request = request;
	
		String typeParam = request.getParameter("type");
		if (typeParam != null) this.type = typeParam;
		
		/* first & last */
		try
		{
			this.first = Integer.valueOf(getStringValue("first"));
		}
		catch (NumberFormatException nfe)
		{
			__log.warning(nfe.getMessage());
		}
		try
		{
			this.last = Integer.valueOf(getStringValue("last"));
		}
		catch (NumberFormatException nfe)
		{
			__log.warning(nfe.getMessage());
		}		

		if (type.equals("advanced"))
		{
			/* dir & file & ext*/		
			this.dir = getStringValue("dir");
			this.file = getStringValue("file");
			this.ext = getStringValue("ext");
			
			/* maxSize & minSize */
			try
			{			
				this.maxSize = (Long)Math.round(Double.valueOf(getStringValue("maxSize"))*1024*1024);
			}
			catch (NumberFormatException nfe)
			{
				__log.warning(nfe.getMessage());
			}		
			try
			{
				this.minSize = (Long)Math.round(Double.valueOf(getStringValue("minSize"))*1024*1024);
			}
			catch (NumberFormatException nfe)
			{
				__log.warning(nfe.getMessage());
			}
			if (this.minSize != null && this.maxSize != null && this.minSize > this.minSize)
			{
				this.minSize = this.maxSize = null;
			}
					
			/* fromDate & toDate */
			DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
			try
			{
				this.fromDate = fmt.parse(getStringValue("fromDate")).getTime();
			}
			catch (ParseException pe) 
			{
				__log.warning(pe.getMessage());
			}
			try
			{
				this.toDate = fmt.parse(getStringValue("toDate")).getTime();
			}
			catch (ParseException pe) 
			{
				__log.warning(pe.getMessage());
			}
			if (this.fromDate != null && this.toDate != null && this.fromDate > this.toDate)
			{
				this.fromDate = this.toDate = null;
			}		
						
			/* ftp & smb */
			this.smb = (request.getParameter("smb") != null);
			this.ftp = (request.getParameter("ftp") != null);	
		}
		else
		{
			this.query = getStringValue("query");			
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