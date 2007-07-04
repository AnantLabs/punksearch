<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="org.punksearch.web.SearchParams" %>
<%@ page import="org.punksearch.web.SearchResult" %>
<%@ page import="org.punksearch.web.SearchAction" %>

<%! 
	boolean showScores = false;
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<title>PUNK LAN Search</title>
		<link href="css/style.css" type="text/css" rel="stylesheet" />
		<script>
			function changeSearchType(type)
			{
				var params = "?type="+type;
				var queryElement = document.forms["searchForm"].elements["query"];
				var queryParam = ((queryElement==null)||(queryElement.value.length==0)) ? "" : "&query=" + queryElement.value;				
				if (type == "advanced") params+="&ftp=on&smb=on"; else params+=queryParam;
				window.location = params;
			}
			function validateQuery()
			{
				var queryElement = document.forms["searchForm"].elements["query"];
				var query = queryElement==null ? "" : queryElement.value;
				var adultsOnlyRegExp = /.*(?:porno)|(?:sex)|(?:xxx).*/i
				var xxx = query.match(adultsOnlyRegExp);
				var allowed = true;
				if (xxx !=null) allowed = confirm("Attention! \n" + 
												  "You are trying to search files that may contains information allowed for adult persons only. \n" +
												  "If you are older than 14 press 'OK' else press 'Cancel'");
				return allowed;
			}
		</script>
	</head>
	
<%	
	SearchParams params = new SearchParams(request);
	String[][] searchTabs = {{"Everything","everything"},{"Films","films"},{"Music","music"},{"Advanced","advanced"}};
%>
	
<body>

<%@ include file="header.jsp"%>

<div id="searchTabsContainer">
	<table cellspacing=0 id="searchTabs">
		<tr>
			<td style="border-right: 1px solid white; width:50%">&#160;</td>
			<%
			for (String[] tab: searchTabs)
			{			
				if (tab[1].equals(params.type))
				{
					%><td class="tab selected"><%= tab[0] %></d><%		
				}
				else
				{
					%><td class="tab"><a href="changeSearchType" onclick="changeSearchType('<%= tab[1] %>'); return false;"><%= tab[0] %></a></td><%
				}
				if (searchTabs[searchTabs.length-1]!=tab)
				{
					int padding = 2;
					if (searchTabs[searchTabs.length-2]==tab) padding = 20; // spacer before "Advanced" tab
					%><td class="spacer" style="padding-left:<%=padding%>px;">&#160;</td><%		
				}
			}
			%>
			<td style="border-left: 1px solid white; width:50%">&#160;</td>
		</tr>
	</table>
</div>

<div id="searchFormContainer">
	<div style="position:absolute; left:0px; top:0px; width:100px; height:100%; background-color:#004368; border-right:1px solid white;"></div>
	<form id="searchForm" action="search_new.jsp" method="get" onsubmit="return validateQuery();">	
		<input type="hidden" name="type" value="<%=params.type%>" />
		<% 	
		if (params.type.equals("advanced"))
		{
		%>
			<div class="fieldset">
				path&nbsp;<input type="text" id="dir"  name="dir"  value="<%= params.dir  %>" size="20" />
				file&nbsp;<input type="text" id="file" name="file" value="<%= params.file %>" size="20" />
				ext&nbsp;<input  type="text" id="ext"  name="ext"  value="<%= params.ext  %>" size="20" />
			</div>	
			<table class="fieldset" width="100%" style="font-size:16px; font-weight:bold;">
				<tr>
					<td>
						size<span style="vertical-align: sub; font-size: 12px;">Mb</span>						
					</td>
					<td>
						Hint<span style="color:white; font-weight:normal"> use "!" to negate search terms</span>				
					</td>
					<td>
						date<span style="vertical-align: sub; font-size: 12px;">dd.mm.yy</span>
					</td>						
				</tr>				
				<tr>
					<td>
						<input type="text" id="minSize" name="minSize" value="<%= params.getStringValue("minSize") %>" size="6" style="font-size:16px; font-weight:bold"/>&nbsp;-
						<input type="text" id="maxSize" name="maxSize" value="<%= params.getStringValue("maxSize") %>" size="6" style="font-size:16px; font-weight:bold"/>						
					</td>
					<td>
						ftp<input type="checkbox" id="ftp" name="ftp" <%= params.ftp ? "checked" : "" %> style="border:0px;"/>
						&nbsp;<input type="submit" value="search"/>&nbsp;
						<input type="checkbox" id="smb" name="smb" <%= params.smb ? "checked" : "" %> style="border:0px;"/>smb
					</td>
					<td>
						<input type="text" id="fromDate" name="fromDate" value="<%= params.getStringValue("fromDate") %>" size="6" style="font-size:16px; font-weight:bold"/>&nbsp;-
						<input type="text" id="toDate" name="toDate" value="<%= params.getStringValue("toDate") %>" size="6" style="font-size:16px; font-weight:bold"/>						
					</td>
				</tr>						
			</table>						
		<%									
		}
		else
		{
		%>
			<input type="text" name="query" value="<%=params.query%>" style="width:480px;" />
			<input type="submit" value="search"/>					
		<%					
		}
		%>
	</form>
</div>

<div id="searchResultsContainer">
	<%
		SearchAction searchAction  = new SearchAction(params);
		List<SearchResult> searchResults = searchAction.doSearch();
		if (searchResults != null)
		{
			if(!searchResults.isEmpty())
			{
				for (SearchResult file : searchResults)
				{
				%>
				<p><%=file.name%></p>
				<%
				}
			}
			else
			{
				%><div class="infoMessage">Search yields no results</div><%	
			}
		}
	%>
</div>
</body>
</html>
