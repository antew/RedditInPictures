package com.antew.redditinpictures.library.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.antew.redditinpictures.library.R;
import com.antew.redditinpictures.library.interfaces.RedditUpdateProvider;
import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.reddit.MySubreddits;
import com.antew.redditinpictures.library.reddit.MySubreddits.SubredditData;
import com.antew.redditinpictures.library.reddit.RedditApi;
import com.antew.redditinpictures.library.reddit.RedditLoginResponse;
import com.antew.redditinpictures.library.service.RedditService.RequestTypes;
import com.antew.redditinpictures.library.utils.StringUtil;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * 
 * We use a Fragment with setRetainInstance set to true so that we don't lose the request on
 * orientation change
 * 
 */
public final class RedditDataFragment extends RESTResponderFragment {
    public static final String TAG    = RedditDataFragment.class.getSimpleName();
    public static final String URL    = "url";
    public static final String INTENT = "intent";

    public static RedditDataFragment newInstance(Intent intent) {
        RedditDataFragment frag = new RedditDataFragment();
        Bundle args = new Bundle();
        args.putParcelable(INTENT, intent);
        frag.setArguments(args);

        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        Intent intent = getArguments().getParcelable(INTENT);
        makeRequest(intent);
    }

    public void makeRequest(Intent intent) {
        Log.i(TAG, "makeRequest");
        intent.putExtra(RedditService.EXTRA_RESULT_RECEIVER, getResultReceiver());

        RedditUpdateProvider updater = (RedditUpdateProvider) getActivity();
        if (updater != null) {
            updater.createService(intent);
            updater.setRequestInProgress(true);
        }
    }

    @Override
    public void onRESTResult(int code, int requestCode, String result) {
        Log.i(TAG, "onRESTResult");
        Activity activity = getActivity();
        RedditUpdateProvider updater;
        if (activity != null) {
            if (activity instanceof RedditUpdateProvider) {
                updater = (RedditUpdateProvider) activity;
            } else {
                Log.e(TAG, "Activity must implement RedditUpdateProvider interface!");
                return;
            }

            if (code != 200 || result == null) {
                updater.onError(code);
                return;
            }

            updater.setRequestInProgress(false);
            Gson gson = new Gson();

            switch (requestCode) {
                case RequestTypes.POSTS:

                    try {
                        RedditApi redditApi = RedditApi.getGson().fromJson(result, RedditApi.class);
                        updater.addNewPosts(redditApi);
                    } catch (JsonSyntaxException e) {
                        Log.e(TAG, "onReceiveResult - JsonSyntaxException while parsing json!", e);
                        Toast.makeText(activity, getString(R.string.error_parsing_reddit_data), Toast.LENGTH_SHORT).show();
                        return;
                    } catch (IllegalStateException e) {
                        Log.e(TAG, "onReceiveResult - IllegalStateException while parsing json!", e);
                        Toast.makeText(activity, getString(R.string.error_parsing_reddit_data), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    break;

                case RequestTypes.LOGIN:
                    Log.i(TAG, "login request finished");
                    RedditLoginResponse mRedditLoginResponse = new Gson().fromJson(result, RedditLoginResponse.class);

                    if (mRedditLoginResponse.getLoginResponse().getErrors().size() > 0) {
                        Log.i(TAG, "Error on login");
                    } else {
                        String mModHash = mRedditLoginResponse.getLoginResponse().getData().getModhash();
                        Log.i(TAG, "modhash = " + mModHash);
                        String mCookie = mRedditLoginResponse.getLoginResponse().getData().getCookie();
                        Log.i(TAG, "cookie = " + mCookie);
                        // SharedPreferencesHelper.saveLoginInformation(mUsername, mModHash,
                        // mCookie, mJson, context);
                    }
                    updater.loginComplete(mRedditLoginResponse);
                    break;

                case RequestTypes.MY_SUBREDDITS:
                    Log.i(TAG, "MySubreddits complete!");
                    Log.i("MyRedditsJson", result);

                    MySubreddits mySubreddits = gson.fromJson(result, MySubreddits.class);

                    List<String> subReddits = new ArrayList<String>();

                    for (MySubreddits.Children c : mySubreddits.getData().getChildren()) {
                        SubredditData data = c.getData();
                        subReddits.add(data.getDisplay_name());
                        Log.i("Subscribed Subreddits", data.getDisplay_name());
                    }

                    Collections.sort(subReddits, StringUtil.getCaseInsensitiveComparator());
                    // SharedPreferencesHelper.saveArray(subReddits, SubredditManager.PREFS_NAME,
                    // SubredditManager.ARRAY_NAME, ImageGridActivity.this);

                    // getSupportActionBar().setListNavigationCallbacks(getListNavigationSpinner(),
                    // ImageGridActivity.this);

                    break;

            }

        } else {
            Log.i(TAG, "Activity was null, unable to add posts!");
        }
    }
}