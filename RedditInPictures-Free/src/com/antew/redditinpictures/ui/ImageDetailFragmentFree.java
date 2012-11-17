package com.antew.redditinpictures.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.RelativeLayout;

import com.antew.redditinpictures.library.reddit.RedditApi.PostData;
import com.antew.redditinpictures.library.ui.ImageDetailFragment;
import com.antew.redditinpictures.library.ui.ImgurAlbumActivity;
import com.antew.redditinpictures.preferences.SharedPreferencesHelperFree;
import com.antew.redditinpictures.util.AdUtil;
import com.antew.redditinpictures.util.ConstsFree;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class ImageDetailFragmentFree extends ImageDetailFragment {
    public static final String TAG = ImageDetailFragmentFree.class.getSimpleName();
    private AdView mAdView;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout v = (RelativeLayout) super.onCreateView(inflater, container, savedInstanceState);
        
        /**
         * If ads are disabled we don't need to load any
         */
        if (!SharedPreferencesHelperFree.getDisableAds(getActivity())) {
            mAdView = new AdView(getActivity(), AdSize.SMART_BANNER, ConstsFree.ADMOB_ID);
            
            /**
             * The AdView should be attached to the bottom of the screen
             */
            RelativeLayout.LayoutParams adParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            mAdView.setLayoutParams(adParams);
            v.addView(mAdView, adParams);
        }
        
        return v;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdView != null)
            mAdView.destroy();
    }
    
    @Override
    public void hidePostDetails() {
        super.hidePostDetails();
        if (mAdView != null) {
            RelativeLayout.LayoutParams imageParams = (RelativeLayout.LayoutParams) mImageView.getLayoutParams();
            mImageView.setLayoutParams(imageParams);
            mAdView.setVisibility(View.VISIBLE);
            mAdView.loadAd(AdUtil.getAdRequest());
            
            /**
             * We use the onGlobalLayoutListener here in order to adjust the bottom margin of the ImageView
             * so that the ad doesn't obscure the image
             */
            mAdView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                
                @Override
                public void onGlobalLayout() {
                    if (mAdView != null && mImageView != null) {
                        int height = mAdView.getHeight();
                        if (height > 0) {
                            RelativeLayout.LayoutParams imageParams = (RelativeLayout.LayoutParams) mImageView.getLayoutParams();
                            imageParams.setMargins(0, 0, 0, height);
                            mImageView.setLayoutParams(imageParams);
                            mAdView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }
                    }
                }
            });
        }
    }
    
    @Override
    public void showPostDetails() {
        super.showPostDetails();
        if (mAdView != null && mImageView != null) {
            mAdView.setVisibility(View.GONE);
            RelativeLayout.LayoutParams imageParams = (RelativeLayout.LayoutParams) mImageView.getLayoutParams();
            imageParams.setMargins(0, 0, 0, 0);
            mImageView.setLayoutParams(imageParams);
        }
    }
    
    @Override
    public Class<? extends ImgurAlbumActivity> getImgurAlbumActivity() {
        return ImgurAlbumActivityFree.class;
    }
}
