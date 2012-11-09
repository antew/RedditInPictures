package com.antew.redditinpictures.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

import com.antew.redditinpictures.library.reddit.RedditApi.PostData;
import com.antew.redditinpictures.library.ui.ImageDetailFragment;
import com.antew.redditinpictures.library.ui.ImgurAlbumActivity;
import com.antew.redditinpictures.preferences.SharedPreferencesHelperFree;
import com.antew.redditinpictures.util.AdUtil;
import com.antew.redditinpictures.util.Consts;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class ImageDetailFragmentFree extends ImageDetailFragment {
    public static final String TAG = ImageDetailFragmentFree.class.getSimpleName();
    private AdView adView;

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
            adView = new AdView(getActivity(), AdSize.SMART_BANNER, Consts.ADMOB_ID);
            
            /**
             * The AdView should be attached to the bottom of the screen
             */
            RelativeLayout.LayoutParams adParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            adView.setLayoutParams(adParams);
            v.addView(adView, adParams);
        }
        
        return v;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (adView != null)
            adView.destroy();
    }
    
    @Override
    public void hidePostDetails() {
        super.hidePostDetails();
        if (adView != null) {
            adView.setVisibility(View.VISIBLE);
            adView.loadAd(AdUtil.getAdRequest());
        }
    }
    
    @Override
    public void showPostDetails() {
        super.showPostDetails();
        if (adView != null)
            adView.setVisibility(View.GONE);
    }
    
    @Override
    public Class<? extends ImgurAlbumActivity> getImgurAlbumActivity() {
        return ImgurAlbumActivityFree.class;
    }
}
