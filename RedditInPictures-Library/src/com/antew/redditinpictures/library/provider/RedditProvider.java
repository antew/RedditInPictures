package com.antew.redditinpictures.library.provider;

import java.util.List;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.reddit.RedditApi;
import com.antew.redditinpictures.library.reddit.RedditApi.PostData;
import com.antew.redditinpictures.library.reddit.Vote;
import com.antew.redditinpictures.sqlite.RedditContract;
import com.antew.redditinpictures.sqlite.RedditContract.Posts;
import com.antew.redditinpictures.sqlite.RedditContract.RedditData;
import com.antew.redditinpictures.sqlite.RedditContract.RedditDataColumns;
import com.antew.redditinpictures.sqlite.RedditDatabase;
import com.antew.redditinpictures.sqlite.RedditDatabase.Tables;

public class RedditProvider extends ContentProvider {
    private RedditDatabase   mDatabase;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    public static final String TAG = RedditProvider.class.getSimpleName();

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
        Cursor cursor = null;
        switch (match) {
            case RedditContract.REDDIT:
                cursor = db.query(Tables.REDDIT_DATA, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case RedditContract.REDDIT_ID:
                cursor = db.query(Tables.REDDIT_DATA, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case RedditContract.POSTS:
                cursor = db.query(Tables.POSTDATA, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case RedditContract.POSTS_ID:
                cursor = db.query(Tables.POSTDATA, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        
        return cursor;
        
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
                
        switch (match) {
            case RedditContract.REDDIT:
                return RedditData.CONTENT_TYPE;
            case RedditContract.REDDIT_ID:
                return RedditData.CONTENT_ITEM_TYPE;
            case RedditContract.POSTS:
                return Posts.CONTENT_TYPE;
            case RedditContract.POSTS_ID:
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
            case RedditContract.REDDIT:
            case RedditContract.REDDIT_ID:
                db.insertOrThrow(Tables.REDDIT_DATA, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return RedditData.buildPostDataUri(values.getAsString(RedditDataColumns.MODHASH));

            case RedditContract.POSTS:
            case RedditContract.POSTS_ID:
                long id = db.insertOrThrow(Tables.POSTDATA, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Posts.buildPostDataUri(id);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
                
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.i(TAG, "delete(uri=" + uri + ")");
        final SQLiteDatabase db = mDatabase.getWritableDatabase();
        
        final int match = sUriMatcher.match(uri);
        switch (match) {
            
            case RedditContract.BASE:
                deleteDatabase();
                getContext().getContentResolver().notifyChange(uri, null, false);
                return 1;
                
            case RedditContract.REDDIT:
            case RedditContract.REDDIT_ID:
                int rows = db.delete(Tables.REDDIT_DATA, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return rows;

            case RedditContract.POSTS:
            case RedditContract.POSTS_ID:
                rows = db.delete(Tables.POSTDATA, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return rows;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
                
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.i(TAG, "update(uri=" + uri + ", values=" + values.toString() + ")");
        final SQLiteDatabase db = mDatabase.getWritableDatabase();
        
        final int match = sUriMatcher.match(uri);
        switch (match) {
            
            case RedditContract.REDDIT:
            case RedditContract.REDDIT_ID:
                int rows = db.update(Tables.REDDIT_DATA, values, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return rows;

            case RedditContract.POSTS:
            case RedditContract.POSTS_ID:
                rows = db.update(Tables.POSTDATA, values, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return rows;
                
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
                
        }
    }

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = RedditContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, RedditContract.BASE_CONTENT_URI.toString(), 0);

        matcher.addURI(authority, RedditContract.PATH_POSTS, RedditContract.POSTS);
        matcher.addURI(authority, RedditContract.PATH_POSTS + "/*", RedditContract.POSTS_ID);

        matcher.addURI(authority, RedditContract.PATH_REDDIT_DATA, RedditContract.REDDIT);
        matcher.addURI(authority, RedditContract.PATH_REDDIT_DATA + "/*", RedditContract.REDDIT_ID);
        
        return matcher;
    }
    
    public static ContentValues contentValuesFromRedditApi(RedditApi api) {
        ContentValues values = new ContentValues();
        values.put(RedditContract.RedditData.AFTER, api.getData().getAfter());
        values.put(RedditContract.RedditData.BEFORE, api.getData().getBefore());
        values.put(RedditContract.RedditData.MODHASH, api.getData().getModhash());

        return values;
    }
    
    /**
     * Convenience method to transform a {@link List} of {@link PostData} into an {@link Array} of {@link ContentValues}
     * @param data List of {@link PostData} to consume
     * @return Array of {@link ContentValues} for use with {@link ContentProvider#bulkInsert(Uri, ContentValues[])}
     * 
     */
    public static ContentValues[] contentValuesFromPostData(List<PostData> data) {
        ContentValues[] operations = new ContentValues[data.size()];

        for (int i = 0; i < data.size(); i++) {
            operations[i] = RedditProvider.contentValuesFromPostData(data.get(i));
        }
        
        return operations;

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
    
    public static PostData cursorToPostData(Cursor cursor) {
        PostData p = new PostData();
        
        //@formatter:off
        p.setDomain                (cursor.getString(cursor.getColumnIndex(RedditContract.Posts.DOMAIN)));
        p.setBanned_by             (cursor.getString(cursor.getColumnIndex(RedditContract.Posts.BANNED_BY)));
        p.setSubreddit             (cursor.getString(cursor.getColumnIndex(RedditContract.Posts.SUBREDDIT)));
        p.setSelftext_html         (cursor.getString(cursor.getColumnIndex(RedditContract.Posts.SELFTEXT_HTML)));
        p.setSelftext              (cursor.getString(cursor.getColumnIndex(RedditContract.Posts.SELFTEXT)));
        p.setVote     (Vote.valueOf(cursor.getString(cursor.getColumnIndex(RedditContract.Posts.VOTE))));
        p.setSaved                 (cursor.getInt   (cursor.getColumnIndex(RedditContract.Posts.SAVED)) == 1);
        p.setId                    (cursor.getString(cursor.getColumnIndex(RedditContract.Posts.POST_ID)));
        p.setClicked               (cursor.getInt   (cursor.getColumnIndex(RedditContract.Posts.CLICKED)) == 1);
        p.setTitle                 (cursor.getString(cursor.getColumnIndex(RedditContract.Posts.TITLE)));
        p.setNum_comments          (cursor.getInt   (cursor.getColumnIndex(RedditContract.Posts.COMMENTS)));
        p.setScore                 (cursor.getInt   (cursor.getColumnIndex(RedditContract.Posts.SCORE)));
        p.setApproved_by           (cursor.getString(cursor.getColumnIndex(RedditContract.Posts.APPROVED_BY)));
        p.setOver_18               (cursor.getInt   (cursor.getColumnIndex(RedditContract.Posts.OVER_18)) == 1);
        p.setHidden                (cursor.getInt   (cursor.getColumnIndex(RedditContract.Posts.HIDDEN)) == 1);
        p.setThumbnail             (cursor.getString(cursor.getColumnIndex(RedditContract.Posts.THUMBNAIL)));
        p.setSubreddit_id          (cursor.getString(cursor.getColumnIndex(RedditContract.Posts.SUBREDDIT_ID)));
        p.setAuthor_flair_css_class(cursor.getString(cursor.getColumnIndex(RedditContract.Posts.AUTHOR_FLAIR_CSS_CLASS)));
        p.setDowns                 (cursor.getInt   (cursor.getColumnIndex(RedditContract.Posts.DOWNS)));
        p.setIs_self               (cursor.getInt   (cursor.getColumnIndex(RedditContract.Posts.IS_SELF)) == 1);
        p.setPermalink             (cursor.getString(cursor.getColumnIndex(RedditContract.Posts.PERMALINK)));
        p.setName                  (cursor.getString(cursor.getColumnIndex(RedditContract.Posts.NAME)));
        p.setCreated               (cursor.getLong  (cursor.getColumnIndex(RedditContract.Posts.CREATED)));
        p.setUrl                   (cursor.getString(cursor.getColumnIndex(RedditContract.Posts.URL)));
        p.setAuthor_flair_text     (cursor.getString(cursor.getColumnIndex(RedditContract.Posts.AUTHOR_FLAIR_TEXT)));
        p.setAuthor                (cursor.getString(cursor.getColumnIndex(RedditContract.Posts.AUTHOR)));
        p.setCreated_utc           (cursor.getLong  (cursor.getColumnIndex(RedditContract.Posts.CREATED_UTC)));
        p.setLink_flair_text       (cursor.getString(cursor.getColumnIndex(RedditContract.Posts.LINK_FLAIR_TEXT)));
        p.setDecodedUrl            (cursor.getString(cursor.getColumnIndex(RedditContract.Posts.DECODED_URL)));
        //@formatter:off
        
        return p;
    }
    
}
