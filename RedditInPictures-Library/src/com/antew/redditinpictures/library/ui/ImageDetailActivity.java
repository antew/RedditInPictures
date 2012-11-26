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

import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.antew.redditinpictures.library.R;
import com.antew.redditinpictures.library.adapter.ImagePagerAdapter;
import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.preferences.SharedPreferencesHelper;
import com.antew.redditinpictures.library.reddit.RedditApi;
import com.antew.redditinpictures.library.reddit.RedditApi.PostData;
import com.antew.redditinpictures.library.reddit.RedditApiManager;
import com.antew.redditinpictures.library.reddit.RedditApiManager.Vote;
import com.antew.redditinpictures.library.reddit.RedditUrl;
import com.antew.redditinpictures.library.service.RESTResponderFragment;
import com.antew.redditinpictures.library.service.RESTService;
import com.antew.redditinpictures.library.utils.Consts;
import com.antew.redditinpictures.library.utils.StringUtil;
import com.google.gson.JsonSyntaxException;

public class ImageDetailActivity extends ImageViewerActivity {

    public static final String TAG = "ImageDetailActivity";
    protected MenuItem         mUpvoteMenuItem;
    protected MenuItem         mDownvoteMenuItem;
    protected RedditUrl        mRedditUrl;
    protected RedditApi        mRedditApi;
    protected boolean          mRequestInProgress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        displayVote();

