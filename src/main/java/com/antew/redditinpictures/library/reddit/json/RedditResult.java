package com.antew.redditinpictures.library.reddit.json;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.antew.redditinpictures.library.json.JsonResult;
import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.pro.BuildConfig;

;

public class RedditResult extends JsonResult {
    private boolean mReplaceAll;

    public RedditResult(Intent result) {
        super(result);
        Bundle args = result.getBundleExtra(RedditService.EXTRA_BUNDLE);
        mReplaceAll = args.getBoolean(RedditService.EXTRA_REPLACE_ALL);
    }

    public boolean isReplaceAll() {
        return mReplaceAll;
    }

    public void handleResponse(Context context) {
        RedditResponseHandler type = RedditResponseHandler.newInstance(this);
        if (type != null) {
            type.processHttpResponse(context);
        } else {
            if (BuildConfig.DEBUG) {
                throw new NullPointerException("ResponseType was null, is the requestCode defined in ResponseType.newInstance()?");
            }
        }
    }
}