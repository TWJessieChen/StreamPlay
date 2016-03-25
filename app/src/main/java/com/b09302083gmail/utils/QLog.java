package com.b09302083gmail.utils;

import com.b09302083gmail.streamplayer.BuildConfig;

import android.util.Log;

/**
 * Created by Jessie.PO.Chen on 2015/4/28.
 */
public class QLog {
    public static void v(final String tag, final String log) {
        if (BuildConfig.DEBUG) {
            Log.v(tag, log);
        }
    }

    public static void i(final String tag, final String log) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, log);
        }
    }

    public static void d(final String tag, final String log) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, log);
        }
    }

    public static void w(final String tag, final String log) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, log);
        }
    }

    public static void e(final String tag, final String log) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, log);
        }
    }

    public static void catchException(Exception aException) {
        if (BuildConfig.DEBUG) {
        }
    }
}
