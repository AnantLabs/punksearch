package org.punksearch.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * User: gubarkov
 * Date: 11.06.12
 * Time: 17:21
 */
public class SearchHistory {
    private static Log log = LogFactory.getLog(SearchHistory.class);

    public static void logSearch(HttpServletRequest request,
                                 SearchParams params, int items, long searchTime, long presentationTime) {

        log.info(request.getRemoteAddr() + ": " + formatParams(params) + ": found=" + items +
                ", searchT=" + searchTime / 1000f + ", presT=" + presentationTime / 1000f);
    }

    private static String formatParams(SearchParams params) {
        return new Formatter()
                .add(params.query, "query")
                .add(params.type, "type")
                .add(params.dir, "dir")
                .add(params.file, "file")
                .add(params.ext, "ext")
                .add(params.maxSize, "maxSize")
                .add(params.minSize, "minSize")
                .add(params.first, "first")
                .value();
    }

    static class Formatter {
        private final StringBuilder stringBuilder;

        public Formatter() {
            stringBuilder = new StringBuilder();
        }

        public Formatter add(Object val, String key) {
            if (val != null) {
                stringBuilder.append(key)
                        .append('=')
                        .append(val)
                        .append(", ");
            }
            return this;
        }

        public String value() {
            final int length = stringBuilder.length();
            if (length > 2) {
                stringBuilder.setLength(length - 2);
            }
            return stringBuilder.toString();
        }
    }
}
