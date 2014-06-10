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
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.GridView;
import butterknife.InjectView;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.adapter.ImageCursorAdapter;
import com.antew.redditinpictures.library.database.QueryCriteria;
import com.antew.redditinpictures.library.database.RedditContract;
import com.antew.redditinpictures.library.event.ForcePostRefreshEvent;
import com.antew.redditinpictures.library.event.RequestCompletedEvent;
import com.antew.redditinpictures.library.event.RequestInProgressEvent;
import com.antew.redditinpictures.library.image.ThumbnailInfo;
import com.antew.redditinpictures.library.model.Age;
import com.antew.redditinpictures.library.model.Category;
import com.antew.redditinpictures.pro.R;
import com.squareup.otto.Subscribe;

public class RedditImageGridFragment extends RedditImageAdapterViewFragment<GridView, ImageCursorAdapter> {
    //9 is a good number, it's not as great as 8 or as majestic as 42 but it is indeed the product of 3 3s which is okay...I guess.
    private static final int                          POST_LOAD_OFFSET          = 9;
    private              AbsListView.OnScrollListener mGridViewOnScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView absListView, int scrollState) {
            // TODO: Enable this with Picasso https://github.com/square/picasso/issues/248
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                //mImageFetcher.setPauseWork(true);
            } else {
                //mImageFetcher.setPauseWork(false);
            }
        }

        @Override
        public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            // if we're are approaching the bottom of the gridview, load more data
            if (!mRequestInProgress && firstVisibleItem + visibleItemCount >= totalItemCount - POST_LOAD_OFFSET && totalItemCount > 0) {
                fetchAdditionalImagesFromReddit();
            }
        }
    };
    private static final QueryCriteria                mQueryCriteria            = new QueryCriteria(
        RedditContract.Posts.GRIDVIEW_PROJECTION, RedditContract.Posts.DEFAULT_SORT);
    @InjectView(R.id.gridView)
    protected GridView      mGridView;
    private   ThumbnailInfo mThumbnailInfo;

    public static Fragment newInstance(String subreddit, Category category, Age age) {
        final Fragment f = new RedditImageGridFragment();

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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mThumbnailInfo = ThumbnailInfo.getThumbnailInfo(getResources());
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);
        mGridView.setOnScrollListener(mGridViewOnScrollListener);
        mGridView.getViewTreeObserver().addOnGlobalLayoutListener(getGridGlobalLayoutListener(mGridView));
    }

    /**
     * This listener is used to get the final width of the GridView and then calculate the number
     * of
     * columns and the width of each column. The width of each column is variable as the GridView
     * has stretchMode=columnWidth. The column width is used to set the height of each view so we
     * get nice square thumbnails.
     *
     * @return The global layout listener
     */
    private ViewTreeObserver.OnGlobalLayoutListener getGridGlobalLayoutListener(final GridView gridView) {
        return new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mAdapter != null && mAdapter.getNumColumns() == 0) {
                    final int numColumns = (int) Math.floor(gridView.getWidth() / (mThumbnailInfo.getSize() + mThumbnailInfo.getSpacing()));
                    if (numColumns > 0) {
                        final int columnWidth = (gridView.getWidth() / numColumns) - mThumbnailInfo.getSpacing();
                        mAdapter.setNumColumns(numColumns);
                        mAdapter.setItemHeight(columnWidth);
                    }
                }
            }
        };
    }

    /**
     * If we're forcing a refresh from Reddit we want
     * to discard the old posts so that the user has
     * a better indication we are fetching posts anew.
     *
     * @param event
     */
    @Override @Subscribe public void handleForcePostRefreshEvent(ForcePostRefreshEvent event) {
        super.handleForcePostRefreshEvent(event);
    }

    @Override protected int getLayoutId() {
        return R.layout.image_grid_fragment;
    }

    @Override protected GridView getAdapterView() {
        return mGridView;
    }

    @Override protected ImageCursorAdapter getNewAdapter() {
        return new ImageCursorAdapter(getActivity());
    }

    @Override protected QueryCriteria getPostsQueryCriteria() {
        return mQueryCriteria;
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mGridView != null) {
            outState.putInt(Constants.Extra.EXTRA_VISIBLE_POSITION, mGridView.getFirstVisiblePosition());

            View topView = mGridView.getChildAt(0);
            outState.putInt(Constants.Extra.EXTRA_TOP_OFFSET, topView != null ? topView.getTop() : 0);
        }
    }

    @Override
    public void onPostsLoaded() {
        if (mVisiblePosition != null) {
            mGridView.setSelection(mVisiblePosition);
            mVisiblePosition = null;
            mTopOffset = null;
        }
    }
}
