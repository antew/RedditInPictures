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
import android.widget.RelativeLayout;
import com.antew.redditinpictures.library.model.Age;
import com.antew.redditinpictures.library.model.Category;
import com.antew.redditinpictures.library.ui.ImageDetailActivity;
import com.antew.redditinpictures.library.ui.RedditImageListFragment;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.preferences.SharedPreferencesHelperFree;
import com.antew.redditinpictures.util.AdUtil;
import com.antew.redditinpictures.util.ConstsFree;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class RedditImageListFragmentFree extends RedditImageListFragment {
    private AdView mAdView;

    // This code has to be repeated here since the method is static. Static methods and values are not inherited by subclasses.
    public static Fragment newInstance(String subreddit, Category category, Age age) {
        final Fragment f = new RedditImageListFragmentFree();

        final Bundle args = new Bundle();
        args.putString(Constants.Extra.EXTRA_SUBREDDIT, subreddit);
        args.putString(Constants.Extra.EXTRA_CATEGORY, category.toString());
        // Age is null when we're sorting as 'New' or 'Rising'
        args.putString(Constants.Extra.EXTRA_AGE, age == null ? null : age.toString());
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
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout v = (RelativeLayout) super.onCreateView(inflater, container, savedInstanceState);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mHideAds, new IntentFilter(ConstsFree.REMOVE_ADS));
        /**
         * If ads are disabled we don't need to load any
         */
        if (!SharedPreferencesHelperFree.getDisableAds(getActivity())) {
            mAdView = new AdView(getActivity(), AdSize.SMART_BANNER, ConstsFree.ADMOB_ID);

            /**
             * The AdView should be attached to the bottom of the screen, with the GridView position above it
             */
            RelativeLayout.LayoutParams adParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                                                                   ViewGroup.LayoutParams.WRAP_CONTENT);
            adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            v.addView(mAdView, adParams);

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
