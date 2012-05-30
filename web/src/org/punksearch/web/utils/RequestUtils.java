package org.punksearch.web.utils;

import javax.servlet.http.HttpServletRequest;

/**
 * User: gubarkov
 * Date: 30.05.12
 * Time: 18:55
 */
public class RequestUtils {
    public static final String USER_AGENT = "User-Agent";

    public static BrowserOS getBrowserOS(HttpServletRequest request) {
        String userAgent = request.getHeader(USER_AGENT);
        userAgent = userAgent == null ? "" : userAgent.toLowerCase();

        if (userAgent.contains("linux")) {
            // TODO: mac os?
            return BrowserOS.UNIX_LIKE;
        } else if (userAgent.contains("windows")){
            return BrowserOS.WINDOWS;
        } else {
            return BrowserOS.UNKNOWN;
        }
    }
}
