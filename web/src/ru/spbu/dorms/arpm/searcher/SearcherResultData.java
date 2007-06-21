package ru.spbu.dorms.arpm.searcher;

public class SearcherResultData
{
	private String name;
	private String path;
	private String size;

	public SearcherResultData(String name, String path, String size)
	{
		this.name = name;
		this.path = path;
		this.size = size;
	}

	public String getName()
	{
		return name;
	}

	public String getPath()
	{
		return path;
	}

	public String getSize()
	{
		return size;
	}
}
