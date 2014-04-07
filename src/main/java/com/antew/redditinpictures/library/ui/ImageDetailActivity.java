/*
 * Copyright (C) 2012 The Android Open Source Project
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
import android.support.v4.content.LocalBroadcastManager;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.adapter.CursorPagerAdapter;
import com.antew.redditinpictures.library.dialog.LoginDialogFragment;
import com.antew.redditinpictures.library.enums.Age;
import com.antew.redditinpictures.library.enums.Category;
import com.antew.redditinpictures.library.enums.Vote;
import com.antew.redditinpictures.library.preferences.SharedPreferencesHelper;
import com.antew.redditinpictures.library.reddit.PostData;
import com.antew.redditinpictures.library.reddit.RedditLoginInformation;
import com.antew.redditinpictures.library.reddit.RedditUrl;
import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.library.utils.Ln;
import com.antew.redditinpictures.library.utils.StringUtil;
import com.antew.redditinpictures.library.utils.Strings;
import com.antew.redditinpictures.pro.R;
import com.antew.redditinpictures.sqlite.RedditContract;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImageDetailActivity extends ImageViewerActivity implements LoaderManager.LoaderCallbacks<Cursor> {
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

        getSupportLoaderManager().initLoader(Constants.LOADER_REDDIT, null, this);
        getSupportLoaderManager().initLoader(Constants.LOADER_POSTS, null, this);
        // Put the current page / total pages text in the ActionBar
        updateDisplay(mPager.getCurrentItem());
    }

    public void getExtras() {
        Intent i = getIntent();

        if (i.hasExtra(Constants.EXTRA_AGE)) {
            mAge = Age.valueOf(i.getStringExtra(Constants.EXTRA_AGE));
        }

        if (i.hasExtra(Constants.EXTRA_CATEGORY)) {
            mCategory = Category.valueOf(i.getStringExtra(Constants.EXTRA_CATEGORY));
        }

        if (i.hasExtra(Constants.EXTRA_SELECTED_SUBREDDIT)) {
            mSubreddit = i.getStringExtra(Constants.EXTRA_SELECTED_SUBREDDIT);
        }

        mRequestedPage = getIntent().getIntExtra(Constants.EXTRA_IMAGE, -1);

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
    public void reachedLastPage() {
        super.reachedLastPage();

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
        outState.putParcelable(Constants.EXTRA_REDDIT_URL, mRedditUrl);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(Constants.EXTRA_REDDIT_URL)) {
            mRedditUrl = savedInstanceState.getParcelable(Constants.EXTRA_REDDIT_URL);
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
        // We save the upvote and downvote icons so that we can change their icon later
        mUpvoteMenuItem = menu.findItem(R.id.upvote);
        mDownvoteMenuItem = menu.findItem(R.id.downvote);

        // We save the icon for locking the view pager so that we can reference
        // it when we receive a broadcast message to toggle the ViewPager lock state
        lockViewPagerItem = menu.findItem(R.id.lock_viewpager);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        int itemId = item.getItemId();
        if (itemId == R.id.upvote || itemId == R.id.downvote) {
            handleVote(item);
        }

        return true;
    }

    protected void showLogin() {
        // Only needs to be shown if they aren't currently logged in.
        if (!RedditLoginInformation.isLoggedIn()) {
            LoginDialogFragment loginFragment = LoginDialogFragment.newInstance();
            loginFragment.show(getSupportFragmentManager(), Constants.DIALOG_LOGIN);
        }
    }

    public void handleVote(MenuItem item) {
        if (!RedditLoginInformation.isLoggedIn()) {
            showLogin();
        } else {
            int itemId = item.getItemId();
            if (itemId == R.id.upvote) {
                vote(Vote.UP, item, getAdapter().getPost(mPager.getCurrentItem()));
            } else if (itemId == R.id.downvote) {
                vote(Vote.DOWN, item, getAdapter().getPost(mPager.getCurrentItem()));
            }
        }
    }

    /**
     * Handles updating the vote based on the action bar vote icon that was clicked, broadcasts a
     * message to have the fragment update the score.
     * <p>
     * If the user is not logged in, we return immediately.
     * </p>
     * <p>
     * If the current vote is UP and the new vote is UP, the vote is changed to NEUTRAL.<br>
     * If the current vote is UP and the new vote is DOWN, the vote is changed to DOWN.
     * </p>
     * <p>
     * If the current vote is DOWN and the new vote is DOWN, the vote is changed to NEUTRAL<br>
     * If the current vote is DOWN and the new vote is UP, the vote is changed to UP.
     * </p>
     *
     * @param whichVoteButton
     *     The vote representing the menu item which was clicked
     * @param item
     *     The menu item which was clicked
     * @param p
     *     The post this vote is for
     */
    private void vote(Vote whichVoteButton, MenuItem item, PostData p) {
        if (!RedditLoginInformation.isLoggedIn()) {
            return;
        }

        Intent intent = new Intent(Constants.BROADCAST_UPDATE_SCORE);
        intent.putExtra(Constants.EXTRA_PERMALINK, p.getPermalink());

        switch (whichVoteButton) {
            case DOWN:
                switch (p.getVote()) {
                    case DOWN:
                        RedditService.vote(this, p.getName(), Vote.NEUTRAL);
                        item.setIcon(R.drawable.ic_action_downvote);
                        p.setVote(Vote.NEUTRAL);
                        p.setScore(p.getScore() + 1);
                        break;

                    case NEUTRAL:
                    case UP:
                        RedditService.vote(this, p.getName(), Vote.DOWN);
                        item.setIcon(R.drawable.ic_action_downvote_highlighted);
                        p.setVote(Vote.DOWN);
                        mUpvoteMenuItem.setIcon(R.drawable.ic_action_upvote);
                        p.setScore(p.getScore() - 1);
                        break;
                }
                break;

            case UP:
                switch (p.getVote()) {
                    case NEUTRAL:
                    case DOWN:
                        RedditService.vote(this, p.getName(), Vote.UP);
                        item.setIcon(R.drawable.ic_action_upvote_highlighted);
                        p.setVote(Vote.UP);
                        p.setScore(p.getScore() + 1);
                        mDownvoteMenuItem.setIcon(R.drawable.ic_action_downvote);
                        break;

                    case UP:
                        RedditService.vote(this, p.getName(), Vote.NEUTRAL);
                        item.setIcon(R.drawable.ic_action_upvote);
                        p.setVote(Vote.NEUTRAL);
                        p.setScore(p.getScore() - 1);
                        break;
                }

                break;

            default:
                break;
        }

        // Broadcast the intent to update the score in the ImageDetailFragment
        intent.putExtra(Constants.EXTRA_SCORE, p.getScore());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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
     * Get the currently displayed image fragment and cause it to refresh the currently displayed contents.
     */
    @Override
    protected void refreshCurentImage() {

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
        PostData p = getAdapter().getPost(mPager.getCurrentItem());
        Intent intent = new Intent(Constants.BROADCAST_DOWNLOAD_IMAGE);
        intent.putExtra(Constants.EXTRA_PERMALINK, p.getPermalink());
        intent.putExtra(Constants.EXTRA_FILENAME, filename);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
        switch (id) {
            case Constants.LOADER_REDDIT:
                return new CursorLoader(this, RedditContract.RedditData.CONTENT_URI, null, null, null,
                                        RedditContract.RedditData.DEFAULT_SORT);

            case Constants.LOADER_POSTS:
                String selection = null;
                String[] selectionArgs = null;
                List<String> selectionArgsList = new ArrayList<String>();

                // If we have an aggregate subreddit we want to return relevant things.
                if (mSubreddit.equals(Constants.REDDIT_FRONTPAGE)
                    || mSubreddit.equals(Constants.REDDIT_FRONTPAGE_DISPLAY_NAME)
                    || mSubreddit.equals(Constants.REDDIT_ALL_DISPLAY_NAME)) {
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
            case Constants.LOADER_REDDIT:
                if (cursor != null && cursor.moveToFirst()) {
                    mAfter = cursor.getString(cursor.getColumnIndex(RedditContract.RedditData.AFTER));
                    mBefore = cursor.getString(cursor.getColumnIndex(RedditContract.RedditData.BEFORE));
                }
                break;

            case Constants.LOADER_POSTS:
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
}
