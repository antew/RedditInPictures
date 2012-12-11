package com.antew.redditinpictures.library.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.antew.redditinpictures.library.reddit.RedditApi.PostData;
import com.antew.redditinpictures.sqlite.RedditContract;
import com.antew.redditinpictures.sqlite.RedditContract.Posts;
import com.antew.redditinpictures.sqlite.RedditContract.RedditData;
import com.antew.redditinpictures.sqlite.RedditContract.RedditDataColumns;
import com.antew.redditinpictures.sqlite.RedditDatabase;
import com.antew.redditinpictures.sqlite.RedditDatabase.Tables;

public class RedditProvider extends ContentProvider {
    private RedditDatabase   mDatabase;

    private static final int REDDIT    = 100;
    private static final int REDDIT_ID = 101;

    private static final int POSTS     = 200;
    private static final int POSTS_ID  = 201;
    
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    @Override
    public boolean onCreate() {
        mDatabase = new RedditDatabase(getContext());
        return true;
    }
    
    private void deleteDatabase() {
        // TODO: wait for content provider operations to finish, then tear down
        mDatabase.close();
        Context context = getContext();
        RedditDatabase.deleteDatabase(context);
        mDatabase = new RedditDatabase(getContext());
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mDatabase.getReadableDatabase();
//        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        final int match = sUriMatcher.match(uri);
        
        switch (match) {
            case REDDIT:
//                builder.setTables(Tables.REDDIT_DATA);
                return db.query(Tables.REDDIT_DATA, projection, selection, selectionArgs, null, null, sortOrder);
            case REDDIT_ID:
                return db.query(Tables.REDDIT_DATA, projection, selection, selectionArgs, null, null, sortOrder);
            case POSTS:
                return db.query(Tables.POSTDATA, projection, selection, selectionArgs, null, null, sortOrder);
            case POSTS_ID:
                return db.query(Tables.POSTDATA, projection, selection, selectionArgs, null, null, sortOrder);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
                
        switch (match) {
            case REDDIT:
                return RedditData.CONTENT_TYPE;
            case REDDIT_ID:
                return RedditData.CONTENT_ITEM_TYPE;
            case POSTS:
                return Posts.CONTENT_TYPE;
            case POSTS_ID:
                return Posts.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDatabase.getReadableDatabase();
        final int match = sUriMatcher.match(uri);
        
        switch (match) {
            case REDDIT:
            case REDDIT_ID:
                db.insertOrThrow(Tables.REDDIT_DATA, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return RedditData.buildPostDataUri(values.getAsString(RedditDataColumns.MODHASH));

            case POSTS:
            case POSTS_ID:
                long id = db.insertOrThrow(Tables.POSTDATA, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Posts.buildPostDataUri(id);
            default:
                break;
                
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = RedditContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, "posts", POSTS);
        matcher.addURI(authority, "posts/*", POSTS_ID);

        matcher.addURI(authority, "reddit/", REDDIT);
        matcher.addURI(authority, "reddit/*", REDDIT_ID);
        
        return matcher;
    }
    
    public static ContentValues contentValuesFromPostData(PostData data) {
        ContentValues values = new ContentValues();
        values.put(RedditContract.PostColumns.DOMAIN                , data.getDomain());
        values.put(RedditContract.PostColumns.BANNED_BY             , data.getBanned_by());
        values.put(RedditContract.PostColumns.SUBREDDIT             , data.getSubreddit());
        values.put(RedditContract.PostColumns.SELFTEXT_HTML         , data.getSelftext_html());
        values.put(RedditContract.PostColumns.SELFTEXT              , data.getSelftext());
        values.put(RedditContract.PostColumns.VOTE                  , data.getVote().name());
        values.put(RedditContract.PostColumns.SAVED                 , data.isSaved());
        values.put(RedditContract.PostColumns.POST_ID               , data.getId());
        values.put(RedditContract.PostColumns.CLICKED               , data.isClicked());
        values.put(RedditContract.PostColumns.TITLE                 , data.getTitle());
        values.put(RedditContract.PostColumns.COMMENTS              , data.getNum_comments());
        values.put(RedditContract.PostColumns.SCORE                 , data.getScore());
        values.put(RedditContract.PostColumns.APPROVED_BY           , data.getApproved_by());
        values.put(RedditContract.PostColumns.OVER_18               , data.isOver_18());
        values.put(RedditContract.PostColumns.HIDDEN                , data.isHidden());
        values.put(RedditContract.PostColumns.THUMBNAIL             , data.getThumbnail());
        values.put(RedditContract.PostColumns.SUBREDDIT_ID          , data.getSubreddit_id());
        values.put(RedditContract.PostColumns.AUTHOR_FLAIR_CSS_CLASS, data.getAuthor_flair_css_class());
        values.put(RedditContract.PostColumns.DOWNS                 , data.getDowns());
        values.put(RedditContract.PostColumns.IS_SELF               , data.isIs_self());
        values.put(RedditContract.PostColumns.PERMALINK             , data.getPermalink());
        values.put(RedditContract.PostColumns.NAME                  , data.getName());
        values.put(RedditContract.PostColumns.CREATED               , data.getCreated());
        values.put(RedditContract.PostColumns.URL                   , data.getUrl());
        values.put(RedditContract.PostColumns.AUTHOR_FLAIR_TEXT     , data.getAuthor_flair_text());
        values.put(RedditContract.PostColumns.AUTHOR                , data.getAuthor());
        values.put(RedditContract.PostColumns.CREATED_UTC           , data.getCreated_utc());
        values.put(RedditContract.PostColumns.LINK_FLAIR_TEXT       , data.getLink_flair_text());
        
        return values;
    }
    
}
