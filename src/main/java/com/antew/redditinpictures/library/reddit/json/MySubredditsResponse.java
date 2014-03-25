package com.antew.redditinpictures.library.reddit.json;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import com.antew.redditinpictures.library.json.JsonDeserializer;
import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.reddit.MySubreddits;
import com.antew.redditinpictures.library.reddit.SubredditData;
import com.antew.redditinpictures.library.utils.Constants;
import com.antew.redditinpictures.sqlite.RedditContract;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MySubredditsResponse extends RedditResponseHandler {

    public static final String TAG = MySubredditsResponse.class.getSimpleName();
    private RedditResult       result;

    public MySubredditsResponse(RedditResult result) {
        this.result = result;
    }

    @Override
    public void processHttpResponse(Context context) {
        ContentResolver resolver = context.getContentResolver();

        // Don't wipe out the default subreddits
        int userRowsDeleted = resolver.delete(RedditContract.Subreddits.CONTENT_URI, "isDefaultSubreddit = ?", new String[] { "0" });
        
        Log.i(TAG, "MySubreddits complete! = " + result.getJson());
        MySubreddits mySubreddits = JsonDeserializer.deserialize(result.getJson(), MySubreddits.class);

        if (mySubreddits == null) {
            Log.e("MySubreddits",
                  "Something went wrong on mySubreddits! status = " + result.getHttpStatusCode() +
                  ", json = " + result.getJson() == null ? "null" : result.getJson());
            return;
        }

        List<ContentValues> operations = new ArrayList<ContentValues>(100);
        // Add in the default subreddits ('Frontpage' and 'All')
        DefaultSubreddit[] defaultSubreddits = DefaultSubreddit.values();
        for (DefaultSubreddit subreddit : defaultSubreddits) {
            operations.add(mySubreddits.getContentValues(new SubredditData(subreddit.getDisplayName(), subreddit.getPriority())));
        }

        // Get the subreddits in an array
        operations.addAll(Arrays.asList(mySubreddits.getContentValuesArray()));

        int rowsInserted = resolver.bulkInsert(
                RedditContract.Subreddits.CONTENT_URI,
                operations.toArray(new ContentValues[operations.size()])
        );

        Log.i(TAG, "Inserted " + rowsInserted + " rows");
        
        /*
        ArrayList<String> subReddits = new ArrayList<String>();

        for (SubredditChildren c : mySubreddits.getData().getChildren()) {
            SubredditData data = c.getData();
            subReddits.add(data.getDisplay_name());
            Log.i("Subscribed Subreddits", data.getDisplay_name());
        }

        Collections.sort(subReddits, StringUtil.getCaseInsensitiveComparator());
        SharedPreferencesHelper.saveArray(subReddits, SubredditManager.PREFS_NAME, SubredditManager.ARRAY_NAME, context);

        Intent intent = new Intent(Constants.BROADCAST_MY_SUBREDDITS);
        intent.putStringArrayListExtra(Constants.EXTRA_MY_SUBREDDITS, subReddits);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        */
    }

    public enum DefaultSubreddit {
        FRONTPAGE(Constants.REDDIT_FRONTPAGE_DISPLAY_NAME, 99999),
        ALL(Constants.REDDIT_ALL_DISPLAY_NAME, 99998);

        private final String displayName;
        private final int    priority;

        DefaultSubreddit(String displayName, int priority) {
            this.displayName = displayName;
            this.priority = priority;
        }

        public String getDisplayName() {
            return displayName;
        }
        public int getPriority() { return priority; }

    }
    
}
