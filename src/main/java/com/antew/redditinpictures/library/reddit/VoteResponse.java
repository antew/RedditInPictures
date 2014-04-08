package com.antew.redditinpictures.library.reddit;

import android.content.Context;
import com.antew.redditinpictures.library.util.Ln;
import com.antew.redditinpictures.library.util.Strings;

public class VoteResponse extends RedditResponseHandler {
    private RedditResult result;

    public VoteResponse(RedditResult result) {
        this.result = result;
    }

    @Override
    public void processHttpResponse(Context context) {
        Ln.i("Got back from vote! = %s", Strings.toString(result.getJson()));
    }
}
