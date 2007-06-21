<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%@ page import="ru.spbu.dorms.arpm.commons.*" %>
<%@ page import="ru.spbu.dorms.arpm.indexer.*" %>
<%@ page import="ru.spbu.dorms.arpm.utils.*" %>
<%
	// it is possible to conduct admin tasks without loggining first
	// just submit admin password along with other parameters
	// this is useful for automization (of indexing, for example, using wget & cron)
	String adminPassword = getServletContext().getInitParameter("adminPassword");
	String pass = request.getParameter("password");
	if (pass == null || !pass.equals(adminPassword))
	{
		String loggedSessionAttribute = (String)session.getAttribute("logged");
		if (loggedSessionAttribute == null || !loggedSessionAttribute.equals("true"))
		{
			pageContext.forward("login.jsp");
		}
	}
%>
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="content-type" content="application/xhtml+xml; charset=UTF-8" />	
		<title>PUNK LAN Search | Admin</title>
		<link href="css/style.css" type=text/css rel=stylesheet />		
	</head>
<body>
	<%!
		private static Thread indexThread = null;
	%>
	
	<div id="header">Admin Interface</div>
	
	<%
		boolean isIndexing = (indexThread != null && indexThread.isAlive());
		String action = request.getParameter("action");
		String error = "";
		if (action != null) {
			if (action.equals("stop"))
			{
				if (isIndexing)
				{
					Indexer.getInstance().stop();
				}
				else
				{
					error = "Can't stop. Not running.";
				}
			}
			else if (action.equals("start"))
			{
				if (!isIndexing)
				{
					SearcherConfig.getInstance().setIpRanges(request.getParameter("ip"));
					SearcherConfig.getInstance().setSmbLogin(request.getParameter("smbLogin"));
					SearcherConfig.getInstance().setSmbTimeout(Integer.valueOf(request.getParameter("smbTimeout")));
					SearcherConfig.getInstance().setIndexThreads(Integer.valueOf(request.getParameter("threads")));
					SearcherConfig.getInstance().setIndexDeep(Integer.valueOf(request.getParameter("deep")));
					SearcherConfig.getInstance().setIndexDirectory(request.getParameter("indexDir"));
					indexThread = new Thread(Indexer.getInstance(), "Indexer");
					indexThread.start();
					//pageContext.forward("admin.jsp");
				}
				else
				{
					error = "Can't start. Already running.";
				}
			}
			else
			{
				error = "Unknown action: " + action;
			}
		}
	%>
	<%
	if (error.length() != 0) {
	%>
	<div style="background-color: #FFFF00; width: 100%; padding: 5px;">
		<%=error%>
	</div>
	<%
	}
	%>
	
	<h3>Status</h3>
	<%=(isIndexing)? "Indexing" : "Stopped"%>
	<%
		if (isIndexing) {
		List<IndexerThread> threads = Indexer.getInstance().getThreads();
	%>
		<table>
			<tr><th>thread</th><th>ip</th></tr>
		<%
		for (IndexerThread thread : threads) {
		%>
			<tr><td><%=thread.getName()%></td><td><%= (thread.getIp() == null)? "Stopped" : thread.getIp() %></td></tr>
		<%
		}
		%>
		</table>
	<%
	}
	%>
	<br/><br/>
	<h3>Actions</h3>
	<div style="background-color: #909090;">
		<% 	if (!isIndexing) { %>
		<form action="admin.jsp">
		<% } %>
			<input type="hidden" name="action" value="start"/>
			<table width="100%" align="center">
				<tr>
					<th>IPs</th>
					<td>
						<input type="text" name="ip" value="<%= SearcherConfig.getInstance().getIpRangesString() %>" size="80"/><br/>
						<em>like: 1.2.3.4-1.2.5.23,2.5.3.8</em>
					</td>
				</tr>
				<tr>
					<th>Threads</th>
					<td>
						<input type="text" name="threads" value="<%= SearcherConfig.getInstance().getIndexThreads() %>"/><br/>
						<em>count of parallel threads running</em>
					</td>
					
				</tr>
				<tr>
					<th>Deep</th>
					<td>
						<input type="text" name="deep" value="<%= SearcherConfig.getInstance().getIndexDeep() %>"/><br/>
						<em>deep of file tree traversal</em>
					</td>
				</tr>
				<tr>
					<th>Index Directory</th>
					<td>
						<input type="text" name="indexDir" size="40" value="<%= SearcherConfig.getInstance().getIndexDirectory() %>"/><br/>
						<em>location of lucene index</em>
					</td>
				</tr>
				<tr>
					<th>SMB Login</th>
					<td>
						<input type="text" name="smbLogin" value="<%= SearcherConfig.getInstance().getSmbLogin() %>"/><br/>
						<em>DOMAIN\login:password</em>
					</td>
				</tr>
				<tr>
					<th>SMB Timeout</th>
					<td>
						<input type="text" name="smbTimeout" value="<%= SearcherConfig.getInstance().getSmbTimeout() %>"/><br/>
						<em>in milliseconds</em>
					</td>
				</tr>
			</table>
		<% 	if (!isIndexing) { %>
			<input type="submit" value="Start"/>
		</form>
		<% } else { %>
			<a href="admin.jsp?action=stop">Stop</a>
		<% } %>
	</div>
	<%@ include file="footer.jsp" %>
</body>
</html>