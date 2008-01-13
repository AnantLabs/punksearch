package org.punksearch.commons;

public class SearcherException extends Exception
{
	public SearcherException(Exception e)
	{
		super(e);
	}

	public SearcherException(String message, Exception e)
	{
		super(message, e);
	}
}
