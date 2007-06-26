package org.punksearch.commons;

import org.apache.log4j.PropertyConfigurator;


import javax.servlet.http.HttpServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.io.File;

public class InitServlet extends HttpServlet
{
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

		String path = getServletContext().getRealPath("");
		String configFileName = (path + "/WEB-INF/classes/log4j.properties").replace('/', File.separatorChar);
		PropertyConfigurator.configure(configFileName);
		
		SearcherConfig sc = SearcherConfig.getInstance();
		sc.setIndexDirectory(getServletContext().getInitParameter("indexDirectory"));
		sc.setIndexThreads(Integer.parseInt(getServletContext().getInitParameter("indexThreads")));
		sc.setIndexDeep(Integer.parseInt(getServletContext().getInitParameter("indexDeep")));
		sc.setIpRanges(getServletContext().getInitParameter("ipRange"));
		sc.setSmbLogin(getServletContext().getInitParameter("smbLogin"));
		sc.setFtpDefaultEncoding(getServletContext().getInitParameter("ftpDefaultEncoding"));
		sc.setFtpCustomEncodings(getServletContext().getInitParameter("ftpCustomEncodings"));
		sc.setMaxClauseCount(Integer.parseInt(getServletContext().getInitParameter("searchMaxClauseCount")));
		
		//System.setProperty("jcifs.smb.client.responseTimeout", "5000");
		System.setProperty("jcifs.util.loglevel", "0");
		
	}
}