/***************************************************************************
 *                                                                         *
 *   PunkSearch - Searching over LAN                                       *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package org.punksearch.web;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletRequest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class SearchParams {
    private static Log log = LogFactory.getLog(SearchParams.class);

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    public String type = "everything";
    public String query = null;

    public String dir = null;
    public String file = null;
    public String ext = null;

    public Long minSize = null;
    public Long maxSize = null;

    public Long fromDate = null;
    public Long toDate = null;

    public final Integer first;
    public final Integer last;

    public SearchParams(ServletRequest request) {
        type = getStringValue(request, "type");
        if (type.length() == 0)
            type = "everything";

        /* first & last */
        first = getIntegerValue(request, "first", 0);
        last = getIntegerValue(request, "last", SearchPager.PAGE_SIZE/* - 1*/); // TODO: why -1???

        if (type.equals("advanced")) {
            /* dir & file & ext */
            dir = getStringValue(request, "dir");
            file = getStringValue(request, "file");
            ext = getStringValue(request, "ext");

            /* maxSize & minSize */
            minSize = Math.round(getDoubleValue(request, "minSize", 0.0) * 1024 * 1024);
            maxSize = Math.round(getDoubleValue(request, "maxSize", 0.0) * 1024 * 1024);
            if (minSize == 0)
                minSize = null;
            if (maxSize == 0)
                maxSize = null;
            if (minSize != null && maxSize != null && minSize > maxSize) {
                minSize = maxSize = null;
            }

            /* fromDate & toDate */
            DateFormat fmt = new SimpleDateFormat(DATE_FORMAT);

            final String fromDateStr = getStringValue(request, "fromDate");
            final String toDateStr = getStringValue(request, "toDate");

            if (StringUtils.isNotEmpty(fromDateStr)) {
                try {
                    fromDate = fmt.parse(fromDateStr).getTime();
                } catch (ParseException pe) {
                    log.warn("problem parsing fromDate: " + pe.getMessage());
                }
            }

            if (StringUtils.isNotEmpty(toDateStr)) {
                try {
                    toDate = fmt.parse(toDateStr).getTime();
                } catch (ParseException pe) {
                    log.warn("problem parsing toDate: " + pe.getMessage());
                }
            }

            if (fromDate != null && toDate != null && fromDate > toDate) {
                fromDate = toDate = null;
            }
        } else {
            query = getStringValue(request, "query");
        }
    }

    public static String getStringValue(ServletRequest request, String paramName) {
        String paramValue = request.getParameter(paramName);
        if (paramValue == null)
            return "";

//        OMG WTF??? this prevents russian queries!
/*		try {
			paramValue = new String(paramValue.getBytes("ISO-8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException uee) {
			log.warn(uee.getMessage());
		}*/

        return paramValue;
    }

    public static Integer getIntegerValue(ServletRequest request, String paramName, Integer byDefault) {
        String paramValue = request.getParameter(paramName);
        if (paramValue == null || paramValue.equals(""))
            return byDefault;

        return Integer.valueOf(paramValue);

    }

    public static Double getDoubleValue(ServletRequest request, String paramName, Double byDefault) {
        String paramValue = request.getParameter(paramName);
        if (paramValue == null || paramValue.equals(""))
            return byDefault;

        return Double.valueOf(paramValue);

    }

    public static Boolean getBooleanValue(ServletRequest request, String paramName, Boolean byDefault) {
        String paramValue = request.getParameter(paramName);
        if (paramValue == null || paramValue.equals(""))
            return byDefault;

        return paramValue.equals("true") || paramValue.equals("on");

    }

    public String getType() {
        return type;
    }

    public String getQuery() {
        return query;
    }
}