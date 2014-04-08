package com.antew.redditinpictures.library.reddit;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.json.JsonDeserializer;
import com.antew.redditinpictures.library.model.reddit.RedditLoginResponse;
import com.antew.redditinpictures.library.util.BundleUtil;
import com.antew.redditinpictures.library.util.Ln;
import com.antew.redditinpictures.sqlite.RedditContract;

public class LoginResponse extends RedditResponseHandler {
    public static String TAG = LoginResponse.class.getSimpleName();
    private RedditResult result;

    public LoginResponse(RedditResult result) {
        this.result = result;
    }

    @Override
    public void processHttpResponse(Context context) {
        ContentResolver resolver = context.getContentResolver();

        // Delete old logins
        resolver.delete(RedditContract.Login.CONTENT_URI, null, null);

        RedditLoginResponse response = JsonDeserializer.deserialize(result.getJson(), RedditLoginResponse.class);

        if (response == null) {
            Ln.e("Error parsing Reddit login response");
            return;
        }

        // The username isn't sent back with the login response, so we have it passed
        // through from the login request
        String username = BundleUtil.getString(result.getExtraData(), RedditContract.Login.USERNAME, null);
        if (response.getLoginResponse() != null && response.getLoginResponse().getData() != null) {
            response.getLoginResponse().getData().setUsername(username);
        }

        ContentValues loginValues = response.getContentValues();
        Intent loginNotify = new Intent(Constants.Broadcast.BROADCAST_LOGIN_COMPLETE);
        loginNotify.putExtra(Constants.Extra.EXTRA_USERNAME, username);
        if (loginValues.getAsBoolean(RedditContract.Login.SUCCESS)) {
            loginNotify.putExtra(Constants.Extra.EXTRA_SUCCESS, true);
            resolver.insert(RedditContract.Login.CONTENT_URI, loginValues);
        } else {
            loginNotify.putExtra(Constants.Extra.EXTRA_SUCCESS, false);
            loginNotify.putExtra(Constants.Extra.EXTRA_ERROR_MESSAGE, loginValues.getAsString(RedditContract.Login.ERROR_MESSAGE));
        }

        LocalBroadcastManager.getInstance(context).sendBroadcast(loginNotify);
    }
}
