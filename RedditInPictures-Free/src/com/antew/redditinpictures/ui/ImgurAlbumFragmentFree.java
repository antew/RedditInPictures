package com.antew.redditinpictures.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

import com.antew.redditinpictures.library.imgur.ImgurImageApi.ImgurImage;
import com.antew.redditinpictures.library.reddit.RedditApi.PostData;
import com.antew.redditinpictures.library.ui.ImgurAlbumFragment;
import com.antew.redditinpictures.preferences.SharedPreferencesHelperFree;
import com.antew.redditinpictures.util.AdUtil;
import com.antew.redditinpictures.util.Consts;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class ImgurAlbumFragmentFree extends ImgurAlbumFragment {
    public static final String TAG = ImgurAlbumFragmentFree.class.getSimpleName();
    private AdView adView;

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
    
}