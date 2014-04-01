package com.antew.redditinpictures.library.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.antew.redditinpictures.library.image.ThumbnailInfo;
import com.antew.redditinpictures.library.interfaces.RedditDataProvider;
import com.antew.redditinpictures.library.interfaces.ScrollPosReadable;
import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.preferences.SharedPreferencesHelper;
import com.antew.redditinpictures.library.reddit.RedditLoginInformation;
import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.library.service.RequestCode;
import com.antew.redditinpictures.library.ui.base.BaseFragment;
import com.antew.redditinpictures.library.utils.Constants;
import com.antew.redditinpictures.library.utils.Ln;
import com.antew.redditinpictures.library.utils.Util;
import com.antew.redditinpictures.pro.R;
import com.antew.redditinpictures.sqlite.QueryCriteria;
import com.antew.redditinpictures.sqlite.RedditContract;
import com.squareup.otto.Bus;
import javax.inject.Inject;

/**
 * Fragment with convenience methods for displaying images
 *
 * @param <T>
 *     The type of view the fragment is using, e.g. GridView, ListView
 * @param <V>
 *     The type of the cursor adapter backing the view
 */
public abstract class ImageFragment<T extends AdapterView, V extends CursorAdapter> extends BaseFragment
    implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor>, ScrollPosReadable {
    public static final  String TAG             = "ImageFragment";
    private static final String IMAGE_CACHE_DIR = "thumbs";
    protected V                  mAdapter;
    protected ThumbnailInfo      mThumbnailInfo;
    protected RedditDataProvider mRedditDataProvider;
    @Inject
    protected Bus mBus;
    @InjectView(R.id.no_images)
    protected TextView mNoImages;
    private   String             mAfter;
    private BroadcastReceiver mSubredditSelected = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "New subreddit selected, fetching new posts");
            mAdapter.swapCursor(null);
            mAfter = null;
            fetchImagesFromReddit(true);
        }
    };
    private   MenuItem           mLoginMenuItem;
    private boolean mRequestInProgress = false;
    private boolean mFullRefresh       = true;
    private BroadcastReceiver mHttpRequestComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle args = intent.getBundleExtra(RedditService.EXTRA_BUNDLE);
            RequestCode requestCode = (RequestCode) intent.getSerializableExtra(RedditService.EXTRA_REQUEST_CODE);

            Bundle passThru = intent.getBundleExtra(RedditService.EXTRA_PASS_THROUGH);
            int statusCode = args.getInt(RedditService.EXTRA_STATUS_CODE);
            boolean replaceAll = args.getBoolean(RedditService.EXTRA_REPLACE_ALL);
            String json = args.getString(RedditService.REST_RESULT);
        }
    };

    private BroadcastReceiver mRemoveNsfwImages = new BroadcastReceiver() {
        //@formatter:off
        @Override
        public void onReceive(Context context, Intent intent) {
            mAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // @formatter:off
        FragmentActivity activity = getActivity();
        LocalBroadcastManager.getInstance(activity).registerReceiver(mRemoveNsfwImages, new IntentFilter(
            Constants.BROADCAST_REMOVE_NSFW_IMAGES));
        LocalBroadcastManager.getInstance(activity).registerReceiver(mHttpRequestComplete, new IntentFilter(
            Constants.BROADCAST_HTTP_FINISHED));
        LocalBroadcastManager.getInstance(activity).registerReceiver(mSubredditSelected, new IntentFilter(
            Constants.BROADCAST_SUBREDDIT_SELECTED));
        // @formatter:on

        activity.getSupportLoaderManager().initLoader(Constants.LOADER_REDDIT, null, ImageFragment.this);
        activity.getSupportLoaderManager().initLoader(Constants.LOADER_POSTS, null, ImageFragment.this);

        if (activity instanceof RedditDataProvider) {
            mRedditDataProvider = (RedditDataProvider) activity;
        } else {
            throw new RuntimeException("Activity " + activity.toString() + " must implement the RedditDataProvider interface");
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        mLoginMenuItem = menu.findItem(R.id.login);

        // If the user is logged in, update the Logout menu item to "Log out <username>"
        if (RedditLoginInformation.isLoggedIn()) {
            mLoginMenuItem.setTitle(getString(R.string.log_out_) + RedditLoginInformation.getUsername());
            mLoginMenuItem.setIcon(R.drawable.ic_action_exit_dark);
        } else {
            mLoginMenuItem.setTitle(R.string.log_on);
            mLoginMenuItem.setIcon(R.drawable.ic_action_key_dark);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN) @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        final Intent i = new Intent(getActivity(), getImageDetailActivityClass());
        i.putExtra(Constants.EXTRA_IMAGE, (int) position);
        Bundle b = new Bundle();
        b.putString(Constants.EXTRA_AGE, mRedditDataProvider.getAge().name());
        b.putString(Constants.EXTRA_CATEGORY, mRedditDataProvider.getCategory().name());
        b.putString(Constants.EXTRA_SELECTED_SUBREDDIT, mRedditDataProvider.getSubreddit());
        i.putExtras(b);

        if (Util.hasJellyBean()) {
            ActivityOptions options = ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight());
            getActivity().startActivity(i, options.toBundle());
        } else {
            startActivity(i);
        }
    }

    public Class<? extends ImageDetailActivity> getImageDetailActivityClass() {
        return ImageDetailActivity.class;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
        Log.i(TAG, "onCreateLoader");
        switch (id) {
            //@formatter:off
            case Constants.LOADER_REDDIT:
                return new CursorLoader(getActivity(),
                    RedditContract.RedditData.CONTENT_URI, // uri
                    null,                                  // projection
                    null,                                  // selection
                    null,                                  // selectionArgs[]
                    RedditContract.Posts.DEFAULT_SORT);    // sort

            case Constants.LOADER_POSTS:
                QueryCriteria queryCriteria = getPostsQueryCriteria();
                // If the user doesn't want to see NSFW images, filter them out. Otherwise do nothing.
                String selection = null;
                String[] selectionArgs = null;

                if (!SharedPreferencesHelper.getShowNsfwImages(getActivity())) {
                    selection = RedditContract.PostColumns.OVER_18 + " = ?";
                    selectionArgs = new String[] { "0" };
                }

                return new CursorLoader(getActivity(),
                    RedditContract.Posts.CONTENT_URI,  // uri
                    queryCriteria.getProjection(),     // projection
                    selection,                         // selection
                    selectionArgs,                     // selectionArgs[]
                    queryCriteria.getSort());          // sort
            //@formatter:on
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.i(TAG, "onLoadFinished");
        if (!cursor.moveToFirst()) {
            Ln.e("Exiting onLoadFinished cUnable to move to first position on cursor: " + loader.getId());
        }
        switch (loader.getId()) {
            case Constants.LOADER_REDDIT:
                Log.i(TAG, "onLoadFinished REDDIT_LOADER, " + cursor.getCount() + " rows");
                if (cursor != null && cursor.moveToFirst()) {
                    mAfter = cursor.getString(cursor.getColumnIndex(RedditContract.RedditData.AFTER));
                }
                break;

            case Constants.LOADER_POSTS:
                Log.i(TAG, "onLoadFinished POST_LOADER, total = " + cursor.getCount() + " rows");
                mAdapter.swapCursor(cursor);
                setRequestInProgress(false);

                if (cursor.getCount() == 0) {
                    fetchImagesFromReddit(true);
                    mNoImages.setVisibility(View.VISIBLE);
                } else if (mNoImages.getVisibility() == View.VISIBLE) {
                    mNoImages.setVisibility(View.GONE);
                }

                // If a full refresh was initiated, scroll to the top.
                if (mFullRefresh) {
                    getAdapterView().setSelection(0);
                    mFullRefresh = false;
                }

                break;
        }
    }

    protected void fetchImagesFromReddit(boolean replaceAll) {
        setRequestInProgress(true);
        mFullRefresh = replaceAll;
        //@formatter:off
        RedditService.getPosts(getActivity(),
                mRedditDataProvider.getSubreddit(),
                mRedditDataProvider.getAge(),
                mRedditDataProvider.getCategory(),
                mAfter);
        //@formatter:on
    }

    protected abstract T getAdapterView();

    @Override
    public void onLoaderReset(Loader<Cursor> cursor) {
        Log.i(TAG, "onLoaderReset");
        switch (cursor.getId()) {
            case Constants.LOADER_REDDIT:
                break;

            case Constants.LOADER_POSTS:
                mAdapter.swapCursor(null);
                break;
        }
    }

    protected abstract QueryCriteria getPostsQueryCriteria();

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Activity activity = getActivity();
        if (activity != null && activity instanceof ScrollPosReadable) {
            if (hidden) {
                // Save the first visible position so that the next image viewing fragment can pick it up
                ((ScrollPosReadable) activity).setFirstVisiblePosition(getFirstVisiblePosition());
            } else {
                // Set the first visible position to the same as the previous image viewing fragment
                int firstVisiblePos = ((ScrollPosReadable) activity).getFirstVisiblePosition();
                setFirstVisiblePosition(firstVisiblePos);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        mThumbnailInfo = ThumbnailInfo.getThumbnailInfo(getResources());

        // Initialize the adapter to null, the adapter will be populated in onLoadFinished
        mAdapter = getNewAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(getLayoutId(), container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mRemoveNsfwImages);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mHttpRequestComplete);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mSubredditSelected);
    }

    protected abstract int getLayoutId();

    protected abstract V getNewAdapter();

    @Override
    public int getFirstVisiblePosition() {
        AdapterView<?> adapterView = getAdapterView();
        if (adapterView == null) {
            return 0;
        }

        return adapterView.getFirstVisiblePosition();
    }

    @Override
    public void setFirstVisiblePosition(final int firstVisiblePosition) {
        AdapterView<?> adapterView = getAdapterView();
        if (adapterView != null) {
            getAdapterView().setSelection(firstVisiblePosition);
        } else {
            Ln.d("Unable to set first visible position in adapter, AdapterView was null");
        }
    }

    protected boolean isRequestInProgress() {
        return mRequestInProgress;
    }

    protected void setRequestInProgress(boolean inProgress) {
        mRequestInProgress = inProgress;
        if (inProgress) {
            mNoImages.setVisibility(View.GONE);
        }
    }
}
