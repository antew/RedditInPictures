package com.antew.redditinpictures.preferences;

import android.content.Context;
import android.preference.PreferenceManager;

import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.preferences.SharedPreferencesHelper;

public class SharedPreferencesHelperFree extends SharedPreferencesHelper {
    public static final String  DISABLE_ADS          = "disableAds";

    public static boolean getDisableAds(Context context) {
        Log.i("getDisableAds", "" + PreferenceManager.getDefaultSharedPreferences(context).getBoolean(DISABLE_ADS, false));
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(DISABLE_ADS, false);
    }

}
