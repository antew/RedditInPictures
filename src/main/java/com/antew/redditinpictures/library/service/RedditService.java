package com.antew.redditinpictures.library.service;

import android.content.Context;
import android.content.Intent;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.enums.Age;
import com.antew.redditinpictures.library.enums.Category;
import com.antew.redditinpictures.library.enums.SubscribeAction;
import com.antew.redditinpictures.library.enums.Vote;
import com.antew.redditinpictures.library.reddit.RedditLoginInformation;
import com.antew.redditinpictures.library.reddit.RedditUrl;
import com.antew.redditinpictures.library.reddit.json.RedditResult;
import com.antew.redditinpictures.library.utils.Ln;
import com.antew.redditinpictures.library.utils.SafeAsyncTask;
import com.antew.redditinpictures.library.utils.Strings;
import com.antew.redditinpictures.library.utils.SubredditUtils;
import com.antew.redditinpictures.sqlite.RedditContract;
import com.antew.redditinpictures.sqlite.RedditDatabase;
import java.util.Calendar;
import java.util.Date;

public class RedditService extends RESTService {

    public static void getPostsIfNeeded(Context context, String subreddit, Age age, Category category) {
        new GetNewPostsIfNeededTask(context, subreddit, age, category).execute();
    }

    public static void getPosts(Context context, String subreddit, Age age, Category category) {
        getPosts(context, subreddit, age, category, null);
    }

    public static void getPosts(Context context, String subreddit, Age age, Category category, String after) {
        if (subreddit == null) {
            subreddit = Constants.REDDIT_FRONTPAGE;
        }

        if (age == null) {
            age = Age.TODAY;
        }

        if (category == null) {
            category = Category.HOT;
        }

        Ln.d("Retrieving Posts For %s %s %s After %s", subreddit, category.toString(), age.toString(), after);

        RedditUrl url = new RedditUrl.Builder(subreddit).age(age).category(category).count(Constants.POSTS_TO_FETCH).after(after).build();

        Intent intent = new Intent(context, RedditService.class);
        intent = getIntentBasics(intent);
        intent.putExtra(RedditService.EXTRA_REQUEST_CODE, RequestCode.POSTS);
        intent.putExtra(RedditService.EXTRA_REPLACE_ALL, Strings.isEmpty(after));

        Bundle extraPassthru = new Bundle();
        extraPassthru.putString(RedditService.EXTRA_SUBREDDIT, subreddit);
        extraPassthru.putString(RedditService.EXTRA_CATEGORY, Strings.toString(category));
        extraPassthru.putString(RedditService.EXTRA_AGE, Strings.toString(age));

        intent.putExtra(EXTRA_PASS_THROUGH, extraPassthru);
        intent.setData(Uri.parse(url.getUrl()));

        context.startService(intent);
    }

    private static Intent getIntentBasics(Intent intent) {
        intent.putExtra(EXTRA_USER_AGENT, Constants.Reddit.USER_AGENT);

        if (RedditLoginInformation.isLoggedIn()) {
            intent.putExtra(EXTRA_COOKIE, Constants.Reddit.REDDIT_SESSION + "=" + RedditLoginInformation.getCookie());
        }

        return intent;
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

    public static void unsubscribe(Context context, String subreddit) {
        changeSubscription(context, subreddit, SubscribeAction.UNSUBSCRIBE);
    }

    public static void searchSubreddits(Context context, String query, boolean searchNsfw) {
        Intent intent = new Intent(context, RedditService.class);
        intent = getIntentBasics(intent);
        intent.putExtra(RedditService.EXTRA_REQUEST_CODE, RequestCode.SEARCH_SUBREDDITS);
        String url = Constants.Reddit.REDDIT_SEARCH_SUBREDDITS_URL + "?query=" + query + "&include_over_18=" + (searchNsfw == true ? "true"
                                                                                                                                   : "false");
        intent.setData(Uri.parse(url));
        intent.putExtra(EXTRA_HTTP_VERB, POST);

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

    private static class GetNewPostsIfNeededTask extends SafeAsyncTask<Void> {
        Context  mContext;
        String   mSubreddit;
        Category mCategory;
        Age      mAge;

        public GetNewPostsIfNeededTask(Context context, String subreddit, Age age, Category category) {
            mContext = context;
            mSubreddit = subreddit;
            mCategory = category;
            mAge = age;
        }

        @Override
        public Void call() throws Exception {
            RedditDatabase databaseHelper = new RedditDatabase(mContext);
            SQLiteDatabase database = databaseHelper.getReadableDatabase();

            // If we have an aggregate subreddit, we just want to see if other stuff has been saved if so we need a full refresh.
            // TODO: Make this handle caching better...
            if (SubredditUtils.isAggregateSubreddit(mSubreddit)) {
                long numRecords = DatabaseUtils.queryNumEntries(database, RedditDatabase.Tables.REDDIT_DATA);

                Ln.d("%s is an Aggregate Subreddit and We Have %d Rows of Reddit Data", mSubreddit, numRecords);

                // If we have more than 1 record it is safe to assume that we need to do a full refresh.
                if (numRecords > 1) {
                    database.close();
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

            // TODO: Make this work for < API 11.
            long numUpdates = DatabaseUtils.queryNumEntries(database, RedditDatabase.Tables.REDDIT_DATA,
                                                            "subreddit = ? AND retrievedDate BETWEEN ? AND ?", new String[] {
                    mSubreddit, String.valueOf(fiveMinutesAgoDate.getTime()), String.valueOf(currentDate.getTime())
                }
                                                           );

            database.close();
            Ln.d("There Are %d Rows In 5 Minutes For %s", numUpdates, mSubreddit);

            if (numUpdates <= 0) {
                getPosts(mContext, mSubreddit, mAge, mCategory);
            }

            return null;
        }
    }
}
