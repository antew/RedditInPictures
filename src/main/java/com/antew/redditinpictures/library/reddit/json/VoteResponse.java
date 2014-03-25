package com.antew.redditinpictures.library.reddit.json;

import android.content.Context;
import com.antew.redditinpictures.library.logging.Log;

public class VoteResponse extends RedditResponseHandler {

    public static final String TAG = VoteResponse.class.getSimpleName();
    private RedditResult       result;

    public VoteResponse(RedditResult result) {
        this.result = result;
    }

    @Override
    public void processHttpResponse(Context context) {
        Log.i(TAG, "Got back from vote! = " + result.getJson());
    }

}
