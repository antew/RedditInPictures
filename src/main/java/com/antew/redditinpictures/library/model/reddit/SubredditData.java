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

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.antew.redditinpictures.library.util.AndroidUtil;
import com.antew.redditinpictures.library.database.RedditContract;

public class SubredditData implements Parcelable {
    private String  display_name;
    private String  description;
    private String  name;
    private long    created;
    private String  url;
    private String  title;
    private long    created_utc;
    private String  public_description;
    private int[]   header_size;
    private int     accounts_active;
    private boolean over18;
    private int     subscribers;
    private String  header_title;
    private String  id;
    private String  header_img;
    private int     priority;
    private boolean user_is_subscriber;

    private SubredditData() {

    }

    public SubredditData(String display_name) {
        this(display_name, 0);
    }

    public SubredditData(String display_name, int priority) {
        this.display_name = display_name;
        this.priority = priority;
    }

    public static SubredditData fromProjection(Cursor cursor) {
        SubredditData subredditData = new SubredditData();
        subredditData.display_name = cursor.getString(cursor.getColumnIndex(RedditContract.Subreddits.DISPLAY_NAME));
        subredditData.name = cursor.getString(cursor.getColumnIndex(RedditContract.Subreddits.NAME));
        subredditData.user_is_subscriber = cursor.getInt(cursor.getColumnIndex(RedditContract.Subreddits.USER_IS_SUBSCRIBER)) == 1;
        subredditData.over18 = cursor.getInt(cursor.getColumnIndex(RedditContract.Subreddits.OVER_18)) == 1;
        subredditData.subscribers = cursor.getInt(cursor.getColumnIndex(RedditContract.Subreddits.SUBSCRIBERS));
        subredditData.description = cursor.getString(cursor.getColumnIndex(RedditContract.Subreddits.DESCRIPTION));
        subredditData.public_description = cursor.getString(cursor.getColumnIndex(RedditContract.Subreddits.PUBLIC_DESCRIPTION));
        subredditData.header_img = cursor.getString(cursor.getColumnIndex(RedditContract.Subreddits.HEADER_IMAGE));
        return subredditData;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public long getCreated() {
        return created;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public long getCreated_utc() {
        return created_utc;
    }

    public String getPublic_description() {
        return public_description;
    }

    public int[] getHeader_size() {
        return header_size;
    }

    public boolean isOver18() {
        return over18;
    }

    public int getSubscribers() {
        return subscribers;
    }

    public String getHeader_title() {
        return header_title;
    }

    public String getId() {
        return id;
    }

    public String getHeader_img() {
        return header_img;
    }

    public int getAccountsActive() {
        return accounts_active;
    }

    public int getPriority() { return priority; }

    public boolean getUserIsSubscriber() { return user_is_subscriber; }

    public SubredditData(Parcel source) {
        display_name = source.readString();
        description = source.readString();
        name = source.readString();
        created = source.readLong();
        url = source.readString();
        title = source.readString();
        created_utc = source.readLong();
        public_description = source.readString();
        source.readIntArray(header_size);
        accounts_active = source.readInt();
        over18 = source.readByte() == 1;
        subscribers = source.readInt();
        header_title = source.readString();
        id = source.readString();
        header_img = source.readString();
        priority = source.readInt();
        user_is_subscriber = source.readByte() == 1;
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable's
     * marshalled representation.
     *
     * @return a bitmask indicating the set of special object types marshalled
     * by the Parcelable.
     */
    @Override public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest
     *     The Parcel in which the object should be written.
     * @param flags
     *     Additional flags about how the object should be written.
     *     May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(display_name);
        dest.writeString(description);
        dest.writeString(name);
        dest.writeLong(created);
        dest.writeString(url);
        dest.writeString(title);
        dest.writeLong(created_utc);
        dest.writeString(public_description);
        dest.writeIntArray(header_size);
        dest.writeInt(accounts_active);
        dest.writeByte(AndroidUtil.parcelBoolean(over18));
        dest.writeInt(subscribers);
        dest.writeString(header_title);
        dest.writeString(id);
        dest.writeString(header_img);
        dest.writeInt(priority);
        dest.writeByte(AndroidUtil.parcelBoolean(user_is_subscriber));
    }

    public static final Parcelable.Creator<SubredditData> CREATOR = new Parcelable.Creator<SubredditData>() {

        @Override
        public SubredditData createFromParcel(Parcel source) {
            return new SubredditData(source);
        }

        public SubredditData[] newArray(int size) {
            return new SubredditData[size];
        }

        ;
    };
}