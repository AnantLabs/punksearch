<%@ include file="/WEB-INF/inc/imports.jsp" %>
<%@ page import="org.punksearch.Core" %>
<%@ page import="org.punksearch.common.PunksearchHost" %>
<%@ page import="java.util.List" %>
<%
    final List<PunksearchHost> indexedHosts = Core.getPunksearchLogic().listIndexedHosts();
    request.setAttribute("indexedHosts", indexedHosts);
%>

<h2>Scanned hosts</h2>

<table align="center" class="data">
    <tr>
        <th>Protocol</th>
        <th>IP</th>
        <th>Host</th>
        <th>Online status</th>
    </tr>
    <c:forEach items="${indexedHosts}" var="host">
        <c:set var="online" value="${host.online ? 'online' : 'offline'}"/>
        <tr>
            <td>${host.protocol}</td>
            <td>${host.ip}</td>
            <td>${host.hostName}</td>
            <td class="${online}">${online}</td>
        </tr>
    </c:forEach>
</table>

