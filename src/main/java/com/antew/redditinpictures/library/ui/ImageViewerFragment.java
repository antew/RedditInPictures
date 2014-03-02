/*
 * Copyright (C) 2012 The Android Open Source Project
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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
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
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.actionbarsherlock.app.SherlockFragment;
import com.antew.redditinpictures.library.enums.ImageSize;
import com.antew.redditinpictures.library.image.Image;
import com.antew.redditinpictures.library.image.ImageResolver;
import com.antew.redditinpictures.library.imgur.ImgurAlbumApi.Album;
import com.antew.redditinpictures.library.interfaces.SystemUiStateProvider;
import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.reddit.PostData;
import com.antew.redditinpictures.library.utils.Consts;
import com.antew.redditinpictures.library.utils.ImageUtil;
import com.antew.redditinpictures.library.utils.Util;
import com.antew.redditinpictures.pro.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

/**
 * This fragment will populate the children of the ViewPager from {@link ImageDetailActivity}.
 */
public abstract class ImageViewerFragment extends SherlockFragment {
    public static final String TAG = "ImageViewerFragment";
    protected static final String IMAGE_DATA_EXTRA = "extra_image_data";
    protected static final String IMAGE_ALBUM_EXTRA = "extra_image_album";
    protected PostData mImage;

    @InjectView(R.id.iv_imageView)
    protected ImageView mImageView;
    @InjectView(R.id.pb_progress)
    ProgressBar mProgress;
    @InjectView(R.id.rl_post_information_wrapper)
    RelativeLayout mPostInformationWrapper;
    @InjectView(R.id.tv_post_title)
    TextView mPostTitle;
    @InjectView(R.id.tv_post_information)
    TextView mPostInformation;
    @InjectView(R.id.btn_view_gallery)
    Button mBtnViewGallery;
    @InjectView(R.id.webview_stub)
    ViewStub mViewStub;
    @InjectView(R.id.tv_post_votes)
    TextView mPostVotes;
    @InjectView(R.id.tv_error_message)
    TextView mErrorMessage;

    protected WebView mWebView;
    protected boolean mPauseWork = false;
    private final Object mPauseWorkLock = new Object();
    private boolean mExitTasksEarly = false;
    protected String mResolvedImageUrl = null;
    protected Image mResolvedImage = null;
    protected int mActionBarHeight;
    protected Album mAlbum;
    protected AsyncTask<String, Void, Image> mResolveImageTask = null;

    private boolean mCancelClick = false;
    private float mDownXPos = 0;
    private float mDownYPos = 0;

    /**
     * Movement threshold used to decide whether to cancel the toggle
     * between windowed mode and fullscreen mode in the
     * WebView in {@link #getWebViewOnTouchListener()}
     */
    private static float MOVE_THRESHOLD;

    protected SystemUiStateProvider mSystemUiStateProvider;

    /**
     * Empty constructor as per the Fragment documentation
     */
    public ImageViewerFragment() {
    }

    protected abstract void resolveImage();

    protected abstract void populatePostData(View v);

    protected abstract boolean shouldShowPostInformation();

