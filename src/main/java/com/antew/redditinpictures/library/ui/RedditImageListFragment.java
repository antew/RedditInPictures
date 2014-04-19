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
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;
import butterknife.InjectView;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.adapter.ImageListCursorAdapter;
import com.antew.redditinpictures.library.database.QueryCriteria;
import com.antew.redditinpictures.library.database.RedditContract;
import com.antew.redditinpictures.library.event.ForcePostRefreshEvent;
import com.antew.redditinpictures.library.event.RequestCompletedEvent;
import com.antew.redditinpictures.library.event.RequestInProgressEvent;
import com.antew.redditinpictures.library.event.SaveImageEvent;
import com.antew.redditinpictures.library.model.Age;
import com.antew.redditinpictures.library.model.Category;
import com.antew.redditinpictures.library.model.reddit.PostData;
import com.antew.redditinpictures.library.preferences.SharedPreferencesHelper;
import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.library.widget.SwipeListView;
import com.antew.redditinpictures.pro.R;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.gson.Gson;
import com.squareup.otto.Subscribe;

public class RedditImageListFragment extends RedditImageAdapterViewFragment<ListView, ImageListCursorAdapter>
    implements ImageListCursorAdapter.ImageListItemMenuActionListener {
    //8 is a good number, the kind of number that you could say take home to your parents and not be worried about what they might think about it.
    private static final int                          POST_LOAD_OFFSET    = 8;
    private              AbsListView.OnScrollListener mListScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView absListView, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            // if we're are approaching the bottom of the listview, load more data.
            if (!mRequestInProgress && firstVisibleItem + visibleItemCount >= totalItemCount - POST_LOAD_OFFSET && totalItemCount > 0) {
                fetchAdditionalImagesFromReddit();
            }
        }
    };
    private static final QueryCriteria                mQueryCriteria      = new QueryCriteria(RedditContract.Posts.LISTVIEW_PROJECTION,
                                                                                              RedditContract.Posts.DEFAULT_SORT);
    @InjectView(R.id.image_list)
    protected SwipeListView mImageListView;

    public static Fragment newInstance(String subreddit, Category category, Age age) {
        final Fragment f = new RedditImageListFragment();

        final Bundle args = new Bundle();
        args.putString(Constants.Extra.EXTRA_SUBREDDIT, subreddit);
        if (category != null) {
            args.putString(Constants.Extra.EXTRA_CATEGORY, category.getName());
        }
        if (age != null) {
            args.putString(Constants.Extra.EXTRA_AGE, age.getAge());
        }
        f.setArguments(args);

        return f;
    }

    /**
     * If we're forcing a refresh from Reddit we want
     * to discard the old posts so that the user has
     * a better indication we are fetching posts anew.
     *
     * @param event
     */
    @Override
    @Subscribe
    public void handleForcePostRefreshEvent(ForcePostRefreshEvent event) {
        super.handleForcePostRefreshEvent(event);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mImageListView.setAdapter(mAdapter);
        mImageListView.setOnScrollListener(mListScrollListener);
    }

    private void openImageAtPosition(int position) {
        final Intent i = new Intent(getActivity(), getImageDetailActivityClass());
        Bundle b = new Bundle();
        b.putString(Constants.Extra.EXTRA_SUBREDDIT, mCurrentSubreddit);
        b.putString(Constants.Extra.EXTRA_CATEGORY, mCategory.name());
        b.putString(Constants.Extra.EXTRA_AGE, mAge.name());
        i.putExtra(Constants.Extra.EXTRA_IMAGE, position);
        i.putExtras(b);

        startActivity(i);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.image_list_fragment;
    }

    @Override
    protected ListView getAdapterView() {
        return mImageListView;
    }

    @Override
    protected ImageListCursorAdapter getNewAdapter() {
        return new ImageListCursorAdapter(getActivity(), this);
    }

    @Override
    protected QueryCriteria getPostsQueryCriteria() {
        return mQueryCriteria;
    }

    /**
     * Request to view the given image.
     *
     * @param postData
     *     The PostData of the image to open
     * @param position
     */
    @Override
    public void viewImage(PostData postData, int position) {
        EasyTracker.getInstance(getActivity())
                   .send(MapBuilder.createEvent(Constants.Analytics.Category.POST_MENU_ACTION, Constants.Analytics.Action.OPEN_POST,
                                                mCurrentSubreddit, null).build()
                        );
        openImageAtPosition(position);
    }

    /**
     * Request to save the given image.
     *
     * @param postData
     *     The PostData of the image.
     */
    @Override
    public void saveImage(PostData postData) {
        EasyTracker.getInstance(getActivity())
                   .send(MapBuilder.createEvent(Constants.Analytics.Category.POST_MENU_ACTION, Constants.Analytics.Action.SAVE_POST,
                                                mCurrentSubreddit, null).build()
                        );
        mBus.post(new SaveImageEvent(postData));
    }

    /**
     * Request to share the given image.
     *
     * @param postData
     *     The PostData of the image.
     */
    @Override
    public void shareImage(PostData postData) {
        EasyTracker.getInstance(getActivity())
                   .send(MapBuilder.createEvent(Constants.Analytics.Category.POST_MENU_ACTION, Constants.Analytics.Action.SHARE_POST,
                                                mCurrentSubreddit, null).build()
                        );
        String subject = getString(R.string.check_out_this_image);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, subject + " " + postData.getUrl());
        startActivity(Intent.createChooser(intent, getString(R.string.share_using_)));
    }

    /**
     * Request to open the given image in an external application.
     *
     * @param postData
     *     The PostData of the image.
     */
    @Override
    public void openPostExternal(PostData postData) {
        EasyTracker.getInstance(getActivity())
                   .send(
                       MapBuilder.createEvent(Constants.Analytics.Category.POST_MENU_ACTION, Constants.Analytics.Action.OPEN_POST_EXTERNAL,
                                              mCurrentSubreddit, null).build()
                        );
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
            postData.getFullPermalink(SharedPreferencesHelper.getUseMobileInterface(getActivity()))));
        startActivity(browserIntent);
    }

    /**
     * Request to report the given image.
     *
     * @param postData
     *     The PostData of the image.
     */
    @Override
    public void reportImage(final PostData postData) {
        EasyTracker.getInstance(getActivity())
                   .send(MapBuilder.createEvent(Constants.Analytics.Category.POST_MENU_ACTION, Constants.Analytics.Action.REPORT_POST,
                                                mCurrentSubreddit, null).build()
                        );
        new Thread(new Runnable() {
            @Override
            public void run() {
                RedditService.reportPost(getActivity(), postData);
            }
        }).start();

        Toast.makeText(getActivity(), R.string.image_display_issue_reported, Toast.LENGTH_LONG).show();
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
}
