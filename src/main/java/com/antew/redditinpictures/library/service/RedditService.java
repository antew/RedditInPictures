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
package com.antew.redditinpictures.library.service;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.Injector;
import com.antew.redditinpictures.library.database.RedditContract;
import com.antew.redditinpictures.library.database.RedditDatabase;
import com.antew.redditinpictures.library.event.RequestInProgressEvent;
import com.antew.redditinpictures.library.imgur.ImgurImageApi;
import com.antew.redditinpictures.library.model.Age;
import com.antew.redditinpictures.library.model.Category;
import com.antew.redditinpictures.library.model.SubscribeAction;
import com.antew.redditinpictures.library.model.Vote;
import com.antew.redditinpictures.library.model.reddit.PostData;
import com.antew.redditinpictures.library.model.reddit.RedditLoginInformation;
import com.antew.redditinpictures.library.model.reddit.RedditUrl;
import com.antew.redditinpictures.library.reddit.RedditResult;
import com.antew.redditinpictures.library.util.AndroidUtil;
import com.antew.redditinpictures.library.util.Ln;
import com.antew.redditinpictures.library.util.SafeAsyncTask;
import com.antew.redditinpictures.library.util.Strings;
import com.antew.redditinpictures.library.util.SubredditUtil;
import com.google.gson.Gson;
import com.squareup.otto.Bus;
import java.util.Calendar;
import java.util.Date;
import javax.inject.Inject;

public class RedditService extends RESTService {

    public static void getPostsIfNeeded(Context context, String subreddit, Age age, Category category) {
        new GetNewPostsIfNeededTask(context, subreddit, age, category).execute();
    }

    public static void getPosts(Context context, String subreddit, Age age, Category category) {
        SubredditUtil.deletePostsForSubreddit(context, subreddit);
        getPosts(context, subreddit, age, category, null);
    }

    public static void getPosts(Context context, String subreddit, Age age, Category category, String after) {
        if (subreddit == null) {
            subreddit = Constants.Reddit.REDDIT_FRONTPAGE;
        }

        if (age == null) {
            age = Age.TODAY;
        }

        if (category == null) {
            category = Category.HOT;
        }

        Ln.d("Retrieving Posts For %s %s %s After %s", subreddit, category.toString(), age.toString(), after);

        RedditUrl url = new RedditUrl.Builder(subreddit).age(age)
                                                        .category(category)
                                                        .count(Constants.Reddit.POSTS_TO_FETCH)
                                                        .after(after)
                                                        .build();

        Intent intent = new Intent(context, RedditService.class);
        intent = getIntentBasics(intent);
        intent.putExtra(RedditService.EXTRA_REQUEST_CODE, RequestCode.POSTS);

        Bundle extraPassthru = new Bundle();
        extraPassthru.putBoolean(RedditService.EXTRA_REPLACE_ALL, Strings.isEmpty(after));
        extraPassthru.putString(Constants.Extra.EXTRA_SUBREDDIT, subreddit);
        extraPassthru.putString(Constants.Extra.EXTRA_CATEGORY, Strings.toString(category));
        extraPassthru.putString(Constants.Extra.EXTRA_AGE, Strings.toString(age));

        intent.putExtra(EXTRA_PASS_THROUGH, extraPassthru);
        intent.setData(Uri.parse(url.getUrl()));

        context.startService(intent);
    }

    private static Intent getIntentBasics(Intent intent) {
        intent.putExtra(EXTRA_USER_AGENT, Constants.Reddit.USER_AGENT);

        if (RedditLoginInformation.isLoggedIn()) {
            intent.putExtra(EXTRA_COOKIE, Constants.Reddit.Endpoint.REDDIT_SESSION + "=" + RedditLoginInformation.getCookie());
        }

        return intent;
    }

    public static void vote(Context context, String name, Vote vote) {
        Intent intent = new Intent(context, RedditService.class);
        intent = getIntentBasics(intent);
        intent.setData(Uri.parse(Constants.Reddit.Endpoint.REDDIT_VOTE_URL));
        intent.putExtra(EXTRA_HTTP_VERB, POST);
        intent.putExtra(RedditService.EXTRA_REQUEST_CODE, RequestCode.VOTE);

        Bundle bundle = new Bundle();
        bundle.putString("id", name);
        bundle.putInt("dir", vote.getVote());
        bundle.putString("uh", RedditLoginInformation.getModhash());

        intent.putExtra(EXTRA_PARAMS, bundle);

        context.startService(intent);
    }

