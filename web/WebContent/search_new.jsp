<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<title>PUNK LAN Search | Login</title>
		<link href="css/style.css" type=text/css rel=stylesheet />
		<script>
			function formSubmit(type)
			{
				document.forms["queryForm"].elements["type"].value = type;
				document.forms["queryForm"].submit(); 
			}
		</script>				
	</head>
	<body>

<%@ include file="header.jsp" %>
<%
	String type = request.getParameter("type");
	if (type == null) type = "everything";
	
	String query = request.getParameter("query");
	if (query == null) query = "";
	
	String[][] searchTabs = {{"Everything","everything"},{"Films","films"},{"Music","music"},{"Advanced","advanced"}};
%>

<div id="searchFormContainer">
	<div id="searchTabs">
		<%
			for (String[] tab: searchTabs)
			{	
				if (tab[1].equals(type))
				{
		%>
				<span class="selected"><%= tab[0] %></span>
		<%		
				}
				else
				{
		%>
				<span><a href="search_new.jsp" onclick="formSubmit('<%= tab[1] %>'); return false;"><%= tab[0] %></a></span>
		<%
				}
			}
		%>
	</div>
	<div id="searchForm">
		<form id="queryForm" action="search_new.jsp" method="get" style="margin:0px;">
			<input type="hidden" name="type" value="<%=type%>"/>
			<input type="text" name="query"  value="<%=query%>" style="width:500px; font-size:20px; margin-top:5px;"/>
			<input type="submit" style="font-size:20px;" value="Search"/>
		</form>	
	</div>
</div>
 
	</body>
</html>