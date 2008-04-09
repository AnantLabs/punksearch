package org.punksearch.web.filters;

import java.io.IOException;
import java.util.BitSet;
import java.util.Date;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Filter;

/**
 * @deprecated use CachingWrapperFilter instead
 */
@Deprecated
public abstract class CachedFilter extends Filter {
	private Filter filter;
	private BitSet bitSet    = null;
	private Date   cacheDate = null;

	public CachedFilter(Filter filter) {
		this.filter = filter;
	}

	@Override
	public BitSet bits(IndexReader reader) throws IOException {
		if (bitSet != null && isGoodCache(cacheDate)) {
			return bitSet;
		}
		synchronized (bitSet) {
			bitSet = filter.bits(reader);
			cacheDate = new Date();
		}
		return bitSet;
	}

	public abstract boolean isGoodCache(Date dateOfCache);

}