    public static void login(Context context, String username, String password) {
        String url = Constants.Reddit.Endpoint.REDDIT_LOGIN_URL + username;

        Intent intent = new Intent(context, RedditService.class);
        intent = getIntentBasics(intent);
        intent.setData(Uri.parse(url));
        intent.putExtra(RedditService.EXTRA_REQUEST_CODE, RequestCode.LOGIN);
        intent.putExtra(EXTRA_HTTP_VERB, POST);

        Bundle extraPassthru = new Bundle();
        extraPassthru.putString(RedditContract.Login.USERNAME, username);

        Bundle params = new Bundle();
        params.putString("api_type", "json");
        params.putString("user", username);
        params.putString("passwd", password);

        intent.putExtra(EXTRA_PASS_THROUGH, extraPassthru);
        intent.putExtra(EXTRA_PARAMS, params);

        context.startService(intent);
    }

    public static void getMySubreddits(Context context) {
        Intent intent = new Intent(context, RedditService.class);
        intent = getIntentBasics(intent);
        intent.setData(Uri.parse(Constants.Reddit.Endpoint.REDDIT_MY_SUBREDDITS_URL));
        intent.putExtra(RedditService.EXTRA_REQUEST_CODE, RequestCode.MY_SUBREDDITS);
        intent.putExtra(EXTRA_HTTP_VERB, GET);

        context.startService(intent);
    }

    public static void aboutSubreddit(Context context, String subreddit) {
        Intent intent = new Intent(context, RedditService.class);
        intent = getIntentBasics(intent);
        intent.putExtra(RedditService.EXTRA_REQUEST_CODE, RequestCode.ABOUT_SUBREDDIT);
        intent.setData(Uri.parse(String.format(Constants.Reddit.Endpoint.REDDIT_ABOUT_URL, subreddit)));

        context.startService(intent);
    }

    public static void subscribe(Context context, String subreddit) {
        changeSubscription(context, subreddit, SubscribeAction.SUBSCRIBE);
    }

    private static void changeSubscription(Context context, String subreddit, SubscribeAction action) {
        Intent intent = new Intent(context, RedditService.class);
        intent = getIntentBasics(intent);
        intent.putExtra(RedditService.EXTRA_REQUEST_CODE, RequestCode.SUBSCRIBE);
        intent.setData(Uri.parse(Constants.Reddit.Endpoint.REDDIT_SUBSCRIBE_URL));
        intent.putExtra(EXTRA_HTTP_VERB, POST);

        Bundle bundle = new Bundle();
        bundle.putString("action", action.getAction());
        bundle.putString("sr", subreddit);
        bundle.putString("uh", RedditLoginInformation.getModhash());
        intent.putExtra(EXTRA_PARAMS, bundle);

        context.startService(intent);
    }

    public static void unsubscribe(Context context, String subreddit) {
        changeSubscription(context, subreddit, SubscribeAction.UNSUBSCRIBE);
    }

    public static void searchSubreddits(Context context, String query, boolean searchNsfw) {
        Intent intent = new Intent(context, RedditService.class);
        intent = getIntentBasics(intent);
        intent.putExtra(RedditService.EXTRA_REQUEST_CODE, RequestCode.SEARCH_SUBREDDITS);
        String url = Constants.Reddit.Endpoint.REDDIT_SEARCH_SUBREDDITS_URL + "?query=" + query + "&include_over_18=" + (searchNsfw == true
                                                                                                                         ? "true"
                                                                                                                         : "false");
        intent.setData(Uri.parse(url));
        intent.putExtra(EXTRA_HTTP_VERB, POST);

        context.startService(intent);
    }

    public static void reportPost(Context context, PostData postData) {
        String json = null;
        if (postData != null) {
            Gson gson = new Gson();
            json = gson.toJson(postData);
        }

        reportIssue(context, Constants.REPORT_POST_URL, json);
    }

    public static void reportImage(Context context, ImgurImageApi.ImgurImage image) {
        String json = null;
        if (image != null) {
            Gson gson = new Gson();
            json = gson.toJson(image);
        }

        reportIssue(context, Constants.REPORT_IMAGE_URL, json);
    }

    private static void reportIssue(Context context, String url, String jsonData) {
        Intent intent = new Intent(context, RedditService.class);
        intent = getIntentBasics(intent);
        intent.putExtra(RedditService.EXTRA_REQUEST_CODE, RequestCode.REPORT_IMAGE);
        intent.setData(Uri.parse(url));
        intent.putExtra(EXTRA_HTTP_VERB, POST);

        String appVersion = null;
        PackageInfo packageInfo = null;

        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (packageInfo != null) {
            appVersion = packageInfo.versionName + " (" + packageInfo.versionCode + ")";
        }

        if (Strings.isEmpty(appVersion)) {
            appVersion = "null";
        }

        if (Strings.isEmpty(jsonData)) {
            jsonData = "null";
        }

        Bundle params = new Bundle();
        params.putString("appVersion", appVersion);
        params.putString("json", jsonData);

        intent.putExtra(EXTRA_PARAMS, params);
        context.startService(intent);
    }

