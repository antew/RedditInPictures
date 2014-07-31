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

import android.os.Parcel;
import android.os.Parcelable;
import com.antew.redditinpictures.library.model.Age;
import com.antew.redditinpictures.library.model.Category;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RedditApiData implements Parcelable {
    public static final Parcelable.Creator<RedditApiData> CREATOR = new Parcelable.Creator<RedditApiData>() {

        @Override
        public RedditApiData createFromParcel(Parcel source) {
            return new RedditApiData(source);
        }

        @Override
        public RedditApiData[] newArray(int size) {
            return new RedditApiData[size];
        }
    };
    private String         modhash;
    private List<Child>    children;
    private String         after;
    private String         before;
    private Date           retrievedDate;
    private String         subreddit;
    private Category       category;
    private Age            age;

    public RedditApiData(Parcel source) {
        children = new ArrayList<Child>();
        modhash = source.readString();
        source.readList(children, Child.class.getClassLoader());
        after = source.readString();
        before = source.readString();
        retrievedDate = new Date(source.readLong());
        subreddit = source.readString();
        category = Category.fromString(source.readString());
        age = Age.fromString(source.readString());
    }

    public void addChildren(List<Child> children) {
        this.children.addAll(children);
    }

    public String getModhash() { return modhash; }

    public List<Child> getChildren() { return children; }

    public String getAfter() { return after; }

    public void setAfter(String after) { this.after = after; }

    public String getBefore() { return before;}

    public void setBefore(String before) { this.before = before; }

    public Date getRetrievedDate() {
        return retrievedDate;
    }

    public void setRetrievedDate(Date retrievedDate) {
        this.retrievedDate = retrievedDate;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Age getAge() {
        return age;
    }

    public void setAge(Age age) {
        this.age = age;
    }

    public List<PostData> getPosts() {
        List<PostData> posts = new ArrayList<PostData>(children.size());
        for (Child child : children) {
            posts.add((PostData) child.getData());
        }

        return posts;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(modhash);
        dest.writeList(children);
        dest.writeString(after);
        dest.writeString(before);
        dest.writeLong(retrievedDate.getTime());
        dest.writeString(subreddit);
        dest.writeString(category.getName());
        dest.writeString(age.getAge());
    }
}