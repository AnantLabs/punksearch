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
package org.punksearch.searcher.filters;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanFilter;
import org.apache.lucene.search.Filter;

import java.util.List;

public class CompositeFilter extends BooleanFilter {
    public CompositeFilter() {
    }

    public CompositeFilter(List<Filter> filters) {
        for (Filter filter : filters) {
            add(filter);
        }
    }

    public void add(Filter filter) {
        add(filter, BooleanClause.Occur.MUST);
    }
}
