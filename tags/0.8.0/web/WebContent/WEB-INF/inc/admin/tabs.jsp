<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core"%>

<%
	String tab = request.getParameter("tab");
	if (tab != null) {
		session.setAttribute("tab", request.getParameter("tab"));
	} else if (session.getAttribute("tab") == null) {
		session.setAttribute("tab", "status");
	}
%>

<c:set var="currentTab" value="<%= session.getAttribute("tab") %>" />

<div id="tabsContainer">
<table cellspacing="0" id="tabs">
	<tr>
		<td style="width: 200px;">&#160;</td>
		<c:forEach items="${'status,config,statistics'}" var="tab">
			<td class="spacer" style="padding-left: 2px;">&#160;</td>
			<c:choose>
				<c:when test="${tab == currentTab}">
					<td class="tab selected"><span><c:out value="${tab}" /></span></td>
				</c:when>
				<c:otherwise>
					<td class="tab"><a href="admin.jsp?tab=<c:out value="${tab}" />"><c:out value="${tab}" /></a></td>
				</c:otherwise>
			</c:choose>
		</c:forEach>
		<td class="spacer" style="padding-left: 50px;">&#160;</td>
		<td class="tab"><a href="login.jsp?action=logout">logout</a></td>
		<td>&#160;</td>
	</tr>
</table>
</div>