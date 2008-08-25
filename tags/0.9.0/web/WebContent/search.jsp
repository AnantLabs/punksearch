<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page import="org.punksearch.web.*" %>
<%
		SearchParams params = new SearchParams(request);
		session.setAttribute("params", params);
%>
<%@page import="org.punksearch.common.PunksearchFs"%>
<html xmlns="http://www.w3.org/1999/xhtml">
	
	<%@ include file="/WEB-INF/inc/head.jsp"%>
	
	<body onload="setFocus('<%= params.type %>')">
	
		<script type="text/javascript" src="js/search.js" > </script>
		
		<%@ include file="/WEB-INF/inc/header.jsp" %>
		<jsp:include page="/WEB-INF/inc/search/query.jsp" />
		<% if (SearcherWrapper.isReady()) { %>
		<jsp:include page="/WEB-INF/inc/search/results.jsp" />
		<% } else { %>
		<div class="errorMessage">
			PUNKSearch is not ready.<br/>
			Index directory "<%= PunksearchFs.resolveIndexDirectory() %>" is invalid.<br/>
			Either crawl the network or supply correct index directory.
		</div>
		<% } %>
		
		<div id="hint">
			use "+/-" to specialize search terms, like: "+pink -floyd"
		</div>
	</body>
</html>