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
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
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
    private static final String        USER_AGENT               = "Reddit In Pictures Android by /u/antew";
    private static final String        TAG                      = "RedditApiManager";
    private static final String        REDDIT_LOGIN_URL         = "https://ssl.reddit.com/api/login/";
    private static final String        REDDIT_SUBSCRIBE_URL     = "http://www.reddit.com/api/subscribe";
    private static final String        REDDIT_VOTE_URL          = "http://www.reddit.com/api/vote";
    private static final String        REDDIT_ABOUT_URL         = "http://www.reddit.com/r/%s/about.json";
    private static final String        REDDIT_SESSION           = "reddit_session";
    private static final String        REDDIT_MY_SUBREDDITS_URL = "http://www.reddit.com/reddits/mine/subscriber.json";
    public static final String         COMPACT_URL              = "/.compact";
    public static final String         REDDIT_BASE_URL          = "http://www.reddit.com";
    private static String              mModHash;
    private static String              mCookie;
    private static RedditLoginResponse mRedditLoginResponse;
    private static boolean             mIsLoggedIn              = false;
    private static String              mUsername;
    private static String              mJson;
    private static About               about;

    public static String getModHash() {
        return mModHash;
    }

    public static String getCookie() {
        return mCookie;
    }

    public static RedditLoginResponse getRedditLoginResponse() {
        return mRedditLoginResponse;
    }

    public static boolean isLoggedIn() {
        return mIsLoggedIn;
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

    public static void login(String username, String password, final Context context, final String callbackFunc) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("api_type", "json");
        params.put("user", username);
        params.put("passwd", password);

        mUsername = username;

        String url = REDDIT_LOGIN_URL + username;

        final AQuery aQuery = new AQuery(context);
        AjaxCallback<String> cb = new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (status.getCode() == HttpURLConnection.HTTP_OK && status.getCookies().size() > 0) {
                    super.callback(url, object, status);
                    mJson = object;
                    Log.i("JSON", object);
                    Gson gson = new Gson();
                    Method mthd = null;
                    try {
                        mthd = context.getClass().getMethod(callbackFunc, new Class[] { String.class, String.class, AjaxStatus.class });
                        mRedditLoginResponse = gson.fromJson(object, RedditLoginResponse.class);

                        if (mRedditLoginResponse.getLoginResponse().getErrors().size() > 0) {
                            mIsLoggedIn = false;
                            showLoginError(mRedditLoginResponse);
                        } else {
                            mIsLoggedIn = true;
                            mModHash = mRedditLoginResponse.getLoginResponse().getData().getModhash();
                            mCookie = mRedditLoginResponse.getLoginResponse().getData().getCookie();
                            SharedPreferencesHelper.saveLoginInformation(mUsername, mModHash, mCookie, mJson, context);
                        }

                        mthd.invoke(context, url, object, status);
                    } catch (NoSuchMethodException e) {
                        Log.e(TAG, callbackFunc, e);
                    } catch (JsonSyntaxException e) {
                        Log.e(TAG, "Error parsing login response from String = " + object, e);
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, callbackFunc, e);
                    } catch (IllegalAccessException e) {
                        Log.e(TAG, callbackFunc, e);
                    } catch (InvocationTargetException e) {
                        Log.e(TAG, callbackFunc, e);
                    }
                } else {
                    Log.e("Login", "Something went wrong on login! status = " + status.getCode() + " | json = " + object == null ? "null"
                            : object);
                }
            }

            private void showLoginError(RedditLoginResponse rlp) {
                StringBuilder buf = new StringBuilder();
                for (String[] err : rlp.getLoginResponse().getErrors())
                    buf.append(err[1] + " ");

                Toast.makeText(context, "Error: " + buf.toString(), Toast.LENGTH_SHORT).show();
            }

        };
        cb.url(url).type(String.class).params(params).header("User-Agent", USER_AGENT);
        aQuery.ajax(cb);
    }

    public static void vote(String id, String subreddit, Vote vote, Context context) {
        if (!mIsLoggedIn) {
            Toast.makeText(context, "Must be logged in to vote!", Toast.LENGTH_SHORT).show();
            return;
        }

        AQuery aQuery = new AQuery(context);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        params.put("dir", vote.getVote());
        params.put("r", subreddit);
        params.put("uh", mRedditLoginResponse.getLoginResponse().getData().getModhash());

        AjaxCallback<String> cb = new AjaxCallback<String>() {
            @Override
            public void callback(String url, String object, AjaxStatus status) {
                Log.i("Got back from vote!", object);
                if (status.getCode() == HttpURLConnection.HTTP_OK) {

                }

            }

        };

        cb.header("User-Agent", USER_AGENT);
        cb.url(REDDIT_VOTE_URL);
        cb.type(String.class);
        cb.params(params);
        cb.cookie(REDDIT_SESSION, mRedditLoginResponse.getLoginResponse().getData().getCookie());
        aQuery.ajax(cb);
    }

    public static void getMySubreddits(String callbackFunc, Context context) {
        if (!mIsLoggedIn) {
            Toast.makeText(context, "Must be logged in to load subscribed subreddits!", Toast.LENGTH_SHORT).show();
            return;
        }

        AQuery aQuery = new AQuery(context);
        AjaxCallback<String> callback = new AjaxCallback<String>();

        //@formatter:off
        callback.url(REDDIT_MY_SUBREDDITS_URL)
                .type(String.class)
                .header("User-Agent", USER_AGENT)
                .cookie(REDDIT_SESSION, mRedditLoginResponse.getLoginResponse().getData().getCookie())
                .weakHandler(context,  callbackFunc);
        //@formatter:on

        aQuery.ajax(callback);
    }

    public static void resetToDefaultSubreddits(Context context) {
        List<String> subreddits = Arrays.asList(context.getResources().getStringArray(R.array.default_reddits));
        Collections.sort(subreddits, StringUtil.getCaseInsensitiveComparator());
        SharedPreferencesHelper.saveArray(subreddits, SubredditManager.PREFS_NAME, SubredditManager.ARRAY_NAME, context);
    }

    public static void makeRequest(String url, String callbackFunc, Context context) {
        AjaxCallback<String> cb = new AjaxCallback<String>();
        AQuery aQuery = new AQuery(context);
        cb.url(url).type(String.class).weakHandler(context, callbackFunc).header("User-Agent", USER_AGENT);
        if (mRedditLoginResponse == null) {
            Log.i("mRedditLoginResponse", "null");

        } else {
            Log.i("mRedditLoginResponse", "not null");
            cb.cookie(REDDIT_SESSION, mRedditLoginResponse.getLoginResponse().getData().getCookie());
        }

        aQuery.ajax(cb);
    }

    public static void subscribe(String subreddit, SubscribeAction action, final Context context) {
        if (!mIsLoggedIn) {
            Toast.makeText(context, "Must be logged in to subscribe!", Toast.LENGTH_SHORT).show();
            return;
        }

        AQuery aQuery = new AQuery(context);
        AjaxCallback<String> aboutCallback = getSubredditAbout(subreddit, action, context);

        aQuery.ajax(aboutCallback);

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

    public static AjaxCallback<String> getSubredditAbout(String subreddit, final SubscribeAction action, final Context context) {
        final AQuery aQuery = new AQuery(context);

        AjaxCallback<String> cb = new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {

                Log.i("Got back from about!", object);
                if (status.getCode() == HttpURLConnection.HTTP_OK) {
                    Gson gson = new Gson();
                    try {
                        about = gson.fromJson(object, About.class);
                    } catch (JsonSyntaxException e) {
                        Log.e(TAG, "getSubredditAbout", e);
                    }

                    if (about == null) {
                        Log.e(TAG, "getSubredditAbout null after parsing response");
                        return;
                    }

                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("action", action.getAction());
                    params.put("sr", about.getData().getName());
                    params.put("uh", mRedditLoginResponse.getLoginResponse().getData().getModhash());

                    AjaxCallback<String> cb = new AjaxCallback<String>() {
                        @Override
                        public void callback(String url, String object, AjaxStatus status) {
                            Log.i("Got back from subscribe!", object);
                            if (status.getCode() == HttpURLConnection.HTTP_OK) {

                            }

                        }

                    };

                    cb.header("User-Agent", USER_AGENT);
                    cb.url(REDDIT_SUBSCRIBE_URL);
                    cb.type(String.class);
                    cb.params(params);
                    cb.cookie(REDDIT_SESSION, mRedditLoginResponse.getLoginResponse().getData().getCookie());
                    aQuery.ajax(cb);
                }

            }

        };

        cb.header("User-Agent", USER_AGENT);
        cb.url(String.format(REDDIT_ABOUT_URL, subreddit));
        cb.type(String.class);

        return cb;
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
