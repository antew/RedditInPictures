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
package com.antew.redditinpictures.library.reddit;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;

import com.antew.redditinpictures.library.R;
import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.preferences.SharedPreferencesHelper;
import com.antew.redditinpictures.library.reddit.RedditApi.Children;
import com.antew.redditinpictures.library.reddit.RedditApi.PostData;
import com.antew.redditinpictures.library.subredditmanager.SubredditManager;
import com.antew.redditinpictures.library.utils.ImageUtil;
import com.antew.redditinpictures.library.utils.StringUtil;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class RedditApiManager {
    private static final String        TAG                      = RedditApiManager.class.getSimpleName();
    private static String              mModHash;
    private static String              mCookie;
    private static RedditLoginResponse mRedditLoginResponse;
    private static boolean             mIsLoggedIn              = false;
    private static String              mUsername;
    private static String              mJson;

    public static String getModHash() {
        return mModHash;
    }

    public static void setModHash(String modhash) {
        mModHash = modhash;
    }
    
    public static String getCookie() {
        return mCookie;
    }
    
    public static void setCookie(String cookie) {
        mCookie = cookie;
    }

    public static RedditLoginResponse getRedditLoginResponse() {
        return mRedditLoginResponse;
    }

    public static boolean isLoggedIn() {
        return mIsLoggedIn;
    }

    public static void setRedditLoginResponse(RedditLoginResponse response) {
        mRedditLoginResponse = response;
    }
    public static void logout(Context context) {
        mModHash = null;
        mCookie = null;
        mRedditLoginResponse = null;
        mIsLoggedIn = false;
        mJson = null;
        RedditApiManager.resetToDefaultSubreddits(context);
        SharedPreferencesHelper.clearLoginInformation(context);
    }
    
    public static String getLoginCookie() {
        return mCookie;
    }

    public static void saveRedditLoginInformation(Context context, String username, String modHash, String cookie) {
        mUsername = username;
        mModHash = modHash;
        mCookie = cookie;
        mIsLoggedIn = true;
        SharedPreferencesHelper.saveLoginInformation(username, modHash, cookie, context);
    }
    
    public static void parseRedditLoginResponse(String username, String modHash, String cookie, String response) {
        try {
            mRedditLoginResponse = new Gson().fromJson(response, RedditLoginResponse.class);
            mUsername = username;
            mModHash = modHash;
            mCookie = cookie;
            mIsLoggedIn = true;
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "parseRedditLoginResponse", e);
        }
    }

    public static void resetToDefaultSubreddits(Context context) {
        List<String> subreddits = Arrays.asList(context.getResources().getStringArray(R.array.default_reddits));
        Collections.sort(subreddits, StringUtil.getCaseInsensitiveComparator());
        SharedPreferencesHelper.saveArray(subreddits, SubredditManager.PREFS_NAME, SubredditManager.ARRAY_NAME, context);
    }

    /*
     * Suppressing lint for this function since it uses reflection to find out if the method exists
     */
    @SuppressLint("NewApi")
    public static File getPicturesDirectory() {
        File picturesDirectory = null;
        try {
            Method getPublicDir = Environment.class.getMethod("getExternalStoragePublicDirectory", new Class[] { String.class });
            getPublicDir.invoke(null, Environment.DIRECTORY_PICTURES);
            picturesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        } catch (NoSuchMethodException e) {
            picturesDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "redditinpictures"
                    + File.separator);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "getExternalStoragePublicDirectory", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "getExternalStoragePublicDirectory", e);
        } catch (InvocationTargetException e) {
            Log.e(TAG, "getExternalStoragePublicDirectory", e);
        }

        if (picturesDirectory != null)
            picturesDirectory.mkdirs();

        return picturesDirectory;
    }

    public static void setIsLoggedIn(boolean b) {
        mIsLoggedIn = b;
    }

    public static String getUsername() {
        return mUsername;
    }

    public static String getLoginJson() {
        return mJson;
    }

    public static void setLoginJson(String json) {
        mJson = json;
    }

    /**
     * This method removes unusable posts from the Reddit API data.
     * 
     * @param ra
     *            The API data returned from reddit (e.g. from http://www.reddit.com/.json).
     * @return The filtered list of posts after removing non-images, unsupported images, and NSFW
     *         entries. Whether NSFW images are retained can be controlled via settings.
     */
    public static List<PostData> filterPosts(RedditApi ra, boolean includeNsfwImages) {
        List<PostData> entries = new ArrayList<PostData>();

        for (Children c : ra.getData().getChildren()) {
            PostData pd = c.getData();
            if (ImageUtil.isSupportedUrl(pd.getUrl())) {
                if (!pd.isOver_18() || includeNsfwImages)
                    entries.add(pd);

            }
        }

        return entries;
    }

    public static List<Children> filterChildren(RedditApi ra, boolean includeNsfwImages) {
        List<Children> entries = new ArrayList<Children>();

        for (Children c : ra.getData().getChildren()) {
            PostData pd = c.getData();
            if (ImageUtil.isSupportedUrl(pd.getUrl())) {
                if (!pd.isOver_18() || includeNsfwImages)
                    entries.add(c);
            }
        }

        return entries;
    }
}
