package com.antew.redditinpictures.library.reddit.json;

import java.util.ArrayList;
import java.util.Collections;

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
import com.antew.redditinpictures.library.subredditmanager.SubredditManager;
import com.antew.redditinpictures.library.utils.Consts;
import com.antew.redditinpictures.library.utils.StringUtil;
import com.antew.redditinpictures.sqlite.RedditContract;

public class MySubredditsResponse extends RedditResponseHandler {

    public static final String TAG = MySubredditsResponse.class.getSimpleName();
    private RedditResult       result;

    public MySubredditsResponse(RedditResult result) {
        this.result = result;
    }

    @Override
    public void processHttpResponse(Context context) {
        ContentResolver resolver = context.getContentResolver();
        
        int userRowsDeleted = resolver.delete(RedditContract.Subreddits.CONTENT_URI, null, null);
        
        Log.i(TAG, "MySubreddits complete! = " + result.getJson());
        MySubreddits mySubreddits = JsonDeserializer.deserialize(result.getJson(), MySubreddits.class);

        if (mySubreddits == null) {
            Log.e("MySubreddits",
                  "Something went wrong on mySubreddits! status = " + result.getHttpStatusCode() +
                  ", json = " + result.getJson() == null ? "null" : result.getJson());
            return;
        }

        
        ContentValues[] operations = mySubreddits.getContentValuesArray();
        
        int rowsInserted = resolver.bulkInsert(RedditContract.Subreddits.CONTENT_URI, operations);
        Log.i(TAG, "Inserted " + rowsInserted + " rows");
        
        ArrayList<String> subReddits = new ArrayList<String>();

        for (SubredditChildren c : mySubreddits.getData().getChildren()) {
            SubredditData data = c.getData();
            subReddits.add(data.getDisplay_name());
            Log.i("Subscribed Subreddits", data.getDisplay_name());
        }

        Collections.sort(subReddits, StringUtil.getCaseInsensitiveComparator());
        SharedPreferencesHelper.saveArray(subReddits, SubredditManager.PREFS_NAME, SubredditManager.ARRAY_NAME, context);

        Intent intent = new Intent(Consts.BROADCAST_MY_SUBREDDITS);
        intent.putStringArrayListExtra(Consts.EXTRA_MY_SUBREDDITS, subReddits);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
    
}
