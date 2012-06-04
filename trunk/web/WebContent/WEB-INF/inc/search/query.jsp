<%@ page contentType="text/html; charset=UTF-8" %>
<%@ include file="/WEB-INF/inc/imports.jsp" %>
<%@ page import="org.punksearch.web.SearchParams"%>

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