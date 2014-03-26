package com.antew.redditinpictures.library.service;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import com.antew.redditinpictures.library.enums.Age;
import com.antew.redditinpictures.library.enums.Category;
import com.antew.redditinpictures.library.enums.SubscribeAction;
import com.antew.redditinpictures.library.enums.Vote;
import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.reddit.RedditLoginInformation;
import com.antew.redditinpictures.library.reddit.RedditUrl;
import com.antew.redditinpictures.library.reddit.json.RedditResult;
import com.antew.redditinpictures.library.utils.Constants;
import com.antew.redditinpictures.library.utils.Ln;
import com.antew.redditinpictures.library.utils.SafeAsyncTask;
import com.antew.redditinpictures.sqlite.RedditContract;
import com.antew.redditinpictures.sqlite.RedditDatabase;
import java.util.ArrayList;
import java.util.List;

public class RedditService extends RESTService {
    private static ForceRefreshSubredditTask mForceRefreshSubredditTask;

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

    private static Intent getIntentBasics(Intent intent) {
        intent.putExtra(EXTRA_USER_AGENT, Constants.Reddit.USER_AGENT);

        if (RedditLoginInformation.isLoggedIn())
            intent.putExtra(EXTRA_COOKIE, Constants.Reddit.REDDIT_SESSION + "=" + RedditLoginInformation.getCookie());

        return intent;
    }

    public static void forceRefreshSubreddit(Context context, String subreddit, Age age, Category category) {
        Ln.d("Attempting to Force Refresh For %s %s %s", subreddit, age, category);
        if (mForceRefreshSubredditTask == null) {
            mForceRefreshSubredditTask = new ForceRefreshSubredditTask(context, subreddit, age, category);
            mForceRefreshSubredditTask.execute();
            return;
        }

        // If a request is currently processing, let's see if we need to cancel it.
        if (mForceRefreshSubredditTask.isProcessing()) {
            // If we have a request that doesn't exactly equal what we are doing, then let's cancel it and start a new request.
            if (!mForceRefreshSubredditTask.mSubreddit.equals(subreddit) || mForceRefreshSubredditTask.mAge != age || mForceRefreshSubredditTask.mCategory != category) {
                mForceRefreshSubredditTask = new ForceRefreshSubredditTask(context, subreddit, age, category);
                mForceRefreshSubredditTask.execute();
            } else {
                // If we have the same request going on, just let it go.
                return;
            }
        } else {
            // Otherwise we aren't processing anything currently.

            // If the currently created task isn't for the same thing, create a new one.
            if (!mForceRefreshSubredditTask.mSubreddit.equals(subreddit) || mForceRefreshSubredditTask.mAge != age || mForceRefreshSubredditTask.mCategory != category) {
                mForceRefreshSubredditTask = new ForceRefreshSubredditTask(context, subreddit, age, category);
            }

            // Now we have either created a new task or we are restarting an old one with for the same thing.
            mForceRefreshSubredditTask.execute();
        }
    }

    public static void getPosts(Context context, String subreddit, Age age, Category category) {
        getPosts(context, subreddit, age, category, null);
    }

    public static void getPosts(Context context, String subreddit, Age age, Category category, String after) {
        if (subreddit == null) subreddit = Constants.REDDIT_FRONTPAGE;

        if (age == null) age = Age.TODAY;

        if (category == null) category = Category.HOT;

        RedditUrl url = new RedditUrl.Builder(subreddit).age(age)
            .category(category)
            .count(Constants.POSTS_TO_FETCH)
            .after(after)
            .build();

        getPosts(context, url.getUrl());
    }

    private static void getPosts(Context context, String url) {
        Intent intent = new Intent(context, RedditService.class);
        intent = getIntentBasics(intent);
        intent.putExtra(RedditService.EXTRA_REQUEST_CODE, RequestCode.POSTS);
        intent.setData(Uri.parse(url));

        context.startService(intent);
    }

    public static void vote(Context context, String name, Vote vote) {
        Intent intent = new Intent(context, RedditService.class);
        intent = getIntentBasics(intent);
        intent.setData(Uri.parse(Constants.Reddit.REDDIT_VOTE_URL));
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
        String url = Constants.Reddit.REDDIT_LOGIN_URL + username;

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
        intent.setData(Uri.parse(Constants.Reddit.REDDIT_MY_SUBREDDITS_URL));
        intent.putExtra(RedditService.EXTRA_REQUEST_CODE, RequestCode.MY_SUBREDDITS);
        intent.putExtra(EXTRA_HTTP_VERB, GET);

        context.startService(intent);
    }

