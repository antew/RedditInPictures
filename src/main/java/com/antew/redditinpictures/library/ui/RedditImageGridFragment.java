package com.antew.redditinpictures.library.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.GridView;
import butterknife.InjectView;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.adapter.ImageCursorAdapter;
import com.antew.redditinpictures.library.enums.Age;
import com.antew.redditinpictures.library.enums.Category;
import com.antew.redditinpictures.library.event.ForcePostRefreshEvent;
import com.antew.redditinpictures.library.image.ThumbnailInfo;
import com.antew.redditinpictures.pro.R;
import com.antew.redditinpictures.sqlite.QueryCriteria;
import com.antew.redditinpictures.sqlite.RedditContract;
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
            if (firstVisibleItem + visibleItemCount >= totalItemCount - POST_LOAD_OFFSET && totalItemCount > 0) {
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
        args.putString(Constants.EXTRA_SELECTED_SUBREDDIT, subreddit);
        args.putString(Constants.EXTRA_CATEGORY, category.toString());
        // Age is null when we're sorting as 'New' or 'Rising'
        args.putString(Constants.EXTRA_AGE, age == null ? null : age.toString());
        f.setArguments(args);

        return f;
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
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
                if (mAdapter.getNumColumns() == 0) {
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
    @Override @Subscribe protected void handleForcePostRefreshEvent(ForcePostRefreshEvent event) {
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
}
