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

public class CompositeFilter extends BooleanFilter {

    public void add(Filter filter) {
        add(filter, BooleanClause.Occur.MUST);
    }
    /*private Set<Filter> filters = new HashSet<Filter>();

     public void add(Filter filter)
     {
         if (filter == null)
         {
             throw new IllegalArgumentException("filter is null");
         }
         filters.add(filter);
     }

     public void remove(Filter filter)
     {
         filters.remove(filter);
     }

     @Override
     public BitSet bits(IndexReader reader) throws IOException
     {
         if (filters.size() == 0)
         {
             return new BitSet(reader.maxDoc());
         }

         BitSet result = null;
         for (Filter filter : filters)
         {
             if (result == null)
                 result = filter.bits(reader);
             else
                 result.and(filter.bits(reader));
         }
         return result;
     }

     @Override
     public String toString()
     {
         StringBuilder result = new StringBuilder("");
         for (Filter filter : filters)
         {
             result.append("; ").append(filter.toString());
         }
         return result.toString();
     }

     @Override
     public boolean equals(Object o)
     {
         if (this == o) {
             return true;
         }
         if (!(o instanceof CompositeFilter)) {
             return false;
         }

         CompositeFilter other = (CompositeFilter) o;
         if (other.filters.size() != this.filters.size())
         {
             return false;
         }
         else
         {
             for (Filter myFilter : this.filters)
             {
                 if (!other.filters.contains(myFilter))
                 {
                     return false;
                 }
             }
             for (Filter hisFilter : other.filters)
             {
                 if (!this.filters.contains(hisFilter))
                 {
                     return false;
                 }
             }
             return true;
         }

     }

     *//**
     * Defines hash code as the sum of hash codes of wrapped filters
     *//*
	@Override
	public int hashCode() {
		int result = 0;
		for (Filter filter : filters) {
			result += filter.hashCode();
		}
		return result;
	}*/

}
