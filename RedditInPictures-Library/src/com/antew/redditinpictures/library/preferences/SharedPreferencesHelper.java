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

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.antew.redditinpictures.library.R;
import com.antew.redditinpictures.library.reddit.RedditUrl;
import com.antew.redditinpictures.library.reddit.RedditUrl.Age;
import com.antew.redditinpictures.library.reddit.RedditUrl.Category;
import com.antew.redditinpictures.library.ui.ImageGridActivity;
import com.antew.redditinpictures.library.utils.Util;

@SuppressLint("CommitPrefEdits")
public class SharedPreferencesHelper {
    private static final String COOKIE                   = "cookie";
    private static final String MOD_HASH                 = "modHash";
    private static final String USERNAME                 = "username";
    public static final String  GLOBAL_PREFS_NAME        = "reddit_in_pictures_prefs";
    public static final String  ENABLE_HW_ACCEL          = "enableHwAccel";
    public static final String  AGE                      = "age";
    public static final String  CATEGORY                 = "category";

    /**
     * Save an array to {@link SharedPreferences}
     * 
     * @param array
     *            The array of strings to save
     * @param prefsName
     *            The name of the preferences file
     * @param arrayName
     *            The name to save the array as
     * @param context
     *            The context
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
     * Load an array of Strings from {@link SharedPreferences}
     * 
     * @param prefsName
     *            The name of the preferences file
     * @param arrayName
     *            The name of the array
     * @param context
     *            The context
     * @return An {@link ArrayList} of strings containing the data loaded from
     *         {@link SharedPreferences}
     */
    public static List<String> loadArray(String prefsName, String arrayName, Context context) {
        List<String> returnList = new ArrayList<String>();
        SharedPreferences prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);

        int size = prefs.getInt(arrayName + "_size", 0);

        for (int i = 0; i < size; i++)
            returnList.add(prefs.getString(arrayName + "_" + i, null));

        return returnList;
    }

    /**
     * Add a single item to the array saved in {@link SharedPreferences}
     * 
     * @param valueToAdd
     *            The String to add
     * @param prefsName
     *            The preferences file name
     * @param arrayName
     *            The name of the array
     * @param context
     *            The context
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
     * Save the Reddit login information to preferences
     * 
     * @param username
     *            The username
     * @param modHash
     *            The mod hash
     * @param cookie
     *            The cookie
     * @param json
     *            The login json
     * @param context
     *            The context
     */
    // public static void saveLoginInformation(String username, String modHash, String cookie,
    // Context context) {
    // SharedPreferences prefs = context.getSharedPreferences(GLOBAL_PREFS_NAME, 0);
    // SharedPreferences.Editor editor = prefs.edit();
    //
    // editor.putString(USERNAME, username);
    // editor.putString(MOD_HASH, modHash);
    // editor.putString(COOKIE, cookie);
    //
    // save(editor);
    // }

    /**
     * Save the selected Age and Category to preferences. Used to save/restore the selected
     * Category/Age combination from {@link ImageGridActivity}
     * 
     * @param age
     *            The age
     * @param category
     *            The category
     * @param context
     *            The context
     */
    public static void saveCategorySelectionLoginInformation(Age age, Category category, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(GLOBAL_PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(AGE, age.name());
        editor.putString(CATEGORY, category.name());

        save(editor);
    }

    /**
     * Commit the preferences asynchronously in Gingerbread and later, otherwise we have to do it
     * synchronously
     * 
     * @param editor
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static void save(SharedPreferences.Editor editor) {
        if (Util.hasGingerbread())
            editor.apply();
        else
            editor.commit();
    }

    // public static LoginData getLoginData(Context context) {
    // SharedPreferences prefs = context.getSharedPreferences(GLOBAL_PREFS_NAME, 0);
    //
    // return new LoginData(prefs.getString(USERNAME, ""),
    // prefs.getString(MOD_HASH, ""),
    // prefs.getString(COOKIE, ""));
    // }
    //
    // public static String getUsername(Context context) {
    // return context.getSharedPreferences(GLOBAL_PREFS_NAME, 0).getString(USERNAME, "");
    // }
    //
    // public static String getModHash(Context context) {
    // return context.getSharedPreferences(GLOBAL_PREFS_NAME, 0).getString(MOD_HASH, "");
    // }
    //
    // public static String getCookie(Context context) {
    // return context.getSharedPreferences(GLOBAL_PREFS_NAME, 0).getString(COOKIE, "");
    // }

    /**
     * Remove the saved login information
     * 
     * @param context
     *            The context
     * @return True if the removal is successful
     */
    // public static boolean clearLoginInformation(Context context) {
    // return context.getSharedPreferences(GLOBAL_PREFS_NAME,
    // 0).edit().remove(USERNAME).remove(MOD_HASH).remove(COOKIE).commit();
    // }

    /**
     * This is currently unused, I envisioned that it would be used to allow the user to toggle HW
     * acceleration on/off
     * 
     * @param context
     *            The context
     * @return True if the hardware acceleration should be enabled
     */
    public static boolean getEnableHwAccel(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(ENABLE_HW_ACCEL, false);
    }

    /**
     * Whether the Reddit mobile interface should be used when launching links.
     * 
     * @param context
     *            The context
     * @return Returns true if Reddit links should be launched to the mobile site
     */
    public static boolean getUseMobileInterface(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_use_mobile_interface), true);
    }

    /**
     * Whether NSFW images should be shown
     * 
     * @param context
     *            The context
     * @return True if NSFW images should be shown
     */
    public static boolean getShowNsfwImages(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_show_nsfw_images), false);
    }

    /**
     * Whether the holo background should be used. Some users reported performance problems and I
     * think it may have been due to issues with scaling the Holo background image
     * 
     * @param context
     *            The context
     * @return Whether the Holo background should be used
     */
    public static boolean getUseHoloBackground(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_use_holo_background), false);
    }

    /**
     * The saved {@link RedditUrl#Age}
     * 
     * @param context
     *            The context
     * @return The saved {@link RedditUrl#Age}
     */
    public static Age getAge(Context context) {
        return Age.valueOf(context.getSharedPreferences(GLOBAL_PREFS_NAME, 0).getString(AGE, Age.TODAY.name()));
    }

    /**
     * The saved {@link RedditUrl#Category}
     * 
     * @param context
     *            The context
     * @return The saved {@link RedditUrl#Category}
     */
    public static Category getCategory(Context context) {
        return Category.valueOf(context.getSharedPreferences(GLOBAL_PREFS_NAME, 0).getString(CATEGORY, Category.HOT.name()));
    }
}
