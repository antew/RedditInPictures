package com.antew.redditinpictures.library.reddit;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.util.Ln;
import com.antew.redditinpictures.library.util.Strings;

public class SubscribeResponse extends RedditResponseHandler {
    private RedditResult result;

    public SubscribeResponse(RedditResult result) {
        this.result = result;
    }

    @Override
    public void processHttpResponse(Context context) {
        Ln.i("Got back from subscribe! = %s", Strings.toString(result.getJson()));
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constants.Broadcast.BROADCAST_SUBSCRIBE));
    }
}
