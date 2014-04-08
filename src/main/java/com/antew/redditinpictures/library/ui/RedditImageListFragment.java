package com.antew.redditinpictures.library.ui;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.AbsListView;
import android.widget.ListView;
import butterknife.InjectView;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.adapter.ImageListCursorAdapter;
import com.antew.redditinpictures.library.enums.Age;
import com.antew.redditinpictures.library.enums.Category;
import com.antew.redditinpictures.library.event.ForcePostRefreshEvent;
import com.antew.redditinpictures.library.utils.Util;
import com.antew.redditinpictures.pro.R;
import com.antew.redditinpictures.sqlite.QueryCriteria;
import com.antew.redditinpictures.sqlite.RedditContract;
import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;
import com.fortysevendeg.swipelistview.SwipeListViewListener;
import com.squareup.otto.Subscribe;

public class RedditImageListFragment extends RedditImageAdapterViewFragment<ListView, ImageListCursorAdapter> implements SwipeListViewListener {
    //8 is a good number, the kind of number that you could say take home to your parents and not be worried about what they might think about it.
    private static final int                          POST_LOAD_OFFSET    = 8;
    private              AbsListView.OnScrollListener mListScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView absListView, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            // if we're are approaching the bottom of the listview, load more data.
            if (firstVisibleItem + visibleItemCount >= totalItemCount - POST_LOAD_OFFSET && totalItemCount > 0) {
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
        args.putString(Constants.EXTRA_SELECTED_SUBREDDIT, subreddit);
        args.putString(Constants.EXTRA_CATEGORY, category.toString());
        // Age is null when we're sorting as 'New' or 'Rising'
        args.putString(Constants.EXTRA_AGE, age == null ? null : age.toString());
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
    protected void handleForcePostRefreshEvent(ForcePostRefreshEvent event) {
        super.handleForcePostRefreshEvent(event);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mImageListView.setAdapter(mAdapter);
        mImageListView.setOnScrollListener(mListScrollListener);
        mImageListView.setSwipeListViewListener(this);
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
        return new ImageListCursorAdapter(getActivity());
    }

    @Override
    protected QueryCriteria getPostsQueryCriteria() {
        return mQueryCriteria;
    }

    @Override
    public void onOpened(int position, boolean toRight) {
    }

    @Override
    public void onClosed(int position, boolean fromRight) {
    }

    @Override
    public void onListChanged() {
    }

    @Override
    public void onMove(int position, float x) {
    }

    @Override
    public void onStartOpen(int position, int action, boolean right) {
    }

    @Override
    public void onStartClose(int position, boolean right) {
    }

    @Override
    public void onClickFrontView(int position) {
        final Intent i = new Intent(getActivity(), getImageDetailActivityClass());
        Bundle b = new Bundle();
        b.putString(Constants.EXTRA_SELECTED_SUBREDDIT, mCurrentSubreddit);
        b.putString(Constants.EXTRA_CATEGORY, mCategory.name());
        b.putString(Constants.EXTRA_AGE, mAge.name());
        i.putExtra(Constants.EXTRA_IMAGE, position);
        i.putExtras(b);

        startActivity(i);
    }

    @Override
    public void onClickBackView(int position) {
    }

    @Override
    public void onDismiss(int[] reverseSortedPositions) {
    }

    @Override
    public int onChangeSwipeMode(int position) {
        return SwipeListView.SWIPE_MODE_DEFAULT;
    }

    @Override
    public void onChoiceChanged(int position, boolean selected) {
    }

    @Override
    public void onChoiceStarted() {
    }

    @Override
    public void onChoiceEnded() {
    }

    @Override
    public void onFirstListItem() {
    }

    @Override
    public void onLastListItem() {
    }
}
