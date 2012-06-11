<%@ include file="/WEB-INF/inc/imports.jsp" %>
<%@ tag import="org.punksearch.logic.online.OnlineStatuses" %>
<%@ attribute name="file" type="org.punksearch.web.SearchResult" required="true" %>
<c:set var="online" value='<%= OnlineStatuses.getInstance().isOnline(file.host) ? "online" : "offline" %>'/>

<img src="images/<%= file.ext.length() != 0 ? "stock_new-16.gif" : "stock_folder-16.gif" %>"/>

<div title="${online}" style='font-size: 4px; margin: 1px;' class="${online}">&nbsp;&nbsp;</div>