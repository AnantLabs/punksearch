package org.punksearch.utils;

/**
 * Date: 15.06.2006
 *
 * @author arPm
 */
public class NumberUtils
{
	public static final int MAX_SIZE_LENGTH = 12; // 999 999 999 999 b - max size
	public static final String BYTE     = "b";
	public static final String KILOBYTE = "kb";
	public static final String MEGABYTE = "mb";
	public static final String GIGABYTE = "gb";

	/**
	 * Pads input number with leading zeros and converts it to string (for <code>RangeQuery</code> and sort purpose).
	 * Length of resulting string is equals to <code>MAX_SIZE_LENGTH</code>
	 *
	 * @param number Input number
	 * @return Number as String
	 * @throws IllegalArgumentException Length of input number as string is more then <code>MAX_SIZE_LENGTH</code>
	 */
	public static String pad(long number) throws IllegalArgumentException
	{
		return pad(Long.toString(number));
	}

	/**
	 * Pads input string with leading zeros (for <code>RangeQuery</code> and sort purpose).
	 * Length of resulting string is equals to <code>MAX_SIZE_LENGTH</code>
	 *
	 * @param number Input string
	 * @return result String
	 * @throws IllegalArgumentException Length of input string is more then <code>MAX_SIZE_LENGTH</code>
	 */
	public static String pad(String number) throws IllegalArgumentException
	{
		if (number.length() > MAX_SIZE_LENGTH)
		{
			throw new IllegalArgumentException("Number too large for NumberUtils current config");
		}
		for (int i = number.length(); i < MAX_SIZE_LENGTH; ++ i)
		{
			number = "0" + number;
		}
		return number;
	}

	/**
	 * Parses strings like "10 kb"
	 * @param fileSize Input string
	 * @return parsed string
	 * @throws IllegalArgumentException error occurred while parsing
	 */
	public static String parseSizeString(String fileSize) throws IllegalArgumentException
	{
		if (fileSize == null)
		{
			throw new IllegalArgumentException("Input string must not be null");
		}
		fileSize = fileSize.trim().toLowerCase();
		if (fileSize.equals(""))
		{
			throw new IllegalArgumentException("Input string must not be empty");
		}
		int numberFinishIndex = -1;
		while (Character.getType(fileSize.charAt(++ numberFinishIndex)) == Character.DECIMAL_DIGIT_NUMBER)
		{

		}
		String numberPart = fileSize.substring(0, numberFinishIndex);
		if (numberPart.equals(""))
		{
			throw new IllegalArgumentException("Input string does not contain number-part");
		}
		String typePart = fileSize.substring(numberFinishIndex, fileSize.length()).trim();
		if (typePart.equals(BYTE))
		{
		}
		else if (typePart.equals(KILOBYTE))
		{
			numberPart += "000";
		}
		else if (typePart.equals(MEGABYTE))
		{
			numberPart += "000000";
		}
		else if (typePart.equals(GIGABYTE))
		{
			numberPart += "000000000";
		}
		else
		{
			throw new IllegalArgumentException("Input string does not contain type-part or it is invalid");
		}
		return NumberUtils.pad(numberPart);
	}

	/**
	 * Parses string for output
	 * @param fileSizeStr Input string
	 * @param fraction true if need fractional part
	 * @return parsed string
	 */
	public static String parseSizeStringForOutput(String fileSizeStr, boolean fraction)
	{
		if (fileSizeStr.equals(""))
		{
			return "";
		}
		long fileSize = Long.parseLong(fileSizeStr);
		String tempResult = Long.toString(fileSize);
		int length = tempResult.length();
		int index;
		String result;
		if (fileSize >= 1000000000L)
		{
			index = length - 9;
			result = tempResult.substring(0, index);
			if (fraction)
			{
				result = result + "." + tempResult.substring(index, index + 2);
			}
			return (result + GIGABYTE);
		}
		if (fileSize >= 1000000L)
		{
			index = length - 6;
			result = tempResult.substring(0, index);
			if (fraction)
			{
				result = result + "." + tempResult.substring(index, index + 2);
			}

			return (result + MEGABYTE);
		}
		if (fileSize >= 1000L)
		{
			index = length - 3;
			result = tempResult.substring(0, index);
			if (fraction)
			{
				result = result + "." + tempResult.substring(index, index + 2);
			}
			return (result + KILOBYTE);
		}
		return tempResult + BYTE;
	}
}