    @Override
    public void onRequestComplete(Intent result) {
        super.onRequestComplete(result);

        RedditResult redditResult = new RedditResult(result);

        if (redditResult.getHttpStatusCode() != 200 || redditResult.getJson() == null) {
            Ln.i("onRequestComplete - error retrieving data, status code was %d", redditResult.getHttpStatusCode());
            return;
        }

        redditResult.handleResponse(getApplicationContext());
    }

    public static class GetNewPostsIfNeededTask extends SafeAsyncTask<Void> {
        Context  mContext;
        String   mSubreddit;
        Category mCategory;
        Age      mAge;
        @Inject
        Bus mBus;

        public GetNewPostsIfNeededTask(Context context, String subreddit, Age age, Category category) {
            mContext = context;

            if (Strings.isEmpty(subreddit)) {
                subreddit = Constants.Reddit.REDDIT_FRONTPAGE;
            }

            if (category == null) {
                category = Category.HOT;
            }

            if (age == null) {
                age = Age.TODAY;
            }

            mSubreddit = subreddit;
            mCategory = category;
            mAge = age;
            Injector.inject(this);
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public Void call() throws Exception {
            SQLiteDatabase database = null;
            Cursor rowCountCursor = null;
            try {

                RedditDatabase databaseHelper = new RedditDatabase(mContext);
                database = databaseHelper.getReadableDatabase();

                // If we have an aggregate subreddit, we just want to see if other stuff has been saved if so we need a full refresh.
                // TODO: Make this handle caching better...
                if (SubredditUtil.isAggregateSubreddit(mSubreddit)) {
                    long numRecords = DatabaseUtils.queryNumEntries(database, RedditDatabase.Tables.REDDIT_DATA);

                    Ln.d("%s is an Aggregate Subreddit and We Have %d Rows of Reddit Data", mSubreddit, numRecords);

                    // If we have more than 1 record it is safe to assume that we need to do a full refresh.
                    if (numRecords > 1) {
                        database.close();
                        mBus.post(new RequestInProgressEvent());
                        getPosts(mContext, mSubreddit, mAge, mCategory);
                        return null;
                    }
                    // Otherwise, we want to carry on like normal.
                }

                Date currentDate = new Date();

                Calendar cal = Calendar.getInstance();
                cal.setTime(currentDate);
                cal.add(Calendar.MINUTE, -5);
                Date fiveMinutesAgoDate = cal.getTime();

                int numUpdates;
                if (AndroidUtil.hasHoneycomb()) {
                    numUpdates = (int) DatabaseUtils.queryNumEntries(database, RedditDatabase.Tables.REDDIT_DATA,
                                                                     "subreddit = ? AND category = ? AND age = ? AND retrievedDate BETWEEN ? AND ?",
                                                                     new String[] {
                                                                         mSubreddit, mCategory.getName(), mAge.getAge(),
                                                                         String.valueOf(fiveMinutesAgoDate.getTime()),
                                                                         String.valueOf(currentDate.getTime())
                                                                     }
                                                                    );
                } else {
                    // While we support < API 11 we can't use DatabaseUtils.queryNumEntries
                    rowCountCursor = database.rawQuery("select count(*) from " + RedditDatabase.Tables.REDDIT_DATA +
                                                       " where subreddit = '" + mSubreddit + "'" +
                                                       " AND category = '" + mCategory.getName() + "'" +
                                                       " AND age = '" + mAge.getAge() + "'" +
                                                       " AND retrievedDate BETWEEN '" + fiveMinutesAgoDate.getTime() + "'" +
                                                       " AND '" + currentDate.getTime() + "'", null
                                                      );
                    rowCountCursor.moveToFirst();
                    numUpdates = rowCountCursor.getInt(0);
                }

                Ln.d("There Are %d Rows In 5 Minutes For %s", numUpdates, mSubreddit);

                if (numUpdates <= 0) {
                    mBus.post(new RequestInProgressEvent());
                    getPosts(mContext, mSubreddit, mAge, mCategory);
                }
            } finally {
                if (database != null) {
                    database.close();
                }

                if (rowCountCursor != null) {
                    rowCountCursor.close();
                }
            }

            return null;
        }
    }
}
