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
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.antew.redditinpictures.library.adapter.CursorPagerAdapter;
import com.antew.redditinpictures.library.enums.Age;
import com.antew.redditinpictures.library.enums.Category;
import com.antew.redditinpictures.library.enums.Vote;
import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.preferences.SharedPreferencesHelper;
import com.antew.redditinpictures.library.reddit.PostData;
import com.antew.redditinpictures.library.reddit.RedditLoginInformation;
import com.antew.redditinpictures.library.reddit.RedditUrl;
import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.library.utils.Consts;
import com.antew.redditinpictures.library.utils.StringUtil;
import com.antew.redditinpictures.pro.R;
import com.antew.redditinpictures.sqlite.RedditContract;

public class ImageDetailActivity extends ImageViewerActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG        = "ImageDetailActivity";
    protected MenuItem         mUpvoteMenuItem;
    protected MenuItem         mDownvoteMenuItem;
    protected RedditUrl        mRedditUrl;
    protected boolean          mRequestInProgress;
    private String             mAfter;
    private String             mBefore;
    private Category           mCategory;
    private Age                mAge;
    private String             mSubreddit;
    private int                mRequestedPage;
    private boolean            mFirstLoad = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        displayVote();

        getSupportLoaderManager().initLoader(Consts.LOADER_REDDIT, null, this);
        getSupportLoaderManager().initLoader(Consts.LOADER_POSTS, null, this);
        // Put the current page / total pages text in the ActionBar
        updateDisplay(mPager.getCurrentItem());

    }

    private CursorPagerAdapter getAdapter() {
        return (CursorPagerAdapter) mAdapter;
    }

    public FragmentStatePagerAdapter getPagerAdapter() {
        return new CursorPagerAdapter(getSupportFragmentManager(), null);
    }

    public void getExtras() {
        Intent i = getIntent();

        if (i.hasExtra(Consts.EXTRA_AGE))
            mAge = Age.valueOf(i.getStringExtra(Consts.EXTRA_AGE));

        if (i.hasExtra(Consts.EXTRA_CATEGORY))
            mCategory = Category.valueOf(i.getStringExtra(Consts.EXTRA_CATEGORY));

        if (i.hasExtra(Consts.EXTRA_SELECTED_SUBREDDIT))
            mSubreddit = i.getStringExtra(Consts.EXTRA_SELECTED_SUBREDDIT);

        mRequestedPage = getIntent().getIntExtra(Consts.EXTRA_IMAGE, -1);

        Log.i(TAG, "getExtras() - Age = " + mAge.name() + ", Category = " + mCategory.name() + ", Subreddit = " + mSubreddit);
    }

    /**
     * Update the vote
     */
    protected void updateDisplay(int position) {
        PostData p = getAdapter().getPost(position);
        if (p != null)
            displayVote(p.getVote());

        int count = getAdapter().getCount();
        if (count > 0)
            getSupportActionBar().setTitle(++position + "/" + getAdapter().getCount() + " - " + getString(R.string.reddit_in_pictures));
        else
            getSupportActionBar().setTitle(getString(R.string.reddit_in_pictures));
            
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        displayVote();
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

    public void handleVote(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.upvote) {
            vote(Vote.UP, item, getAdapter().getPost(mPager.getCurrentItem()));
        } else if (itemId == R.id.downvote) {
            vote(Vote.DOWN, item, getAdapter().getPost(mPager.getCurrentItem()));
        }
    }

    /**
     * Get the Uri for the Reddit page of the current post in the ViewPager.
     * 
     * @return The Uri for the post on reddit
     */
    protected Uri getPostUri() {
        //@formatter:off
        return Uri.parse(getAdapter().getPost(mPager.getCurrentItem())
                .getFullPermalink(SharedPreferencesHelper.getUseMobileInterface(this)));
        //@formatter:on
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(Consts.EXTRA_REDDIT_URL, mRedditUrl);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(Consts.EXTRA_REDDIT_URL))
            mRedditUrl = savedInstanceState.getParcelable(Consts.EXTRA_REDDIT_URL);
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
     *            The vote representing the menu item which was clicked
     * @param item
     *            The menu item which was clicked
     * @param p
     *            The post this vote is for
     */
    private void vote(Vote whichVoteButton, MenuItem item, PostData p) {
        if (!RedditLoginInformation.isLoggedIn()) {
            Toast.makeText(this, R.string.you_must_be_logged_in_to_vote, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Consts.BROADCAST_UPDATE_SCORE);
        intent.putExtra(Consts.EXTRA_PERMALINK, p.getPermalink());

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
        intent.putExtra(Consts.EXTRA_SCORE, p.getScore());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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

        if (!RedditLoginInformation.isLoggedIn()) {
            mUpvoteMenuItem.setVisible(false);
            mDownvoteMenuItem.setVisible(false);
        }

        // We save the icon for locking the view pager so that we can reference
        // it when we receive a broadcast message to toggle the ViewPager lock state
        lockViewPagerItem = menu.findItem(R.id.lock_viewpager);

        return true;
    }

    public void displayVote() {
        if (getAdapter() != null && mPager != null) {
            PostData post = getAdapter().getPost(mPager.getCurrentItem());
            if (post != null)
                displayVote(post.getVote());
        }

    }

    public void displayVote(Vote vote) {
        if (mUpvoteMenuItem == null || mDownvoteMenuItem == null) {
            Log.i(TAG, "mUpvoteMenuItem or mDownvoteMenuItem null, not updating vote, vote was = " + vote.name());
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
        Intent intent = new Intent(Consts.BROADCAST_DOWNLOAD_IMAGE);
        intent.putExtra(Consts.EXTRA_PERMALINK, p.getPermalink());
        intent.putExtra(Consts.EXTRA_FILENAME, filename);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }

    @Override
    public void reachedLastPage() {
        super.reachedLastPage();

        Log.i(TAG, "reachedLastPage()");
        if (!isRequestInProgress() && mAdapter.getCount() > 0) {
            Log.i(TAG, "reachedLastPage() - Loading more images");
            setRequestInProgress(true);
            RedditService.getPosts(this, mSubreddit, mAge, mCategory, mAfter, false);
        }
    }

    public void setRequestInProgress(boolean requestInProgress) {
        mRequestInProgress = requestInProgress;
        setSupportProgressBarIndeterminateVisibility(requestInProgress);
    }

    public boolean isRequestInProgress() {
        return mRequestInProgress;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
        Log.i(TAG, "onCreateLoader");
        switch (id) {
            case Consts.LOADER_REDDIT:
                return new CursorLoader(this, RedditContract.RedditData.CONTENT_URI, null, null, null, RedditContract.RedditData.DEFAULT_SORT);

            case Consts.LOADER_POSTS:
                return new CursorLoader(this, RedditContract.Posts.CONTENT_URI, null, null, null, RedditContract.Posts.DEFAULT_SORT);
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.i(TAG, "onLoadFinished");
        switch (loader.getId()) {
            case Consts.LOADER_REDDIT:
                Log.i(TAG, "onLoadFinished REDDIT_LOADER, cursor has " + cursor.getCount() + " rows");
                Log.i(TAG, "After column = " + cursor.getColumnIndex(RedditContract.RedditData.AFTER));
                Log.i(TAG, "Before column = " + cursor.getColumnIndex(RedditContract.RedditData.BEFORE));

                if (cursor != null && cursor.moveToFirst()) {
                    mAfter = cursor.getString(cursor.getColumnIndex(RedditContract.RedditData.AFTER));
                    mBefore = cursor.getString(cursor.getColumnIndex(RedditContract.RedditData.BEFORE));
                }
                break;

            case Consts.LOADER_POSTS:
                Log.i(TAG, "onLoadFinished POST_LOADER, cursor has " + cursor.getCount() + " rows");
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
        Log.i(TAG, "onLoaderReset");
        getAdapter().swapCursor(null);
    }

}
