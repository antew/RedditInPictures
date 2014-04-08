package com.antew.redditinpictures.library.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;

public class BundleUtil {

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public static String getString(Bundle bundle, String key, String defaultValue) {
        if (bundle == null || Strings.isEmpty(key)) {
            return defaultValue;
        }

        if (Util.hasHoneycombMR1()) {
            return bundle.getString(key, defaultValue);
        } else {
            String value = defaultValue;
            if (bundle.containsKey(key)) {
                value = bundle.getString(key);
            }

            if (Strings.isEmpty(value)) {
                value = defaultValue;
            }

            return value;
        }
    }

    public static boolean getBoolean(Bundle bundle, String key, boolean defaultValue) {
        if (bundle == null || Strings.isEmpty(key)) {
            return defaultValue;
        }

        return bundle.getBoolean(key, defaultValue);
    }
}
