<%@ page contentType="text/html; charset=UTF-8" %>
<%@ include file="/WEB-INF/inc/imports.jsp"%>
<%@ page import="org.punksearch.common.Settings"%>
<%@ page import="org.punksearch.web.*" %>
<%@ page import="org.punksearch.web.utils.BrowserOS" %>
<%@ page import="org.punksearch.web.utils.RequestUtils" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>

<% boolean showScores = Settings.getBool("org.punksearch.web.showscores"); %>
<div id="searchResultsContainer">
	<%
        BrowserOS os = RequestUtils.getBrowserOS(request);
		Map parameterMap = request.getParameterMap();
		if (!parameterMap.isEmpty() && !(parameterMap.size() == 1 && parameterMap.get("type") != null)) {
			SearchAction searchAction = new SearchAction(params);
			List<ItemGroup> searchResults = searchAction.doSearchGroupped(params.first, params.last);
			int items = searchAction.getOverallCount();
			int pages = SearchPager.getPageCount(searchAction.getOverallCount());
			long searchTime = searchAction.getSearchTime();
			long presentationTime = searchAction.getPresentationTime();

            SearchHistory.logSearch(request, params, items,searchTime,presentationTime);

			if (!searchResults.isEmpty()) {
	%>
	<table id="pager" cellspacing="0" cellpadding="0" align="center">
		<tr>
			<td style="font-size: 12px; text-align: left; padding-left: 2px;">
				<span style="font-size: 18px;"><%=items%></span> items (<%=pages%> pages) in <span title="<%=searchTime / 1000.0 + " search + " + presentationTime / 1000.0 + " presentation"%>"><%= (searchTime + presentationTime) / 1000.0 %></span> secs
			</td>
			<td style="text-align: right; vertical-align: bottom;"><%=SearchPager.makePagesRow(request, searchAction.getOverallCount())%></td>
		</tr>
	</table>

	<table id="results" cellspacing="0" align="center">
		<%
			int counter = 0;
			for (ItemGroup group : searchResults) {
				SearchResult file = new SearchResult(group.getItems().get(0), os);
				counter++;
		%>
		<tr>
			<td style="width: 16px; padding-right: 2px; vertical-align: top; padding-top: 4px;">
                <punksearch:icon_and_online_status file="<%= file %>" />
			</td>
			<td style="padding-left: 2px;">
				<span style="font-size: 12pt;" class="name"><%=file.name%></span>
				<span class="more"><%=group.getItems().size() > 1 ? "( <a href=\"#\" onClick=\"toggle('subGroup" + counter + "');return false;\">" + (group.getItems().size() - 1) + " more</a> )" : ""%></span>
				<%= showScores ? "(" + file.score + ")" : ""%><br/>
                <punksearch:path_line file="<%= file %>" />
                <br/>
				<div class="othersInGroup" id="subGroup<%= counter %>" style="display:none;">
					<table>
						<%
							for (int i = 1; i < group.getItems().size(); i++) {
								SearchResult subFile = new SearchResult(group.getItems().get(i), os);
						%>
						<tr>
							<td>
                                <punksearch:icon_and_online_status file="<%= subFile %>" />
                            </td>
							<td>
								<span class="name"><%= subFile.name %></span><br/>
                                <punksearch:path_line file="<%= subFile %>" />
							</td>
						</tr>
						<%
							}
						%>
					</table>
				</div>
			</td>
			<td style="text-align: right;"><%= file.date %></td>
			<td style="text-align: right;"><%= file.size %> Mb</td>
		</tr>
		<%
			}
		%>
	</table>
	<%
		} else {
	%><div class="infoMessage">search yields no results</div><%
			}
		}
	%>
</div>
