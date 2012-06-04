<%@ include file="/WEB-INF/inc/imports.jsp" %>
<%@ page import="org.punksearch.Core" %>
<%@ page import="org.punksearch.common.PunksearchHost" %>
<%@ page import="java.util.List" %>
<%
    final List<PunksearchHost> indexedHosts = Core.getPunksearchLogic().listIndexedHosts();
    request.setAttribute("indexedHosts", indexedHosts);
%>

<table>
    <c:forEach items="${indexedHosts}" var="host">
        <tr><td>${host.hostName}</td></tr>
    </c:forEach>
</table>

