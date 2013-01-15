/*
 * Copyright (C) 2012 Antew | antewcode@gmail.com
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
package com.antew.redditinpictures.library.reddit;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentProviderOperation;
import android.content.ContentValues;

import com.antew.redditinpictures.library.interfaces.ContentValuesOperation;
import com.antew.redditinpictures.library.json.GsonType;
import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.sqlite.RedditContract;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

public class RedditLoginResponse extends GsonType implements ContentValuesOperation {
    public static final String TAG = RedditLoginResponse.class.getSimpleName();
    
    @SerializedName("json") private LoginResponse loginResponse;
    
    public LoginResponse getLoginResponse() {
        return loginResponse;
    }

    public static class LoginResponse {
        private List<String[]> errors;
        private LoginData      data;

        public List<String[]> getErrors() {
            return errors;
        }

        public LoginData getData() {
            return data;
        }
    }

    public static class LoginData {
        private String username;
        private String modhash;
        private String cookie;

        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getModhash() {
            return modhash;
        }

        public String getCookie() {
            return cookie;
        }
    }

    @Override
    public ArrayList<ContentProviderOperation> parse(String json) {
        final ArrayList<ContentProviderOperation> list = new ArrayList<ContentProviderOperation>();
        
        try {
            RedditLoginResponse loginResponse = new Gson().fromJson(json, RedditLoginResponse.class);
            ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(RedditContract.Login.CONTENT_URI);
            builder.withValue(RedditContract.Login.COOKIE, loginResponse.getLoginResponse().getData().getCookie());
            builder.withValue(RedditContract.Login.MODHASH, loginResponse.getLoginResponse().getData().getModhash());
            
            list.add(builder.build());
        } catch (JsonSyntaxException e) {
            String result = json == null ? "null" : "json";
            Log.e(TAG, "Error parsing login response, json = " + result);
        }
        
        return list;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        if (loginResponse.getErrors().size() > 0) {
            StringBuilder errorMessage = new StringBuilder();
            for (String[] error : loginResponse.getErrors())
                errorMessage.append(error[1] + " ");
            
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
