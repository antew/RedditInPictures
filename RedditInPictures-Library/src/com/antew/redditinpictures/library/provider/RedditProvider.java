package com.antew.redditinpictures.library.provider;

import java.lang.reflect.Array;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.reddit.RedditApi.PostData;
import com.antew.redditinpictures.sqlite.RedditContract;
import com.antew.redditinpictures.sqlite.RedditContract.Login;
import com.antew.redditinpictures.sqlite.RedditContract.LoginColumns;
import com.antew.redditinpictures.sqlite.RedditContract.Posts;
import com.antew.redditinpictures.sqlite.RedditContract.RedditData;
import com.antew.redditinpictures.sqlite.RedditContract.RedditDataColumns;
import com.antew.redditinpictures.sqlite.RedditContract.Subreddits;
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
            case RedditContract.LOGIN:
                cursor = db.query(Tables.LOGIN, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case RedditContract.LOGIN_ID:
                cursor = db.query(Tables.LOGIN, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case RedditContract.SUBREDDIT:
            case RedditContract.SUBREDDIT_ID:
                cursor = db.query(Tables.SUBREDDITS, projection, selection, selectionArgs, null, null, sortOrder);
                break;
                
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        
        return cursor;
        
    }

    /* (non-Javadoc)
     * @see android.content.ContentProvider#getType(android.net.Uri)
     */
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
            case RedditContract.LOGIN:
                return Login.CONTENT_TYPE;
            case RedditContract.LOGIN_ID:
                return Login.CONTENT_ITEM_TYPE;
            case RedditContract.SUBREDDIT:
                return Subreddits.CONTENT_TYPE;
            case RedditContract.SUBREDDIT_ID:
                return Subreddits.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /* (non-Javadoc)
     * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDatabase.getReadableDatabase();
        final int match = sUriMatcher.match(uri);
        ContentResolver resolver = getContext().getContentResolver();
        
        switch (match) {
            case RedditContract.REDDIT:
            case RedditContract.REDDIT_ID:
                db.insertOrThrow(Tables.REDDIT_DATA, null, values);
                resolver.notifyChange(uri, null);
                return RedditData.buildPostDataUri(values.getAsString(RedditDataColumns.MODHASH));

            case RedditContract.POSTS:
            case RedditContract.POSTS_ID:
                long id = db.insertOrThrow(Tables.POSTDATA, null, values);
                resolver.notifyChange(uri, null);
                return Posts.buildPostDataUri(id);
                
            case RedditContract.LOGIN:
            case RedditContract.LOGIN_ID:
                db.insertOrThrow(Tables.LOGIN, null, values);
                resolver.notifyChange(uri, null);
                return Login.buildLoginUri(values.getAsString(LoginColumns.USERNAME));
                
            case RedditContract.SUBREDDIT:
            case RedditContract.SUBREDDIT_ID:
                db.insertOrThrow(Tables.SUBREDDITS, null, values);
                resolver.notifyChange(uri, null);
                return Subreddits.buildSubredditUri(values.getAsString(Subreddits.DISPLAY_NAME));
                
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
                
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.i(TAG, "delete(uri=" + uri + ")");
        final SQLiteDatabase db = mDatabase.getWritableDatabase();
        ContentResolver resolver = getContext().getContentResolver();
        
        final int match = sUriMatcher.match(uri);
        switch (match) {
            
            case RedditContract.BASE:
                deleteDatabase();
                resolver.notifyChange(uri, null, false);
                return 1;
                
            case RedditContract.REDDIT:
            case RedditContract.REDDIT_ID:
                int rows = db.delete(Tables.REDDIT_DATA, selection, selectionArgs);
                resolver.notifyChange(uri, null);
                return rows;

            case RedditContract.POSTS:
            case RedditContract.POSTS_ID:
                rows = db.delete(Tables.POSTDATA, selection, selectionArgs);
                resolver.notifyChange(uri, null);
                return rows;
            
            case RedditContract.LOGIN:
            case RedditContract.LOGIN_ID:
                rows = db.delete(Tables.LOGIN, selection, selectionArgs);
                resolver.notifyChange(uri, null);
                return rows;
            
            case RedditContract.SUBREDDIT:
            case RedditContract.SUBREDDIT_ID:
                rows = db.delete(Tables.SUBREDDITS, selection, selectionArgs);
                resolver.notifyChange(uri, null);
                return rows;
                
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
                
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.i(TAG, "update(uri=" + uri + ", values=" + values.toString() + ")");
        final SQLiteDatabase db = mDatabase.getWritableDatabase();
        ContentResolver resolver = getContext().getContentResolver();
        
        final int match = sUriMatcher.match(uri);
        switch (match) {
            
            case RedditContract.REDDIT:
            case RedditContract.REDDIT_ID:
                int rows = db.update(Tables.REDDIT_DATA, values, selection, selectionArgs);
                resolver.notifyChange(uri, null);
                return rows;

            case RedditContract.POSTS:
            case RedditContract.POSTS_ID:
                rows = db.update(Tables.POSTDATA, values, selection, selectionArgs);
                resolver.notifyChange(uri, null);
                return rows;
            
            case RedditContract.LOGIN:
            case RedditContract.LOGIN_ID:
                rows = db.update(Tables.LOGIN, values, selection, selectionArgs);
                resolver.notifyChange(uri, null);
                return rows;
            
            case RedditContract.SUBREDDIT:
            case RedditContract.SUBREDDIT_ID:
                rows = db.update(Tables.SUBREDDITS, values, selection, selectionArgs);
                resolver.notifyChange(uri, null);
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
        
        matcher.addURI(authority, RedditContract.PATH_LOGIN, RedditContract.LOGIN);
        matcher.addURI(authority, RedditContract.PATH_LOGIN+ "/*", RedditContract.LOGIN_ID);
        
        matcher.addURI(authority, RedditContract.PATH_SUBREDDITS, RedditContract.SUBREDDIT);
        matcher.addURI(authority, RedditContract.PATH_SUBREDDITS + "/*", RedditContract.SUBREDDIT_ID);
        
        return matcher;
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
            operations[i] = data.get(i).getContentValues();
        }
        
        return operations;
    }
    
}
