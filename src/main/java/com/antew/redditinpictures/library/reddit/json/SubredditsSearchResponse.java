package com.antew.redditinpictures.library.reddit.json;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.antew.redditinpictures.library.json.JsonDeserializer;
import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.preferences.SharedPreferencesHelper;
import com.antew.redditinpictures.library.reddit.MySubreddits;
import com.antew.redditinpictures.library.reddit.SubredditChildren;
import com.antew.redditinpictures.library.reddit.SubredditData;
import com.antew.redditinpictures.library.reddit.SubredditsSearch;
import com.antew.redditinpictures.library.subredditmanager.SubredditManager;
import com.antew.redditinpictures.library.utils.Consts;
import com.antew.redditinpictures.library.utils.Ln;
import com.antew.redditinpictures.library.utils.StringUtil;
import com.antew.redditinpictures.sqlite.RedditContract;

import java.util.ArrayList;
import java.util.Collections;

public class SubredditsSearchResponse extends RedditResponseHandler {

    public static final String TAG = SubredditsSearchResponse.class.getSimpleName();
    private RedditResult       result;

    public SubredditsSearchResponse(RedditResult result) {
        this.result = result;
    }

    @Override
    public void processHttpResponse(Context context) {
        Log.i(TAG, "Subreddit Search complete! = " + result.getJson());
        SubredditsSearch subredditsSearch = JsonDeserializer.deserialize(result.getJson(), SubredditsSearch.class);

        if (subredditsSearch == null) {
            Ln.e("Something went wrong on Subreddit Search status code: %d json: %s", result.getHttpStatusCode(), result.getJson());
            return;
        }

        Intent intent = new Intent(Consts.BROADCAST_SUBREDDIT_SEARCH);
        intent.putStringArrayListExtra(Consts.EXTRA_SUBREDDIT_NAMES, (ArrayList<String>) subredditsSearch.getNames());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
    
}
