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
package com.antew.redditinpictures.library.model.reddit;

import android.content.ContentValues;
import com.antew.redditinpictures.library.database.RedditContract;
import com.antew.redditinpictures.library.interfaces.ContentValuesOperation;
import com.google.gson.annotations.SerializedName;

public class RedditLoginResponse implements ContentValuesOperation {
    public static final String TAG = RedditLoginResponse.class.getSimpleName();

    @SerializedName("json") private LoginResponse loginResponse;

    public LoginResponse getLoginResponse() {
        return loginResponse;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        if (loginResponse.getErrors().size() > 0) {
            StringBuilder errorMessage = new StringBuilder();
            for (String[] error : loginResponse.getErrors()) {
                errorMessage.append(error[1] + " ");
            }

            values.put(RedditContract.Login.SUCCESS, 0);
            values.put(RedditContract.Login.ERROR_MESSAGE, errorMessage.toString());
        } else {
            LoginData data = loginResponse.getData();
            values.put(RedditContract.Login.USERNAME, data.getUsername());
            values.put(RedditContract.Login.COOKIE, data.getCookie());
            values.put(RedditContract.Login.MODHASH, data.getModhash());
            values.put(RedditContract.Login.SUCCESS, 1);
        }

        return values;
    }
}
