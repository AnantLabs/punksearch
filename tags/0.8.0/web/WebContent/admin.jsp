<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<%@include file="/WEB-INF/inc/head.jsp" %>
	<body id="admin">
		<%@include file="/WEB-INF/inc/header.jsp" %>
		<%@include file="/WEB-INF/inc/admin/tabs.jsp" %>
		
		<div id="searchFormContainer">
		<div id="searchForm"></div>
		</div>
		
		<% if (session.getAttribute("tab").equals("status")) { %>
				<%@ include file="/WEB-INF/inc/admin/status.jsp" %>
		<% } %>

		<% if (session.getAttribute("tab").equals("config")) { %>
				<%@ include file="/WEB-INF/inc/admin/config.jsp" %>
		<% } %>

		<% if (session.getAttribute("tab").equals("statistics")) { %>
				<%@ include file="/WEB-INF/inc/admin/statistics.jsp" %>
		<% } %>

		<br/><br/>

	</body>
</html>