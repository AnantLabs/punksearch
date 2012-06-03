package org.punksearch.web.web.filters;

import org.punksearch.common.Settings;
import org.punksearch.ip.Ip;
import org.punksearch.ip.IpRange;
import org.punksearch.ip.IpRanges;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: gubarkov
 * Date: 04.06.12
 * Time: 0:18
 */

public class IpFilter implements Filter {
    private IpRanges allowedIps;

    private static IpRanges prepareAllowedIps() {
        final String allowedIpsStr = Settings.get("org.punksearch.web.allowed_ips");
        if (allowedIpsStr != null) {
            final IpRanges ipRanges = new IpRanges(allowedIpsStr);
            ipRanges.add(new IpRange("127.0.0.1"));
            return ipRanges;
        } else {
            return null;
        }
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        allowedIps = prepareAllowedIps();
    }

    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        if (allowedIps != null) {
            HttpServletResponse httpResp = (HttpServletResponse) response;

            if (!allowedIps.contains(new Ip(request.getRemoteAddr()))) {
                httpResp.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden!");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    public void destroy() {
    }
}