package com.antew.redditinpictures.library.reddit;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class RedditApiData implements Parcelable {
    //@formatter:off
    public static final Parcelable.Creator<RedditApiData> CREATOR
        = new Parcelable.Creator<RedditApiData>() {

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
    private List<Children> children;
    private String         after;
    private String         before;

    public RedditApiData(Parcel source) {
        children = new ArrayList<Children>();
        modhash = source.readString();
        source.readList(children, Children.class.getClassLoader());
        after = source.readString();
        before = source.readString();
    }

    public void addChildren(List<Children> children) {
        this.children.addAll(children);
    }

    //@formatter:off
    public String getModhash()           { return modhash; }

    public List<Children> getChildren()  { return children; }

    public String getAfter()             { return after; }

    public void setAfter(String after)   { this.after = after; }

    public String getBefore()            { return before;}

    public void setBefore(String before) { this.before = before; }
    //@formatter:on

    public List<PostData> getPosts() {
        List<PostData> posts = new ArrayList<PostData>(children.size());
        for (Children child : children) {
            posts.add(child.getData());
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
    }
    //@formatter:on
}