/*
 * Copyright 2002-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.punksearch.web.web.filters;

import javax.servlet.*;
import java.io.IOException;

/**
 * Servlet 2.3/2.4 Filter that allows one to specify a character encoding for
 * requests. This is useful because current browsers typically do not set a
 * character encoding even if specified in the HTML page or form.
 * <p/>
 * <p>This filter can either apply its encoding if the request does not
 * already specify an encoding, or enforce this filter's encoding in any case
 * ("forceEncoding"="true"). In the latter case, the encoding will also be
 * applied as default response encoding on Servlet 2.4+ containers (although
 * this will usually be overridden by a full content type set in the view).
 *
 * @author Juergen Hoeller
 * @see #setEncoding
 * @see #setForceEncoding
 * @see javax.servlet.http.HttpServletRequest#setCharacterEncoding
 * @see javax.servlet.http.HttpServletResponse#setCharacterEncoding
 * @since 15.03.2004
 */
public class CharacterEncodingFilter implements Filter {

    private String encoding;

    private boolean forceEncoding = false;


    /**
     * Set the encoding to use for requests. This encoding will be passed into a
     * {@link javax.servlet.http.HttpServletRequest#setCharacterEncoding} call.
     * <p>Whether this encoding will override existing request encodings
     * (and whether it will be applied as default response encoding as well)
     * depends on the {@link #setForceEncoding "forceEncoding"} flag.
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Set whether the configured {@link #setEncoding encoding} of this filter
     * is supposed to override existing request and response encodings.
     * <p>Default is "false", i.e. do not modify the encoding if
     * {@link javax.servlet.http.HttpServletRequest#getCharacterEncoding()}
     * returns a non-null value. Switch this to "true" to enforce the specified
     * encoding in any case, applying it as default response encoding as well.
     * <p>Note that the response encoding will only be set on Servlet 2.4+
     * containers, since Servlet 2.3 did not provide a facility for setting
     * a default response encoding.
     */
    public void setForceEncoding(boolean forceEncoding) {
        this.forceEncoding = forceEncoding;
    }


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        setEncoding(filterConfig.getInitParameter("encoding"));
        setForceEncoding("true".equals(filterConfig.getInitParameter("forceEncoding")));
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        if (this.encoding != null && (this.forceEncoding || servletRequest.getCharacterEncoding() == null)) {
            servletRequest.setCharacterEncoding(this.encoding);
            if (this.forceEncoding) {
                servletResponse.setCharacterEncoding(this.encoding);
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
    }
}
