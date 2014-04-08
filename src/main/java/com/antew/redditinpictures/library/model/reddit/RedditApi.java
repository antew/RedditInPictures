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
package com.antew.redditinpictures.library.model.reddit;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import com.antew.redditinpictures.library.interfaces.ContentValuesOperation;
import com.antew.redditinpictures.library.interfaces.RedditPostFilter;
import com.antew.redditinpictures.library.utils.ImageUtil;
import com.antew.redditinpictures.sqlite.RedditContract;
import java.util.ArrayList;
import java.util.List;

public class RedditApi implements Parcelable, ContentValuesOperation, RedditPostFilter<PostData> {
    public static final Parcelable.Creator<RedditApi> CREATOR = new Parcelable.Creator<RedditApi>() {

        @Override
        public RedditApi createFromParcel(Parcel source) {
            return new RedditApi(source);
        }

        @Override
        public RedditApi[] newArray(int size) {
            return new RedditApi[size];
        }
    };
    private String        kind;
    private RedditApiData data;

    public RedditApi(Parcel source) {
        kind = source.readString();
        data = source.readParcelable(RedditApiData.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(kind);
        dest.writeParcelable(data, flags);
    }

    public String getKind() {
        return kind;
    }

    public RedditApiData getData() {
        return data;
    }

    /**
     * Convenience method to return an Array of {@link ContentValues} based on the {@link PostData}
     * from this {@link RedditApi} object.
     *
     * @param includeNsfwImages
     *     Whether to include Not-Safe-For-Work images in the list
     *
     * @return Array of {@link ContentValues} for use with
     * {@link ContentProvider#bulkInsert(Uri, ContentValues[])}
     */
    public ContentValues[] getPostDataContentValues(boolean includeNsfwImages) {
        List<PostData> postData = filterPosts(includeNsfwImages);
        ContentValues[] operations = new ContentValues[postData.size()];

        for (int i = 0; i < postData.size(); i++) {
            operations[i] = postData.get(i).getContentValues();
        }

        return operations;
    }

    /**
     * This method removes unusable posts from the Reddit API data.
     *
     * @return The filtered list of posts after removing non-images, unsupported images, and NSFW
     * entries. Whether NSFW images are retained can be controlled via settings.
     */
    @Override
    public List<PostData> filterPosts(boolean includeNsfwImages) {
        List<PostData> entries = new ArrayList<PostData>();

        for (Children c : data.getChildren()) {
            PostData pd = c.getData();
            if (ImageUtil.isSupportedUrl(pd.getUrl())) {
                if (!pd.isOver_18() || includeNsfwImages) {
                    entries.add(pd);
                }
            }
        }

        return entries;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(RedditContract.RedditData.AFTER, data.getAfter());
        values.put(RedditContract.RedditData.BEFORE, data.getBefore());
        values.put(RedditContract.RedditData.MODHASH, data.getModhash());
        values.put(RedditContract.RedditData.RETRIEVED_DATE, data.getRetrievedDate().getTime());
        values.put(RedditContract.RedditData.SUBREDDIT, data.getSubreddit());
        values.put(RedditContract.RedditData.CATEGORY, data.getCategory().getName());
        values.put(RedditContract.RedditData.AGE, data.getAge().getAge());

        return values;
    }
}
