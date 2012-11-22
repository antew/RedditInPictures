package com.antew.redditinpictures.library.logging;

import com.antew.redditinpictures.library.BuildConfig;

@SuppressWarnings("unused")
public class Log {
    public static final int LEVEL = android.util.Log.VERBOSE;

    public static void d(String tag, String message) {
        if (LEVEL <= android.util.Log.DEBUG && BuildConfig.DEBUG) {
            android.util.Log.d(tag, message);
        }
    }

    public static void d(String tag, Throwable t, String message) {
        if (LEVEL <= android.util.Log.DEBUG && BuildConfig.DEBUG) {
            android.util.Log.d(tag, message, t);
        }
    }
    
    public static void v(String tag, String message) {
        if (LEVEL <= android.util.Log.VERBOSE && BuildConfig.DEBUG) {
            android.util.Log.v(tag, message);
        }
    }
    
    public static void v(String tag, Throwable t, String message) {
        if (LEVEL <= android.util.Log.VERBOSE && BuildConfig.DEBUG) {
            android.util.Log.v(tag, message, t);
        }
    }
    
    public static void i(String tag, String message) {
        if (LEVEL <= android.util.Log.INFO && BuildConfig.DEBUG ) {
            android.util.Log.i(tag, message);
        }
    }
    
    public static void e(String tag, String message) {
        if (LEVEL <= android.util.Log.ERROR && BuildConfig.DEBUG) {
            android.util.Log.e(tag, message);
        }
    }
    
    public static void e(String tag, String message, Throwable e) {
        if (LEVEL <= android.util.Log.ERROR && BuildConfig.DEBUG) {
            android.util.Log.e(tag, message, e);
        }
    }

}
