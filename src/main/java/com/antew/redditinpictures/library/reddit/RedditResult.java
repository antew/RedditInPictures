package com.antew.redditinpictures.library.reddit;

import android.content.Context;
import android.content.Intent;
import com.antew.redditinpictures.library.json.JsonResult;
import com.antew.redditinpictures.pro.BuildConfig;

;

public class RedditResult extends JsonResult {
    public RedditResult(Intent result) {
        super(result);
    }

    public void handleResponse(Context context) {
        RedditResponseHandler type = RedditResponseHandler.newInstance(this);
        if (type != null) {
            type.processHttpResponse(context);
        }
    }
}