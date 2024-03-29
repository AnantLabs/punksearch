<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<head>

	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

	<% if (request.getParameter("refresh") != null) {%>
	<meta http-equiv="Refresh" content="<%= request.getParameter("refresh") %>">
	<% } %>
	
	<title>PUNKSearch</title>

	<link href="css/style.css" type="text/css" rel="stylesheet" />
	<%
		Cookie[] cookies = request.getCookies();
		String theme = "orange_blue";
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				Cookie cookie = cookies[i];
				if (cookie.getName().equals("theme")) {
					theme = cookie.getValue();
					break;
				}
			}
		}
	%>
	<link href="css/<%= theme %>.css" type="text/css" rel="stylesheet" id="theme_css" />

	<script type="text/javascript" src="js/common.js"> </script>
	<script type="text/javascript" src="js/theme.js"> </script>

</head>