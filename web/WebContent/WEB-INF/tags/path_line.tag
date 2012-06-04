<%@ attribute name="file" type="org.punksearch.web.SearchResult" required="true" %>
<span class="path"><%= file.protocol %>://<span title="<%= file.ip %>"><%=
file.hostname %></span><%= file.path %></span>

