<%@ page contentType="text/html; charset=UTF-8" %>
<%@ include file="/WEB-INF/inc/imports.jsp" %>
<%@ page import="org.punksearch.web.SearchParams"%>

<%
    String defaultTabs = "films,serials,music,clips,pictures";
    String tabs = System.getProperty("org.punksearch.web.tabs", defaultTabs);
    tabs = "everything," + tabs + ",advanced";
    String[] tabsArray = tabs.split(",");
%>
<c:set var="tabsArray" value="<%=tabsArray%>" />

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