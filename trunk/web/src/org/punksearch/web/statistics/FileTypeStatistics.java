package org.punksearch.web.statistics;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.punksearch.common.FileTypes;
import org.punksearch.common.IndexFields;
import org.punksearch.searcher.SearcherResult;
import org.punksearch.web.SearcherWrapper;
import org.punksearch.web.filters.TypeFilters;

public class FileTypeStatistics {

	private static Query makeQuery() {
		BooleanQuery query = new BooleanQuery();
		Query smbQuery = new WildcardQuery(new Term(IndexFields.HOST, "smb_*"));
		Query ftpQuery = new WildcardQuery(new Term(IndexFields.HOST, "ftp_*"));
		query.add(smbQuery, BooleanClause.Occur.SHOULD);
		query.add(ftpQuery, BooleanClause.Occur.SHOULD);
		return query;
	}

	public static int count(String type) {
		Filter filter = TypeFilters.get(type);
		try {
			SearcherResult result = SearcherWrapper.search(makeQuery(), 0, 1, filter);
			return result.count();
		} catch (Exception e) {
			return 0;
		}
	}

	public static int size(String type) {
		System.out.println("Statistics.size start for " + type);
		Filter filter = TypeFilters.get(type);
		try {
			SearcherResult result = SearcherWrapper.search(makeQuery(), filter, Integer.MAX_VALUE);
			int size = 0;
			for (Document doc : result.items()) {
				size += Integer.parseInt(doc.get(IndexFields.SIZE));
			}
			return size / 1024;
		} catch (Exception e) {
			return 0;
		}
	}

	public static Map<String, Integer> count() {
		Map<String, Integer> countMap = new HashMap<String, Integer>();

		FileTypes types = TypeFilters.getTypes();
		for (String key : types.list()) {
			countMap.put(key, count(key));
		}

		return countMap;
	}

	public static PieDataset makePieDataset(Map<String, Integer> values, int total) {
		int sum = 0;
		for (String key : values.keySet()) {
			sum += values.get(key);
		}
		int other = total - sum;

		NumberFormat nf = NumberFormat.getPercentInstance();
		nf.setMaximumFractionDigits(2);

		DefaultPieDataset dataset = new DefaultPieDataset();
		for (String key : values.keySet()) {
			int value = values.get(key);
			dataset.setValue(key + " (" + nf.format(value / (total + 0.0)) + ", " + value + ")", value);
		}
		dataset.setValue("other (" + nf.format(other / (total + 0.0)) + ", " + other + ")", other);

		return dataset;
	}

}
