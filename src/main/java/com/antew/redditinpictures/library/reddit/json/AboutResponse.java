package com.antew.redditinpictures.library.reddit.json;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.utils.Consts;

public class AboutResponse extends RedditResponseHandler {

    public static final String TAG = AboutResponse.class.getSimpleName();
    private RedditResult       result;

    public AboutResponse(RedditResult result) {
        this.result = result;
    }

    @Override
    public void processHttpResponse(Context context) {
        Log.i(TAG, "Got back subreddit about! = " + result.getJson());
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Consts.BROADCAST_ABOUT_SUBREDDIT));
    }

}
