package com.antew.redditinpictures.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.antew.redditinpictures.R;
import com.antew.redditinpictures.library.ui.ImageDetailActivity;
import com.antew.redditinpictures.library.ui.ImageGridFragment;
import com.antew.redditinpictures.preferences.SharedPreferencesHelperFree;
import com.antew.redditinpictures.util.AdUtil;
import com.antew.redditinpictures.util.ConstsFree;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class ImageGridFragmentFree extends ImageGridFragment {
    public static final String TAG = ImageGridFragmentFree.class.getSimpleName();
    private AdView mAdView;
    private GridView mGridView;
    
    private BroadcastReceiver mHideAds = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mAdView != null) {
                mAdView.setVisibility(View.GONE);
                mAdView.destroy();
            }
            
            // Remove the margin from the GridView
            if (mGridView != null) {
                RelativeLayout.LayoutParams gridViewParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                gridViewParams.setMargins(0, 0, 0, 0);
                mGridView.setLayoutParams(gridViewParams);
            }
        }
    };
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout v = (RelativeLayout) super.onCreateView(inflater, container, savedInstanceState);
        mGridView = (GridView) v.findViewById(R.id.gridView);
        
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mHideAds , new IntentFilter(ConstsFree.REMOVE_ADS));
        /**
         * If ads are disabled we don't need to load any
         */
        if (!SharedPreferencesHelperFree.getDisableAds(getActivity())) {
            mAdView = new AdView(getActivity(), AdSize.SMART_BANNER, ConstsFree.ADMOB_ID);

            /**
             * The AdView should be attached to the bottom of the screen, with the GridView position above it
             */
            RelativeLayout.LayoutParams adParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            v.addView(mAdView, adParams);
            
            /**
             * We use the onGlobalLayoutListener here in order to adjust the bottom margin of the GridView
             * so that when the user scrolls to the bottom of the GridView the last images are not obscured
             * by the AdView
             */
            mAdView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                
                @Override
                public void onGlobalLayout() {
                    if (mAdView != null && mGridView != null) {
                        int height = mAdView.getHeight();
                        if (height > 0) {
                            RelativeLayout.LayoutParams gridViewParams = (RelativeLayout.LayoutParams) mGridView.getLayoutParams();
                            gridViewParams.setMargins(0, 0, 0, height);
                            mGridView.setLayoutParams(gridViewParams);
                            mAdView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }
                    }
                }
            });
            mAdView.loadAd(AdUtil.getAdRequest());
        }

        return v;
    }
    
    /**
     * Unregister our BroadcastReceiver
     */
    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mHideAds);
        if (mAdView != null) {
            mAdView.destroy();
            mAdView = null;
        }
        super.onDestroy();
        
    }
    
    @Override
    public Class<? extends ImageDetailActivity> getImageDetailActivityClass() {
        return ImageDetailActivityFree.class;
    }

}