    /**
     * Populate image using a url from extras.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        loadExtras();
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        if (mSystemUiStateProvider.isSystemUiVisible()) {
            showPostDetails();
        } else {
            hidePostDetails();
        }
    }

    public void loadExtras() {
        mImage = getArguments() != null ? (PostData) getArguments().getParcelable(IMAGE_DATA_EXTRA)
            : null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        final View view = inflater.inflate(R.layout.image_detail_fragment, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        populatePostData(mPostInformationWrapper);

        if (savedInstanceState != null) loadSavedInstanceState(savedInstanceState);

        final Activity act = getActivity();

        LocalBroadcastManager.getInstance(act)
            .registerReceiver(mScoreUpdateReceiver,
                new IntentFilter(Consts.BROADCAST_UPDATE_SCORE));
        LocalBroadcastManager.getInstance(act)
            .registerReceiver(mToggleFullscreenIntent,
                new IntentFilter(Consts.BROADCAST_TOGGLE_FULLSCREEN));

        // Set up on our tap listener for the PhotoView which we use to toggle between fullscreen
        // and windowed mode
        ((PhotoView) mImageView).setOnPhotoTapListener(getOnPhotoTapListener(act));

        MOVE_THRESHOLD = 20 * getResources().getDisplayMetrics().density;

        // Calculate ActionBar height
        TypedValue tv = new TypedValue();
        if (getActivity().getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
            mActionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,
                getActivity().getResources().getDisplayMetrics());
        }

        try {
            mSystemUiStateProvider = (SystemUiStateProvider) getActivity();
        } catch (ClassCastException e) {
            Log.e(TAG, "The activity must implement the SystemUiStateProvider interface", e);
        }

        // Use the parent activity to load the image asynchronously into the ImageView (so a single
        // cache can be used over all pages in the ViewPager
        if (ImageViewerActivity.class.isInstance(getActivity())) {
            resolveImage();
        }
    }

    private OnPhotoTapListener getOnPhotoTapListener(final Activity activity) {
        return new OnPhotoTapListener() {

            @Override
            public void onPhotoTap(View view, float x, float y) {
                Intent intent = new Intent(Consts.BROADCAST_TOGGLE_FULLSCREEN);
                intent.putExtra(Consts.EXTRA_IS_SYSTEM_UI_VISIBLE,
                    mSystemUiStateProvider.isSystemUiVisible());
                LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
            }
        };
    }

    protected void loadSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(IMAGE_DATA_EXTRA)) {
            mImage = savedInstanceState.getParcelable(IMAGE_DATA_EXTRA);
        }

        if (savedInstanceState.containsKey(IMAGE_ALBUM_EXTRA)) {
            mAlbum = savedInstanceState.getParcelable(IMAGE_ALBUM_EXTRA);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState");
        if (mImage != null) outState.putParcelable(IMAGE_DATA_EXTRA, mImage);

        if (mAlbum != null) outState.putParcelable(IMAGE_ALBUM_EXTRA, mAlbum);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mScoreUpdateReceiver);
        LocalBroadcastManager.getInstance(getActivity())
            .unregisterReceiver(mToggleFullscreenIntent);

        if (mImageView != null) {
            mImageView.setImageDrawable(null);
        }

        if (mWebView != null) mWebView.destroy();

        if (mResolveImageTask != null) {
            Log.i(TAG, "onDestroy - resolveImageTask not null");
            if (mResolveImageTask.getStatus() != AsyncTask.Status.FINISHED) {
                Log.i(TAG, "onDestroy - Cancelling resolveImageTask");
                mResolveImageTask.cancel(true);
            }
        } else {
            Log.i(TAG, "onDestroy - Not cancelling resolveImageTask");
        }

        super.onDestroy();
    }

    public void hidePostDetails() {
        Log.i(TAG, "hidePostDetails");
        animate(mPostInformationWrapper).setDuration(500).y(-400);
    }

    public void showPostDetails() {
        Log.i(TAG, "showPostDetails");
        if (shouldShowPostInformation()) {
            mPostInformationWrapper.setVisibility(View.VISIBLE);
            animate(mPostInformationWrapper).setDuration(500).y(mActionBarHeight);
        }
    }

    /**
     * This handles receiving the touch events in the WebView so that we can
     * toggle between fullscreen and windowed mode.
     *
     * The first time the user touches the screen we save the X and Y coordinates.
     * If we receive a {@link MotionEvent#ACTION_DOWN} event we compare the previous
     * X and Y coordinates to the saved coordinates, if they are greater than {@link
     * #MOVE_THRESHOLD}
     * we prevent the toggle from windowed mode to fullscreen mode or vice versa, the idea
     * being that the user is either dragging the image or using pinch-to-zoom.
     *
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

                            Intent intent = new Intent(Consts.BROADCAST_TOGGLE_FULLSCREEN);
                            intent.putExtra(Consts.EXTRA_IS_SYSTEM_UI_VISIBLE,
                                mSystemUiStateProvider.isSystemUiVisible());
                            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(event.getX() - mDownXPos) > MOVE_THRESHOLD
                            || Math.abs(event.getY() - mDownYPos) > MOVE_THRESHOLD) {
                            mCancelClick = true;
                        }
                        break;
                }

                // Return false so that we still let the WebView consume the event
                return false;
            }
        };
    }

    public void loadImage(Image image) {
        if (image == null) {
            Log.e(TAG, "Received null url in loadImage(String imageUrl)");
            mProgress.setVisibility(View.GONE);
            mErrorMessage.setVisibility(View.VISIBLE);
            return;
        }

        mResolvedImage = image;
        mResolvedImageUrl = image.getSize(ImageSize.ORIGINAL);

        if (ImageUtil.isGif(mResolvedImageUrl)) {
            loadGifInWebView(mResolvedImageUrl);
        } else {
            Picasso.with(getActivity()).load(mResolvedImageUrl).resize(mImageView.getWidth(), mImageView.getHeight()).centerInside().placeholder(R.drawable.loading_spinner_76).into(mImageView, new Callback() {
                @Override
                public void onSuccess() {
                    if (mProgress != null) mProgress.setVisibility(View.GONE);
                }

                @Override
                public void onError() {
                    if (mErrorMessage != null) mErrorMessage.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    public void loadGifInWebView(String imageUrl) {
        if (mViewStub.getParent() != null) mWebView = (WebView) mViewStub.inflate();

        initializeWebView(mWebView);
        mImageView.setVisibility(View.GONE);
        mWebView.loadData(getHtmlForImageDisplay(imageUrl), "text/html", "utf-8");
    }

    public String getHtmlForImageDisplay(String imageUrl) {
        return String.format(Consts.WEBVIEW_IMAGE_HTML, imageUrl);
    }

    @SuppressLint("SetJavaScriptEnabled") @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void initializeWebView(WebView webview) {
        assert webview != null : "WebView should not be null!";

        WebSettings settings = webview.getSettings();
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);

        if (Util.hasHoneycomb()) settings.setDisplayZoomControls(false);

        webview.setBackgroundColor(Color.BLACK);
        webview.setVisibility(View.VISIBLE);
        webview.setOnTouchListener(getWebViewOnTouchListener());
    }

    //@formatter:off
    /**
     * This BroadcastReceiver handles updating the score when a vote is cast or changed
     */
    private BroadcastReceiver mScoreUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(Consts.EXTRA_PERMALINK) &&
                intent.hasExtra(Consts.EXTRA_SCORE) &&
                mImage != null &&
                mImage.getPermalink().equals(intent.getStringExtra(Consts.EXTRA_PERMALINK))) {

                if (mPostVotes != null) {
                    Log.i(TAG, "Updating score to " + intent.getIntExtra(Consts.EXTRA_SCORE, 0));
                    mPostVotes.setText("" + intent.getIntExtra(Consts.EXTRA_SCORE, 0));
                }
            }
        }
    };

    private BroadcastReceiver mToggleFullscreenIntent = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isSystemUiVisible =
                intent.getBooleanExtra(Consts.EXTRA_IS_SYSTEM_UI_VISIBLE, false);
            if (isSystemUiVisible) {
                hidePostDetails();
            } else {
                showPostDetails();
            }
        }
    };

    //@formatter:on

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
            Log.d(TAG, "doInBackground - starting work");

            data = params[0];
            Image resolvedImage = null;
            Log.i(TAG, "ResolveImageTask url = " + data);
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

            Log.d(TAG, "doInBackground - finished work");

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
