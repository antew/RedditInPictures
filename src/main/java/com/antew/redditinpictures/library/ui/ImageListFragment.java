package com.antew.redditinpictures.library.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.Loader;
import android.widget.AbsListView;
import android.widget.ListView;
import butterknife.InjectView;
import com.antew.redditinpictures.library.adapter.ImageListCursorAdapter;
import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.pro.R;
import com.antew.redditinpictures.sqlite.QueryCriteria;
import com.antew.redditinpictures.sqlite.RedditContract;

public class ImageListFragment extends ImageFragment<ListView, ImageListCursorAdapter> {
    public static final String TAG = "ImageListFragment";
    private static final QueryCriteria mQueryCriteria =
        new QueryCriteria(RedditContract.Posts.LISTVIEW_PROJECTION,
            RedditContract.Posts.DEFAULT_SORT);
    AbsListView.OnScrollListener mListScrollListener;

    @InjectView(R.id.image_list)
    protected ListView mImageListView;

    protected Parcelable mListViewSavedInstanceState;
    protected boolean fetchMoreImages = false;

    /**
     * Empty constructor as per the Fragment documentation
     */
    public ImageListFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mImageListView.setAdapter(mAdapter);
        mImageListView.setOnScrollListener(getListViewOnScrollListener());
        mImageListView.setOnItemClickListener(this);
    }

    private AbsListView.OnScrollListener getListViewOnScrollListener() {
        if (mListScrollListener == null) {
            mListScrollListener = new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView absListView, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView absListView, int firstVisibleItem,
                    int visibleItemCount, int totalItemCount) {
                    // if we're at the bottom of the listview, load more data
                    boolean lastItemIsVisible =
                        (firstVisibleItem + visibleItemCount) >= totalItemCount;
                    if (!isRequestInProgress() && totalItemCount > 0 && lastItemIsVisible) {
                        Log.i(TAG, "Reached last visible item in GridView, fetching more posts");
                        fetchMoreImages = true;
                        fetchImagesFromReddit(false);
                    }
                }
            };
        }

        return mListScrollListener;
    }

    @Override public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Save scroll position, only if we are just fetching more images.
        if (fetchMoreImages) {
            mListViewSavedInstanceState = mImageListView.onSaveInstanceState();
        }
        super.onLoadFinished(loader, cursor);
        // Restore scroll position, only if we are just fetching more images.
        if (fetchMoreImages) {
            mImageListView.onRestoreInstanceState(mListViewSavedInstanceState);
            fetchMoreImages = false;
        }
    }

    public Class<? extends ImageDetailActivity> getImageDetailActivityClass() {
        return ImageDetailActivity.class;
    }

    @Override protected ListView getAdapterView() {
        return mImageListView;
    }

    @Override protected QueryCriteria getPostsQueryCriteria() {
        return mQueryCriteria;
    }

    @Override public int getLayoutId() {
        return R.layout.image_list_fragment;
    }

    @Override protected ImageListCursorAdapter getNewAdapter() {
        return new ImageListCursorAdapter(getActivity());
    }
}
