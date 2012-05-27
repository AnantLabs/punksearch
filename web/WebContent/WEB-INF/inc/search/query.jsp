<%@ page contentType="text/html; charset=UTF-8" %>
<%@ include file="/WEB-INF/inc/imports.jsp" %>
<%@ page import="org.punksearch.web.SearchParams"%>

<%
	SearchParams params = (SearchParams) session.getAttribute("params");
	String defaultTabs = "films,serials,music,clips,pictures";
	String tabs = System.getProperty("org.punksearch.web.tabs", defaultTabs);
	tabs = "everything," + tabs + ",advanced";
	String[] tabsArray = tabs.split(",");
%>
<c:set var="tabsArray" value="<%=tabsArray%>" />
<c:set var="currentTab" value="<%=params.type%>" />

<div id="tabsContainer">
<table cellspacing="0" id="tabs">
	<tr>
		<%--<td style="width: 200px;">&#160;</td>--%>
		<c:forEach items="${tabsArray}" var="tab">
			<c:choose>
				<c:when test="${tab == 'advanced'}" >
					<td class="spacer" style="padding-left: 50px;">&#160;</td>
				</c:when>
				<c:otherwise>
					<td class="spacer" style="padding-left: 2px;">&#160;</td>
				</c:otherwise>
			</c:choose>
			<c:choose>
				<c:when test="${tab == currentTab}">
					<td class="tab selected"><span><c:out value="${tab}" /></span></td>
				</c:when>
				<c:otherwise>
					<td class="tab"><a href="#" onclick="changeSearchType('<c:out value="${tab}" />'); return false;"><c:out value="${tab}" /></a></td>
				</c:otherwise>
			</c:choose>
		</c:forEach>
		<td>&#160;</td>
	</tr>
</table>
</div>

<div id="searchFormContainer">
<form id="searchForm" action="search.jsp" method="get">
	<input type="hidden" name="type" value="<%= params.type %>" />
	<c:choose>
		<c:when test="${currentTab == 'advanced'}">
			<table class="fieldset" width="100%" style="font-size: 16px; font-weight: bold;">
				<tr>
					<td>path<br /><input type="text" id="dir" name="dir" value="<%= params.dir %>" size="20" /></td>
					<td>filename<br /><input type="text" id="file" name="file" value="<%= params.file %>" size="20" /></td>
					<td>extension<br /><input type="text" id="ext" name="ext" value="<%= params.ext %>" size="10" />
					</td>
				</tr>
				<tr>
					<td>
						date<span style="vertical-align: sub; font-size: 12px;">yyyy-mm-dd</span><br />
						<input type="text" id="fromDate" name="fromDate" value="<%=SearchParams.getStringValue(request, "fromDate")%>" size="11" style="font-size: 16px; font-weight: bold" />&nbsp;-
						<input type="text" id="toDate" name="toDate" value="<%=SearchParams.getStringValue(request, "toDate")%>" size="11" style="font-size: 16px; font-weight: bold" />
					</td>
					<td>
						size<span style="vertical-align: sub; font-size: 12px;">Mb</span><br />
						<input type="text" id="minSize" name="minSize" value="<%=SearchParams.getStringValue(request, "minSize")%>" size="6" style="font-size: 16px; font-weight: bold" />&nbsp;- 
						<input type="text" id="maxSize" name="maxSize" value="<%=SearchParams.getStringValue(request, "maxSize")%>" size="6" style="font-size: 16px; font-weight: bold" />
					</td>
					<td><input type="submit" value="search" /></td>
				</tr>
			</table>
		</c:when>
		<c:otherwise>
			<input id="query" type="text" name="query" value="<c:out value="<%= params.query %>" />" style="width: 480px;" />
			<input id="submit" type="submit" value="search" />
		</c:otherwise>
	</c:choose>
</form>
</div>