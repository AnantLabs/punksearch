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
<%@ page session="true" %> 
<html xmlns="http://www.w3.org/1999/xhtml">
	<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<title>Statistics - PUNKSearch</title>
		<link href="css/style.css" type="text/css" rel="stylesheet" />
	</head>
	<body>
		<h2>Index Statistics</h2>
		<%
		IndexReader ir = IndexReader.open(PunksearchProperties.resolveIndexDirectory());
		%>
		Last Modified: <%= new Date(IndexReader.lastModified(PunksearchProperties.resolveIndexDirectory())).toString() %>
		<br/>
		Total documents: <%= ir.numDocs() %>
		
		<hr/>
		
		<%
		int total = ir.numDocs(); //FileTypeStatistics.count("");
		Map<String, Integer> countValues = FileTypeStatistics.count();
		JFreeChart countChart = ChartFactory.createPieChart("Count Distribution", FileTypeStatistics.makePieDataset(countValues, total), false, true, false);
		session.setAttribute("countChart", countChart);
		%>
		<img src="chart/countChart" />
	</body>
</html>