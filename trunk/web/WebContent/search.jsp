<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@page import="org.punksearch.web.Types"%>
<%@page import="org.punksearch.commons.IndexFields"%>
<%@ page import="java.util.List" %>
<%@ page import="org.punksearch.web.online.CachedOnlineChecker" %>
<%@ page import="org.punksearch.web.SearchParams" %>
<%@ page import="org.punksearch.web.SearchResult" %>
<%@ page import="org.punksearch.web.SearchAction" %>
<%@ page import="org.punksearch.web.SearchPager" %>
<%@ page import="org.punksearch.web.filters.TypeFilters" %>
<%@ page import="org.punksearch.web.SearcherConfig" %>
<%@ page import="org.punksearch.web.ItemGroup" %>
<%@ page import="java.util.Map" %>
<%!boolean showScores = false;%>
<%
  SearchParams params = new SearchParams(request);
%>
<html xmlns="http://www.w3.org/1999/xhtml">
    <%@ include file="head.jsp"%>
    <body onload="setFocus('<%= params.type %>')">

        <%@ include file="header.jsp" %>
        <%@ include file="query.jsp" %>
        <%@ include file="results.jsp" %>

        <div id="hint">
            use "+/-" to specialize search terms, like: "+pink -floyd"
        </div>
    </body>
</html>