        // Put the current page / total pages text in the ActionBar
        updateDisplay(mPager.getCurrentItem());
    }

    @SuppressWarnings("unchecked")
    protected List<PostData> getImages() {
        return (List<PostData>) mImages;
    }

    private ImagePagerAdapter getAdapter() {
        return (ImagePagerAdapter) mAdapter;
    }

    public FragmentStatePagerAdapter getPagerAdapter() {
        return new ImagePagerAdapter(getSupportFragmentManager(), getImages());
    }

    public void getExtras() {
        Intent i = getIntent();
        if (i.hasExtra(Consts.EXTRA_ENTRIES))
            mImages = getIntent().getParcelableArrayListExtra(Consts.EXTRA_ENTRIES);

        if (i.hasExtra(Consts.EXTRA_REDDIT_URL))
            mRedditUrl = getIntent().getParcelableExtra(Consts.EXTRA_REDDIT_URL);

        if (i.hasExtra(Consts.EXTRA_REDDIT_API))
            mRedditApi = getIntent().getParcelableExtra(Consts.EXTRA_REDDIT_API);
    }

    /**
     * Update the vote
     */
    protected void updateDisplay(int position) {
        PostData p = getAdapter().getPost(position);
        displayVote(p.getVote());

        getSupportActionBar().setTitle(++position + "/" + getAdapter().getCount() + " - " + getString(R.string.reddit_in_pictures));
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
        outState.putParcelable(Consts.EXTRA_REDDIT_API, mRedditApi);
        outState.putParcelable(Consts.EXTRA_REDDIT_URL, mRedditUrl);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(Consts.EXTRA_REDDIT_API))
            mRedditApi = savedInstanceState.getParcelable(Consts.EXTRA_REDDIT_API);

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
        if (!RedditApiManager.isLoggedIn()) {
            Toast.makeText(this, R.string.you_must_be_logged_in_to_vote, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Consts.BROADCAST_UPDATE_SCORE);
        intent.putExtra(Consts.EXTRA_PERMALINK, p.getPermalink());

        switch (whichVoteButton) {
            case DOWN:
                switch (p.getVote()) {
                    case DOWN:
                        RedditApiManager.vote(p.getName(), p.getSubreddit(), Vote.NEUTRAL, getApplicationContext());
                        item.setIcon(R.drawable.ic_action_downvote);
                        p.setVote(Vote.NEUTRAL);
                        p.setScore(p.getScore() + 1);
                        break;

                    case NEUTRAL:
                    case UP:
                        RedditApiManager.vote(p.getName(), p.getSubreddit(), Vote.DOWN, getApplicationContext());
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
                        RedditApiManager.vote(p.getName(), p.getSubreddit(), Vote.UP, getApplicationContext());
                        item.setIcon(R.drawable.ic_action_upvote_highlighted);
                        p.setVote(Vote.UP);
                        p.setScore(p.getScore() + 1);
                        mDownvoteMenuItem.setIcon(R.drawable.ic_action_downvote);
                        break;

                    case UP:
                        RedditApiManager.vote(p.getName(), p.getSubreddit(), Vote.NEUTRAL, getApplicationContext());
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

        if (!RedditApiManager.isLoggedIn()) {
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

        //@formatter:off
        String after = mRedditApi != null 
                && mRedditApi.getData() != null 
                && mRedditApi.getData().getAfter() != null ? mRedditApi.getData().getAfter() : null;
        //@formatter:on

        if (!isRequestInProgress() && after != null && mRedditUrl != null) {
            //@formatter:off
            RedditUrl.Builder builder = new RedditUrl.Builder(mRedditUrl.subreddit)
                                                     .age(mRedditUrl.age)
                                                     .category(mRedditUrl.category)
                                                     .count(50)
                                                     .isLoggedIn(mRedditUrl.isLoggedIn)
                                                     .after(after);


            RedditDataFragment redditFragment = RedditDataFragment.newInstance(builder.build().getUrl());
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(redditFragment, "RedditDataFragment");
            ft.commit();
            //@formatter:on
        } else {
            if (after == null)
                Log.i(TAG, "reachedLastPage, after was null");

            if (mRedditUrl == null)
                Log.i(TAG, "reachedLastPage, mRedditUrl was null");

            if (mRequestInProgress)
                Log.i(TAG, "reachedLastPage() - Request already in progress, ignoring");
        }
    }
    
    public void addImages(RedditApi api) {
        boolean showNsfwImages = SharedPreferencesHelper.getShowNsfwImages(this);
        List<PostData> images = RedditApiManager.filterPosts(api, showNsfwImages);
        
        // We want to keep the previous posts in mRedditApi and add new ones
        mRedditApi.getData().addChildren(RedditApiManager.filterChildren(api, showNsfwImages));
        mRedditApi.getData().setAfter(api.getData().getAfter());
        mRedditApi.getData().setBefore(api.getData().getBefore());

        getImages().addAll(images);
        updateDisplay(mPager.getCurrentItem());
    }
    
    public void setRequestInProgress(boolean requestInProgress) {
        mRequestInProgress = requestInProgress;
        setSupportProgressBarIndeterminateVisibility(requestInProgress);
    }
    
    public boolean isRequestInProgress() {
        return mRequestInProgress;
    }
    
    /**
     * 
     * We use a Fragment with setRetainInstance set to true so that we don't lose the request on orientation change
     * 
     */
    public static final class RedditDataFragment extends RESTResponderFragment {
        public static final String TAG = RedditDataFragment.class.getSimpleName();
        public static final String URL = "url";
        
        public static RedditDataFragment newInstance(String url) {
            RedditDataFragment frag = new RedditDataFragment();
            Bundle args = new Bundle();
            args.putString(URL, url);
            frag.setArguments(args);
            
            return frag;
        }
        
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            String url = getArguments().getString(URL);
            makeRequest(url);
        }
        
        public void makeRequest(String url) {
            Intent intent = new Intent(getActivity(), RESTService.class);
            intent.setData(Uri.parse(url));
            intent.putExtra(RESTService.EXTRA_RESULT_RECEIVER, getResultReceiver());
            
            ImageDetailActivity activity = (ImageDetailActivity) getActivity();
            if (activity != null) {
                activity.startService(intent);
                activity.setRequestInProgress(true);
            }
        }
        
        @Override
        public void onRESTResult(int code, String result) {
            ImageDetailActivity activity = (ImageDetailActivity) getActivity();
            if (activity != null) {
                activity.setRequestInProgress(false);

                // If the request was successful
                if (code == 200 && result != null) {

                    try {
                        RedditApi redditApi = RedditApi.getGson().fromJson(result, RedditApi.class);
                        activity.addImages(redditApi);

                    } catch (JsonSyntaxException e) {
                        Log.e(TAG, "onReceiveResult - JsonSyntaxException while parsing json!", e);
                        Toast.makeText(activity, getString(R.string.error_parsing_reddit_data), Toast.LENGTH_SHORT).show();
                        return;
                    } catch (IllegalStateException e) {
                        Log.e(TAG, "onReceiveResult - IllegalStateException while parsing json!", e);
                        Toast.makeText(activity, getString(R.string.error_parsing_reddit_data), Toast.LENGTH_SHORT).show();
                        return;
                    }

                } else {
                    Toast.makeText(activity, "Failed to get data from Reddit", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.i(TAG, "Activity was null, unable to add posts!");
            }
        }
        
    }

}
