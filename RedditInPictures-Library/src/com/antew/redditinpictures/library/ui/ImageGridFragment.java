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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.antew.redditinpictures.library.R;
import com.antew.redditinpictures.library.adapter.ImageCursorAdapter;
import com.antew.redditinpictures.library.enums.ImageSize;
import com.antew.redditinpictures.library.image.ThumbnailInfo;
import com.antew.redditinpictures.library.imgur.SizeAwareImageFetcher;
import com.antew.redditinpictures.library.interfaces.RedditDataProvider;
import com.antew.redditinpictures.library.interfaces.ScrollPosReadable;
import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.reddit.RedditLoginInformation;
import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.library.service.RequestCode;
import com.antew.redditinpictures.library.utils.Consts;
import com.antew.redditinpictures.library.utils.ImageCache.ImageCacheParams;
import com.antew.redditinpictures.library.utils.ImageFetcher;
import com.antew.redditinpictures.library.utils.Util;
import com.antew.redditinpictures.sqlite.RedditContract;

public class ImageGridFragment extends SherlockFragment implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor>, ScrollPosReadable {
    public static final String   TAG                = "ImageGridFragment";
    private static final String  IMAGE_CACHE_DIR    = "thumbs";
    private ThumbnailInfo        mThumbnailInfo;
    protected ImageCursorAdapter mAdapter;
    private ImageFetcher         mImageFetcher;
    private String               mAfter;
    private boolean              mRequestInProgress = false;
    private boolean              mFirstRequest      = true;
    private ProgressBar          mProgress;
    private TextView             mNoImages;
    private RedditDataProvider   mRedditDataProvider;
    private MenuItem             mLoginMenuItem;
    private GridView             mGridView;
    /**
     * Empty constructor as per the Fragment documentation
     */
    public ImageGridFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        mThumbnailInfo = ThumbnailInfo.getThumbnailInfo(getResources());
        initializeImageFetcher();

