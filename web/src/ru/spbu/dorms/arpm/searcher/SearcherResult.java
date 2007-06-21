package ru.spbu.dorms.arpm.searcher;

import java.util.List;

/**
 * Date: 17.06.2006
 *
 * @author arPm
 */
public class SearcherResult
{
	private int resultsAmount;
	private int pagesAmount;
	private List<SearcherResultData> results;

	public SearcherResult(int resultAmount, int pagesAmount, List<SearcherResultData> results)
	{
		this.resultsAmount = resultAmount;
		this.pagesAmount = pagesAmount;
		this.results = results;
	}

	public int getResultsAmount()
	{
		return resultsAmount;
	}

	public int getPagesAmount()
	{
		return pagesAmount;
	}

	public List<SearcherResultData> getResults()
	{
		return results;
	}
}
