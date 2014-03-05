package com.antew.redditinpictures.library.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ProgressBar;
import butterknife.InjectView;
import com.antew.redditinpictures.library.adapter.ImageCursorAdapter;
import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.pro.R;
import com.antew.redditinpictures.sqlite.QueryCriteria;
import com.antew.redditinpictures.sqlite.RedditContract;

public class ImageGridFragment extends ImageFragment<GridView, ImageCursorAdapter> {
    public static final  String        TAG            = "ImageGridFragment";
    private static final QueryCriteria mQueryCriteria =
        new QueryCriteria(RedditContract.Posts.GRIDVIEW_PROJECTION, RedditContract.Posts.DEFAULT_SORT);

    @InjectView(R.id.gridView)
    protected GridView mGridView;

    @InjectView(R.id.progress)
    protected ProgressBar mProgress;

    /**
     * Empty constructor as per the Fragment documentation
     */
    public ImageGridFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        setUpGridView(mGridView);
        return v;
    }

    public void setUpGridView(GridView gridView) {
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(this);
        gridView.setOnScrollListener(getGridViewOnScrollListener());
        gridView.getViewTreeObserver().addOnGlobalLayoutListener(getGridGlobalLayoutListener(gridView));
    }

    @Override protected void setRequestInProgress(boolean inProgress) {
        super.setRequestInProgress(inProgress);
        if (inProgress) {
            mProgress.setVisibility(View.VISIBLE);
        } else {
            mProgress.setVisibility(View.GONE);
        }
    }

    private AbsListView.OnScrollListener getGridViewOnScrollListener() {
        return new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                // Pause fetcher to ensure smoother scrolling when flinging
                // TODO: Enable this with Picasso https://github.com/square/picasso/issues/248
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    //mImageFetcher.setPauseWork(true);
                } else {
                    //mImageFetcher.setPauseWork(false);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount,
                int totalItemCount) {
                // if we're are approaching the end of the listview, load more data
                boolean lastItemIsVisible = (firstVisibleItem + visibleItemCount) >= totalItemCount - 5;
                if (!isRequestInProgress() && totalItemCount > 0 && lastItemIsVisible) {
                    Log.i(TAG, "Reached last visible item in GridView, fetching more posts");
                    fetchImagesFromReddit(false);
                }
            }
        };
    }

    /**
     * This listener is used to get the final width of the GridView and then calculate the number of
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
                    final int numColumns = (int) Math.floor(
                        gridView.getWidth() / (mThumbnailInfo.getSize() + mThumbnailInfo.getSpacing()));
                    if (numColumns > 0) {
                        final int columnWidth = (gridView.getWidth() / numColumns) - mThumbnailInfo.getSpacing();
                        mAdapter.setNumColumns(numColumns);
                        mAdapter.setItemHeight(columnWidth);
                    }
                }
            }
        };
    }

    @Override
    public int getFirstVisiblePosition() {
        if (mGridView == null) return 0;

        return mGridView.getFirstVisiblePosition();
    }

    @Override
    public void setFirstVisiblePosition(final int firstVisiblePosition) {
        mGridView.setSelection(firstVisiblePosition);
    }

    @Override public int getLayoutId() {
        return R.layout.image_grid_fragment;
    }

    @Override protected ImageCursorAdapter getNewAdapter() {
        return new ImageCursorAdapter(getActivity());
    }

    public Class<? extends ImageDetailActivity> getImageDetailActivityClass() {
        return ImageDetailActivity.class;
    }

    @Override protected GridView getAdapterView() {
        return mGridView;
    }

    @Override protected QueryCriteria getPostsQueryCriteria() {
        return mQueryCriteria;
    }
}
