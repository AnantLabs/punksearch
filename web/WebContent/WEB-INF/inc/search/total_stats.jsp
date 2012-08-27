<%@ page import="org.apache.commons.io.FileUtils" %>
<%@ page import="org.punksearch.stats.TotalStats" %>
<%@ page import="org.punksearch.stats.TotalStatsReader" %>

<% TotalStats totalStats = TotalStatsReader.getCurrent(); %>
<c:set var="totalStats" value="<%= totalStats %>"/>

<table align="center" class="data">
    <tr>
        <td>Last scan date</td>
        <td><fmt:formatDate value="${totalStats.scanDate}" pattern="yyyy-MM-dd HH:mm"/></td>
    </tr>
    <tr>
        <td>Scanned hosts</td>
        <td>${totalStats.scannedHosts}</td>
    </tr>
    <tr>
        <td>Scanned shares</td>
        <td>${totalStats.scannedShares}</td>
    </tr>
    <tr>
        <td>Scanned files</td>
        <td><fmt:formatNumber value="${totalStats.scannedFiles}"/></td>
    </tr>
    <tr>
        <td>Scanned data</td>
        <td>
            <%= FileUtils.byteCountToDisplaySize(totalStats.getScannedBytes())%>
        </td>
    </tr>
</table>