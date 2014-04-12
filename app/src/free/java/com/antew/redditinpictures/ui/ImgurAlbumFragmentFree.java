package com.antew.redditinpictures.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.antew.redditinpictures.library.image.Image;
import com.antew.redditinpictures.library.imgur.ImgurImageApi.ImgurImage;
import com.antew.redditinpictures.library.ui.ImgurAlbumFragment;
import com.antew.redditinpictures.preferences.SharedPreferencesHelperFree;
import com.antew.redditinpictures.pro.R;
import com.antew.redditinpictures.util.AdUtil;
import com.antew.redditinpictures.util.ConstsFree;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class ImgurAlbumFragmentFree extends ImgurAlbumFragment {
    public static final String TAG = ImgurAlbumFragmentFree.class.getSimpleName();
    private AdView mAdView;
    private boolean mAdsDisabled;
    private RelativeLayout mWrapper;

    /**
     * Factory method to generate a new instance of the fragment given an {@link ImgurImage}
     * 
     * @param postData
     *            The post to load
     * @return A new instance of ImageDetailFragment with imageNum extras
     */
    public static ImgurAlbumFragmentFree newInstance(ImgurImage image) {
        final ImgurAlbumFragmentFree f = new ImgurAlbumFragmentFree();
        final Bundle args = new Bundle();
        args.putParcelable(IMAGE_DATA_EXTRA, image);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mWrapper = (RelativeLayout) getView().findViewById(R.id.fragment_wrapper);
        mAdsDisabled = SharedPreferencesHelperFree.getDisableAds(getActivity());
    }

    @Override
    public void loadImage(Image image) {
        super.loadImage(image);
        displayAdIfNeeded();
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
    
}