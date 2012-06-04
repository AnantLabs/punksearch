<%@ tag import="org.punksearch.online.OnlineStatuses" %>
<%@ attribute name="file" type="org.punksearch.web.SearchResult" required="true" %>
<%
    boolean online = OnlineStatuses.getInstance().isOnline(file.host);
%>
<img src="images/<%= file.ext.length() != 0 ? "stock_new-16.gif" : "stock_folder-16.gif" %>"/>
<div style='font-size: 4px; margin: 1px; background-color: <%= online ? "#00FF00;" : "#FF0000;" %>;'>&nbsp;&nbsp;</div>