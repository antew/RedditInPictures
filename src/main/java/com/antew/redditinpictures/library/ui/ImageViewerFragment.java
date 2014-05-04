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

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.antew.redditinpictures.device.ScreenSize;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.image.Image;
import com.antew.redditinpictures.library.image.ImageResolver;
import com.antew.redditinpictures.library.imgur.ImgurAlbumApi.Album;
import com.antew.redditinpictures.library.interfaces.SystemUiStateProvider;
import com.antew.redditinpictures.library.model.ImageSize;
import com.antew.redditinpictures.library.model.reddit.PostData;
import com.antew.redditinpictures.library.ui.base.BaseFragment;
import com.antew.redditinpictures.library.util.AndroidUtil;
import com.antew.redditinpictures.library.util.ImageDownloader;
import com.antew.redditinpictures.library.util.ImageUtil;
import com.antew.redditinpictures.library.util.Ln;
import com.antew.redditinpictures.library.util.Strings;
import com.antew.redditinpictures.pro.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import javax.inject.Inject;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;

/**
 * This fragment will populate the children of the ViewPager from {@link ImageDetailActivity}.
 */
public abstract class ImageViewerFragment extends BaseFragment {
    protected static final String IMAGE_DATA_EXTRA  = "extra_image_data";
    protected static final String IMAGE_ALBUM_EXTRA = "extra_image_album";
    /**
     * Movement threshold used to decide whether to cancel the toggle
     * between windowed mode and fullscreen mode in the
     * WebView in {@link #getWebViewOnTouchListener()}
     */
    private static float MOVE_THRESHOLD;
    private final Object mPauseWorkLock = new Object();
    protected PostData  mImage;
    @InjectView(R.id.iv_imageView)
    protected ImageView mImageView;
    protected WebView   mWebView;
    protected boolean mWebViewInitialized = false;
    protected boolean mPauseWork          = false;
    protected String  mResolvedImageUrl   = null;
    protected Image   mResolvedImage      = null;
    protected int   mActionBarHeight;
    protected Album mAlbum;
    protected AsyncTask<String, Void, Image> mResolveImageTask = null;
    protected SystemUiStateProvider mSystemUiStateProvider;
    protected boolean mFragmentVisibleToUser = false;
    @InjectView(R.id.pb_progress)
    ProgressBar    mProgress;
    @InjectView(R.id.rl_post_information_wrapper)
    RelativeLayout mPostInformationWrapper;
    @InjectView(R.id.tv_post_title)
    TextView       mPostTitle;
    @InjectView(R.id.tv_post_information)
    TextView       mPostInformation;
    @InjectView(R.id.btn_view_gallery)
    Button         mBtnViewGallery;
    @InjectView(R.id.webview_stub)
    ViewStub       mViewStub;
    @InjectView(R.id.tv_post_votes)
    TextView       mPostVotes;
    /**
     * This BroadcastReceiver handles updating the score when a vote is cast or changed
     */
    private BroadcastReceiver mScoreUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(Constants.Extra.EXTRA_PERMALINK) &&
                intent.hasExtra(Constants.Extra.EXTRA_SCORE) &&
                mImage != null &&
                mImage.getPermalink().equals(intent.getStringExtra(Constants.Extra.EXTRA_PERMALINK))) {

                if (mPostVotes != null) {
                    mPostVotes.setText("" + intent.getIntExtra(Constants.Extra.EXTRA_SCORE, 0));
                }
            }
        }
    };
    @InjectView(R.id.b_retry)
    Button          mRetry;
    @Inject
    ImageDownloader mImageDownloader;
    @InjectView(R.id.tv_error_message)
    TextView        mErrorMessage;
    @Inject
    ScreenSize      mScreenSize;
    private boolean           mExitTasksEarly         = false;
    private boolean           mCancelClick            = false;
    private float             mDownXPos               = 0;
    private float             mDownYPos               = 0;
    private BroadcastReceiver mToggleFullscreenIntent = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isSystemUiVisible = intent.getBooleanExtra(Constants.Extra.EXTRA_IS_SYSTEM_UI_VISIBLE, false);
            if (isSystemUiVisible) {
                hidePostDetails();
            } else {
                showPostDetails();
            }
        }
    };

    /**
     * Empty constructor as per the Fragment documentation
     */
    public ImageViewerFragment() {
    }

    /**
     * Set a hint to the system about whether this fragment's UI is currently visible
     * to the user. This hint defaults to true and is persistent across fragment instance
     * state save and restore.
     * <p/>
     * <p>An app may set this to false to indicate that the fragment's UI is
     * scrolled out of visibility or is otherwise not directly visible to the user.
     * This may be used by the system to prioritize operations such as fragment lifecycle updates
     * or loader ordering behavior.</p>
     *
     * @param isVisibleToUser
     *     true if this fragment's UI is currently visible to the user (default),
     *     false if it is not.
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mFragmentVisibleToUser = isVisibleToUser;

        // If we have a webview shown we want to hide it when the fragment is not being viewed to prevent the lag from swiping between fragments.
        if (mWebView != null) {
            if (mFragmentVisibleToUser) {
                mWebView.setVisibility(View.VISIBLE);
            } else {
                mWebView.setVisibility(View.GONE);
                // Set the view to uninitialized since it could get GC'd and we want to show the progress bar when/if they navigate back.
                mWebViewInitialized = false;
            }
        }
    }

    /**
     * Populate image using a url from extras.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        loadExtras();
    }

    public void loadExtras() {
        mImage = getArguments() != null ? (PostData) getArguments().getParcelable(IMAGE_DATA_EXTRA) : null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.image_detail_fragment, container, false);
        ButterKnife.inject(this, view);

        mRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resolveImage();
            }
        });

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mImage != null) {
            outState.putParcelable(IMAGE_DATA_EXTRA, mImage);
        }

        if (mAlbum != null) {
            outState.putParcelable(IMAGE_ALBUM_EXTRA, mAlbum);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mScoreUpdateReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mToggleFullscreenIntent);

        if (mImageView != null) {
            mImageView.setImageDrawable(null);
        }

        if (mWebView != null) {
            mWebView.destroy();
        }

        if (mResolveImageTask != null) {
            if (mResolveImageTask.getStatus() != AsyncTask.Status.FINISHED) {
                Ln.i("onDestroy - Cancelling resolveImageTask");
                mResolveImageTask.cancel(true);
            }
        }

        super.onDestroy();
    }

    protected abstract void resolveImage();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        populatePostData(mPostInformationWrapper);

        if (savedInstanceState != null) {
            loadSavedInstanceState(savedInstanceState);
        }

        final Activity act = getActivity();

        LocalBroadcastManager.getInstance(act)
                             .registerReceiver(mScoreUpdateReceiver, new IntentFilter(Constants.Broadcast.BROADCAST_UPDATE_SCORE));
        LocalBroadcastManager.getInstance(act)
                             .registerReceiver(mToggleFullscreenIntent, new IntentFilter(Constants.Broadcast.BROADCAST_TOGGLE_FULLSCREEN));

        // Set up on our tap listener for the PhotoView which we use to toggle between fullscreen
        // and windowed mode
        ((PhotoView) mImageView).setOnPhotoTapListener(getOnPhotoTapListener(act));

        MOVE_THRESHOLD = 20 * getResources().getDisplayMetrics().density;

        // Calculate ActionBar height
        TypedValue tv = new TypedValue();
        if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            mActionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getActivity().getResources().getDisplayMetrics());
        }

        try {
            mSystemUiStateProvider = (SystemUiStateProvider) getActivity();
        } catch (ClassCastException e) {
            Ln.e(e, "The activity must implement the SystemUiStateProvider interface");
        }

        // Use the parent activity to load the image asynchronously into the ImageView (so a single
        // cache can be used over all pages in the ViewPager
        if (ImageViewerActivity.class.isInstance(getActivity())) {
            resolveImage();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSystemUiStateProvider.isSystemUiVisible()) {
            showPostDetails();
        } else {
            hidePostDetails();
        }
    }

    public void showPostDetails() {
        if (shouldShowPostInformation()) {
            mPostInformationWrapper.setVisibility(View.VISIBLE);
            mPostInformationWrapper.animate().setDuration(500).y(mActionBarHeight);
        }
    }

    public void hidePostDetails() {
        mPostInformationWrapper.animate().setDuration(500).y(-400);
    }

    protected abstract boolean shouldShowPostInformation();

    protected abstract void populatePostData(View v);

    protected void loadSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(IMAGE_DATA_EXTRA)) {
            mImage = savedInstanceState.getParcelable(IMAGE_DATA_EXTRA);
        }

        if (savedInstanceState.containsKey(IMAGE_ALBUM_EXTRA)) {
            mAlbum = savedInstanceState.getParcelable(IMAGE_ALBUM_EXTRA);
        }
    }

    private OnPhotoTapListener getOnPhotoTapListener(final Activity activity) {
        return new OnPhotoTapListener() {

            @Override
            public void onPhotoTap(View view, float x, float y) {
                Intent intent = new Intent(Constants.Broadcast.BROADCAST_TOGGLE_FULLSCREEN);
                intent.putExtra(Constants.Extra.EXTRA_IS_SYSTEM_UI_VISIBLE, mSystemUiStateProvider.isSystemUiVisible());
                LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
            }
        };
    }

    public void loadImage(Image image) {
        if (image == null) {
            Ln.e("Received null url in loadImage(String imageUrl)");
            showImageError();
            return;
        }

        try {
            mResolvedImage = image;
            mResolvedImageUrl = mResolvedImage.getSize(ImageSize.LARGE_THUMBNAIL);

            // Falllback to the Original if we can't resolve.
            if (Strings.isEmpty(mResolvedImageUrl)) {
                mResolvedImageUrl = mResolvedImage.getSize(ImageSize.ORIGINAL);
            }

            // Fallback to the URL if we can't resolve.
            if (Strings.isEmpty(mResolvedImageUrl)) {
                mResolvedImageUrl = mResolvedImage.getUrl();
            }

            if (ImageUtil.isGif(mResolvedImageUrl)) {
                Picasso.with(getActivity()).load(R.drawable.loading_spinner_76).into(mImageView);
                loadGifInWebView(mResolvedImageUrl);
            } else {
                Picasso.with(getActivity())
                       .load(Uri.parse(mResolvedImageUrl))
                       .resize(mScreenSize.getWidth(), mScreenSize.getHeight())
                       .centerInside()
                       .into(mImageView, new Callback() {
                           @Override
                           public void onSuccess() {
                               hideProgress();
                           }

                           @Override
                           public void onError() {
                               showImageError();
                           }
                       });
            }
        } catch (Exception e) {
            Ln.e(e, "Failed to load image");
            showImageError();
        }
    }

    protected void showImageError() {
        hideProgress();
        if (mErrorMessage != null) {
            mErrorMessage.setVisibility(View.VISIBLE);
        }
        if (mRetry != null) {
            mRetry.setVisibility(View.VISIBLE);
        }
    }

    public void loadGifInWebView(final String imageUrl) {
        if (mViewStub.getParent() != null) {
            mWebView = (WebView) mViewStub.inflate();
        }

        initializeWebView();
        /**
         * On earlier version of Android, {@link android.webkit.WebView#loadData(String, String, String)} decides to just show the HTML instead of actually display it.
         *
         * So, for older version we make it load from a base URL, which fixes it for some reason...
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                // When the current view isn't being directly viewed by the user we want to defer it slightly so that the one currently being displayed can have priority.
                if (!mFragmentVisibleToUser) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Ln.e(e);
                    }
                }
                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (AndroidUtil.hasHoneycomb()) {
                            mWebView.loadData(getHtmlForImageDisplay(imageUrl), "text/html", "utf-8");
                        } else {
                            mWebView.loadDataWithBaseURL("", getHtmlForImageDisplay(imageUrl), "text/html", "utf-8", "");
                        }
                        mImageView.setVisibility(View.GONE);
                    }
                });
            }
        }).start();
    }

    protected void hideProgress() {
        if (mProgress != null) {
            mProgress.setVisibility(View.GONE);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void initializeWebView() {
        assert mWebView != null : "WebView should not be null!";

        WebSettings settings = mWebView.getSettings();
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        if (AndroidUtil.hasHoneycomb()) {
            settings.setDisplayZoomControls(false);
        }
        // Before loading the actual content, let's let the WebView initialize everything.
        mWebView.loadData("<html></html>", "text/html", "utf-8");

        // Hardware acceleration wasn't introduced until Honeycomb. So we want to use the drawing cache for older devices.
        if (!AndroidUtil.hasHoneycomb()) {
            mWebView.setDrawingCacheEnabled(true);
        }
        mWebView.setBackgroundColor(Color.BLACK);
        mWebView.setWebViewClient(new WebViewClient() {
            /**
             * Notify the host application that a page has finished loading. This method
             * is called only for main frame. When onPageFinished() is called, the
             * rendering picture may not be updated yet. To get the notification for the
             * new Picture, use {@link android.webkit.WebView.PictureListener#onNewPicture}.
             *
             * @param view
             *     The WebView that is initiating the callback.
             * @param url
             *     The url of the page.
             */
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // We first initialize the webview with mostly blank HTML content, so don't want to show it until we get to the image.
                if (mWebView != null && mWebViewInitialized) {
                    mWebView.setVisibility(View.VISIBLE);
                } else if (!mWebViewInitialized) {
                    mWebViewInitialized = true;
                }
            }
        });
        mWebView.setOnTouchListener(getWebViewOnTouchListener());
    }

    public String getHtmlForImageDisplay(String imageUrl) {
        return Constants.WEBVIEW_IMAGE_HTML_BEGIN + imageUrl + Constants.WEBVIEW_IMAGE_HTML_END;
    }

    /**
     * This handles receiving the touch events in the WebView so that we can
     * toggle between fullscreen and windowed mode.
     * <p/>
     * The first time the user touches the screen we save the X and Y coordinates.
     * If we receive a {@link MotionEvent#ACTION_DOWN} event we compare the previous
     * X and Y coordinates to the saved coordinates, if they are greater than {@link
     * #MOVE_THRESHOLD}
     * we prevent the toggle from windowed mode to fullscreen mode or vice versa, the idea
     * being that the user is either dragging the image or using pinch-to-zoom.
     * <p/>
     * TODO: Implement handling for double tap to zoom.
     *
     * @return The {@link OnTouchListener} for the {@link WebView} to use.
     */
    public OnTouchListener getWebViewOnTouchListener() {
        return new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mCancelClick = false;
                        mDownXPos = event.getX();
                        mDownYPos = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (!mCancelClick) {
                            Intent intent = new Intent(Constants.Broadcast.BROADCAST_TOGGLE_FULLSCREEN);
                            intent.putExtra(Constants.Extra.EXTRA_IS_SYSTEM_UI_VISIBLE, mSystemUiStateProvider.isSystemUiVisible());
                            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(event.getX() - mDownXPos) > MOVE_THRESHOLD || Math.abs(event.getY() - mDownYPos) > MOVE_THRESHOLD) {
                            mCancelClick = true;
                        }
                        break;
                }

                // Return false so that we still let the WebView consume the event
                return false;
            }
        };
    }

    protected void showProgress() {
        if (mProgress != null) {
            mProgress.setVisibility(View.VISIBLE);
        }
    }

    /**
     * The actual AsyncTask that will asynchronously resolve the URL we should display.
     */
    class ResolveImageTask extends AsyncTask<String, Void, Image> {
        private String data;

        public ResolveImageTask() {
        }

        /**
         * Background processing.
         */
        @Override
        protected Image doInBackground(String... params) {
            data = params[0];
            Image resolvedImage = null;
            Ln.i("Resolving Image Url: %s", data);
            // Wait here if work is paused and the task is not cancelled
            synchronized (mPauseWorkLock) {
                while (mPauseWork && !isCancelled()) {
                    try {
                        mPauseWorkLock.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }

            if (!isCancelled() && !mExitTasksEarly) {
                resolvedImage = ImageResolver.resolve(data);
            }

            return resolvedImage;
        }

        /**
         * Once we're resolved the URL, pass it to our load function to download/display it
         */
        @Override
        protected void onPostExecute(Image image) {
            // if cancel was called on this task or the "exit early" flag is set then we're done
            if (isCancelled() || mExitTasksEarly) {
                image = null;
            }

            loadImage(image);
        }
    }
}
