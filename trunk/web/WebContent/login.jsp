<%@ page import="org.punksearch.common.Settings" %>
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<%@ include file="/WEB-INF/inc/head.jsp" %>	
	<body>
		<%@ include file="/WEB-INF/inc/header.jsp" %>	
		<div id="searchFormContainer" style="height:50px">
			<form id="searchForm" action="login.jsp" method="post">
				Password: <input type="password" name="password" />&#160;<input type="submit" value="Login" />
			</form>
		</div>
		
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
					String adminPassword = Settings.get("org.punksearch.web.admin_password");
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
	</body>
</html>