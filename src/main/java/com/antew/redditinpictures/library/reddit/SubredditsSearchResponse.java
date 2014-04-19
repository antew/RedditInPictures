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
package com.antew.redditinpictures.library.reddit;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.json.JsonDeserializer;
import com.antew.redditinpictures.library.model.reddit.SubredditsSearch;
import com.antew.redditinpictures.library.util.Ln;
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

        Intent intent = new Intent(Constants.Broadcast.BROADCAST_SUBREDDIT_SEARCH);
        intent.putStringArrayListExtra(Constants.Extra.EXTRA_SUBREDDIT_NAMES, (ArrayList<String>) subredditsSearch.getNames());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
