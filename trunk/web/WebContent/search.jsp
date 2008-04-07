<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page import="org.punksearch.web.*" %>
<%@ page import="org.punksearch.common.IndexFields"%>
<%@ page import="org.punksearch.web.online.CachedOnlineChecker" %>
<%@ page import="org.punksearch.web.filters.TypeFilters" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>
<%
		SearchParams params = new SearchParams(request);
		session.setAttribute("params", params);
%>
<html xmlns="http://www.w3.org/1999/xhtml">
	
	<%@ include file="inc/head.jsp"%>
	
	<body onload="setFocus('<%= params.type %>')">
	
		<script type="text/javascript" src="js/search.js" > </script>
		
		<%@ include file="inc/header.jsp" %>
		<jsp:include page="inc/search/query.jsp" />
		<jsp:include page="inc/search/results.jsp" />
		
		<div id="hint">
			use "+/-" to specialize search terms, like: "+pink -floyd"
		</div>
	</body>
</html>
