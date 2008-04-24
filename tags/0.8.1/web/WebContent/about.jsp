<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<%@ include file="/WEB-INF/inc/head.jsp"%>
	<body>
		<%@ include file="/WEB-INF/inc/header.jsp"%>
		<br/><br/>
		<table align="center" class="data">
		 <tr><th>Version</th><td>0.8.1</td></tr>
		 <tr><th>Java</th><td><%= System.getProperty("java.version") %> by <%= System.getProperty("java.vendor") %></td></tr>
		 <tr><th>OS</th><td><%= System.getProperty("os.name") %> <%= System.getProperty("os.version") %></td></tr>
		 <tr><th>Architecture</th><td><%= System.getProperty("os.arch") %></td></tr>
		</table>
	</body>
</html>

