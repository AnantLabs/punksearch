<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@page import="org.punksearch.web.statistics.FileTypeStatistics"%>
<%@page import="org.punksearch.web.Types"%>
<%@page import="org.punksearch.web.SearcherConfig"%>
<%@page import="org.apache.lucene.index.IndexReader"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.DateFormat"%>
<html xmlns="http://www.w3.org/1999/xhtml">
	<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<title>Statistics - PUNKSearch</title>
		<link href="css/style.css" type="text/css" rel="stylesheet" />
	</head>
	<body>
		<h2>Main</h2>
		<%
			IndexReader ir = IndexReader.open(SearcherConfig.getInstance().getIndexDirectory());
		%>
		Last Modified: <%= new Date(IndexReader.lastModified(SearcherConfig.getInstance().getIndexDirectory())).toString() %>
		<br/>
		Total documents: <%= ir.numDocs() %>
		
		<h2>Types</h2>
		<% 
			int total = FileTypeStatistics.count("");
			int dir   = FileTypeStatistics.count(Types.DIR);
			int films = FileTypeStatistics.count(Types.FILM);
			int clips = FileTypeStatistics.count(Types.CLIP);
			int music = FileTypeStatistics.count(Types.MUSIC);
			int iso   = FileTypeStatistics.count(Types.ISO);
			int pict  = FileTypeStatistics.count(Types.PICTURE);
			int arch  = FileTypeStatistics.count(Types.ARCHIVE);
			int exe   = FileTypeStatistics.count(Types.EXE);
			int doc   = FileTypeStatistics.count(Types.DOC);
		%>
		<table>
			<tr><th>type</th><th>count</th></tr>
			<tr>
				<td>films</td><td><%= films %></td>
			</tr>
			<tr>
				<td>clips</td><td><%= clips %></td>
			</tr>
			<tr>
				<td>music</td><td><%= music %></td>
			</tr>
			<tr>
				<td>disc</td><td><%= iso %></td>
			</tr>
			<tr>
				<td>picture</td><td><%= pict %></td>
			</tr>
			<tr>
				<td>archive</td><td><%= arch %></td>
			</tr>
			<tr>
				<td>exe</td><td><%= exe %></td>
			</tr>
			<tr>
				<td>doc</td><td><%= doc %></td>
			</tr>
			<tr>
				<td>directory</td><td><%= dir %></td>
			</tr>
			<tr>
				<td>other</td><td><%= total - dir - films - clips - music - iso - pict - arch - exe - doc %></td>
			</tr>
			<tr>
				<td>total</td><td><%= total %></td>
			</tr>
		</table>
	
	</body>
</html>