<%@ include file="/WEB-INF/inc/imports.jsp" %>

<%@ page import="java.util.*" %>
<%@ page import="org.punksearch.common.*" %>
<%@ page import="org.punksearch.crawler.*" %>
<%@ page import="org.punksearch.ip.*" %>
<%@ page import="org.punksearch.web.filters.TypeFilters"%>
<%@ page import="org.punksearch.web.SearcherWrapper"%>

		<%!
			public static Thread indexThread = null;
		%>
		<%
			boolean isIndexing = (indexThread != null && indexThread.isAlive());
			String action = request.getParameter("action");
			String error = "";
			if (action != null) {
				if (action.equals("stop")) {
					if (isIndexing) {
						NetworkCrawler.getInstance().stop();
					}
				} else if (action.equals("start")) {
					if (!isIndexing) {
						indexThread = new Thread(NetworkCrawler.getInstance(), "Crawler");
						indexThread.start();
						//pageContext.forward("admin.jsp");
						response.sendRedirect("admin.jsp?refresh=30");
					}
				}
			} else {
				if (!isIndexing && request.getParameter("refresh") != null) {
					response.sendRedirect("admin.jsp");
				}
			}
		%>

		<% if (error.length() != 0) { %>
		<div style="background-color: #FFFF00; width: 100%; padding: 5px;"><%=error%></div>
		<% } %>
		
		<h2>Current Status</h2>
		
		<% if (isIndexing) { %>
			<span style="font-size:15px;">crawling</span><br/><br/>
			<a href="admin.jsp?action=stop" style="font-size: 15px;">stop</a>
		<% } else { %>
			<span style="font-size:15px;">stopped</span><br/><br/>
			<a href="admin.jsp?action=start" style="font-size: 15px;">start now</a>
		<% } %>
		
		<br/><br/>
		
		<% List<HostCrawler> threadList = NetworkCrawler.getInstance().getThreads(); %>
		<% synchronized(threadList) { %>
			<% if (threadList.size() != 0) { %>
				<table align="center" class="data" width="300px">
					<tr><th>thread</th><th>ip</th><th>crawled</th></tr>
					<% for (HostCrawler thread : threadList) { %>
						<tr>
							<td><%= thread.getName() %></td>
							<td><%= (thread.getIp() == null)? "inactive" : thread.getIp() %></td>
							<td><%= thread.getCrawledHosts().size() %></td>
						</tr>
					<% } %>
				</table>
			<% } %>
		<% } %>		