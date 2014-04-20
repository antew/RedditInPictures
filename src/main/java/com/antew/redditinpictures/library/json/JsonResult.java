/*
 * Copyright (C) 2014 Antew
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.antew.redditinpictures.library.json;

import android.content.Intent;
import android.os.Bundle;
import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.library.service.RequestCode;
import com.antew.redditinpictures.library.util.BundleUtil;

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
        mJson = BundleUtil.getString(args, RedditService.REST_RESULT, null);
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