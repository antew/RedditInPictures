package com.antew.redditinpictures.library.reddit;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.antew.redditinpictures.library.enums.Vote;
import com.antew.redditinpictures.library.imgur.ImgurAlbumApi.Album;
import com.antew.redditinpictures.library.imgur.ImgurImageApi.ImgurImage;
import com.antew.redditinpictures.library.interfaces.ContentValuesOperation;
import com.antew.redditinpictures.library.utils.Consts;
import com.antew.redditinpictures.library.utils.Util;
import com.antew.redditinpictures.sqlite.RedditContract;

public class PostData implements Parcelable, ContentValuesOperation {

    private String     domain;
    private String     banned_by;
    private MediaEmbed media_embed;
    private String     subreddit;
    private String     selftext_html;
    private String     selftext;
    private Vote       likes;
    private boolean    saved;
    private String     id;
    private boolean    clicked;
    private String     title;
    private int        num_comments;
    private int        score;
    private String     approved_by;
    private boolean    over_18;
    private boolean    hidden;
    private String     thumbnail;
    private String     subreddit_id;
    private String     author_flair_css_class;
    private int        downs;
    private boolean    is_self;
    private String     permalink;
    private String     name;
    private long       created;
    private String     url;
    private String     author_flair_text;
    private String     author;
    private long       created_utc;
    private String     link_flair_text;
    private String     decoded_url;
    private ImgurImage image;
    private Album      album;

    /**
     * Leaving media commented out for now. On some subreddits it seems to return an object, but on
     * others it returns a string. Here is an example of it returning a string from the "funny"
     * subreddit.
     * 
     * { "data" : { "approved_by" : null, "author" : "AlejandroTheGreat", "author_flair_css_class" :
     * null, "author_flair_text" : null, "banned_by" : null, "clicked" : false, "created" :
     * 1246982492.0, "created_utc" : 1246982492.0, "domain" : "youtube.com", "downs" : 925, "edited"
     * : false, "hidden" : false, "id" : "8yzvh", "is_self" : false, "likes" : null,
     * "link_flair_css_class" : null, "link_flair_text" : null, "media" :
     * "<object width=\"480\" height=\"295\"><param name=\"movie\" value=\"http://www.youtube-nocookie.com/v/5YGc4zOqozo\"></param><param name=\"wmode\" value=\"transparent\"></param><embed src=\"http://www.youtube-nocookie.com/v/5YGc4zOqozo\" type=\"application/x-shockwave-flash\" wmode=\"transparent\" width=\"480\" height=\"295\"></embed></object>"
     * , "media_embed" : { }, "name" : "t3_8yzvh", "num_comments" : 850, "num_reports" : null,
     * "over_18" : false, "permalink" :
     * "/r/funny/comments/8yzvh/musician_witnesses_united_airlines_baggage/" , "saved" : false,
     * "score" : 4137, "selftext" : "", "selftext_html" : null, "subreddit" : "funny",
     * "subreddit_id" : "t5_2qh33", "thumbnail" : "", "title" :
     * "Musician witnesses United Airlines baggage handlers throwing his guitar. Guitar is wrecked. United won't help. Singer writes a song and makes a video \"United Breaks Guitars.\""
     * , "ups" : 5062, "url" : "http://www.youtube.com/watch?v=5YGc4zOqozo" }, "kind" : "t3" }
     */
    // Media media;
    int        num_reports;
    int        ups;

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
    public void setScore(int score)           { this.score = score; } 
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

