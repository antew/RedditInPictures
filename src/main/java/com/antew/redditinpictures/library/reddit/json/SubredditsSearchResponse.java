package com.antew.redditinpictures.library.reddit.json;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.antew.redditinpictures.library.json.JsonDeserializer;
import com.antew.redditinpictures.library.reddit.SubredditsSearch;
import com.antew.redditinpictures.library.utils.Constants;
import com.antew.redditinpictures.library.utils.Ln;
import java.util.ArrayList;

public class SubredditsSearchResponse extends RedditResponseHandler {

    public static final String TAG = SubredditsSearchResponse.class.getSimpleName();
    private RedditResult result;

    public SubredditsSearchResponse(RedditResult result) {
        this.result = result;
    }

    @Override
    public void processHttpResponse(Context context) {
        Ln.d("Subreddit Search complete! = %s", result.getJson());
        SubredditsSearch subredditsSearch = JsonDeserializer.deserialize(result.getJson(), SubredditsSearch.class);

        if (subredditsSearch == null) {
            Ln.e("Something went wrong on Subreddit Search status code: %d json: %s", result.getHttpStatusCode(), result.getJson());
            return;
        }

        Intent intent = new Intent(Constants.BROADCAST_SUBREDDIT_SEARCH);
        intent.putStringArrayListExtra(Constants.EXTRA_SUBREDDIT_NAMES, (ArrayList<String>) subredditsSearch.getNames());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
