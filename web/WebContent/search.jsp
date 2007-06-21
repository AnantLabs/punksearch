<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%@ page import="org.apache.lucene.index.*" %>
<%@ page import="org.apache.lucene.search.*" %>
<%@ page import="org.apache.lucene.document.Document" %>
<%@ page import="ru.spbu.dorms.arpm.searcher.*" %>
<%@ page import="ru.spbu.dorms.arpm.commons.SearcherConstants" %>
<%!
	private static final int MIN_TERM_LENGTH = 3;
	private static final int PAGE_LINKS_PER_SIDE = 10;
	private static final int PAGE_SIZE = 15;

	private HttpServletRequest req;
	
	private void setRequest(HttpServletRequest request)
	{
		req = request;
	}
	
    private List<String> prepareQueryParameter(String str)
    {
    	List<String> result = new ArrayList<String>();
    	if (str != null)
    	{
	    	String[] terms = str.toLowerCase().split(" ");
	    	for (String term : terms)
	    	{
	    		term = term.replace("*", "");
	    		term = term.trim();
	    		if (term.length() >= MIN_TERM_LENGTH)
	    		{
	    			result.add(term);
	    		}
	    	}
    	}
    	return result;
    }
    
    private Query makeQuery(List<String> dirTerms, List<String> fileTerms, List<String> extTerms)
    {
		BooleanQuery query = new BooleanQuery(false);
		BooleanQuery.setMaxClauseCount(Integer.parseInt(getServletContext().getInitParameter("searchMaxClauseCount")));

		if (fileTerms.size() != 0 || extTerms.size() != 0) // search for files
		{
			if (fileTerms.size() != 0)
			{
				BooleanQuery fileQuery = new BooleanQuery();
				for (String item : fileTerms)
				{
					BooleanQuery itemQuery = new BooleanQuery();
					Query nameQuery = new WildcardQuery(new Term(SearcherConstants.NAME, "*" + item + "*"));
					itemQuery.add(nameQuery, BooleanClause.Occur.MUST);
					Query extensionQuery = new TermQuery(new Term(SearcherConstants.EXTENSION, SearcherConstants.DIRECTORY_EXTENSION));
					itemQuery.add(extensionQuery, BooleanClause.Occur.MUST_NOT);
					fileQuery.add(itemQuery, BooleanClause.Occur.SHOULD);
				}
				query.add(fileQuery, BooleanClause.Occur.MUST);
			}
			
			if (extTerms.size() != 0)
			{
				BooleanQuery extQuery = new BooleanQuery();
				for (String item : extTerms)
				{
					Query termQuery = new TermQuery(new Term(SearcherConstants.EXTENSION, item));
					extQuery.add(termQuery, BooleanClause.Occur.SHOULD);
				}
				query.add(extQuery, BooleanClause.Occur.MUST);
			}
			
			if (dirTerms.size() != 0) // restrict files to occur in specified directories only
			{
				BooleanQuery dirQuery = new BooleanQuery();
				for (String item : dirTerms)
				{				
					Query pathQuery = new WildcardQuery(new Term(SearcherConstants.PATH, "*" + item + "*"));
					dirQuery.add(pathQuery, BooleanClause.Occur.SHOULD);
				}
				query.add(dirQuery, BooleanClause.Occur.MUST);
			}
		}
		else if (dirTerms.size() != 0) // search for directories only, since file name was not specified
		{
			for (String item : dirTerms)
			{				
				BooleanQuery dirQuery = new BooleanQuery();
				Query nameQuery = new WildcardQuery(new Term(SearcherConstants.NAME, "*" + item + "*"));
				dirQuery.add(nameQuery, BooleanClause.Occur.MUST);
				Query extensionQuery = new TermQuery(new Term(SearcherConstants.EXTENSION, SearcherConstants.DIRECTORY_EXTENSION));
				dirQuery.add(extensionQuery, BooleanClause.Occur.MUST);
				query.add(dirQuery, BooleanClause.Occur.SHOULD);
			}
		}
		return query;
    }
    
    private String padWithZeroes(Long value)
    {
		String result = Math.round((value+0.0f)/(1024*1024)*100)/100.0f + "";
		if (result.indexOf(".") == -1)
		{
			result += ".00";
		}
		else
		{
			int gap = result.length() - result.indexOf(".") - 1;
			for (int digit = gap; digit < 2; digit++)
			{
				result += "0";
			}
		}
    	return result;
    }
    
    private String makePagesRow(int cur, int all, int size)
    {
    	if (all <= size)
    		return "";

    	String result = "<div>";
    	
    	int maxNum = (all%size == 0)? all/size : all/size + 1; 
    	//int pageNum = 0;
    	int startNum = (cur - PAGE_LINKS_PER_SIDE < 0)? 0 : cur - PAGE_LINKS_PER_SIDE;
    	int stopNum  = (cur + PAGE_LINKS_PER_SIDE + 1 > maxNum)? maxNum : cur + PAGE_LINKS_PER_SIDE + 1;
    	
    	String urlPrefix = "search.jsp?";
    	for (Object o_key : req.getParameterMap().keySet())
    	{
    		String key = (String) o_key;
    		if (!key.equals("first") && !key.equals("last"))
    		{
    			//if (key != "min" && key != "max")
    				urlPrefix += key + "=" + getParameter(key) + "&";
    			//else
	    		//	urlPrefix += key + "=" + Integer.valueOf(getParameter(key)) + "&";
    		}
    	}
    	
    	for (int i = startNum; i < stopNum; i++)
    	{
    		if (i == cur)
    		{
    			result += makePageLink("", i+1);
    		}
    		else
    		{
    			String url = urlPrefix + "first=" + (i*PAGE_SIZE) + "&last=" + ((i+1)*PAGE_SIZE-1);
    			result += makePageLink(url, i+1);
    		}
    	}
     	return result+"</div>";
    }
    
    private String makePageLink(String url, int num)
    {
    	if (url.length() != 0)
    	{
    		return "<a href=\""+url+"\" class=\"pageNumStyle\">" + num + "</a>";
    	}
    	else
    	{
    		return "<a class=\"pageCurrNumStyle\">" + num + "</a>";
    	}
    }

    String getParameter(String key)
    {
    	String result = req.getParameter(key);
    	return (result != null)? result.toString() : "";
    }
 
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page import="ru.spbu.dorms.arpm.commons.SearcherConfig"%>
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="content-type" content="application/xhtml+xml; charset=UTF-8" />	
		<title>PUNK LAN Search</title>
		<link href="css/style.css" type=text/css rel=stylesheet />		
	</head>
	<body>
		<%
			setRequest(request);
		
			//int pageSize = 15;
			boolean showScores = false;
		
			String dirParam   = getParameter("dir");
			String fileParam  = getParameter("file");
			String extParam   = getParameter("ext");
			String minParam   = getParameter("min");
			String maxParam   = getParameter("max");
			String firstParam = getParameter("first");
			String lastParam  = getParameter("last");
			String fromParam  = getParameter("from");
			String toParam    = getParameter("to");
		%>
		<div id="logo" style="position:absolute; top:10px; left:25px; font-weight:bold;font-size:15pt;color:#FFFFFF;">
			PUNK<span style="color:#FF7B00;">Search</span>
		</div>
		<div id="queryContainer" style="padding-left: 100px;">
			<form id="queryForm" action="search.jsp" method="get">
					path&nbsp;<input type="text" id="dir"  name="dir"  value="<%= dirParam  %>" size="20" />
					file&nbsp;<input type="text" id="file" name="file" value="<%= fileParam %>" size="20" />
					ext&nbsp;<input  type="text" id="ext"  name="ext"  value="<%= extParam  %>" size="10" />
					size <span style="vertical-align: sub; font-size: 8pt;">Mb</span>&nbsp;<input type="text" id="min" name="min" value="<%= minParam %>" size="4" />&nbsp;:&nbsp;<input type="text" id="max" name="max" value="<%= maxParam %>" size="4" />
					<input type="submit" value="search" class="button"/>&nbsp;
			</form>
			<form action="search.jsp" method="get" style="display:inline;">
					<input type="hidden" name="ext" value="avi" />
					<input type="hidden" name="min" value="300" />
					<input type="hidden" name="from" value="<%= new Date().getTime() - (7 * 24 * 3600 * 1000) %>" />
					<input type="submit" value="Films 7" class="button"/>
			</form>
			<form action="search.jsp" method="get" style="display:inline;">
					<input type="hidden" name="ext" value="avi" />
					<input type="hidden" name="min" value="300" />
					<input type="hidden" name="from" value="<%= new Date().getTime() - (30 * 7 * 24 * 3600 * 1000) %>" />
					<input type="submit" value="Films 30" class="button"/>
			</form>
			<form action="search.jsp" method="get" style="display:inline;">
					<input type="hidden" name="ext" value="mp3 wav ogg" />
					<input type="hidden" name="min" value="1" />
					<input type="hidden" name="max" value="100" />
					<input type="hidden" name="from" value="<%= new Date().getTime() - (7 * 24 * 3600 * 1000) %>" />
					<input type="submit" value="Music 7" class="button"/>
			</form>
			<form action="search.jsp" method="get" style="display:inline;">
					<input type="hidden" name="ext" value="mp3 wav ogg" />
					<input type="hidden" name="min" value="1" />
					<input type="hidden" name="max" value="100" />
					<input type="hidden" name="from" value="<%= new Date().getTime() - (30 * 7 * 24 * 3600 * 1000) %>" />
					<input type="submit" value="Music 30" class="button"/>
			</form>
			<form action="search.jsp" method="get" style="display:inline;">
					<input type="hidden" name="ext" value="iso mdf" />
					<input type="hidden" name="from" value="<%= new Date().getTime() - (7 * 24 * 3600 * 1000) %>" />
					<input type="submit" value="Iso 7" class="button"/>
			</form>
			<form action="search.jsp" method="get" style="display:inline;">
					<input type="hidden" name="ext" value="iso mdf" />
					<input type="hidden" name="from" value="<%= new Date().getTime() - (30 * 7 * 24 * 3600 * 1000) %>" />
					<input type="submit" value="Iso 30" class="button"/>
			</form>
		</div>
		<div id="resultsContainer">
			&nbsp;
			<%
				List<String> dirTerms 	= prepareQueryParameter(dirParam);
				List<String> fileTerms	= prepareQueryParameter(fileParam);
				List<String> extTerms	= prepareQueryParameter(extParam);
				Query query = makeQuery(dirTerms, fileTerms, extTerms);

				Integer first = (firstParam.length() != 0)? Integer.valueOf(firstParam) : 0;
				Integer last  = (lastParam.length()  != 0)? Integer.valueOf(lastParam) : PAGE_SIZE - 1;
				
				Long  min  = ((minParam != null) && (minParam.length() > 0))? (Long)Math.round(Double.valueOf(minParam)*1024*1024) : null;
				Long  max  = ((maxParam != null) && (maxParam.length() > 0))? (Long)Math.round(Double.valueOf(maxParam)*1024*1024) : null;
				if (min != null && max != null && min > max)
				{
					min = null;
					max = null;
				}
				NumberRangeFilter sizeFilter = null;
				if (min != null || max != null)
				{
					sizeFilter = LuceneSearcher.createNumberFilter(SearcherConstants.SIZE, min, max);
				}
				
				Long  from = ((fromParam != null) && (fromParam.length() > 0))? Long.valueOf(fromParam) : null;
				Long  to   = ((toParam   != null) && (toParam.length()   > 0))? Long.valueOf(toParam)   : null;
				if (from != null && to != null && from > to)
				{
					from = null;
					to   = null;
				}
				NumberRangeFilter dateFilter = null;
				if (from != null || to != null)
				{
					dateFilter = LuceneSearcher.createNumberFilter(SearcherConstants.DATE, from, to);
				}
				
				CompositeFilter filter = null;
				if (sizeFilter != null || dateFilter != null)
				{
					filter = new CompositeFilter();
					if (sizeFilter != null) filter.add(sizeFilter);
					if (dateFilter != null) filter.add(dateFilter);
				}
				
				LuceneSearcher searcher = new LuceneSearcher(SearcherConfig.getInstance().getIndexDirectory());
				
				Date startDate = new Date();
				List<Document> results  = searcher.search(query, first, last, filter);
				Date stopDate  = new Date();
				long time = stopDate.getTime() - startDate.getTime();
				
				if (results != null && !results.isEmpty())
				{
					int	allCount = searcher.overallCount();
					int cur      = (firstParam != null && firstParam.length() != 0)? Integer.valueOf(firstParam)/PAGE_SIZE : 0;
				 	String pageNums = makePagesRow(cur, allCount, PAGE_SIZE);
					%>
						<table cellspacing="0" cellpadding="0" id="statsTable">
							<tr>
								<td style="font-size: 10pt;">
									<span style="font-size: 14pt;"><%= allCount %></span> items (<%= (allCount%PAGE_SIZE == 0)? allCount/PAGE_SIZE : allCount/PAGE_SIZE + 1  %> pages) in <%= time/1000.0 %> secs
								</td>
								<td style="text-align: right; vertical-align: bottom;"><%= pageNums %></td>
							</tr>
						</table>
						<table id="resultsTable" cellspacing="0" style="border-top: 5px solid #FF7B00;">
							<!--tr>
								<th>name</th>
								<th width="10%" style="text-align:right; padding-right: 5px;">size<sub>Mb</sub></th>
							</tr-->
					<%
					boolean dark = false;
					for (Document doc: results)
					{
						String host = doc.get(SearcherConstants.HOST);
						String path = doc.get(SearcherConstants.PATH).replaceAll("&", "&amp;");
						String name = doc.get(SearcherConstants.NAME).replaceAll("&", "&amp;");
						String size = doc.get(SearcherConstants.SIZE);
						String ex   = doc.get(SearcherConstants.EXTENSION);
						String date = doc.get(SearcherConstants.DATE);

						if (ex.length() != 0)
							name += "." + ex;
						
						String sizeStr = padWithZeroes(Long.valueOf(size));
						
						Calendar cal = new GregorianCalendar();
						cal.setTimeInMillis(Long.valueOf(date));
						String dateStr = cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH)+1) + "/" + cal.get(Calendar.YEAR);
						%>
							<tr class="<%= (dark)? "light" : "light" %>">
								<td style="width: 16px; padding-right: 2px; vertical-align: top; padding-top: 4px;">
									<img src="images/<%= (ex.length() != 0)? "stock_new-16.png" : "stock_folder-16.png" %>"/>
								</td>
								<td style="padding-left: 2px;">
									<span style="font-size: 12pt;"><%= name %></span><%= (showScores)? "(" + doc.getBoost() + ")": "" %><br/>
									<span style="font-size: 8pt; color:#0070AD; padding-left: 0pt;"><%= host + "/" + path %></span>
								</td>
								<!--td><a href="#"><%= host + "/" + path %></a></td-->
								<td style="text-align: right;"><%= dateStr %></td>
								<td style="text-align: right;"><%= sizeStr %> Mb</td>
							</tr>
						<%
						
						dark = !dark;
					}
					%>
						</table>
					<%
				}
			%>
		</div>
		<div id="footer">
			Evgeny Shiryaev &amp; Yury Soldak<br/><em style="font-size: 8pt;">icq: 76336206, email: ysoldak@gmail.com</em>
		</div>
	</body>
</html>