package com.antew.redditinpictures.library.ui;

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
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.antew.redditinpictures.device.ScreenSize;
import com.antew.redditinpictures.library.enums.ImageSize;
import com.antew.redditinpictures.library.image.Image;
import com.antew.redditinpictures.library.image.ImageResolver;
import com.antew.redditinpictures.library.imgur.ImgurAlbumApi.Album;
import com.antew.redditinpictures.library.interfaces.SystemUiStateProvider;
import com.antew.redditinpictures.library.reddit.PostData;
import com.antew.redditinpictures.library.ui.base.BaseFragment;
import com.antew.redditinpictures.library.utils.Constants;
import com.antew.redditinpictures.library.utils.ImageUtil;
import com.antew.redditinpictures.library.utils.Ln;
import com.antew.redditinpictures.library.utils.Util;
import com.antew.redditinpictures.pro.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
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
    protected boolean mPauseWork        = false;
    protected String  mResolvedImageUrl = null;
    protected Image   mResolvedImage    = null;
    protected int   mActionBarHeight;
    protected Album mAlbum;
    protected AsyncTask<String, Void, Image> mResolveImageTask = null;
    protected                                     SystemUiStateProvider mSystemUiStateProvider;
    @InjectView(R.id.pb_progress)                 ProgressBar           mProgress;
    @InjectView(R.id.rl_post_information_wrapper) RelativeLayout        mPostInformationWrapper;
    @InjectView(R.id.tv_post_title)               TextView              mPostTitle;
    @InjectView(R.id.tv_post_information)         TextView              mPostInformation;
    @InjectView(R.id.btn_view_gallery)            Button                mBtnViewGallery;
    @InjectView(R.id.webview_stub)                ViewStub              mViewStub;
    @InjectView(R.id.tv_post_votes)               TextView              mPostVotes;
    /**
     * This BroadcastReceiver handles updating the score when a vote is cast or changed
     */
    private BroadcastReceiver mScoreUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(Constants.EXTRA_PERMALINK) &&
                intent.hasExtra(Constants.EXTRA_SCORE) &&
                mImage != null &&
                mImage.getPermalink().equals(intent.getStringExtra(Constants.EXTRA_PERMALINK))) {

                if (mPostVotes != null) {
                    mPostVotes.setText("" + intent.getIntExtra(Constants.EXTRA_SCORE, 0));
                }
            }
        }
    };
    @InjectView(R.id.tv_error_message) TextView   mErrorMessage;
    @Inject                            ScreenSize mScreenSize;
    private boolean           mExitTasksEarly         = false;
    private boolean           mCancelClick            = false;
    private float             mDownXPos               = 0;
    private float             mDownYPos               = 0;
    private BroadcastReceiver mToggleFullscreenIntent = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isSystemUiVisible = intent.getBooleanExtra(Constants.EXTRA_IS_SYSTEM_UI_VISIBLE, false);
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
        return view;
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        populatePostData(mPostInformationWrapper);

        if (savedInstanceState != null) {
            loadSavedInstanceState(savedInstanceState);
        }

        final Activity act = getActivity();

        LocalBroadcastManager.getInstance(act).registerReceiver(mScoreUpdateReceiver, new IntentFilter(Constants.BROADCAST_UPDATE_SCORE));
        LocalBroadcastManager.getInstance(act)
                             .registerReceiver(mToggleFullscreenIntent, new IntentFilter(Constants.BROADCAST_TOGGLE_FULLSCREEN));

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
            Ln.e(e, "The activity must implement the SystemUiStateProvider interface");
        }

        // Use the parent activity to load the image asynchronously into the ImageView (so a single
        // cache can be used over all pages in the ViewPager
        if (ImageViewerActivity.class.isInstance(getActivity())) {
            resolveImage();
        }
    }

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
                Intent intent = new Intent(Constants.BROADCAST_TOGGLE_FULLSCREEN);
                intent.putExtra(Constants.EXTRA_IS_SYSTEM_UI_VISIBLE, mSystemUiStateProvider.isSystemUiVisible());
                LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
            }
        };
    }

    protected abstract void resolveImage();

    public void loadImage(Image image) {
        if (image == null) {
            Ln.e("Received null url in loadImage(String imageUrl)");
            mProgress.setVisibility(View.GONE);
            mErrorMessage.setVisibility(View.VISIBLE);
            return;
        }

        mResolvedImage = image;
        mResolvedImageUrl = image.getSize(ImageSize.ORIGINAL);

        if (ImageUtil.isGif(mResolvedImageUrl)) {
            Picasso.with(getActivity()).load(R.drawable.loading_spinner_76).into(mImageView);
            loadGifInWebView(mResolvedImageUrl);
        } else {
            Picasso.with(getActivity())
                   .load(mResolvedImageUrl)
                   .resize(mScreenSize.getWidth(), mScreenSize.getHeight())
                   .centerInside()
                   .into(mImageView, new Callback() {
                       @Override
                       public void onSuccess() {
                           if (mProgress != null) {
                               mProgress.setVisibility(View.GONE);
                           }
                       }

                       @Override
                       public void onError() {
                           if (mErrorMessage != null) {
                               mErrorMessage.setVisibility(View.VISIBLE);
                           }
                       }
                   });
        }
    }

    public void loadGifInWebView(String imageUrl) {
        if (mViewStub.getParent() != null) {
            mWebView = (WebView) mViewStub.inflate();
        }

        initializeWebView(mWebView);
        mWebView.loadData(getHtmlForImageDisplay(imageUrl), "text/html", "utf-8");
        mImageView.setVisibility(View.GONE);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB) public void initializeWebView(WebView webview) {
        assert webview != null : "WebView should not be null!";

        WebSettings settings = webview.getSettings();
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(true);
        if (Util.hasHoneycomb()) {
            settings.setDisplayZoomControls(false);
        }
        webview.setBackgroundColor(Color.BLACK);
        webview.setVisibility(View.VISIBLE);
        webview.setOnTouchListener(getWebViewOnTouchListener());
    }

    //@formatter:off

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

                            Intent intent = new Intent(Constants.BROADCAST_TOGGLE_FULLSCREEN);
                            intent.putExtra(Constants.EXTRA_IS_SYSTEM_UI_VISIBLE, mSystemUiStateProvider.isSystemUiVisible());
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
