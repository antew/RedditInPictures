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
package com.antew.redditinpictures.library.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import com.antew.redditinpictures.library.util.Ln;
import com.antew.redditinpictures.library.database.RedditContract;
import com.antew.redditinpictures.library.database.RedditContract.Login;
import com.antew.redditinpictures.library.database.RedditContract.LoginColumns;
import com.antew.redditinpictures.library.database.RedditContract.Posts;
import com.antew.redditinpictures.library.database.RedditContract.RedditData;
import com.antew.redditinpictures.library.database.RedditContract.RedditDataColumns;
import com.antew.redditinpictures.library.database.RedditContract.Subreddits;
import com.antew.redditinpictures.library.database.RedditDatabase;
import com.antew.redditinpictures.library.database.RedditDatabase.Tables;

public class RedditProvider extends ContentProvider {
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private RedditDatabase mDatabase;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = RedditContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, RedditContract.BASE_CONTENT_URI.toString(), 0);

        matcher.addURI(authority, RedditContract.PATH_POSTS, RedditContract.POSTS);
        matcher.addURI(authority, RedditContract.PATH_POSTS + "/*", RedditContract.POSTS_ID);

        matcher.addURI(authority, RedditContract.PATH_REDDIT_DATA, RedditContract.REDDIT);
        matcher.addURI(authority, RedditContract.PATH_REDDIT_DATA + "/*", RedditContract.REDDIT_ID);

        matcher.addURI(authority, RedditContract.PATH_LOGIN, RedditContract.LOGIN);
        matcher.addURI(authority, RedditContract.PATH_LOGIN + "/*", RedditContract.LOGIN_ID);

        matcher.addURI(authority, RedditContract.PATH_SUBREDDITS, RedditContract.SUBREDDIT);
        matcher.addURI(authority, RedditContract.PATH_SUBREDDITS + "/*", RedditContract.SUBREDDIT_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mDatabase = new RedditDatabase(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mDatabase.getReadableDatabase();

        String table = getTable(uri);
        Cursor cursor = db.query(table, projection, selection, selectionArgs, null, null, sortOrder);
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
        final SQLiteDatabase db = mDatabase.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        ContentResolver resolver = getContext().getContentResolver();

        String table = getTable(uri);
        long id = db.insertOrThrow(table, null, values);
        resolver.notifyChange(uri, null);

        // Return a URI pointing to the new row
        switch (match) {
            case RedditContract.REDDIT:
            case RedditContract.REDDIT_ID:
                return RedditData.buildPostDataUri(values.getAsString(RedditDataColumns.MODHASH));

            case RedditContract.POSTS:
            case RedditContract.POSTS_ID:
                return Posts.buildPostDataUri(id);

            case RedditContract.LOGIN:
            case RedditContract.LOGIN_ID:
                return Login.buildLoginUri(values.getAsString(LoginColumns.USERNAME));

            case RedditContract.SUBREDDIT:
            case RedditContract.SUBREDDIT_ID:
                return Subreddits.buildSubredditUri(values.getAsString(Subreddits.DISPLAY_NAME));

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Ln.i("delete(uri=" + uri + ")");
        final SQLiteDatabase db = mDatabase.getWritableDatabase();

        String table = getTable(uri);
        int rows = db.delete(table, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return rows;
    }

    private void deleteDatabase() {
        // TODO: wait for content provider operations to finish, then tear down
        mDatabase.close();
        Context context = getContext();
        RedditDatabase.deleteDatabase(context);
        mDatabase = new RedditDatabase(getContext());
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Ln.i("update(uri= %s values= %s)", uri, values.toString());
        final SQLiteDatabase db = mDatabase.getWritableDatabase();
        ContentResolver resolver = getContext().getContentResolver();

        String table = getTable(uri);
        int rows = db.update(table, values, selection, selectionArgs);
        resolver.notifyChange(uri, null);

        return rows;
    }

    private String getTable(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case RedditContract.REDDIT:
            case RedditContract.REDDIT_ID:
                return Tables.REDDIT_DATA;

            case RedditContract.POSTS:
            case RedditContract.POSTS_ID:
                return Tables.POSTDATA;

            case RedditContract.LOGIN:
            case RedditContract.LOGIN_ID:
                return Tables.LOGIN;

            case RedditContract.SUBREDDIT:
            case RedditContract.SUBREDDIT_ID:
                return Tables.SUBREDDITS;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }
    /**
     * The default {@link #bulkInsert(android.net.Uri, android.content.ContentValues[])}
     * loops over the data and calls {@link #insert(android.net.Uri, android.content.ContentValues)} for each
     * row.  This speeds it up a bit by wrapping the inserts in a transaction.
     * @param uri The content:// URI of the insertion request.
     * @param values An array of sets of column_name/value pairs to add to the database.
     *    This must not be {@code null}.
     * @return The number of values that were inserted.
     */
    @Override public int bulkInsert(Uri uri, ContentValues[] values) {
        // Get the table for this URI, if it doesn't exist
        // throw an exception for the caller
        String table = getTable(uri);

        final SQLiteDatabase db = mDatabase.getWritableDatabase();
        db.beginTransaction();

        int numValues = values.length;
        try {
            for (int i = 0; i < numValues; i++) {
                db.insertOrThrow(table, null, values[i]);
            }
            db.setTransactionSuccessful();
            // We don't want to get notified for every row, only notify once we have
            // loaded everything
            getContext().getContentResolver().notifyChange(uri, null);
        } finally {
            db.endTransaction();
        }


        return numValues;
    }
}
