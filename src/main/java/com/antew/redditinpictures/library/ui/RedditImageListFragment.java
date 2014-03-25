package com.antew.redditinpictures.library.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.AbsListView;
import android.widget.ListView;
import butterknife.InjectView;
import com.antew.redditinpictures.library.adapter.ImageListCursorAdapter;
import com.antew.redditinpictures.library.enums.Age;
import com.antew.redditinpictures.library.enums.Category;
import com.antew.redditinpictures.library.ui.base.BaseImageFragment;
import com.antew.redditinpictures.library.utils.Consts;
import com.antew.redditinpictures.pro.R;
import com.antew.redditinpictures.sqlite.QueryCriteria;
import com.antew.redditinpictures.sqlite.RedditContract;

public class RedditImageListFragment extends BaseImageFragment<ListView, ImageListCursorAdapter> {
    //8 is a good number, the kind of number that you could say take home to your parents and not be worried about what they might think about it.
    private static final int POST_LOAD_OFFSET = 8;
    @InjectView(R.id.image_list)
    protected ListView mImageListView;
    private static final QueryCriteria mQueryCriteria =
        new QueryCriteria(RedditContract.Posts.LISTVIEW_PROJECTION,
            RedditContract.Posts.DEFAULT_SORT);

    private AbsListView.OnScrollListener mListScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView absListView, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {
            // if we're are approaching the bottom of the listview, load more data
            boolean lastItemIsVisible =
                (firstVisibleItem + visibleItemCount) >= totalItemCount - POST_LOAD_OFFSET;
            if (!isRequestInProgress() && totalItemCount > 0 && lastItemIsVisible) {
                fetchImagesFromReddit(false);
            }
        }
    };

    public static Fragment newInstance(String subreddit, Category category, Age age) {
        final Fragment f = new RedditImageListFragment();

        final Bundle args = new Bundle();
        args.putString(Consts.EXTRA_SELECTED_SUBREDDIT, subreddit);
        args.putString(Consts.EXTRA_CATEGORY, category.toString());
        args.putString(Consts.EXTRA_AGE, age.toString());
        f.setArguments(args);

        return f;
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mImageListView.setAdapter(mAdapter);
        mImageListView.setOnScrollListener(mListScrollListener);
        mImageListView.setOnItemClickListener(this);
    }

    @Override protected int getLayoutId() {
        return R.layout.image_list_fragment;
    }

    @Override protected ListView getAdapterView() {
        return mImageListView;
    }

    @Override protected ImageListCursorAdapter getNewAdapter() {
        return new ImageListCursorAdapter(getActivity());
    }

    @Override protected QueryCriteria getPostsQueryCriteria() {
        return mQueryCriteria;
    }
}
