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
package com.antew.redditinpictures.library.reddit;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.antew.redditinpictures.library.gson.VoteAdapter;
import com.antew.redditinpictures.library.imgur.ImgurAlbumApi.Album;
import com.antew.redditinpictures.library.imgur.ImgurImageApi.ImgurImage;
import com.antew.redditinpictures.library.reddit.RedditApiManager.Vote;
import com.antew.redditinpictures.library.utils.Util;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class RedditApi implements Parcelable  {
    String kind;
    Data   data;

    public RedditApi(Parcel source) {
        kind = source.readString();
        data = source.readParcelable(Data.class.getClassLoader());
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
    
    //@formatter:off
    public static final Parcelable.Creator<RedditApi> CREATOR
        = new Parcelable.Creator<RedditApi>() {

            @Override
            public RedditApi createFromParcel(Parcel source) {
                return new RedditApi(source);
            }

            @Override
            public RedditApi[] newArray(int size) {
                return new RedditApi[size];
            }
            
        
    };
    //@formatter:on
    
    public static Gson getGson () {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Vote.class, new VoteAdapter());
        builder.serializeNulls();
        return builder.create();
    }

    public String getKind() {
        return kind;
    }

    public Data getData() {
        return data;
    }

    public static class Data implements Parcelable {
        String         modhash;
        List<Children> children;
        String         after;
        String         before;

        public Data(Parcel source) {
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
        public void setAfter(String after)   { this.after = after; }
        public void setBefore(String before) { this.before = before; }
        public String getAfter()             { return after; }
        public String getBefore()            { return before;}
        //@formatter:on

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
        
        //@formatter:off
        public static final Parcelable.Creator<Data> CREATOR
            = new Parcelable.Creator<Data>() {

                @Override
                public Data createFromParcel(Parcel source) {
                    return new Data(source);
                }

                @Override
                public Data[] newArray(int size) {
                    return new Data[size];
                }
                
            
        };
        //@formatter:on

    }

    public static class Children implements Parcelable {
        String   kind;
        PostData data;

        public Children(Parcel source) {
            kind = source.readString();
            data = source.readParcelable(PostData.class.getClassLoader());
        }

        public String getKind() {
            return kind;
        }

        public PostData getData() {
            return data;
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

        //@formatter:off
        public static final Parcelable.Creator<Children> CREATOR
            = new Parcelable.Creator<Children>() {

                @Override
                public Children createFromParcel(Parcel source) {
                    return new Children(source);
                }

                @Override
                public Children[] newArray(int size) {
                    return new Children[size];
                }
                
            
        };
        //@formatter:on
    }

    public static class PostData implements Parcelable {
        String     domain;
        String     banned_by;
        MediaEmbed media_embed;
        String     subreddit;
        String     selftext_html;
        String     selftext;
        Vote       likes;
        boolean    saved;
        String     id;
        boolean    clicked;
        String     title;
        int        num_comments;
        int        score;
        String     approved_by;
        boolean    over_18;
        boolean    hidden;
        String     thumbnail;
        String     subreddit_id;
        String     author_flair_css_class;
        int        downs;
        boolean    is_self;
        String     permalink;
        String     name;
        long       created;
        String     url;
        String     author_flair_text;
        String     author;
        long       created_utc;
        String     link_flair_text;
        String     decoded_url;
        ImgurImage image;
        Album      album;

        /**
         * Leaving media commented out for now. On some subreddits it seems to
         * return an object, but on others it returns a string. Here is an
         * example of it returning a string from the "funny" subreddit.
         * 
         * { "data" : { "approved_by" : null, "author" : "AlejandroTheGreat",
         * "author_flair_css_class" : null, "author_flair_text" : null,
         * "banned_by" : null, "clicked" : false, "created" : 1246982492.0,
         * "created_utc" : 1246982492.0, "domain" : "youtube.com", "downs" :
         * 925, "edited" : false, "hidden" : false, "id" : "8yzvh", "is_self" :
         * false, "likes" : null, "link_flair_css_class" : null,
         * "link_flair_text" : null, "media" :
         * "<object width=\"480\" height=\"295\"><param name=\"movie\" value=\"http://www.youtube-nocookie.com/v/5YGc4zOqozo\"></param><param name=\"wmode\" value=\"transparent\"></param><embed src=\"http://www.youtube-nocookie.com/v/5YGc4zOqozo\" type=\"application/x-shockwave-flash\" wmode=\"transparent\" width=\"480\" height=\"295\"></embed></object>"
         * , "media_embed" : { }, "name" : "t3_8yzvh", "num_comments" : 850,
         * "num_reports" : null, "over_18" : false, "permalink" :
         * "/r/funny/comments/8yzvh/musician_witnesses_united_airlines_baggage/"
         * , "saved" : false, "score" : 4137, "selftext" : "", "selftext_html" :
         * null, "subreddit" : "funny", "subreddit_id" : "t5_2qh33", "thumbnail"
         * : "", "title" :
         * "Musician witnesses United Airlines baggage handlers throwing his guitar. Guitar is wrecked. United won't help. Singer writes a song and makes a video \"United Breaks Guitars.\""
         * , "ups" : 5062, "url" : "http://www.youtube.com/watch?v=5YGc4zOqozo"
         * }, "kind" : "t3" }
         */
        // Media media;
        int num_reports;
        int ups;

        //@formatter:off
        public String getDomain()                 { return domain; }
        public String getBanned_by()              { return banned_by; }
        public MediaEmbed getMedia_embed()        { return media_embed; }
        public String getSubreddit()              { return subreddit; }
        public String getSelftext_html()          { return selftext_html; }
        public String getSelftext()               { return selftext; }
        public Vote getLikes()                    { return likes; }
        public boolean isSaved()                  { return saved; }
        public String getId()                     { return id; }
        public boolean isClicked()                { return clicked; }
        public String getTitle()                  { return title; }
        public int getNum_comments()              { return num_comments; }
        public int getScore()                     { return score; }
        public void setScore(int score)            { this.score = score; } 
        public String getApproved_by()            { return approved_by; }
        public boolean isOver_18()                { return over_18; }
        public boolean isHidden()                 { return hidden; }
        public String getThumbnail()              { return thumbnail; }
        public String getSubreddit_id()           { return subreddit_id; }
        public String getAuthor_flair_css_class() { return author_flair_css_class; }
        public int getDowns()                     { return downs; }
        public boolean isIs_self()                { return is_self; }
        public String getPermalink()              { return permalink; }
        public String getName()                   { return name; }
        public long getCreated()                  { return created; }
        public String getUrl()                    { return url; }
        public String getAuthor_flair_text()      { return author_flair_text; }
        public String getAuthor()                 { return author; }
        public long getCreated_utc()              { return created_utc; }
        public String getLink_flair_text()        { return link_flair_text; }
      //public Media getMedia()                   { return media; }
        public int getNum_reports()               { return num_reports; }
        public int getUps()                       { return ups; } 
        public Vote getVote()                     { return likes; }
        public void setVote(Vote vote)            { this.likes = vote; }
        public void setDecodedUrl(String url)     { this.decoded_url = url; }
        public String getDecodedUrl()             { return decoded_url; }
        public ImgurImage getImgurImage()         { return image; }
        public Album      getAlbum()              { return album; }
        //@formatter:on
        
        public String getFullPermalink(boolean useMobileInterface ) {
            String url = RedditApiManager.REDDIT_BASE_URL + permalink;
            if (useMobileInterface)
                url += RedditApiManager.COMPACT_URL;
            
            return url;
        }
        public void setImgurImage(ImgurImage image) {
            this.image = image;
        }
        
        public void setAlbum(Album album) { 
            this.album = album;
        }
        
        public PostData(Parcel source) {
            domain = source.readString();
            banned_by = source.readString();
            media_embed = source.readParcelable(MediaEmbed.class.getClassLoader());
            subreddit = source.readString();
            selftext_html = source.readString();
            selftext = source.readString();
            
            try {
                likes = Vote.valueOf(source.readString());
            } catch (IllegalArgumentException e) {
                likes = Vote.NEUTRAL;
            }
            
            saved = source.readByte() == 1;
            id = source.readString();
            clicked = source.readByte() == 1;
            title = source.readString();
            num_comments = source.readInt();
            score = source.readInt();
            approved_by = source.readString();
            over_18 = source.readByte() == 1;
            hidden = source.readByte() == 1;
            thumbnail = source.readString();
            subreddit_id = source.readString();
            author_flair_css_class = source.readString();
            downs = source.readInt();
            is_self = source.readByte() == 1;
            permalink = source.readString();
            name = source.readString();
            created = source.readLong();
            url = source.readString();
            author_flair_text = source.readString();
            author = source.readString();
            created_utc = source.readLong();
            link_flair_text = source.readString();
            decoded_url = source.readString();
            image = source.readParcelable(ImgurImage.class.getClassLoader());
            album = source.readParcelable(Album.class.getClassLoader());
        }

        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(domain);
            dest.writeString(banned_by);
            dest.writeParcelable(media_embed, flags);
            dest.writeString(subreddit);
            dest.writeString(selftext_html);
            dest.writeString(selftext);
            dest.writeString(likes.name());
            dest.writeByte(Util.parcelBoolean(saved));
            dest.writeString(id);
            dest.writeByte(Util.parcelBoolean(clicked));
            dest.writeString(title);
            dest.writeInt(num_comments);
            dest.writeInt(score);
            dest.writeString(approved_by);
            dest.writeByte(Util.parcelBoolean(over_18));
            dest.writeByte(Util.parcelBoolean(hidden));
            dest.writeString(thumbnail);
            dest.writeString(subreddit_id);
            dest.writeString(author_flair_css_class);
            dest.writeInt(downs);
            dest.writeByte(Util.parcelBoolean(is_self));
            dest.writeString(permalink);
            dest.writeString(name);
            dest.writeLong(created);
            dest.writeString(url);
            dest.writeString(author_flair_text);
            dest.writeString(author);
            dest.writeLong(created_utc);
            dest.writeString(link_flair_text);
            dest.writeString(decoded_url);
            dest.writeParcelable(image, flags);
            dest.writeParcelable(album, flags);
        }
        
        //@formatter:off
        public static final Parcelable.Creator<PostData> CREATOR
            = new Parcelable.Creator<RedditApi.PostData>() {

                @Override
                public PostData createFromParcel(Parcel source) {
                    return new PostData(source);
                }

                @Override
                public PostData[] newArray(int size) {
                    return new PostData[size];
                }
                
            
        };
        //@formatter:on
    }

    public static class Media implements Parcelable {
        String type;
        Oembed oembed;
        
        public Media(Parcel source) {
            type = source.readString();
            oembed = source.readParcelable(Oembed.class.getClassLoader());
        }
        
        @Override
        public int describeContents() {
            return 0;
        }
        
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(type);
            dest.writeParcelable(oembed, flags);
        }
        
        //@formatter:off
        public static final Parcelable.Creator<Media> CREATOR
            = new Parcelable.Creator<Media>() {

                @Override
                public Media createFromParcel(Parcel source) {
                    return new Media(source);
                }

                @Override
                public Media[] newArray(int size) {
                    return new Media[size];
                }
                
            
        };
        //@formatter:on
    }

    public static class Oembed implements Parcelable {
        String provider_url;
        String description;
        String title;
        String url;
        String author_name;
        int    height;
        int    width;
        String html;
        int    thumbnail_width;
        String version;
        String provider_name;
        String thumbnail_url;
        String type;
        int    thumbnail_height;
        String author_url;
        
        
        public Oembed(Parcel source) {
            provider_url = source.readString();
            description = source.readString();
            title = source.readString();
            url = source.readString();
            author_name = source.readString();
            height = source.readInt();
            width = source.readInt();
            html = source.readString();
            thumbnail_width = source.readInt();
            version = source.readString();
            provider_name = source.readString();
            thumbnail_url = source.readString();
            type = source.readString();
            thumbnail_height = source.readInt();
            author_url = source.readString();
        }
        
        @Override
        public int describeContents() {
            return 0;
        }
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(provider_url);
            dest.writeString(description);
            dest.writeString(title);
            dest.writeString(url);
            dest.writeString(author_name);
            dest.writeInt(height);
            dest.writeInt(width);
            dest.writeString(html);
            dest.writeInt(thumbnail_width);
            dest.writeString(version);
            dest.writeString(provider_name);
            dest.writeString(thumbnail_url);
            dest.writeString(type);
            dest.writeInt(thumbnail_height);
            dest.writeString(url);
        }
        
      //@formatter:off
        public static final Parcelable.Creator<Oembed> CREATOR
            = new Parcelable.Creator<Oembed>() {

                @Override
                public Oembed createFromParcel(Parcel source) {
                    return new Oembed(source);
                }

                @Override
                public Oembed[] newArray(int size) {
                    return new Oembed[size];
                }
                
            
        };
        //@formatter:on
    }

    public static class MediaEmbed implements Parcelable {
        String  content;
        int     width;
        int     height;
        boolean scrolling;

        public MediaEmbed(Parcel in) {
            content = in.readString();
            width = in.readInt();
            height = in.readInt();
            scrolling = in.readByte() == 1;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(content);
            dest.writeInt(width);
            dest.writeInt(height);
            dest.writeByte(Util.parcelBoolean(scrolling));
        }

        //@formatter:off
        public static final Parcelable.Creator<MediaEmbed> CREATOR 
            = new Parcelable.Creator<MediaEmbed>() {
                     public MediaEmbed createFromParcel(Parcel in) {
                         return new MediaEmbed(in);
                     }

                     public MediaEmbed[] newArray(int size) {
                         return new MediaEmbed[size];
                     }
                 };
        //@formatter:on
    }
}
