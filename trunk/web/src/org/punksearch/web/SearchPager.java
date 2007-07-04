package org.punksearch.web;

public class SearchPager {

	private static final int PAGE_LINKS_PER_SIDE = 10;
	public static final int PAGE_SIZE = 15;


    private static String makePagesRow(int cur, int all)
    {
    	if (all <= PAGE_SIZE)
    		return "";

    	String result = "<div>";
    	
    	int maxNum = (all%PAGE_SIZE == 0)? all/PAGE_SIZE : all/PAGE_SIZE + 1; 
    	//int pageNum = 0;
    	int startNum = (cur - PAGE_LINKS_PER_SIDE < 0)? 0 : cur - PAGE_LINKS_PER_SIDE;
    	int stopNum  = (cur + PAGE_LINKS_PER_SIDE + 1 > maxNum)? maxNum : cur + PAGE_LINKS_PER_SIDE + 1;
    	
    	String urlPrefix = "search.jsp?";
    	for (Object o_key : req.getParameterMap().keySet())
    	{
    		String key = (String) o_key;
    		if (!key.equals("first") && !key.equals("last"))
    		{
    				urlPrefix += key + "=" + getParameter(key) + "&";
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

    private static String makePageLink(String url, int num)
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
	
	
}
