<%@page import="org.punksearch.common.PunksearchProperties"%>
<%@page import="org.punksearch.common.FileTypes"%>
<%@page import="org.punksearch.web.filters.TypeFilters"%>
<%@page import="org.punksearch.web.Properties"%>
<%@page import="java.util.Map"%>
<div id="config">

	<h2>Current Config</h2>

	<h3>system properties</h3>
	<table align="center" class="data">
	<%
		Map<String, String> properties = Properties.getPunksearchProperties();
		for (String key : properties.keySet()) {
	%>
	<tr><th><%= key.toString() %></th><td><%= properties.get(key.toString()) %></td></tr>	
	<%			
		}
	%>
	</table>

	<h3>file types</h3>
	<table align="center" class="data">
		<tr><th>title</th><th>min (bytes)</th><th>max (bytes)</th><th>extensions</th></tr>
		<% for (String title : TypeFilters.getTypes().list()) { %>
		<%
			long min = TypeFilters.getTypes().get(title).getMinBytes();
			long max = TypeFilters.getTypes().get(title).getMaxBytes();
		%>
		<tr>
			<td><%= title %></td>
			<td><%= (min == 0)? "-" : min %></td>
			<td><%= (max == Long.MAX_VALUE)? "-" : max %></td>
			<td><%= TypeFilters.getTypes().get(title).getExtensions() %></td>
		</tr>	
		<% } %>
	</table>

</div>