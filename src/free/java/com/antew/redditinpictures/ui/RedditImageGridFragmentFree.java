package com.antew.redditinpictures.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.GridView;
import android.widget.RelativeLayout;
import com.antew.redditinpictures.library.model.Age;
import com.antew.redditinpictures.library.model.Category;
import com.antew.redditinpictures.library.ui.ImageDetailActivity;
import com.antew.redditinpictures.library.ui.RedditImageGridFragment;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.preferences.SharedPreferencesHelperFree;
import com.antew.redditinpictures.pro.R;
import com.antew.redditinpictures.util.AdUtil;
import com.antew.redditinpictures.util.ConstsFree;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class RedditImageGridFragmentFree extends RedditImageGridFragment {
    private AdView mAdView;

    // This code has to be repeated here since the method is static. Static methods and values are not inherited by subclasses.
    public static Fragment newInstance(String subreddit, Category category, Age age) {
        final Fragment f = new RedditImageGridFragmentFree();

        final Bundle args = new Bundle();
        args.putString(Constants.Extra.EXTRA_SUBREDDIT, subreddit);
        args.putString(Constants.Extra.EXTRA_CATEGORY, category.toString());
        args.putString(Constants.Extra.EXTRA_AGE, age.toString());
        f.setArguments(args);

        return f;
    }

    private BroadcastReceiver mHideAds = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mAdView != null) {
                mAdView.setVisibility(View.GONE);
                mAdView.destroy();
            }

            // Remove the margin from the GridView
            if (mGridView != null) {
                RelativeLayout.LayoutParams gridViewParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                                             ViewGroup.LayoutParams.MATCH_PARENT);
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
            RelativeLayout.LayoutParams adParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            v.addView(mAdView, adParams);

            /**
             * We use the onGlobalLayoutListener here in order to adjust the bottom margin of the GridView
             * so that when the user scrolls to the bottom of the GridView the last images are not obscured
             * by the AdView
             */
            mAdView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

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
