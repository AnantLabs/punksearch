<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ include file="/WEB-INF/inc/imports.jsp" %>
<%@ page import="org.punksearch.common.PunksearchFs" %>
<%@ page import="org.punksearch.web.SearchParams"%>
<%@ page import="org.punksearch.web.SearcherWrapper" %>
<%
    SearchParams params = new SearchParams(request);
    request.setAttribute("params", params);
%>
<c:set var="currentTab" value="${params.type}" />

<html xmlns="http://www.w3.org/1999/xhtml">

	<%@ include file="/WEB-INF/inc/head.jsp"%>

	<body onload="setFocus('${currentTab}')">

		<script type="text/javascript" src="js/jQuery/jquery-1.8.0.min.js" > </script>
		<script type="text/javascript" src="js/zeroclipboard/ZeroClipboard.js" > </script>
		<script type="text/javascript" src="js/search.js" > </script>

		<%@ include file="/WEB-INF/inc/header.jsp" %>

        <%@ include file="/WEB-INF/inc/search/tabs.jsp" %>

        <c:choose>
            <c:when test="${currentTab == 'scanned'}">
                <%@ include file="WEB-INF/inc/scanned.jsp"%>
            </c:when>
            <c:otherwise>
                <%@ include file="/WEB-INF/inc/search/query.jsp" %>

                <% if (SearcherWrapper.isReady()) { %>

                <%@ include file="/WEB-INF/inc/search/results.jsp" %>

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
            </c:otherwise>
        </c:choose>
	</body>
</html>
