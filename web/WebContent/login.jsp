<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<title>PUNK LAN Search | Login</title>
		<link href="css/style.css" type=text/css rel=stylesheet />		
	</head>
	<body>
		<div id="header">Login</div>
		<%		
			String action = request.getParameter("action");
			if (action != null && action.equals("logout"))
			{
				session.setAttribute("logged", false);
			}
			else
			{				
				String password = request.getParameter("password");
				if (password != null)				
				{
					String adminPassword = getServletContext().getInitParameter("adminPassword");
					if (password.equals(adminPassword))
					{
						session.setAttribute("logged", true);
						response.sendRedirect("admin.jsp");					
					}
					else
					{
		%>
						<div class="errorMessage">Wrong password</div>
		<%
					}
				}
			}
		%>
		<form action="login.jsp" method="post" style="margin:50px;">
			Password: <input type="password" name="password" />&#160;<input type="submit" value="Login" />
		</form>
		<%@ include file="footer.jsp" %>
	</body>
</html>