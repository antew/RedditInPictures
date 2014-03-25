package com.antew.redditinpictures.library.json;

import android.content.Intent;
import android.os.Bundle;
import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.library.service.RequestCode;

public abstract class JsonResult {
    private Bundle      mExtraData;
    private RequestCode mRequestCode;
    private int         mHttpStatusCode;
    private String      mJson;

    public JsonResult(Intent result) {
        mExtraData = result.getBundleExtra(RedditService.EXTRA_PASS_THROUGH);
        
        Bundle args = result.getBundleExtra(RedditService.EXTRA_BUNDLE);
        mRequestCode = (RequestCode) args.getSerializable(RedditService.EXTRA_REQUEST_CODE);
        mHttpStatusCode = args.getInt(RedditService.EXTRA_STATUS_CODE);
        mJson = args.getString(RedditService.REST_RESULT);
    }

    public Bundle getExtraData() {
        return mExtraData;
    }

    public RequestCode getRequestCode() {
        return mRequestCode;
    }

    public int getHttpStatusCode() {
        return mHttpStatusCode;
    }

    public String getJson() {
        return mJson;
    }

}