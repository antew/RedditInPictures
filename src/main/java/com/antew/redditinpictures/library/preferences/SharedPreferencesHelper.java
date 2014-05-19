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
package com.antew.redditinpictures.library.preferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.antew.redditinpictures.library.model.Age;
import com.antew.redditinpictures.library.model.Category;
import com.antew.redditinpictures.pro.R;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("CommitPrefEdits")
public class SharedPreferencesHelper {
    public static final  String GLOBAL_PREFS_NAME = "reddit_in_pictures_prefs";
    public static final  String ENABLE_HW_ACCEL   = "enableHwAccel";
    public static final  String AGE               = "age";
    public static final  String CATEGORY          = "category";
    private static final String COOKIE            = "cookie";
    private static final String MOD_HASH          = "modHash";
    private static final String USERNAME          = "username";
    private static final String ANALYTICS_OPT_OUT = "analyticsOptOut";

    /**
     * Save an array to {@link SharedPreferences}
     *
     * @param array
     *     The array of strings to save
     * @param prefsName
     *     The name of the preferences file
     * @param arrayName
     *     The name to save the array as
     * @param context
     *     The context
     */
    public static void saveArray(List<String> array, String prefsName, String arrayName, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(prefsName, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();

        editor.putInt(arrayName + "_size", array.size());

        for (int i = 0; i < array.size(); i++) {
            String s = array.get(i);
            editor.putString(arrayName + "_" + i, s);
        }

        save(editor);
    }

    /**
     * Commit the preferences asynchronously in Gingerbread and later, otherwise we have to do it
     * synchronously
     *
     * @param editor
     */
    public static void save(SharedPreferences.Editor editor) {
            editor.apply();
    }

    /**
     * Add a single item to the array saved in {@link SharedPreferences}
     *
     * @param valueToAdd
     *     The String to add
     * @param prefsName
     *     The preferences file name
     * @param arrayName
     *     The name of the array
     * @param context
     *     The context
     */
    public static void addToArray(String valueToAdd, String prefsName, String arrayName, Context context) {
        List<String> array = loadArray(prefsName, arrayName, context);
        array.add(valueToAdd);

        SharedPreferences prefs = context.getSharedPreferences(prefsName, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();

        editor.putInt(arrayName + "_size", array.size());

        for (int i = 0; i < array.size(); i++) {
            String s = array.get(i);
            editor.putString(arrayName + "_" + i, s);
        }

        save(editor);
    }

    /**
     * Load an array of Strings from {@link SharedPreferences}
     *
     * @param prefsName
     *     The name of the preferences file
     * @param arrayName
     *     The name of the array
     * @param context
     *     The context
     *
     * @return An {@link ArrayList} of strings containing the data loaded from
     * {@link SharedPreferences}
     */
    public static List<String> loadArray(String prefsName, String arrayName, Context context) {
        List<String> returnList = new ArrayList<String>();
        SharedPreferences prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);

        int size = prefs.getInt(arrayName + "_size", 0);

        for (int i = 0; i < size; i++) {
            returnList.add(prefs.getString(arrayName + "_" + i, null));
        }

        return returnList;
    }

    /**
     * Whether the Reddit mobile interface should be used when launching links.
     *
     * @param context
     *     The context
     *
     * @return Returns true if Reddit links should be launched to the mobile site
     */
    public static boolean getUseMobileInterface(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                                .getBoolean(context.getString(R.string.pref_use_mobile_interface), true);
    }

    /**
     * Whether NSFW images should be shown
     *
     * @param context
     *     The context
     *
     * @return True if NSFW images should be shown
     */
    public static boolean getShowNsfwImages(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_show_nsfw_images), false);
    }

    /**
     * The saved {@link Age}
     *
     * @param context
     *     The context
     *
     * @return The saved {@link Age}
     */
    public static Age getAge(Context context) {
        return Age.fromString(context.getSharedPreferences(GLOBAL_PREFS_NAME, 0).getString(AGE, Age.TODAY.getAge()));
    }

    /**
     * The saved {@link Category}
     *
     * @param context
     *     The context
     *
     * @return The saved {@link Category}
     */
    public static Category getCategory(Context context) {
        return Category.fromString(context.getSharedPreferences(GLOBAL_PREFS_NAME, 0).getString(CATEGORY, Category.HOT.getName()));
    }
}
