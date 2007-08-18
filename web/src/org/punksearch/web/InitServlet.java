package org.punksearch.web;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.punksearch.searcher.Searcher;

public class InitServlet extends HttpServlet
{
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		
		SearcherConfig sc = SearcherConfig.getInstance();
		sc.setIndexDirectory(getServletContext().getInitParameter("indexDirectory"));
		sc.setIndexThreads(Integer.parseInt(getServletContext().getInitParameter("indexThreads")));
		sc.setIndexDeep(Integer.parseInt(getServletContext().getInitParameter("indexDeep")));
		sc.setIpRanges(getServletContext().getInitParameter("ipRange"));
		sc.setSmbLogin(getServletContext().getInitParameter("smbLogin"));
		sc.setFtpDefaultEncoding(getServletContext().getInitParameter("ftpDefaultEncoding"));
		sc.setFtpCustomEncodings(getServletContext().getInitParameter("ftpCustomEncodings"));
		sc.setFtpDefaultMode(getServletContext().getInitParameter("ftpDefaultMode"));
		sc.setFtpCustomModes(getServletContext().getInitParameter("ftpCustomModes"));
		sc.setFtpTimeout(Integer.parseInt(getServletContext().getInitParameter("ftpTimeout")));
		sc.setMaxClauseCount(Integer.parseInt(getServletContext().getInitParameter("searchMaxClauseCount")));
		sc.setGoogleAnalyticsId(getServletContext().getInitParameter("googleAnalyticsId"));
		sc.setFastSearch(Boolean.parseBoolean(getServletContext().getInitParameter("fastSearch")));
		
		//System.setProperty("jcifs.smb.client.responseTimeout", "5000");
		System.setProperty("jcifs.util.loglevel", "0");
		
		SearcherWrapper.init(SearcherConfig.getInstance().getIndexDirectory());
	}
}
