package org.punksearch.web;

import javax.servlet.ServletRequest;

public class SearchPager {

    private static final int PAGE_LINKS_PER_SIDE = 10;
    public static final int PAGE_SIZE = 15;

    public static String makePagesRow(ServletRequest request, int all) {
        int cur = SearchParams.getIntegerValue(request, "first", 0) / PAGE_SIZE;

        if (all <= PAGE_SIZE)
            return "";

        StringBuilder result = new StringBuilder("<div>");

        int maxNum = (all % PAGE_SIZE == 0) ? all / PAGE_SIZE : all / PAGE_SIZE + 1;
        //int pageNum = 0;
        int startNum = (cur - PAGE_LINKS_PER_SIDE < 0) ? 0 : cur - PAGE_LINKS_PER_SIDE;
        int stopNum = (cur + PAGE_LINKS_PER_SIDE + 1 > maxNum) ? maxNum : cur + PAGE_LINKS_PER_SIDE + 1;

        StringBuilder urlPrefix = new StringBuilder("?");
        for (Object o_key : request.getParameterMap().keySet()) {
            String key = (String) o_key;

            if (!key.equals("first") && !key.equals("last")) {
                urlPrefix.append(key)
                        .append("=")
                        .append(SearchParams.getStringValue(request, key).replace("+", "%2B"))
                        .append("&");
            }
        }

        for (int i = startNum; i < stopNum; i++) {
            if (i == cur) {
                result.append(makePageLink("", i + 1));
            } else {
                String url = urlPrefix.toString() + "first=" + (i * PAGE_SIZE) + "&last=" + ((i + 1) * PAGE_SIZE - 1);
                result.append(makePageLink(url, i + 1));
            }
        }

        result.append("</div>");

        return result.toString();
    }

    public static int getPageCount(int all) {
        return (all % PAGE_SIZE == 0) ? all / PAGE_SIZE : all / PAGE_SIZE + 1;
    }

    private static String makePageLink(String url, int num) {
        if (url.length() != 0) {
            return "<a href=\"" + url + "\" class=\"pageNumStyle\">" + num + "</a>";
        } else {
            return "<a class=\"pageCurrNumStyle\">" + num + "</a>";
        }
    }
}