    public String getFullPermalink(boolean useMobileInterface) {
        String url = Consts.REDDIT_BASE_URL + permalink;
        if (useMobileInterface)
            url += Consts.COMPACT_URL;

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

    public PostData() {
        super();
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
        = new Parcelable.Creator<PostData>() {

            @Override
            public PostData createFromParcel(Parcel source) {
                return new PostData(source);
            }

            @Override
            public PostData[] newArray(int size) {
                return new PostData[size];
            }
            
        
    };

    public void setDomain(String domain)                                 { this.domain = domain; }
    public void setBanned_by(String banned_by)                           { this.banned_by = banned_by; }
    public void setMedia_embed(MediaEmbed media_embed)                   { this.media_embed = media_embed; }
    public void setSubreddit(String subreddit)                           { this.subreddit = subreddit; }
    public void setSelftext_html(String selftext_html)                   { this.selftext_html = selftext_html; }
    public void setSelftext(String selftext)                             { this.selftext = selftext; }
    public void setLikes(Vote likes)                                     { this.likes = likes; }
    public void setSaved(boolean saved)                                  { this.saved = saved; }
    public void setId(String id)                                         { this.id = id; }
    public void setClicked(boolean clicked)                              { this.clicked = clicked; }
    public void setTitle(String title)                                   { this.title = title; }
    public void setNum_comments(int num_comments)                        { this.num_comments = num_comments; }
    public void setApproved_by(String approved_by)                       { this.approved_by = approved_by; }
    public void setOver_18(boolean over_18)                              { this.over_18 = over_18; }
    public void setHidden(boolean hidden)                                { this.hidden = hidden; }
    public void setThumbnail(String thumbnail)                           { this.thumbnail = thumbnail; }
    public void setSubreddit_id(String subreddit_id)                     { this.subreddit_id = subreddit_id; }
    public void setAuthor_flair_css_class(String author_flair_css_class) { this.author_flair_css_class = author_flair_css_class; }
    public void setDowns(int downs)                                      { this.downs = downs; }
    public void setIs_self(boolean is_self)                              { this.is_self = is_self; }
    public void setPermalink(String permalink)                           { this.permalink = permalink; }
    public void setName(String name)                                     { this.name = name; }
    public void setCreated(long created)                                 { this.created = created; }
    public void setUrl(String url)                                       { this.url = url; }
    public void setAuthor_flair_text(String author_flair_text)           { this.author_flair_text = author_flair_text; }
    public void setAuthor(String author)                                 { this.author = author; }
    public void setCreated_utc(long created_utc)                         { this.created_utc = created_utc; }
    public void setLink_flair_text(String link_flair_text)               { this.link_flair_text = link_flair_text; }
    public void setDecoded_url(String decoded_url)                       { this.decoded_url = decoded_url; }
    public void setImage(ImgurImage image)                               { this.image = image; }
    public void setNum_reports(int num_reports)                          { this.num_reports = num_reports; }
    public void setUps(int ups)                                          { this.ups = ups; }
    //@formatter:on

    @Override
    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(RedditContract.PostColumns.DOMAIN, domain);
        values.put(RedditContract.PostColumns.BANNED_BY, banned_by);
        values.put(RedditContract.PostColumns.SUBREDDIT, subreddit);
        values.put(RedditContract.PostColumns.SELFTEXT_HTML, selftext_html);
        values.put(RedditContract.PostColumns.SELFTEXT, selftext);
        values.put(RedditContract.PostColumns.VOTE, likes.name());
        values.put(RedditContract.PostColumns.SAVED, saved);
        values.put(RedditContract.PostColumns.POST_ID, id);
        values.put(RedditContract.PostColumns.CLICKED, clicked);
        values.put(RedditContract.PostColumns.TITLE, title);
        values.put(RedditContract.PostColumns.COMMENTS, num_comments);
        values.put(RedditContract.PostColumns.SCORE, score);
        values.put(RedditContract.PostColumns.APPROVED_BY, approved_by);
        values.put(RedditContract.PostColumns.OVER_18, over_18);
        values.put(RedditContract.PostColumns.HIDDEN, hidden);
        values.put(RedditContract.PostColumns.THUMBNAIL, thumbnail);
        values.put(RedditContract.PostColumns.SUBREDDIT_ID, subreddit_id);
        values.put(RedditContract.PostColumns.AUTHOR_FLAIR_CSS_CLASS, author_flair_css_class);
        values.put(RedditContract.PostColumns.DOWNS, downs);
        values.put(RedditContract.PostColumns.IS_SELF, is_self);
        values.put(RedditContract.PostColumns.PERMALINK, permalink);
        values.put(RedditContract.PostColumns.NAME, name);
        values.put(RedditContract.PostColumns.CREATED, created);
        values.put(RedditContract.PostColumns.URL, url);
        values.put(RedditContract.PostColumns.AUTHOR_FLAIR_TEXT, author_flair_text);
        values.put(RedditContract.PostColumns.AUTHOR, author);
        values.put(RedditContract.PostColumns.CREATED_UTC, created_utc);
        values.put(RedditContract.PostColumns.LINK_FLAIR_TEXT, link_flair_text);
        values.put(RedditContract.PostColumns.LOADED_AT, System.currentTimeMillis());

        return values;
    }

    public PostData(Cursor cursor) {
        //@formatter:off
        domain                = cursor.getString(cursor.getColumnIndex(RedditContract.Posts.DOMAIN));
        banned_by             = cursor.getString(cursor.getColumnIndex(RedditContract.Posts.BANNED_BY));
        subreddit             = cursor.getString(cursor.getColumnIndex(RedditContract.Posts.SUBREDDIT));
        selftext_html         = cursor.getString(cursor.getColumnIndex(RedditContract.Posts.SELFTEXT_HTML));
        selftext              = cursor.getString(cursor.getColumnIndex(RedditContract.Posts.SELFTEXT));
        likes    = Vote.valueOf(cursor.getString(cursor.getColumnIndex(RedditContract.Posts.VOTE)));
        saved                 = cursor.getInt   (cursor.getColumnIndex(RedditContract.Posts.SAVED)) == 1;
        id                    = cursor.getString(cursor.getColumnIndex(RedditContract.Posts.POST_ID));
        clicked               = cursor.getInt   (cursor.getColumnIndex(RedditContract.Posts.CLICKED)) == 1;
        title                 = cursor.getString(cursor.getColumnIndex(RedditContract.Posts.TITLE));
        num_comments          = cursor.getInt   (cursor.getColumnIndex(RedditContract.Posts.COMMENTS));
        score                 = cursor.getInt   (cursor.getColumnIndex(RedditContract.Posts.SCORE));
        approved_by           = cursor.getString(cursor.getColumnIndex(RedditContract.Posts.APPROVED_BY));
        over_18               = cursor.getInt   (cursor.getColumnIndex(RedditContract.Posts.OVER_18)) == 1;
        hidden                = cursor.getInt   (cursor.getColumnIndex(RedditContract.Posts.HIDDEN)) == 1;
        thumbnail             = cursor.getString(cursor.getColumnIndex(RedditContract.Posts.THUMBNAIL));
        subreddit_id          = cursor.getString(cursor.getColumnIndex(RedditContract.Posts.SUBREDDIT_ID));
        author_flair_css_class= cursor.getString(cursor.getColumnIndex(RedditContract.Posts.AUTHOR_FLAIR_CSS_CLASS));
        downs                 = cursor.getInt   (cursor.getColumnIndex(RedditContract.Posts.DOWNS));
        is_self               = cursor.getInt   (cursor.getColumnIndex(RedditContract.Posts.IS_SELF)) == 1;
        permalink             = cursor.getString(cursor.getColumnIndex(RedditContract.Posts.PERMALINK));
        name                  = cursor.getString(cursor.getColumnIndex(RedditContract.Posts.NAME));
        created               = cursor.getLong  (cursor.getColumnIndex(RedditContract.Posts.CREATED));
        url                   = cursor.getString(cursor.getColumnIndex(RedditContract.Posts.URL));
        author_flair_text     = cursor.getString(cursor.getColumnIndex(RedditContract.Posts.AUTHOR_FLAIR_TEXT));
        author                = cursor.getString(cursor.getColumnIndex(RedditContract.Posts.AUTHOR));
        created_utc           = cursor.getLong  (cursor.getColumnIndex(RedditContract.Posts.CREATED_UTC));
        link_flair_text       = cursor.getString(cursor.getColumnIndex(RedditContract.Posts.LINK_FLAIR_TEXT));
      //decodedUrl            = cursor.getString(cursor.getColumnIndex(RedditContract.Posts.DECODED_URL));
        //@formatter:off
    }
}
