package edu.mit.powers;

import java.util.IllegalFormatConversionException;

/**
 * Created by kevin on 1/9/14.
 *
 */
public class Log
{
    public static final String LOG_TAG = "powers";

    // Mostly for avoiding "unused" warnings.
    @SuppressWarnings("unused")
    public static void ignore(Object...args){}

    private static String format(String fmt, Object...args)
    {
        try {
            return String.format(fmt, args);
        } catch(IllegalFormatConversionException ice) {
            android.util.Log.e(LOG_TAG, "Bad args", ice);
            return "<malformed log statement>";
        }
    }

    @SuppressWarnings("unused")
    public static void verbose(String fmt, Object...args)
    {
        android.util.Log.v(LOG_TAG, format(fmt, args));
    }

    @SuppressWarnings("unused")
    public static void debug(String fmt, Object...args)
    {
        android.util.Log.d(LOG_TAG, format(fmt, args));
    }

    public static void info(String fmt, Object...args)
    {
        android.util.Log.i(LOG_TAG, format(fmt, args));
    }

    public static void warn(String fmt, Object...args)
    {
        android.util.Log.w(LOG_TAG, format(fmt, args));
    }

    public static void warn(Throwable t, String fmt, Object...args)
    {
        android.util.Log.w(LOG_TAG, format(fmt, args), t);
    }

    public static void error(String fmt, Object...args)
    {
        android.util.Log.e(LOG_TAG, format(fmt, args));
    }

    public static void error(Throwable t, String fmt, Object...args)
    {
        android.util.Log.e(LOG_TAG, format(fmt, args), t);
    }
}
