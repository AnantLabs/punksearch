<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<title>Login</title>
		<link href="css/style.css" type=text/css rel=stylesheet />		
	</head>
	<body>
		<div id="queryContainer">
			Login
		</div>
		<%
			String adminPassword = getServletContext().getInitParameter("adminPassword");
		
			String action = request.getParameter("action");
			if (action != null && action.equals("logout"))
			{
				session.setAttribute("logged", "false");
			}
			else
			{
				String pass = request.getParameter("password");
				if (pass != null && pass.equals(adminPassword))
				{
					session.setAttribute("logged", "true");
					pageContext.forward("admin.jsp");
				}
				else
				{
					if (pass != null)
					{
		%>
						<div style="background-color: #FFFF00; width: 100%; padding: 5px;">
							wrong password
						</div>
		<%
					}
				}
			}
		%>
		<br/><br/>
		<form action="login.jsp" method="post">
			Password: <input type="password" name="password" />&#160;<input type="submit" value="Login" />
		</form>
	</body>
</html>