    public static void aboutSubreddit(Context context, String subreddit) {
        Intent intent = new Intent(context, RedditService.class);
        intent = getIntentBasics(intent);
        intent.putExtra(RedditService.EXTRA_REQUEST_CODE, RequestCode.ABOUT_SUBREDDIT);
        intent.setData(Uri.parse(String.format(Constants.Reddit.REDDIT_ABOUT_URL, subreddit)));

        context.startService(intent);
    }

    public static void subscribe(Context context, String subreddit) {
        changeSubscription(context, subreddit, SubscribeAction.SUBSCRIBE);
    }

    public static void unsubscribe(Context context, String subreddit) {
        changeSubscription(context, subreddit, SubscribeAction.UNSUBSCRIBE);
    }

    private static void changeSubscription(Context context, String subreddit, SubscribeAction action) {
        Intent intent = new Intent(context, RedditService.class);
        intent = getIntentBasics(intent);
        intent.putExtra(RedditService.EXTRA_REQUEST_CODE, RequestCode.SUBSCRIBE);
        intent.setData(Uri.parse(Constants.Reddit.REDDIT_SUBSCRIBE_URL));
        intent.putExtra(EXTRA_HTTP_VERB, POST);

        Bundle bundle = new Bundle();
        bundle.putString("action", action.getAction());
        bundle.putString("sr", subreddit);
        bundle.putString("uh", RedditLoginInformation.getModhash());
        intent.putExtra(EXTRA_PARAMS, bundle);

        context.startService(intent);
    }

    public static void searchSubreddits(Context context, String query, boolean searchNsfw) {
        Intent intent = new Intent(context, RedditService.class);
        intent = getIntentBasics(intent);
        intent.putExtra(RedditService.EXTRA_REQUEST_CODE, RequestCode.SEARCH_SUBREDDITS);
        String url = Constants.Reddit.REDDIT_SEARCH_SUBREDDITS_URL + "?query=" + query + "&include_over_18=" + (searchNsfw == true ? "true" : "false");
        intent.setData(Uri.parse(url));
        intent.putExtra(EXTRA_HTTP_VERB, POST);

        context.startService(intent);
    }

    private static class ForceRefreshSubredditTask extends SafeAsyncTask<Void> {
        private Context mContext;
        protected String mSubreddit;
        protected Age mAge;
        protected Category mCategory;

        private boolean mProcessing = false;

        public ForceRefreshSubredditTask(Context context, String subreddit, Age age, Category category) {
            mContext = context;
            mSubreddit = subreddit;
            mAge = age;
            mCategory = category;
        }

        public boolean isProcessing() {
            return mProcessing;
        }

        @Override
        public Void call() throws Exception {
            mProcessing = true;

            if (mSubreddit == null) {
                mSubreddit = Constants.REDDIT_FRONTPAGE;
            }

            if (mAge == null) {
                mAge = Age.TODAY;
            }

            if (mCategory == null) {
                mCategory = Category.HOT;
            }

            Ln.d("Forcing Refresh For %s %s %s", mSubreddit, mAge, mCategory);

            ContentResolver resolver = mContext.getContentResolver();

            // If we have an aggregate subreddit we need to clear out everything.
            if (mSubreddit.equals(Constants.REDDIT_FRONTPAGE) || mSubreddit.equals(Constants.REDDIT_FRONTPAGE_DISPLAY_NAME) || mSubreddit.equals(Constants.REDDIT_ALL_DISPLAY_NAME)) {
                // Remove all of the post rows.
                resolver.delete(RedditContract.Posts.CONTENT_URI, null, null);
            } else if (mSubreddit.contains("+")) {
                // Poor mans checking for multis. If we have a multi, we want to handle all of them appropriately.
                String[] subredditArray = mSubreddit.split("\\+");

                String where = null;
                List<String> selectionArgsList = new ArrayList<String>();

                for (String item : subredditArray) {
                    if (where == null) {
                        where = RedditContract.PostColumns.SUBREDDIT + " in (?";
                    } else {
                        where += ",?";
                    }
                    selectionArgsList.add(item);
                }
                // Close the in statement.
                where += ")";

                // Only delete records for the subreddits contained in the multi.
                resolver.delete(RedditContract.Posts.CONTENT_URI, where, selectionArgsList.toArray(new String[]{}));
            } else {
                String where = RedditContract.PostColumns.SUBREDDIT + " = ?";
                String[] selectionArgs = new String[] {mSubreddit};

                // Otherwise we have a single subreddit, so we want to remove only posts for that subreddit.
                resolver.delete(RedditContract.Posts.CONTENT_URI, where, selectionArgs);
            }

            getPosts(mContext, mSubreddit, mAge, mCategory);
            return null;
        }

        @Override protected void onFinally() throws RuntimeException {
            super.onFinally();
            mProcessing = false;
        }
    }
}
