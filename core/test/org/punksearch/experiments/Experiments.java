package org.punksearch.experiments;

import org.junit.Test;

import java.io.UnsupportedEncodingException;

/**
 * User: gubarkov
 * Date: 23.05.12
 * Time: 0:14
 */
public class Experiments {
    @Test
    public void stringEncodingConversion() throws UnsupportedEncodingException {
        String paramValue = "тест";
        System.out.println(paramValue);
        paramValue = new String(paramValue.getBytes("ISO-8859-1"), "UTF-8");
        System.out.println(paramValue);
    }
}
