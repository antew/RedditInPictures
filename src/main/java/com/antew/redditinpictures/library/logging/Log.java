/*
 * Copyright (C) 2012 Antew | antewcode@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.antew.redditinpictures.library.logging;

import com.antew.redditinpictures.pro.BuildConfig;;

/**
 * Utility class for Logging, wrapps the standard android {@link android.util.Log} class
 * @author Antew
 *
 */
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
