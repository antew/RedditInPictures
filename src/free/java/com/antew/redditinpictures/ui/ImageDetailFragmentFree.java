package com.antew.redditinpictures.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.antew.redditinpictures.library.image.Image;
import com.antew.redditinpictures.library.reddit.PostData;
import com.antew.redditinpictures.library.ui.ImageDetailFragment;
import com.antew.redditinpictures.library.ui.ImgurAlbumActivity;
import com.antew.redditinpictures.preferences.SharedPreferencesHelperFree;
import com.antew.redditinpictures.pro.R;
import com.antew.redditinpictures.util.AdUtil;
import com.antew.redditinpictures.util.ConstsFree;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class ImageDetailFragmentFree extends ImageDetailFragment {
    public static final String TAG = ImageDetailFragmentFree.class.getSimpleName();
    private AdView mAdView;
    private boolean mAdsDisabled;
    private RelativeLayout mWrapper;
    private Button mViewGalleryButton;

    /**
     * Factory method to generate a new instance of the fragment given an image number.
     * 
     * @param postData
     *            The post to load
     * @return A new instance of ImageDetailFragment with imageNum extras
     */
    public static ImageDetailFragmentFree newInstance(PostData image) {
        final ImageDetailFragmentFree f = new ImageDetailFragmentFree();

        final Bundle args = new Bundle();
        args.putParcelable(IMAGE_DATA_EXTRA, image);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mWrapper = (RelativeLayout) getView().findViewById(R.id.fragment_wrapper);
        mViewGalleryButton = (Button) getView().findViewById(R.id.btn_view_gallery);
        mAdsDisabled = SharedPreferencesHelperFree.getDisableAds(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        removeAdIfNeeded();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeAdIfNeeded();
    }

    @Override
    public void loadImage(Image image) {
        super.loadImage(image);
        displayAdIfNeeded();
    }

    private void displayAdIfNeeded() {
        /**
         * If ads are disabled we don't need to load any
         */
        if (!mAdsDisabled) {
            if (mAdView != null) {
                mAdView.setVisibility(View.VISIBLE);
                mAdView.loadAd(AdUtil.getAdRequest());
            } else {
                mAdView = new AdView(getActivity(), AdSize.SMART_BANNER, ConstsFree.ADMOB_ID);

                /**
                 * The AdView should be attached to the bottom of the screen
                 */
                RelativeLayout.LayoutParams adParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    adParams.bottomMargin = ConstsFree.getActionBarSize(getActivity());

                    if (mViewGalleryButton != null) {
                        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mViewGalleryButton.getLayoutParams();
                        lp.bottomMargin = ConstsFree.getActionBarSize(getActivity()) * 2;
                    }
                }

                mAdView.setLayoutParams(adParams);
                mWrapper.addView(mAdView, adParams);
                mAdView.setVisibility(View.VISIBLE);
                mAdView.loadAd(AdUtil.getAdRequest());

                RelativeLayout.LayoutParams imageViewParams = (RelativeLayout.LayoutParams) mImageView.getLayoutParams();
                imageViewParams.addRule(RelativeLayout.ABOVE, mAdView.getId());
                mImageView.setLayoutParams(imageViewParams);
            }
        } else {
            removeAdIfNeeded();
        }
    }

    private void removeAdIfNeeded() {
        if (mAdView != null) {
            mAdView.setVisibility(View.GONE);
            mAdView.destroy();
            mAdView = null;
        }
    }

    @Override
    public void showPostDetails() {
        super.showPostDetails();
        displayAdIfNeeded();
    }

    @Override
    public void hidePostDetails() {
        super.hidePostDetails();

        if (mAdView != null) {
            mAdView.setVisibility(View.GONE);
        }
    }

    @Override
    public Class<? extends ImgurAlbumActivity> getImgurAlbumActivity() {
        return ImgurAlbumActivityFree.class;
    }
    
}
