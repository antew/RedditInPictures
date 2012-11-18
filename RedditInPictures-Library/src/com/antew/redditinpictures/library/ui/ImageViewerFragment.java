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

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.androidquery.AQuery;
import com.antew.redditinpictures.library.R;
import com.antew.redditinpictures.library.imgur.ImgurAlbumApi.Album;
import com.antew.redditinpictures.library.imgur.ImgurOriginalFetcher;
import com.antew.redditinpictures.library.imgur.ImgurResolver;
import com.antew.redditinpictures.library.imgur.ImgurResolver.ImageSize;
import com.antew.redditinpictures.library.interfaces.SystemUiStateProvider;
import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.reddit.RedditApi.PostData;
import com.antew.redditinpictures.library.reddit.RedditApiManager;
import com.antew.redditinpictures.library.utils.AsyncTask;
import com.antew.redditinpictures.library.utils.Consts;
import com.antew.redditinpictures.library.utils.ImageContainer;
import com.antew.redditinpictures.library.utils.ImageFetcher;
import com.antew.redditinpictures.library.utils.ImageUtil;
import com.antew.redditinpictures.library.utils.ImageWorker;

/**
 * This fragment will populate the children of the ViewPager from {@link ImageDetailActivity}.
 */
public abstract class ImageViewerFragment extends SherlockFragment {
    public static final String      TAG               = "ImageViewerFragment";
    protected static final String   IMAGE_DATA_EXTRA  = "extra_image_data";
    protected static final String   IMAGE_ALBUM_EXTRA = "extra_image_album";
    protected PostData              mImage;
    protected ImageView             mImageView;
    protected ImageFetcher          mImageFetcher;
    protected ProgressBar           mProgress;
    protected RelativeLayout        mPostInformationWrapper;
    protected Button                mBtnViewGallery;
    protected ViewStub              mViewStub;
    protected WebView               mWebView;
    protected boolean               mPauseWork        = false;
    protected TextView              mVotes;
    protected TextView              mErrorMessage;
    private final Object            mPauseWorkLock    = new Object();
    private boolean                 mExitTasksEarly   = false;
    protected String                mResolvedImageUrl = null;
    protected int                   mActionBarHeight;
    protected Album                 mAlbum;

    private boolean                 mCancelClick      = false;
    private float                   mDownXPos         = 0;
    private float                   mDownYPos         = 0;
    private static float            MOVE_THRESHOLD;
    protected SystemUiStateProvider mSystemUiStateProvider;

    /**
     * Empty constructor as per the Fragment documentation
     */
    public ImageViewerFragment() {}

    protected abstract void resolveImage();

    protected abstract void populatePostData(View v);

    protected abstract void downloadImage(Intent intent);
    
    protected abstract boolean shouldShowPostInformation();

