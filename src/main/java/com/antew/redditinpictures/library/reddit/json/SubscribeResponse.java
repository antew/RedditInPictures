package com.antew.redditinpictures.library.reddit.json;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.utils.Constants;

public class SubscribeResponse extends RedditResponseHandler {

    public static final String TAG = SubscribeResponse.class.getSimpleName();
    private RedditResult result;

    public SubscribeResponse(RedditResult result) {
        this.result = result;
    }

    @Override
    public void processHttpResponse(Context context) {
        Log.i(TAG, "Got back from subscribe! = " + result.getJson());
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constants.BROADCAST_SUBSCRIBE));
    }
}
