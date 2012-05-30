package org.punksearch.common;

/**
 * User: gubarkov
 * Date: 24.05.12
 * Time: 18:46
 */
public class Settings {
    /*
   Access methods
    */
    public static int getInt(String key) {
        return getInt(key, 0);
    }

    public static int getInt(String key, int defaultVal) {
        return Integer.getInteger(key, defaultVal);
    }

    public static float getFloat(String key) {
        return getFloat(key, 0F);
    }

    public static float getFloat(String key, float defaultVal) {
        final String propVal = get(key);

        if (propVal == null) {
            return defaultVal;
        }

        return Float.parseFloat(propVal);
    }

    public static long getLong(String key) {
        return getLong(key, 0L);
    }

    public static long getLong(String key, long defaultVal) {
        return Long.getLong(key, defaultVal);
    }

    public static boolean getBool(String key) {
        return getBool(key, false);
    }

    public static boolean getBool(String key, boolean defaultVal) {
        final String propVal = get(key);

        if (propVal == null) {
            return defaultVal;
        }

        return Boolean.parseBoolean(propVal);
    }

    public static String get(String key) {
        return System.getProperty(key);
    }

    public static String get(String key, String defaultVal) {
        return System.getProperty(key, defaultVal);
    }
}
