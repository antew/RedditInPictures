package com.antew.redditinpictures.preferences;

import android.content.Context;
import android.preference.PreferenceManager;

import com.antew.redditinpictures.library.preferences.SharedPreferencesHelper;
import com.antew.redditinpictures.pro.R;

public class SharedPreferencesHelperFree extends SharedPreferencesHelper {

    public static boolean getDisableAds(Context context) {
        Log.i("getDisableAds", "" + PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_disable_ads), false));
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_disable_ads), false);
    }

}
