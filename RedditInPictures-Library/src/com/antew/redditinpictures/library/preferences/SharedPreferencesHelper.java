package com.antew.redditinpictures.library.preferences;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.antew.redditinpictures.library.reddit.RedditUrl.Age;
import com.antew.redditinpictures.library.reddit.RedditUrl.Category;
import com.antew.redditinpictures.library.utils.Util;

@SuppressLint("CommitPrefEdits")
public class SharedPreferencesHelper {
    private static final String LOGIN_JSON           = "loginJson";
    private static final String COOKIE               = "cookie";
    private static final String MOD_HASH             = "modHash";
    private static final String USERNAME             = "username";
    public static final String  GLOBAL_PREFS_NAME    = "reddit_in_pictures_prefs";
    public static final String  ENABLE_HW_ACCEL      = "enableHwAccel";
    public static final String  USE_MOBILE_INTERFACE = "launchMobile";
    public static final String  SHOW_NSFW_IMAGES     = "showNsfwImages";
    public static final String  ABOUT                = "about";
    public static final String  AGE                  = "age";
    public static final String  CATEGORY             = "category";
    public static final String  USE_HOLO_BACKGROUND  = "useHoloBackground";

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

    public static List<String> loadArray(String prefsName, String arrayName, Context context) {
        List<String> returnList = new ArrayList<String>();
        SharedPreferences prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);

        int size = prefs.getInt(arrayName + "_size", 0);

        for (int i = 0; i < size; i++)
            returnList.add(prefs.getString(arrayName + "_" + i, null));

        return returnList;
    }

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

    public static void saveLoginInformation(String username, String modHash, String cookie, String json, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(GLOBAL_PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(USERNAME, username);
        editor.putString(MOD_HASH, modHash);
        editor.putString(COOKIE, cookie);
        editor.putString(LOGIN_JSON, json);

        save(editor);
    }

    public static void saveCategorySelectionLoginInformation(Age age, Category category, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(GLOBAL_PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(AGE, age.name());
        editor.putString(CATEGORY, category.name());
        
        save(editor);
    }
    
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static void save(SharedPreferences.Editor editor) {
        if (Util.hasGingerbread())
            editor.apply();
        else 
            editor.commit();
    }

    public static String getLoginJson(Context context) {
        return context.getSharedPreferences(GLOBAL_PREFS_NAME, 0).getString(LOGIN_JSON, "");
    }

    public static String getUsername(Context context) {
        return context.getSharedPreferences(GLOBAL_PREFS_NAME, 0).getString(USERNAME, "");
    }

    public static String getModHash(Context context) {
        return context.getSharedPreferences(GLOBAL_PREFS_NAME, 0).getString(MOD_HASH, "");
    }

    public static String getCookie(Context context) {
        return context.getSharedPreferences(GLOBAL_PREFS_NAME, 0).getString(COOKIE, "");
    }

    public static boolean clearLoginInformation(Context context) {
        return context.getSharedPreferences(GLOBAL_PREFS_NAME, 0).edit().remove(LOGIN_JSON).remove(USERNAME).remove(MOD_HASH).remove(COOKIE).commit();
    }

    public static boolean getEnableHwAccel(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(ENABLE_HW_ACCEL, false);
    }

    public static boolean getUseMobileInterface(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(USE_MOBILE_INTERFACE, true);
    }

    public static boolean getShowNsfwImages(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SHOW_NSFW_IMAGES, false);
    }

    public static boolean getUseHoloBackground(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(USE_HOLO_BACKGROUND, false);
    }
    
    public static Age getAge(Context context) {
        return Age.valueOf(context.getSharedPreferences(GLOBAL_PREFS_NAME, 0).getString(AGE, Age.TODAY.name()));
    }
    
    public static Category getCategory(Context context) {
        return Category.valueOf(context.getSharedPreferences(GLOBAL_PREFS_NAME, 0).getString(CATEGORY, Category.HOT.name()));
    }
}
