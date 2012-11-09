package com.antew.redditinpictures.library.preferences;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.antew.redditinpictures.library.logging.Log;

public class SharedPreferencesHelper {
    private static final String LOGIN_JSON           = "loginJson";
    private static final String COOKIE               = "cookie";
    private static final String MOD_HASH             = "modHash";
    private static final String USERNAME             = "username";
    public static final String  GLOBAL_PREFS_NAME    = "reddit_in_pictures_prefs";
    public static final String  ENABLE_HW_ACCEL      = "enableHwAccel";
    public static final String  USE_MOBILE_INTERFACE = "launchMobile";
    public static final String  SHOW_NSFW_IMAGES     = "showNsfwImages";

    public static boolean saveArray(List<String> array, String prefsName, String arrayName, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(prefsName, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();

        editor.putInt(arrayName + "_size", array.size());

        for (int i = 0; i < array.size(); i++) {
            String s = array.get(i);
            editor.putString(arrayName + "_" + i, s);
        }

        return editor.commit();
    }

    public static List<String> loadArray(String prefsName, String arrayName, Context context) {
        List<String> returnList = new ArrayList<String>();
        SharedPreferences prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);

        int size = prefs.getInt(arrayName + "_size", 0);

        for (int i = 0; i < size; i++)
            returnList.add(prefs.getString(arrayName + "_" + i, null));

        return returnList;
    }

    public static boolean addToArray(String valueToAdd, String prefsName, String arrayName, Context context) {
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

        return editor.commit();
    }

    public static boolean saveLoginInformation(String username, String modHash, String cookie, String json, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(GLOBAL_PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(USERNAME, username);
        editor.putString(MOD_HASH, modHash);
        editor.putString(COOKIE, cookie);
        editor.putString(LOGIN_JSON, json);

        return editor.commit();
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
}
