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

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class SearchParams {
	private static Log       __log       = LogFactory.getLog(SearchParams.class);

	private static final String DATE_FORMAT = "yyyy-MM-dd";

	public String               type        = "everything";
	public String               query       = null;

	public String               dir         = null;
	public String               file        = null;
	public String               ext         = null;

	public Long                 minSize     = null;
	public Long                 maxSize     = null;

	public Long                 fromDate    = null;
	public Long                 toDate      = null;

	public Integer              first       = 0;
	public Integer              last        = SearchPager.PAGE_SIZE - 1;

	public SearchParams(HttpServletRequest request) {
		this.type = getStringValue(request, "type");
		if (this.type.length() == 0)
			this.type = "everything";

		/* first & last */
		this.first = getIntegerValue(request, "first", 0);
		this.last = getIntegerValue(request, "last", SearchPager.PAGE_SIZE - 1);

		if (type.equals("advanced")) {
			/* dir & file & ext */
			this.dir = getStringValue(request, "dir");
			this.file = getStringValue(request, "file");
			this.ext = getStringValue(request, "ext");

			/* maxSize & minSize */
			this.minSize = (Long) Math.round(getDoubleValue(request, "minSize", 0.0) * 1024 * 1024);
			this.maxSize = (Long) Math.round(getDoubleValue(request, "maxSize", 0.0) * 1024 * 1024);
			if (minSize == 0)
				minSize = null;
			if (maxSize == 0)
				maxSize = null;
			if (this.minSize != null && this.maxSize != null && this.minSize > this.maxSize) {
				this.minSize = this.maxSize = null;
			}

			/* fromDate & toDate */
			DateFormat fmt = new SimpleDateFormat(DATE_FORMAT);
			try {
				this.fromDate = fmt.parse(getStringValue(request, "fromDate")).getTime();
			} catch (ParseException pe) {
				// __log.warning(pe.getMessage());
			}
			try {
				this.toDate = fmt.parse(getStringValue(request, "toDate")).getTime();
			} catch (ParseException pe) {
				// __log.warning(pe.getMessage());
			}
			if (this.fromDate != null && this.toDate != null && this.fromDate > this.toDate) {
				this.fromDate = this.toDate = null;
			}
		} else {
			this.query = getStringValue(request, "query");
		}
	}

	public static String getStringValue(HttpServletRequest request, String paramName) {
		String paramValue = request.getParameter(paramName);
		if (paramValue == null)
			return "";

//        OMG WTF??? this prevents russian queries!
/*		try {
			paramValue = new String(paramValue.getBytes("ISO-8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException uee) {
			__log.warn(uee.getMessage());
		}*/

		return paramValue;
	}

	public static Integer getIntegerValue(HttpServletRequest request, String paramName, Integer byDefault) {
		String paramValue = request.getParameter(paramName);
		if (paramValue == null || paramValue.equals(""))
			return byDefault;

		return Integer.valueOf(paramValue);

	}

	public static Double getDoubleValue(HttpServletRequest request, String paramName, Double byDefault) {
		String paramValue = request.getParameter(paramName);
		if (paramValue == null || paramValue.equals(""))
			return byDefault;

		return Double.valueOf(paramValue);

	}

	public static Boolean getBooleanValue(HttpServletRequest request, String paramName, Boolean byDefault) {
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