package com.antew.redditinpictures.preferences;

import android.content.Context;
import android.preference.PreferenceManager;

import com.antew.redditinpictures.library.preferences.SharedPreferencesHelper;
import com.antew.redditinpictures.library.util.Ln;
import com.antew.redditinpictures.pro.R;

public class SharedPreferencesHelperFree extends SharedPreferencesHelper {

    public static boolean getDisableAds(Context context) {
        Ln.i("getDisableAds %s", PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_disable_ads), false));
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_disable_ads), false);
    }

}
