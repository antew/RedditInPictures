package com.antew.redditinpictures.library.reddit;

import android.content.Context;
import com.antew.redditinpictures.library.utils.Ln;
import com.antew.redditinpictures.library.utils.Strings;

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
