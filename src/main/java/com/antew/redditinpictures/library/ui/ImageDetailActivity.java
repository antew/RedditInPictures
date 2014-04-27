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

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.adapter.CursorPagerAdapter;
import com.antew.redditinpictures.library.database.RedditContract;
import com.antew.redditinpictures.library.dialog.LoginDialogFragment;
import com.antew.redditinpictures.library.event.DownloadImageCompleteEvent;
import com.antew.redditinpictures.library.event.DownloadImageEvent;
import com.antew.redditinpictures.library.model.Age;
import com.antew.redditinpictures.library.model.Category;
import com.antew.redditinpictures.library.model.Vote;
import com.antew.redditinpictures.library.model.reddit.LoginData;
import com.antew.redditinpictures.library.model.reddit.PostData;
import com.antew.redditinpictures.library.model.reddit.RedditLoginInformation;
import com.antew.redditinpictures.library.model.reddit.RedditUrl;
import com.antew.redditinpictures.library.preferences.SharedPreferencesHelper;
import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.library.util.BundleUtil;
import com.antew.redditinpictures.library.util.Ln;
import com.antew.redditinpictures.library.util.PostUtil;
import com.antew.redditinpictures.library.util.StringUtil;
import com.antew.redditinpictures.library.util.Strings;
import com.antew.redditinpictures.library.util.SubredditUtil;
import com.antew.redditinpictures.pro.R;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.squareup.otto.Subscribe;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImageDetailActivity extends ImageViewerActivity
    implements LoaderManager.LoaderCallbacks<Cursor>, LoginDialogFragment.LoginDialogListener {
    protected MenuItem  mUpvoteMenuItem;
    protected MenuItem  mDownvoteMenuItem;
    protected RedditUrl mRedditUrl;
    protected boolean   mRequestInProgress;
    private   String    mAfter;
    private   String    mBefore;
    private   Category  mCategory;
    private   Age       mAge;
    private   String    mSubreddit;
    private   int       mRequestedPage;
    private boolean mFirstLoad = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        displayVote();

        getSupportLoaderManager().initLoader(Constants.Loader.LOADER_REDDIT, null, this);
        getSupportLoaderManager().initLoader(Constants.Loader.LOADER_LOGIN, null, this);
        getSupportLoaderManager().initLoader(Constants.Loader.LOADER_POSTS, null, this);
        // Put the current page / total pages text in the ActionBar
        updateDisplay(mPager.getCurrentItem());
    }

    public void getExtras() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        mSubreddit = BundleUtil.getString(extras, Constants.Extra.EXTRA_SUBREDDIT, Constants.Reddit.REDDIT_FRONTPAGE);
        mCategory = Category.fromString(BundleUtil.getString(extras, Constants.Extra.EXTRA_CATEGORY, Category.HOT.getName()));
        mAge = Age.fromString(BundleUtil.getString(extras, Constants.Extra.EXTRA_AGE, Age.TODAY.getAge()));
        mRequestedPage = BundleUtil.getInt(extras, Constants.Extra.EXTRA_IMAGE, -1);

        Ln.d("Got Extras: Age %s Category %s Subreddit %s", mAge, mCategory, mSubreddit);
    }

    public FragmentStatePagerAdapter getPagerAdapter() {
        return new CursorPagerAdapter(getSupportFragmentManager(), null);
    }

    /**
     * Update the vote
     */
    protected void updateDisplay(int position) {
        PostData p = getAdapter().getPost(position);
        if (p != null) {
            displayVote(p.getVote());
        }

        int count = getAdapter().getCount();
        if (count > 0) {
            getSupportActionBar().setTitle(++position + "/" + getAdapter().getCount() + " - " + getString(R.string.reddit_in_pictures));
        } else {
            getSupportActionBar().setTitle(getString(R.string.reddit_in_pictures));
        }
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
    public void onResume() {
        super.onResume();
        displayVote();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.image_detail_menu, menu);
        super.onCreateOptionsMenu(menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // We save the upvote and downvote icons so that we can change their icon later
        mUpvoteMenuItem = menu.findItem(R.id.upvote);
        mDownvoteMenuItem = menu.findItem(R.id.downvote);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        int itemId = item.getItemId();
        if (itemId == R.id.upvote) {
            EasyTracker.getInstance(this)
                       .send(MapBuilder.createEvent(Constants.Analytics.Category.ACTION_BAR_ACTION, Constants.Analytics.Action.POST_VOTE,
                                                    Constants.Analytics.Label.UP, null).build()
                            );
            handleVote(item);
        } else if (itemId == R.id.downvote) {
            EasyTracker.getInstance(this)
                       .send(MapBuilder.createEvent(Constants.Analytics.Category.ACTION_BAR_ACTION, Constants.Analytics.Action.POST_VOTE,
                                                    Constants.Analytics.Label.DOWN, null).build()
                            );
            handleVote(item);
        }

        return true;
    }

    /**
     * Get the JSON representation of the current image/post in the ViewPager to report an error.
     *
     * @return The JSON representation of the currently viewed object.
     */
    @Override
    protected void reportCurrentItem() {
        RedditService.reportPost(this, getAdapter().getPost(mPager.getCurrentItem()));
    }

    @Override
    public String getSubreddit() {
        return mSubreddit;
    }

    protected void showLogin() {
        // Only needs to be shown if they aren't currently logged in.
        if (!RedditLoginInformation.isLoggedIn()) {
            LoginDialogFragment loginFragment = LoginDialogFragment.newInstance();
            loginFragment.show(getSupportFragmentManager(), Constants.Dialog.DIALOG_LOGIN);
        }
    }

    public void handleVote(MenuItem item) {
        if (!RedditLoginInformation.isLoggedIn()) {
            showLogin();
        } else {
            int itemId = item.getItemId();
            if (itemId == R.id.upvote) {
                PostUtil.votePost(this, getAdapter().getPost(mPager.getCurrentItem()), Vote.UP);
            } else if (itemId == R.id.downvote) {
                PostUtil.votePost(this, getAdapter().getPost(mPager.getCurrentItem()), Vote.DOWN);
            }
        }
    }

    /**
     * Get the URL of the current image in the ViewPager.
     *
     * @return The URL of the current image in the ViewPager.
     */
    public String getUrlForSharing() {
        PostData pd = getAdapter().getPost(mPager.getCurrentItem());
        return pd.getUrl();
    }

    /**
     * Get the Uri for the Reddit page of the current post in the ViewPager.
     *
     * @return The Uri for the post on reddit
     */
    protected Uri getPostUri() {
        return Uri.parse(
            getAdapter().getPost(mPager.getCurrentItem()).getFullPermalink(SharedPreferencesHelper.getUseMobileInterface(this)));
    }

    @Override
    public String getFilenameForSave() {
        if (getAdapter() != null && mPager != null) {
            PostData p = getAdapter().getPost(mPager.getCurrentItem());
            return StringUtil.sanitizeFileName(p.getTitle());
        }

        return super.getFilenameForSave();
    }

    @Override
    public void onFinishSaveImageDialog(String filename) {
        PostData postData = getAdapter().getPost(mPager.getCurrentItem());
        mBus.post(new DownloadImageEvent(postData.getPermalink(), filename));
    }

    public boolean isRequestInProgress() {
        return mRequestInProgress;
    }

    public void setRequestInProgress(boolean requestInProgress) {
        mRequestInProgress = requestInProgress;
        setSupportProgressBarIndeterminateVisibility(requestInProgress);
    }

    public void displayVote() {
        if (getAdapter() != null && mPager != null) {
            PostData post = getAdapter().getPost(mPager.getCurrentItem());
            if (post != null) {
                displayVote(post.getVote());
            }
        }
    }

    private CursorPagerAdapter getAdapter() {
        return (CursorPagerAdapter) mAdapter;
    }

    public void displayVote(Vote vote) {
        if (mUpvoteMenuItem == null || mDownvoteMenuItem == null) {
            return;
        }

        switch (vote) {
            case DOWN:
                mUpvoteMenuItem.setIcon(R.drawable.ic_action_upvote);
                mDownvoteMenuItem.setIcon(R.drawable.ic_action_downvote_highlighted);
                break;

            case UP:
                mUpvoteMenuItem.setIcon(R.drawable.ic_action_upvote_highlighted);
                mDownvoteMenuItem.setIcon(R.drawable.ic_action_downvote);
                break;

            case NEUTRAL:
                mUpvoteMenuItem.setIcon(R.drawable.ic_action_upvote);
                mDownvoteMenuItem.setIcon(R.drawable.ic_action_downvote);
                break;
        }
    }

    @Subscribe
    public void onDownloadImageComplete(DownloadImageCompleteEvent event) {
        Ln.i("DownloadImageComplete - filename was: " + event.getFilename());
        Toast.makeText(this, "Image saved as " + event.getFilename(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
        switch (id) {
            case Constants.Loader.LOADER_REDDIT:
                return new CursorLoader(this, RedditContract.RedditData.CONTENT_URI, null, null, null,
                                        RedditContract.RedditData.DEFAULT_SORT);
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

                // Set the ViewPager to the index the user selected in ImageGridFragment, we only need to do this
                // the first time the data is loaded
                if (mFirstLoad) {
                    mPager.setCurrentItem(mRequestedPage);
                    mFirstLoad = false;
                }

                updateDisplay(mPager.getCurrentItem());
                break;
        }
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
