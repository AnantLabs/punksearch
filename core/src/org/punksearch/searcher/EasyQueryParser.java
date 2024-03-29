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
package org.punksearch.searcher;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.punksearch.common.IndexFields;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import static org.punksearch.common.Settings.getBool;
import static org.punksearch.common.Settings.getInt;

/**
 * Helper class to create Lucene queries easily.
 *
 * @author Yury Soldak (ysoldak@gmail.com)
 */
public class EasyQueryParser {
    private static final Log log = LogFactory.getLog(EasyQueryParser.class);

    public static final String SEARCH_MAX_CLAUSES = "org.punksearch.search.clauses";
    public static final String SEARCH_MIN_TERM_LENGTH = "org.punksearch.search.termlength";
    public static final String SEARCH_EXPAND = "org.punksearch.search.expand";
    public static final String SEARCH_FAST_SEARCH = "org.punksearch.search.fast";

    private static final int maxClauseCount = getInt(SEARCH_MAX_CLAUSES, 10000);
    private static final int minTermLength = getInt(SEARCH_MIN_TERM_LENGTH, 3);
    private static final boolean isExpandTerms = getBool(SEARCH_EXPAND, true);
    private static final boolean isFastSearch = getBool(SEARCH_FAST_SEARCH, true);

    private static final EasyQueryParser instance = new EasyQueryParser();

    private static final Pattern CLEAN_CHARS_REGEX = prepareCleanCharsRegex();

    static {
        BooleanQuery.setMaxClauseCount(maxClauseCount);
    }

    private EasyQueryParser() {
    }

    public static EasyQueryParser getInstance() {
        return instance;
    }

    private static Pattern prepareCleanCharsRegex() {
        String cleanChars = "_|!|\\.|,|:|\\[|\\]|#|\\(|\\)|'|/|&";
        if (isExpandTerms) {
            cleanChars += "|\\*";
        }
        return Pattern.compile(cleanChars);
    }

    public Query makeSimpleQuery(String userQuery) {
        if (log.isDebugEnabled()) {
            log.debug("Query to parse: " + userQuery);
        }
        List<String> terms = prepareQueryParameter(userQuery);

        if (terms.size() == 0) {
            return null;
        }

        BooleanQuery query = new BooleanQuery(false);

        for (String item : terms) {
            BooleanQuery itemQuery = new BooleanQuery();

            final String term = prepareItem(item);
            Query nameQuery = new WildcardQuery(new Term(IndexFields.NAME, term));
            itemQuery.add(nameQuery, BooleanClause.Occur.SHOULD);

            Query pathQuery = new WildcardQuery(new Term(IndexFields.PATH, term));
            itemQuery.add(pathQuery, BooleanClause.Occur.SHOULD);

            query.add(itemQuery, occurItem(item));
        }

        return query;
    }

    public Query makeAdvancedQuery(String dir, String file, String ext) {
        if (log.isDebugEnabled()) {
            log.debug("Query (advanced) to parse: dir(" + dir + ") file(" + file + ") ext(" + ext + ")");
        }

        BooleanQuery query = new BooleanQuery(false);

        List<String> dirTerms = prepareQueryParameter(dir);
        List<String> fileTerms = prepareQueryParameter(file);
        List<String> extTerms = prepareQueryParameter(ext);

        if (fileTerms.size() != 0 || extTerms.size() != 0) {
            if (log.isDebugEnabled()) {
                log.debug("Search for files");
            }
            if (fileTerms.size() != 0) {
                BooleanQuery fileQuery = new BooleanQuery();
                for (String item : fileTerms) {
                    BooleanClause.Occur occurItem = occurItem(item);
                    Query nameQuery = new WildcardQuery(new Term(IndexFields.NAME, prepareItem(item)));
                    fileQuery.add(nameQuery, occurItem);
                }
                query.add(fileQuery, BooleanClause.Occur.MUST);
            }

            if (extTerms.size() != 0) {
                BooleanQuery extQuery = new BooleanQuery();
                for (String item : extTerms) {
                    Query termQuery = new TermQuery(new Term(IndexFields.EXTENSION, item));
                    extQuery.add(termQuery, BooleanClause.Occur.SHOULD);
                }
                query.add(extQuery, BooleanClause.Occur.MUST);
            } else {
                Query typeQuery = new TermQuery(new Term(IndexFields.TYPE, IndexFields.TYPE_DIR));
                query.add(typeQuery, BooleanClause.Occur.MUST_NOT);
            }

            // restrict files to occur in specified directories only
            if (dirTerms.size() != 0) {
                BooleanQuery dirQuery = new BooleanQuery();
                int negations = 0;

                for (String item : dirTerms) {
                    BooleanClause.Occur occurItem = occurItem(item);
                    if (occurItem == BooleanClause.Occur.MUST_NOT) {
                        negations++;
                    }
                    Query pathQuery = new WildcardQuery(new Term(IndexFields.PATH, prepareItem(item)));
                    dirQuery.add(pathQuery, occurItem);
                }
                // it must be at least one positive clause in query to be executed.
                // so add one if all user clauses are negative.
                if (dirTerms.size() == negations) {
                    Query pathQuery = new WildcardQuery(new Term(IndexFields.PATH, "*"));
                    dirQuery.add(pathQuery, BooleanClause.Occur.SHOULD);
                }
                query.add(dirQuery, BooleanClause.Occur.MUST);
            }
        } else if (dirTerms.size() != 0) {
            if (log.isDebugEnabled()) {
                log.debug("Search for directories only, since file name was not specified");
            }
            for (String item : dirTerms) {
                BooleanQuery dirQuery = new BooleanQuery();

                BooleanClause.Occur occurItem = occurItem(item);

                Query nameQuery = new WildcardQuery(new Term(IndexFields.NAME, prepareItem(item)));
                dirQuery.add(nameQuery, BooleanClause.Occur.MUST);

                Query typeQuery = new TermQuery(new Term(IndexFields.TYPE, IndexFields.TYPE_DIR));
                dirQuery.add(typeQuery, BooleanClause.Occur.MUST);

                query.add(dirQuery, occurItem);
            }
        } else {
            return null;
        }
        return query;
    }

    private List<String> prepareQueryParameter(String str) {
        List<String> result = new LinkedList<String>();
        if (str != null) {
            str = CLEAN_CHARS_REGEX.matcher(str).replaceAll(" ");
            String[] terms = StringUtils.split(str.toLowerCase());
            for (String term : terms) {
                term = term.trim();
                if (isExpandTerms && term.length() >= minTermLength
                        || term.length() > 0) {
                    result.add(term);
                }
            }
        }
        return result;
    }

    private String prepareItem(String item) {
        if (item.startsWith("+") || item.startsWith("-")) {
            item = item.substring(1);
        }

        if (isFastSearch) {
            // don't allow user to use "*someterm" search
            item = StringUtils.stripStart(item, "*");
        }

        return !isExpandTerms
                ? item
                : isFastSearch
                ? item + "*"
                : "*" + item + "*";
    }

    private BooleanClause.Occur occurItem(String item) {
        BooleanClause.Occur result = BooleanClause.Occur.SHOULD;
        if (item.startsWith("+")) {
            result = BooleanClause.Occur.MUST;
        } else if (item.startsWith("-")) {
            result = BooleanClause.Occur.MUST_NOT;
        }
        return result;
    }

}
