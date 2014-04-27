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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.antew.redditinpictures.device.ScreenSize;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.image.Image;
import com.antew.redditinpictures.library.image.ImageResolver;
import com.antew.redditinpictures.library.imgur.ImgurAlbumApi.Album;
import com.antew.redditinpictures.library.interfaces.SystemUiStateProvider;
import com.antew.redditinpictures.library.model.ImageSize;
import com.antew.redditinpictures.library.model.gfycat.GfycatImage;
import com.antew.redditinpictures.library.model.reddit.PostData;
import com.antew.redditinpictures.library.service.GfycatService;
import com.antew.redditinpictures.library.ui.base.BaseFragment;
import com.antew.redditinpictures.library.util.ImageDownloader;
import com.antew.redditinpictures.library.util.ImageUtil;
import com.antew.redditinpictures.library.util.Ln;
import com.antew.redditinpictures.library.util.Strings;
import com.antew.redditinpictures.pro.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.net.MalformedURLException;
import javax.inject.Inject;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

/**
 * This fragment will populate the children of the ViewPager from {@link ImageDetailActivity}.
 */
public abstract class ImageViewerFragment extends BaseFragment {
    protected static final String IMAGE_DATA_EXTRA  = "extra_image_data";
    protected static final String IMAGE_ALBUM_EXTRA = "extra_image_album";
    private final Object mPauseWorkLock = new Object();
    protected PostData mImage;
    protected boolean mPauseWork        = false;
    protected String  mResolvedImageUrl = null;
    protected Image   mResolvedImage    = null;
    protected int   mActionBarHeight;
    protected Album mAlbum;
    protected AsyncTask<String, Void, Image> mResolveImageTask = null;
    protected SystemUiStateProvider mSystemUiStateProvider;
    @InjectView(R.id.iv_imageView)
    protected ImageView             mImageView;
    @InjectView(R.id.vv_video)
    protected VideoView             mVideoView;
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

        // Calculate ActionBar height
        TypedValue tv = new TypedValue();
        if (getActivity().getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
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
            animate(mPostInformationWrapper).setDuration(500).y(mActionBarHeight);
        }
    }

    public void hidePostDetails() {
        animate(mPostInformationWrapper).setDuration(500).y(-400);
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
            mResolvedImageUrl = mResolvedImage.getSize(ImageSize.ORIGINAL);

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
        showProgress();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final GfycatImage gfycatImage = GfycatService.convertGif(getActivity(), imageUrl);

                    if (gfycatImage != null) {
                        final Uri videoUri = Uri.parse(gfycatImage.getMp4Url());
                        mVideoView.post(new Runnable() {
                            @Override
                            public void run() {
                                mVideoView.setVideoURI(videoUri);
                                mVideoView.setVisibility(View.VISIBLE);
                                hideProgress();
                            }
                        });
                        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                mVideoView.start();
                            }
                        });
                        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                mVideoView.start();
                            }
                        });
                        mVideoView.setOnTouchListener(getVideoViewOnTouchListener());
                        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                            @Override
                            public boolean onError(MediaPlayer mp, int what, int extra) {
                                final Uri videoUri = Uri.parse(gfycatImage.getWebmUrl());
                                mVideoView.setVideoURI(videoUri);
                                return true;
                            }
                        });
                    }
                } catch (MalformedURLException e) {
                    Ln.e(e);
                }
            }
        }).start();
    }

    protected void hideProgress() {
        if (mProgress != null) {
            mProgress.setVisibility(View.GONE);
        }
    }

    protected void showProgress() {
        if (mProgress != null) {
            mProgress.setVisibility(View.VISIBLE);
        }
    }

    /**
     * This handles receiving the touch events in the VideoView so that we can
     * toggle between fullscreen and windowed mode.
     * <p/>
     *
     * @return The {@link OnTouchListener}
     */
    public OnTouchListener getVideoViewOnTouchListener() {
        return new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Intent intent = new Intent(Constants.Broadcast.BROADCAST_TOGGLE_FULLSCREEN);
                        intent.putExtra(Constants.Extra.EXTRA_IS_SYSTEM_UI_VISIBLE, mSystemUiStateProvider.isSystemUiVisible());
                        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                        break;
                }

                // Return false so that we still let the WebView consume the event
                return false;
            }
        };
    }

    public String getHtmlForImageDisplay(String imageUrl) {
        return Constants.WEBVIEW_IMAGE_HTML_BEGIN + imageUrl + Constants.WEBVIEW_IMAGE_HTML_END;
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
