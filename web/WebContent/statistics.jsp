<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@page import="org.punksearch.web.statistics.FileTypeStatistics"%>
<%@page import="org.punksearch.common.PunksearchProperties"%>
<%@page import="org.apache.lucene.index.IndexReader"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.util.Map"%>
<%@page import="org.jfree.data.general.DefaultPieDataset"%>
<%@page import="org.jfree.chart.JFreeChart"%>
<%@page import="org.jfree.chart.ChartFactory"%>
<%@page import="java.text.NumberFormat" %>
<%@ page session="true" %> 
<html xmlns="http://www.w3.org/1999/xhtml">
	<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<title>Statistics - PUNKSearch</title>
		<link href="css/style.css" type="text/css" rel="stylesheet" />
		<script type="text/javascript" src="js/common.js"> </script>
	</head>
	<body id="statistics">
		<h2>Index Statistics</h2>
		<%
			String indexDir = PunksearchProperties.resolveIndexDirectory();
			IndexReader ir = IndexReader.open(indexDir);
			NumberFormat nf = NumberFormat.getNumberInstance();
			NumberFormat nfPercent = NumberFormat.getPercentInstance();
			nfPercent.setMaximumFractionDigits(2);
			//nfPercent.setMinimumIntegerDigits(2);
			
			Long totalSize = FileTypeStatistics.totalSize();
			int totalCount = ir.numDocs();

		%>
		<table align="center">
			<tr>
				<th>Path to Index Directory</th>
				<td><%= indexDir %></td>
			</tr>
			<tr>
				<th>Last Modified</th>
				<td><%= new Date(IndexReader.lastModified(indexDir)).toString() %></td>
			</tr>
			<tr>
				<th>Total Count of Indexed Items</th>
				<td><%= nf.format(totalCount) %></td>
			</tr>
			<tr>
				<th>Total Size of Indexed Data</th>
				<td><%= nf.format(totalSize) %> bytes</td>
			</tr>
		</table>
		
		<hr/>
		
		<%
		Map<String, Long> countValues = FileTypeStatistics.count();
		JFreeChart countChart = ChartFactory.createPieChart("Count Distribution (items/type) *", FileTypeStatistics.makePieDataset(countValues, totalCount), false, true, false);
		session.setAttribute("countChart", countChart);
		%>
		<table align="center">
			<tr>
				<td>
					<img src="chart/countChart" height="300" width="480" /><br/>
					* assumes filetypes.conf defines disjunct file sets
				</td>
			</tr>
			<tr>
				<td><a href="javascript:toggleTable('count_table')">toggle table</a></td>
			</tr>
			<tr>
				<td>
					<table id="count_table" cellpadding="2" cellspacing="1" style="background-color: gray; display:none; width: 100%;">
						<tr><th>type</th><th>%</th><th>value</th></tr>
						<%
						int sum = 0;
						for (String key : countValues.keySet()) {
						sum += countValues.get(key);
						%>
						<tr><td><%= key %></td><td align="right" ><%= nfPercent.format(countValues.get(key) / (totalCount + 0.0)) %></td><td align="right" ><%= nf.format(countValues.get(key)) %></td></tr>
						<% } %>
						<tr><td>other</td><td align="right" ><%= nfPercent.format((totalCount - sum) / (totalCount + 0.0)) %></td><td align="right" ><%= nf.format(totalCount - sum) %></td></tr>
					</table>
				</td>
			</tr>
		</table>
		
		<br/>
		<br/>

		<%
		Map<String, Long> sizeValues = FileTypeStatistics.size();
		JFreeChart sizeChart = ChartFactory.createPieChart("Size Distribution (bytes/type) *", FileTypeStatistics.makePieDataset(sizeValues, totalSize), false, true, false);
		session.setAttribute("sizeChart", sizeChart);
		%>
		<table align="center">
			<tr>
				<td>
					<img src="chart/sizeChart" height="300" width="480" /><br/>
					* only known file types participating, directories and "other" are omitted
				</td>
			</tr>
			<tr>
				<td><a href="javascript:toggleTable('size_table')">toggle table</a></td>
			</tr>
			<tr>
				<td>
					<table id="size_table" cellpadding="2" cellspacing="1" style="background-color: gray; display:none; width: 100%;">
						<tr><th>type</th><th>%</th><th>value</th></tr>
						<%
						long sumSize = 0;
						for (String key : sizeValues.keySet()) {
						sumSize += sizeValues.get(key);
						%>
						<tr><td><%= key %></td><td align="right" ><%= nfPercent.format(sizeValues.get(key) / (totalSize + 0.0)) %></td><td align="right" ><%= nf.format(sizeValues.get(key)) %></td></tr>
						<% } %>
						<tr><td>other</td><td align="right" ><%= nfPercent.format((totalSize - sumSize) / (totalSize + 0.0)) %></td><td align="right" ><%= nf.format(totalSize - sumSize) %></td></tr>
					</table>
				</td>
			</tr>
		</table>

	</body>
</html>