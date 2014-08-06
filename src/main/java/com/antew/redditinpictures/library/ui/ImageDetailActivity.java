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
package com.antew.redditinpictures.library.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.view.Window;
import android.widget.Toast;

import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.adapter.CursorPagerAdapter;
import com.antew.redditinpictures.library.database.RedditContract;
import com.antew.redditinpictures.library.dialog.LoginDialogFragment;
import com.antew.redditinpictures.library.event.DownloadImageCompleteEvent;
import com.antew.redditinpictures.library.event.DownloadImageEvent;
import com.antew.redditinpictures.library.event.OnBackPressedEvent;
import com.antew.redditinpictures.library.event.RequestCompletedEvent;
import com.antew.redditinpictures.library.event.RequestInProgressEvent;
import com.antew.redditinpictures.library.interfaces.OnBackPressedListener;
import com.antew.redditinpictures.library.model.Age;
import com.antew.redditinpictures.library.model.Category;
import com.antew.redditinpictures.library.model.reddit.LoginData;
import com.antew.redditinpictures.library.model.reddit.PostData;
import com.antew.redditinpictures.library.model.reddit.RedditLoginInformation;
import com.antew.redditinpictures.library.model.reddit.RedditUrl;
import com.antew.redditinpictures.library.preferences.SharedPreferencesHelper;
import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.library.util.BundleUtil;
import com.antew.redditinpictures.library.util.Ln;
import com.antew.redditinpictures.library.util.Strings;
import com.antew.redditinpictures.library.util.SubredditUtil;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImageDetailActivity extends ImageViewerActivity
    implements LoaderManager.LoaderCallbacks<Cursor>, LoginDialogFragment.LoginDialogListener {
    protected RedditUrl mRedditUrl;
    protected boolean   mRequestInProgress;
    private   String    mAfter;
    private   String    mBefore;
    private   Category  mCategory;
    private   Age       mAge;
    private   String    mSubreddit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        getLoaderManager().initLoader(Constants.Loader.LOADER_REDDIT, null, this);
        getLoaderManager().initLoader(Constants.Loader.LOADER_LOGIN, null, this);
        getLoaderManager().initLoader(Constants.Loader.LOADER_POSTS, null, this);
    }

    @Override
    public void getExtras() {
        super.getExtras();
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        mSubreddit = BundleUtil.getString(extras, Constants.Extra.EXTRA_SUBREDDIT, Constants.Reddit.REDDIT_FRONTPAGE);
        mCategory = Category.fromString(BundleUtil.getString(extras, Constants.Extra.EXTRA_CATEGORY, Category.HOT.getName()));
        mAge = Age.fromString(BundleUtil.getString(extras, Constants.Extra.EXTRA_AGE, Age.TODAY.getAge()));

        Ln.d("Got Extras: Age %s Category %s Subreddit %s", mAge, mCategory, mSubreddit);
    }

    public FragmentStatePagerAdapter getPagerAdapter() {
        return new CursorPagerAdapter(getFragmentManager(), null);
    }

    @Override
    public void reachedCloseToLastPage() {
        if (!isRequestInProgress() && mAdapter.getCount() > 0) {
            Ln.d("Reached last page, loading more images");
            setRequestInProgress(true);
            RedditService.getPosts(this, mSubreddit, mAge, mCategory, mAfter);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(Constants.Extra.EXTRA_REDDIT_URL, mRedditUrl);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(Constants.Extra.EXTRA_REDDIT_URL)) {
            mRedditUrl = savedInstanceState.getParcelable(Constants.Extra.EXTRA_REDDIT_URL);
        }
    }

    public boolean isRequestInProgress() {
        return mRequestInProgress;
    }

    public void setRequestInProgress(boolean requestInProgress) {
        mRequestInProgress = requestInProgress;
        setProgressBarIndeterminateVisibility(requestInProgress);
    }

    private CursorPagerAdapter getAdapter() {
        return (CursorPagerAdapter) mAdapter;
    }

    @Subscribe
    public void onDownloadImageComplete(DownloadImageCompleteEvent event) {
        Ln.i("DownloadImageComplete - filename was: " + event.getFilename());
        Toast.makeText(this, "Image saved as " + event.getFilename(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        boolean shouldRespondToBackPress = true;
        if (mAdapter != null) {
            // TODO: instantiateItem seems weird here and should probably be replaced with a smarter adapter
            // Calling getItem(pos) on the adapter will cause a new fragment to be created each time
            // (the framework only calls it when the fragment isn't cached and needs instantiated).
            //
            // However, instantiateItem checks the private list of fragment's in the adapter to see if that position exist.
            // Since the current fragment by definition exists (I hope...), we should be fine calling it with a null ViewGroup,
            // because we will always get the cached instance.
            OnBackPressedListener listener = (OnBackPressedListener) mAdapter.instantiateItem(null, mPager.getCurrentItem());
            shouldRespondToBackPress = listener.shouldRespondToBackPress();
        }

        if (shouldRespondToBackPress) {
            super.onBackPressed();
        }
        // otherwise the fragment has said we don't want to finish() the activity

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
        switch (id) {
            case Constants.Loader.LOADER_REDDIT:
            return new CursorLoader(this, RedditContract.RedditData.CONTENT_URI, // uri
                                    null,                                  // projection
                                    "subreddit = ?",                       // selection
                                    new String[] { mSubreddit },      // selectionArgs[]
                                    RedditContract.Posts.DEFAULT_SORT);    // sort
            case Constants.Loader.LOADER_LOGIN:
                return new CursorLoader(this, RedditContract.Login.CONTENT_URI, null, null, null, RedditContract.Login.DEFAULT_SORT);
            case Constants.Loader.LOADER_POSTS:
                String selection = null;
                String[] selectionArgs = null;
                List<String> selectionArgsList = new ArrayList<String>();

                // If we have an aggregate subreddit we want to return relevant things.
                if (SubredditUtil.isAggregateSubreddit(mSubreddit)) {
                    selection = null;
                    selectionArgs = null;
                } else if (mSubreddit.contains("+")) {
                    // Poor mans checking for multis. If we have a multi, we want to handle all of them appropriately.
                    String[] subredditArray = mSubreddit.split("\\+");

                    for (String item : subredditArray) {
                        if (selection == null) {
                            selection = RedditContract.PostColumns.SUBREDDIT + " in (?";
                        } else {
                            selection += ",?";
                        }
                        selectionArgsList.add(item);
                    }
                    // Close the in statement.
                    selection += ")";
                } else {
                    selection = RedditContract.PostColumns.SUBREDDIT + " = ?";
                    selectionArgsList.add(mSubreddit);
                }

                // If the user doesn't want to see NSFW images, filter them out. Otherwise do nothing.
                if (!SharedPreferencesHelper.getShowNsfwImages(this)) {
                    if (Strings.isEmpty(selection)) {
                        selection = RedditContract.PostColumns.OVER_18 + " = ?";
                    } else {
                        selection += " and " + RedditContract.PostColumns.OVER_18 + " = ?";
                    }
                    selectionArgsList.add("0");
                }

                if (selectionArgsList != null && selectionArgsList.size() > 0) {
                    selectionArgs = selectionArgsList.toArray(new String[] { });
                }

                Ln.d("Retrieving posts For %s %s", selection, Arrays.toString(selectionArgs));

                return new CursorLoader(this, RedditContract.Posts.CONTENT_URI,  // uri
                                        RedditContract.Posts.LISTVIEW_PROJECTION, // projection
                                        selection,                                // selection
                                        selectionArgs,                            // selectionArgs[]
                                        RedditContract.Posts.DEFAULT_SORT);       // sort
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case Constants.Loader.LOADER_REDDIT:
                if (cursor != null && cursor.moveToFirst()) {
                    mAfter = cursor.getString(cursor.getColumnIndex(RedditContract.RedditData.AFTER));
                    mBefore = cursor.getString(cursor.getColumnIndex(RedditContract.RedditData.BEFORE));
                }
                break;
            case Constants.Loader.LOADER_LOGIN:
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        String username = cursor.getString(cursor.getColumnIndex(RedditContract.Login.USERNAME));
                        String cookie = cursor.getString(cursor.getColumnIndex(RedditContract.Login.COOKIE));
                        String modhash = cursor.getString(cursor.getColumnIndex(RedditContract.Login.MODHASH));

                        LoginData data = new LoginData(username, modhash, cookie);
                        if (!data.equals(RedditLoginInformation.getLoginData())) {
                            RedditLoginInformation.setLoginData(data);
                        }
                        new SubredditUtil.SetDefaultSubredditsTask(this).execute();
                    }
                }
                break;
            case Constants.Loader.LOADER_POSTS:
                setRequestInProgress(false);
                getAdapter().swapCursor(cursor);

                if (!mRequestInProgress && mPager.getCurrentItem() >= getAdapter().getCount() - POST_LOAD_OFFSET) {
                    reachedCloseToLastPage();
                }

                moveViewPagerToPosition(getRequestedPage());
                break;
        }
    }

    @Subscribe
    @Override
    public void requestInProgress(RequestInProgressEvent event) {
        super.requestInProgress(event);
    }

    @Subscribe
    @Override
    public void requestCompleted(RequestCompletedEvent event) {
        super.requestCompleted(event);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursor) {
        getAdapter().swapCursor(null);
    }

    @Override
    public void onFinishLoginDialog(String username, String password) {
        RedditService.login(this, username, password);
    }
}