        // Initialize the adapter to null, the adapter will be populated in onLoadFinished
        mAdapter = new ImageCursorAdapter(getActivity(), mImageFetcher, null);

    }

    private void initializeImageFetcher() {
        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        mImageFetcher = new SizeAwareImageFetcher(getActivity(), mThumbnailInfo.getSize(), ImageSize.SMALL_SQUARE);
        mImageFetcher.setLoadingImage(R.drawable.empty_photo);
        mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), getImageCache());
    }

    private ImageCacheParams getImageCache() {
        ImageCacheParams cacheParams = new ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);
        cacheParams.setMemCacheSizePercent(getActivity(), Consts.IMAGE_CACHE_SIZE);
        return cacheParams;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mImageFetcher != null)
            mImageFetcher.clearCache();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FragmentActivity activity = getActivity();
        LocalBroadcastManager.getInstance(activity).registerReceiver(mRemoveNsfwImages, new IntentFilter(Consts.BROADCAST_REMOVE_NSFW_IMAGES));
        LocalBroadcastManager.getInstance(activity).registerReceiver(mHttpRequestComplete, new IntentFilter(Consts.BROADCAST_HTTP_FINISHED));
        LocalBroadcastManager.getInstance(activity).registerReceiver(mSubredditSelected, new IntentFilter(Consts.BROADCAST_SUBREDDIT_SELECTED));
        LocalBroadcastManager.getInstance(activity).registerReceiver(mLoginComplete, new IntentFilter(Consts.BROADCAST_LOGIN_COMPLETE));

        activity.getSupportLoaderManager().initLoader(Consts.LOADER_REDDIT, null, ImageGridFragment.this);
        activity.getSupportLoaderManager().initLoader(Consts.LOADER_POSTS, null, ImageGridFragment.this);

        if (activity instanceof RedditDataProvider)
            mRedditDataProvider = (RedditDataProvider) activity;
        else
            throw new RuntimeException("Activity " + activity.toString() + " must implement the RedditDataProvider interface");
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        mLoginMenuItem = menu.findItem(R.id.login);

        // If the user is logged in, update the Logout menu item to "Log out <username>"
        if (RedditLoginInformation.isLoggedIn()) {
            mLoginMenuItem.setTitle(getString(R.string.log_out_) + RedditLoginInformation.getUsername());
            mLoginMenuItem.setIcon(R.drawable.ic_action_logout);
        } else {
            mLoginMenuItem.setTitle(R.string.log_on);
            mLoginMenuItem.setIcon(R.drawable.ic_action_login);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.image_grid_fragment, container, false);
        mGridView = (GridView) v.findViewById(R.id.gridView);
        mNoImages = (TextView) v.findViewById(R.id.no_images);
        mProgress = (ProgressBar) v.findViewById(R.id.progress);
        setUpGridView(mGridView);
        return v;
    }

    public void setUpGridView(GridView gridView) {
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(this);
        gridView.setOnScrollListener(getGridViewOnScrollListener(gridView));
        gridView.getViewTreeObserver().addOnGlobalLayoutListener(getGridGlobalLayoutListener(gridView));
    }

    private AbsListView.OnScrollListener getGridViewOnScrollListener(final GridView gridView) {
        return new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                // Pause fetcher to ensure smoother scrolling when flinging
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    mImageFetcher.setPauseWork(true);
                } else {
                    mImageFetcher.setPauseWork(false);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // if we're at the bottom of the listview, load more data
                boolean lastItemIsVisible = (firstVisibleItem + visibleItemCount) >= totalItemCount;
                if (!mRequestInProgress && totalItemCount > 0 && lastItemIsVisible) {
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
     * @param gridView
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

    @Override
    public void onResume() {
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        mImageFetcher.setExitTasksEarly(true);
        mImageFetcher.flushCache();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mImageFetcher.closeCache();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mRemoveNsfwImages);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mHttpRequestComplete);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mSubredditSelected);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mLoginComplete);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

        final Intent i = new Intent(getActivity(), getImageDetailActivityClass());
        i.putExtra(Consts.EXTRA_IMAGE, (int) id);
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



    /**
     * This BroadcastReceiver handles updating the score when a vote is cast or changed
     */
    private BroadcastReceiver mRemoveNsfwImages = new BroadcastReceiver() {
                                                    //@formatter:off
        @Override
        public void onReceive(Context context, Intent intent) {
//            mAdapter.removeNsfwImages();
            mAdapter.notifyDataSetChanged();
        }
    };
    
    private BroadcastReceiver mLoginComplete = new BroadcastReceiver() {
        //@formatter:off
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
    
    private BroadcastReceiver mHttpRequestComplete = new BroadcastReceiver() {
        //@formatter:off
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Http request complete, mFirstRequest = " + mFirstRequest);
            if (mFirstRequest) {
                mFirstRequest = false;
            }
            
            Bundle args = intent.getBundleExtra(RedditService.EXTRA_BUNDLE);
            RequestCode requestCode = (RequestCode) intent.getSerializableExtra(RedditService.EXTRA_REQUEST_CODE);
            
            Bundle passThru = intent.getBundleExtra(RedditService.EXTRA_PASS_THROUGH);
            int statusCode = args.getInt(RedditService.EXTRA_STATUS_CODE);
            boolean replaceAll = args.getBoolean(RedditService.EXTRA_REPLACE_ALL);
            String json = args.getString(RedditService.REST_RESULT);
        }
    };

    private BroadcastReceiver mSubredditSelected = new BroadcastReceiver() {
        //@formatter:off
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "New subreddit selected, fetching new posts");
            mAdapter.swapCursor(null);
            mAfter = null;
            fetchImagesFromReddit(true);
        }
    };
    //@formatter:off
    
    private void setRequestInProgress(boolean inProgress) {
        mRequestInProgress = inProgress;
        
        final SherlockFragmentActivity activity = getSherlockActivity();
        if (activity != null) {
            activity.setSupportProgressBarIndeterminateVisibility(inProgress);
        }
        
        
        if (inProgress) {
            mProgress.setVisibility(View.VISIBLE);
            mNoImages.setVisibility(View.GONE);
        } else {
            mProgress.setVisibility(View.GONE);
        }
    }

    private void fetchImagesFromReddit(boolean replaceAll) {
        SherlockFragmentActivity activity = getSherlockActivity();
        if (activity != null)
            setRequestInProgress(true);
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
                return new CursorLoader(getActivity(), 
                                        RedditContract.Posts.CONTENT_URI,         // uri
                                        RedditContract.Posts.GRIDVIEW_PROJECTION, // projection
                                        null,                                     // selection
                                        null,                                     // selectionArgs[]
                                        RedditContract.RedditData.DEFAULT_SORT);  // sort
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
                // This sets the correct first visible position if we're loading the fragment
                // for the first time
                int firstVisiblePos = ((ScrollPosReadable) getActivity()).getFirstVisiblePosition();
                setFirstVisiblePosition(firstVisiblePos);

                if (cursor.getCount() == 0) {
                    fetchImagesFromReddit(true);
                    mNoImages.setVisibility(View.VISIBLE);
                } else if (mNoImages.getVisibility() == View.VISIBLE) {
                    mNoImages.setVisibility(View.GONE);
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
    public int getFirstVisiblePosition() {
        if (mGridView == null)
            return 0;

        return mGridView.getFirstVisiblePosition();
    }

    @Override
    public void setFirstVisiblePosition(final int firstVisiblePosition) {
        mGridView.setSelection(firstVisiblePosition);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Activity activity = getActivity();
        if (activity != null && activity instanceof ScrollPosReadable) {
            if (hidden) {
                // Save the first visible position so that the next image viewing fragment can pick it up
                ((ScrollPosReadable) activity).setFirstVisiblePosition(mGridView.getFirstVisiblePosition());
            } else {
                // Set the first visible position to the same as the previous image viewing fragment
                int firstVisiblePos = ((ScrollPosReadable) activity).getFirstVisiblePosition();
                setFirstVisiblePosition(firstVisiblePos);
            }
        }
    }
}