    /**
     * Populate image using a url from extras, use the convenience factory method
     * {@link ImageViewerFragment#newInstance(String)} to create this fragment.
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
        if (mSystemUiStateProvider.isSystemUiVisible())
            showPostDetails();
        else
            hidePostDetails();
    }
    public void loadExtras() {
        mImage = getArguments() != null ? (PostData) getArguments().getParcelable(IMAGE_DATA_EXTRA) : null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        // Inflate and locate the main ImageView
        final View v = inflater.inflate(R.layout.image_detail_fragment, container, false);
        //@formatter:off
        mProgress               = (ProgressBar)    v.findViewById(R.id.progress);
        mBtnViewGallery         = (Button)         v.findViewById(R.id.btn_view_gallery);
        mImageView              = (PhotoView)      v.findViewById(R.id.imageView);
        mViewStub               = (ViewStub)       v.findViewById(R.id.webview_stub); 
        mImageView              = (ImageView)      v.findViewById(R.id.imageView);
        mVotes                  = (TextView)       v.findViewById(R.id.post_votes);
        mPostInformationWrapper = (RelativeLayout) v.findViewById(R.id.post_information_wrapper);
        mErrorMessage           = (TextView)       v.findViewById(R.id.error_message);
        //@formatter:on

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        populatePostData(mPostInformationWrapper);
        
        if (savedInstanceState != null)
            loadSavedInstanceState(savedInstanceState);

        final Activity act = getActivity();

        LocalBroadcastManager.getInstance(act).registerReceiver(mScoreUpdateReceiver, new IntentFilter(Consts.BROADCAST_UPDATE_SCORE));
        LocalBroadcastManager.getInstance(act).registerReceiver(mToggleFullscreenIntent, new IntentFilter(Consts.BROADCAST_TOGGLE_FULLSCREEN));
        LocalBroadcastManager.getInstance(act).registerReceiver(mDownloadImageIntent, new IntentFilter(Consts.BROADCAST_DOWNLOAD_IMAGE));

        // Set up on our tap listener for the PhotoView which we use to toggle between fullscreen
        // and windowed mode
        ((PhotoView) mImageView).setOnPhotoTapListener(getOnPhotoTapListener(act));

        MOVE_THRESHOLD = 20 * getResources().getDisplayMetrics().density;

        // Calculate ActionBar height
        TypedValue tv = new TypedValue();
        if (getActivity().getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
            mActionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getActivity().getResources().getDisplayMetrics());
        }

        try {
            mSystemUiStateProvider = (SystemUiStateProvider) getActivity();
        } catch (ClassCastException e) {
            Log.e(TAG, "The activity must implement the SystemUiStateProvider interface", e);
        }
        
        // Use the parent activity to load the image asynchronously into the ImageView (so a single
        // cache can be used over all pages in the ViewPager
        if (ImageViewerActivity.class.isInstance(getActivity())) {
            mImageFetcher = ((ImageViewerActivity) getActivity()).getImageFetcher();
            resolveImage();
        }
    }

    private OnPhotoTapListener getOnPhotoTapListener(final Activity activity) {
        return new OnPhotoTapListener() {

            @Override
            public void onPhotoTap(View view, float x, float y) {
                Intent intent = new Intent(Consts.BROADCAST_TOGGLE_FULLSCREEN);
                intent.putExtra(Consts.EXTRA_IS_SYSTEM_UI_VISIBLE, mSystemUiStateProvider.isSystemUiVisible());
                LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
            }
        };
    }

    protected void loadSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(IMAGE_DATA_EXTRA))
            mImage = savedInstanceState.getParcelable(IMAGE_DATA_EXTRA);

        if (savedInstanceState.containsKey(IMAGE_ALBUM_EXTRA))
            mAlbum = savedInstanceState.getParcelable(IMAGE_ALBUM_EXTRA);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState");
        if (mImage != null)
            outState.putParcelable(IMAGE_DATA_EXTRA, mImage);

        if (mAlbum != null)
            outState.putParcelable(IMAGE_ALBUM_EXTRA, mAlbum);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mScoreUpdateReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mToggleFullscreenIntent);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mDownloadImageIntent);

        if (mImageView != null) {
            ImageWorker.cancelWork(mImageView);
            mImageView.setImageDrawable(null);
        }

        if (mWebView != null)
            mWebView.destroy();
        
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
                            intent.putExtra(Consts.EXTRA_IS_SYSTEM_UI_VISIBLE, mSystemUiStateProvider.isSystemUiVisible());
                            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(event.getX() - mDownXPos) > MOVE_THRESHOLD || Math.abs(event.getY() - mDownYPos) > MOVE_THRESHOLD)
                            mCancelClick = true;
                        break;
                }

                // Return false so that we still let the WebView consume the event
                return false;
            }
        };
    }

    public void loadImage(ImageContainer image) {
        if (image == null) {
            Log.e(TAG, "Received null ImageContainer in loadImage(ImageContainer image)");
            mProgress.setVisibility(View.GONE);
            mErrorMessage.setVisibility(View.VISIBLE);
            return;
        }
            
        mResolvedImageUrl = ImgurResolver.getSize(image, ImageSize.ORIGINAL);

        if (ImageUtil.isGif(mResolvedImageUrl)) {
            mWebView = (WebView) mViewStub.inflate();
            mWebView.getSettings().setRenderPriority(RenderPriority.HIGH);
            mWebView.getSettings().setLoadWithOverviewMode(true);
            mWebView.getSettings().setUseWideViewPort(true);
            mWebView.setVisibility(View.VISIBLE);
            mImageView.setVisibility(View.GONE);
            mWebView.setOnTouchListener(getWebViewOnTouchListener());
            new AQuery(getActivity()).id(mWebView).progress(mProgress).webImage(mResolvedImageUrl);
        } else {
            mImageFetcher.loadImage(mResolvedImageUrl, mImageView, mProgress);
        }

    }



    //@formatter:off
    /**
     * This BroadcastReceiver handles updating the score when a vote is cast or changed
     */
    private BroadcastReceiver   mScoreUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(Consts.EXTRA_PERMALINK) && 
                    intent.hasExtra(Consts.EXTRA_SCORE) &&
                    mImage != null &&
                    mImage.getPermalink().equals(intent.getStringExtra(Consts.EXTRA_PERMALINK))) {

                if (mVotes != null) {
                    Log.i(TAG, "Updating score to " + intent.getIntExtra(Consts.EXTRA_SCORE, 0));
                    mVotes.setText("" + intent.getIntExtra(Consts.EXTRA_SCORE, 0));
                }
            }
        }
    };

    private BroadcastReceiver   mToggleFullscreenIntent = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isSystemUiVisible = intent.getBooleanExtra(Consts.EXTRA_IS_SYSTEM_UI_VISIBLE, false);
            if (isSystemUiVisible)
                hidePostDetails();
            else
                showPostDetails();
        }
    };
    
    private BroadcastReceiver   mDownloadImageIntent = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received download image intent!");
            downloadImage(intent);
        }
    };
    //@formatter:on

    /**
     * The actual AsyncTask that will asynchronously resolve the URL we should display.
     */
    class ResolveImageTask extends AsyncTask<String, Void, ImageContainer> {
        private String data;

        public ResolveImageTask() {}

        /**
         * Background processing.
         */
        @Override
        protected ImageContainer doInBackground(String... params) {
            Log.d(TAG, "doInBackground - starting work");

            data = params[0];
            ImageContainer image = null;
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
                image = ((ImgurOriginalFetcher) mImageFetcher).decode(data);
            }

            Log.d(TAG, "doInBackground - finished work");

            return image;
        }

        /**
         * Once we're resolved the URL, pass it to our load function to download/display it
         */
        @Override
        protected void onPostExecute(ImageContainer image) {
            // if cancel was called on this task or the "exit early" flag is set then we're done
            if (isCancelled() || mExitTasksEarly) {
                image = null;
            }

            loadImage(image);

        }

        @Override
        protected void onCancelled(ImageContainer image) {
            super.onCancelled(image);
            synchronized (mPauseWorkLock) {
                mPauseWorkLock.notifyAll();
            }
        }
    }

    /**
     * The actual AsyncTask that will asynchronously resolve the URL we should display.
     */
    class DownloadImageTask extends AsyncTask<String, Void, String> {
        private String data;
        private String filename;

        public DownloadImageTask() {}

        /**
         * Background processing.
         */
        @Override
        protected String doInBackground(String... params) {
            StringBuilder returnVal = new StringBuilder();

            data = params[0];
            filename = params[1];

            Log.d(TAG, "DownloadImageTask - doInBackground - starting work");

            String url = null;
            if (mResolvedImageUrl != null) {
                Log.i(TAG, "DownloadImageTask - Resolved image = " + mResolvedImageUrl);
                url = mResolvedImageUrl;
            } else {
                Log.i(TAG, "DownloadImageTask - Using passed in URL = " + data);
                url = ((ImgurOriginalFetcher) mImageFetcher).decodeUrl(data);
                Log.i(TAG, "DownloadImageTask - Passed in URL Resolved To = " + mResolvedImageUrl);
            }

            String fileExtension = url.substring(url.lastIndexOf("."));
            Log.i(TAG, "DownloadImageTask - fileExtension = " + fileExtension);

            File file = RedditApiManager.getPicturesDirectory();
            File destination = new File(file, filename + fileExtension);
            FileOutputStream fos = null;
            Activity act = getActivity();
            try {
                fos = new FileOutputStream(destination);
                boolean result = ((ImgurOriginalFetcher) mImageFetcher).downloadUrlToStream(url, fos);

                if (result) {
                    returnVal.append("File downloaded to: " + destination.getAbsolutePath());
                    if (act != null)
                        MediaScannerConnection.scanFile(act, new String[] { destination.getAbsolutePath() }, new String[] {"image/*"}, null);
                }
                else
                    returnVal.append("Download failed :(");

            } catch (FileNotFoundException e) {
                Log.e(TAG, "DownloadImageTask - Error creating file", e);
            }

            Log.d(TAG, "DownloadImageTask - doInBackground - finished work");
            return returnVal.toString();
        }

        /**
         * Once we're resolved the URL, pass it to our load function to download/display it
         */
        @Override
        protected void onPostExecute(String result) {
            Activity act = getActivity();
            if (act != null)
                Toast.makeText(act, result, Toast.LENGTH_SHORT).show();
        }

    }
}
