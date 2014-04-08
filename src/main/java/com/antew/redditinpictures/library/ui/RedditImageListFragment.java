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
import com.antew.redditinpictures.library.dialog.SaveImageDialogFragment;
import com.antew.redditinpictures.library.model.Age;
import com.antew.redditinpictures.library.model.Category;
import com.antew.redditinpictures.library.event.ForcePostRefreshEvent;
import com.antew.redditinpictures.library.preferences.SharedPreferencesHelper;
import com.antew.redditinpictures.library.model.reddit.PostData;
import com.antew.redditinpictures.library.util.StringUtil;
import com.antew.redditinpictures.library.util.Strings;
import com.antew.redditinpictures.pro.R;
import com.antew.redditinpictures.library.database.QueryCriteria;
import com.antew.redditinpictures.library.database.RedditContract;
import com.fortysevendeg.swipelistview.SwipeListView;
import com.fortysevendeg.swipelistview.SwipeListViewListener;
import com.squareup.otto.Subscribe;

public class RedditImageListFragment extends RedditImageAdapterViewFragment<ListView, ImageListCursorAdapter>
    implements SwipeListViewListener, ImageListCursorAdapter.ImageListItemMenuActionListener {
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
        args.putString(Constants.Extra.EXTRA_SUBREDDIT, subreddit);
        args.putString(Constants.Extra.EXTRA_CATEGORY, category.toString());
        // Age is null when we're sorting as 'New' or 'Rising'
        args.putString(Constants.Extra.EXTRA_AGE, Strings.toString(age));
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
        openImageAtPosition(position);
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

    /**
     * Request to view the given image.
     *
     * @param postData
     *     The PostData of the image to open
     * @param position
     */
    @Override
    public void viewImage(PostData postData, int position) {
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
        SaveImageDialogFragment saveImageDialog = SaveImageDialogFragment.newInstance(StringUtil.sanitizeFileName(postData.getTitle()));
        saveImageDialog.show(getFragmentManager(), Constants.Dialog.DIALOG_GET_FILENAME);
    }

    /**
     * Request to share the given image.
     *
     * @param postData
     *     The PostData of the image.
     */
    @Override
    public void shareImage(PostData postData) {
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
    public void reportImage(PostData postData) {
        Toast.makeText(getActivity(), "Reporting Images Isn't Implemented Yet. :(", Toast.LENGTH_LONG).show();
    }
}
