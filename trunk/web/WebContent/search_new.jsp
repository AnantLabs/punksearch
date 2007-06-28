<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<title>PUNK LAN Search</title>
		<link href="css/style.css" type="text/css" rel="stylesheet" />
		<script>
			function formSubmit(type)
			{
				document.forms["searchForm"].elements["type"].value = type;
				document.forms["searchForm"].submit(); 
			}
			function validateQuery()
			{
				var query = document.forms["searchForm"].elements["query"].value;
				var adultsOnlyRegExp = /.*(?:porno)|(?:sex).*/
				var xxx = query.match(adultsOnlyRegExp);
				var isAdult = false;
				if (xxx !=null) isAdult = confirm("Attention! \n" + 
												 "You are trying to search files that may contains information allowed for adult persons only. \n" +
												 "If you are older than 14 press 'OK' else press 'Cancel'");
				return isAdult;
			}
		</script>
	</head>
<body>

<%@ include file="header.jsp"%>

<%
	String type = request.getParameter("type");
	if (type == null) type = "everything";
	
	String query = request.getParameter("query");
	if (query == null) query = "";
	
	String[][] searchTabs = {{"Everything","everything"},{"Films","films"},{"Music","music"},{"Advanced","advanced"}};
%>

<div id="searchFormContainer">

<table cellspacing=0 id="searchTabs">
	<tr>
		<td style="border-right: 1px solid white; width:50%">&#160;</td>
		<%
		for (String[] tab: searchTabs)
		{	
			if (tab[1].equals(type))
			{
				%><td class="tab selected"><%= tab[0] %></d><%		
			}
			else
			{
				%><td class="tab"><a href="search_new.jsp" onclick="formSubmit('<%= tab[1] %>'); return false;"><%= tab[0] %></a></td><%
			}
			if (searchTabs[searchTabs.length-1]!=tab)
			{
				%><td class="spacer">&#160;</td><%		
			}
		}
		%>
		<td style="border-left: 1px solid white; width:50%">&#160;</td>
	</tr>
</table>

<form id="searchForm" action="search_new.jsp" method="get" onsubmit="return validateQuery();">	
	<input type="hidden" name="type" value="<%=type%>" />
	<input type="text" name="query" value="<%=query%>" style="width:480px;" />
	<input type="submit" value="Search" />	
</form>

</div>

</body>
</html>
