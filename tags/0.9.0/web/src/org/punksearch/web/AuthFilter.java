package org.punksearch.web;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AuthFilter implements Filter
{
	private static Logger	__log			= Logger.getLogger(AuthFilter.class.getName());

	private FilterConfig	filterConfig	= null;

	/**
	 * This method initiates filters by passing it a FilterConfig object
	 * @see javax.servlet.FilterConfig
	 * */
	public void init(FilterConfig filterConfig)
	{
		this.filterConfig = filterConfig;
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
	{
		try
		{
			if (request instanceof HttpServletRequest)
			{
				// first check if it is request with correct password specified (this can be request from cron)
				String specifiedPass = request.getParameter("password");
				String correctPass   = filterConfig.getServletContext().getInitParameter("admin_password");
				if (specifiedPass != null && specifiedPass.equals(correctPass))
				{
					filterChain.doFilter(request, response);
					return;
				}
				
				// check if user is logged in
				HttpServletRequest httpRequest = (HttpServletRequest) request;
				HttpServletResponse httpResponse = (HttpServletResponse) response;
				HttpSession httpSession = httpRequest.getSession(true);
				Boolean isLogged = (Boolean) httpSession.getAttribute("logged");
				if (isLogged == null || isLogged == false)
				{
					httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				}
				else
				{
					filterChain.doFilter(request, response);
				}

			}
			else
			{
				filterChain.doFilter(request, response);
			}
		}
		catch (ServletException sx)
		{
			__log.warning(sx.getMessage());
		}
		catch (IOException iox)
		{
			__log.warning(iox.getMessage());
		}
	}

	/**
	 * This method deactivates filters by assigning null to its FilterConfig object reference
	 * @see javax.servlet.FilterConfig
	 * */
	public void destroy()
	{
		filterConfig = null;
	}
}
