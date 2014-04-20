/*
 * Copyright (C) 2014 Antew
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
