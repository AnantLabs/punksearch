package ru.spbu.dorms.arpm.commons;

import java.io.IOException;

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
  private FilterConfig filterConfig = null;

  /**
   * This method initiates filters by passing it a FilterConfig object
   * @see javax.servlet.FilterConfig
   * */
  public void init(FilterConfig filterConfig) {
    this.filterConfig = filterConfig;
  }

  /**
   * This doFilter method works as explained below:
   *
   * First of all it checks for existing session. If it doesn't find it, then it redirects user to
   * login procedure. If session exists, it validates presense of session attribute and if it is
   * absent the same redirect is performed.
   * @param request This parameter represents the request processed by this filters and transferred
   *  to other filters in a filters chain
   * @param response This parameter represents the response posed by the filters and transferred to
   * other filters in the filters chain
   * */
  public void doFilter(ServletRequest request, ServletResponse response,
                       FilterChain filterChain) {
    try {
      if (request instanceof HttpServletRequest) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession httpSession = httpRequest.getSession(true);

		String loggedSessionAttribute = (String)httpSession.getAttribute("logged");
		if (loggedSessionAttribute == null || !loggedSessionAttribute.equals("true"))
			httpResponse.sendError(401);
		else
          filterChain.doFilter(request, response);
		
      } else { filterChain.doFilter(request, response); }
    }
    catch (ServletException sx) {
      filterConfig.getServletContext().log(sx.getMessage());
    }
    catch (IOException iox) {
      filterConfig.getServletContext().log(iox.getMessage());
    }
  }

  /**
   * This method deactivates filters by assigning null to its FilterConfig object reference
   * @see javax.servlet.FilterConfig
   * */
  public void destroy() {
    filterConfig = null;
  }
}
