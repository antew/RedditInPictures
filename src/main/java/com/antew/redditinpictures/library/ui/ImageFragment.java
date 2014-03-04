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
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.antew.redditinpictures.library.image.ThumbnailInfo;
import com.antew.redditinpictures.library.interfaces.RedditDataProvider;
import com.antew.redditinpictures.library.interfaces.ScrollPosReadable;
import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.reddit.RedditLoginInformation;
import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.library.service.RequestCode;
import com.antew.redditinpictures.library.utils.Consts;
import com.antew.redditinpictures.library.utils.Ln;
import com.antew.redditinpictures.library.utils.Util;
import com.antew.redditinpictures.pro.R;
import com.antew.redditinpictures.sqlite.QueryCriteria;
import com.antew.redditinpictures.sqlite.RedditContract;

/**
 * Fragment with convenience methods for displaying images
 * @param <T> The type of view the fragment is using, e.g. GridView, ListView
 * @param <V> The type of the cursor adapter backing the view
 */
public abstract class ImageFragment<T extends AdapterView, V extends CursorAdapter> extends SherlockFragment
    implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor>, ScrollPosReadable {
    public static final  String TAG             = "ImageFragment";
    private static final String IMAGE_CACHE_DIR = "thumbs";
    protected V                  mAdapter;
    protected ThumbnailInfo      mThumbnailInfo;
    protected RedditDataProvider mRedditDataProvider;
    private   String             mAfter;
    private   MenuItem           mLoginMenuItem;
    private boolean mRequestInProgress = false;
    private boolean mFullRefresh = true;

    @InjectView(R.id.no_images)
    protected TextView mNoImages;

    private BroadcastReceiver mLoginComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Login request complete");
            boolean successful = intent.getBooleanExtra(Consts.EXTRA_SUCCESS, false);
            if (!successful) {
                String errorMessage = intent.getStringExtra(Consts.EXTRA_ERROR_MESSAGE);
                showLoginError(errorMessage);
            }
        }
    };

    private BroadcastReceiver mSubredditSelected = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "New subreddit selected, fetching new posts");
            mAdapter.swapCursor(null);
            mAfter = null;
            fetchImagesFromReddit(true);
        }
    };

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

    private BroadcastReceiver mRemoveNsfwImages    = new BroadcastReceiver() {
        //@formatter:off
        @Override
        public void onReceive(Context context, Intent intent) {
            mAdapter.notifyDataSetChanged();
        }
    };

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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // @formatter:off
        FragmentActivity activity = getActivity();
        LocalBroadcastManager.getInstance(activity).registerReceiver(mRemoveNsfwImages, new IntentFilter(Consts.BROADCAST_REMOVE_NSFW_IMAGES));
        LocalBroadcastManager.getInstance(activity).registerReceiver(mHttpRequestComplete, new IntentFilter(Consts.BROADCAST_HTTP_FINISHED));
        LocalBroadcastManager.getInstance(activity).registerReceiver(mSubredditSelected, new IntentFilter(Consts.BROADCAST_SUBREDDIT_SELECTED));
        LocalBroadcastManager.getInstance(activity).registerReceiver(mLoginComplete, new IntentFilter(Consts.BROADCAST_LOGIN_COMPLETE));
        // @formatter:on

        activity.getSupportLoaderManager().initLoader(Consts.LOADER_REDDIT, null, ImageFragment.this);
        activity.getSupportLoaderManager().initLoader(Consts.LOADER_POSTS, null, ImageFragment.this);

        if (activity instanceof RedditDataProvider) {
            mRedditDataProvider = (RedditDataProvider) activity;
        } else {
            throw new RuntimeException(
                "Activity " + activity.toString() + " must implement the RedditDataProvider interface");
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(getLayoutId(), container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
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
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mLoginComplete);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN) @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        final Intent i = new Intent(getActivity(), getImageDetailActivityClass());
        i.putExtra(Consts.EXTRA_IMAGE, (int) position);
        Bundle b = new Bundle();
        b.putString(Consts.EXTRA_AGE, mRedditDataProvider.getAge().name());
        b.putString(Consts.EXTRA_CATEGORY, mRedditDataProvider.getCategory().name());
        b.putString(Consts.EXTRA_SELECTED_SUBREDDIT, mRedditDataProvider.getSubreddit());
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

    private void showLoginError(String errorMessage) {
        Toast.makeText(getActivity(), getString(R.string.error) + errorMessage, Toast.LENGTH_SHORT).show();
    }
    //@formatter:off

    protected void setRequestInProgress(boolean inProgress) {
        mRequestInProgress = inProgress;

        final SherlockFragmentActivity activity = getSherlockActivity();
        if (activity != null) {
            activity.setSupportProgressBarIndeterminateVisibility(inProgress);
        }

        if (inProgress) {
            mNoImages.setVisibility(View.GONE);
        }
    }

    protected void fetchImagesFromReddit(boolean replaceAll) {
        SherlockFragmentActivity activity = getSherlockActivity();
        if (activity != null) setRequestInProgress(true);
        mFullRefresh = replaceAll;
        //@formatter:off
        RedditService.getPosts(activity,
                mRedditDataProvider.getSubreddit(),
                mRedditDataProvider.getAge(),
                mRedditDataProvider.getCategory(),
                mAfter,
                replaceAll);
        //@formatter:on
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
        Log.i(TAG, "onCreateLoader");
        switch (id) {
            //@formatter:off
            case Consts.LOADER_REDDIT:
                return new CursorLoader(getActivity(),
                    RedditContract.RedditData.CONTENT_URI, // uri
                    null,                                  // projection
                    null,                                  // selection
                    null,                                  // selectionArgs[]
                    RedditContract.Posts.DEFAULT_SORT);    // sort

            case Consts.LOADER_POSTS:
                QueryCriteria queryCriteria = getPostsQueryCriteria();
                return new CursorLoader(getActivity(),
                    RedditContract.Posts.CONTENT_URI,  // uri
                    queryCriteria.getProjection(),     // projection
                    null,                              // selection
                    null,                              // selectionArgs[]
                    queryCriteria.getSort());          // sort
            //@formatter:on
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.i(TAG, "onLoadFinished");
        switch (loader.getId()) {
            case Consts.LOADER_REDDIT:
                Log.i(TAG, "onLoadFinished REDDIT_LOADER, " + cursor.getCount() + " rows");
                if (cursor != null && cursor.moveToFirst()) {
                    mAfter = cursor.getString(cursor.getColumnIndex(RedditContract.RedditData.AFTER));
                }
                break;

            case Consts.LOADER_POSTS:
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

    @Override
    public void onLoaderReset(Loader<Cursor> cursor) {
        Log.i(TAG, "onLoaderReset");
        mAdapter.swapCursor(null);
    }

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

    protected abstract int getLayoutId();

    protected abstract V getNewAdapter();

    protected abstract T getAdapterView();

    protected abstract QueryCriteria getPostsQueryCriteria();

}
