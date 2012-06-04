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
        <tr>
            <td>${host.protocol}</td>
            <td>${host.ip}</td>
            <td>${host.hostName}</td>
            <td>${host.online ? 'online' : 'offline'}</td>
        </tr>
    </c:forEach>
</table>

