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

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.WindowManager.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.animation.FadeInThenOut;
import com.antew.redditinpictures.library.dialog.SaveImageDialogFragment;
import com.antew.redditinpictures.library.dialog.SaveImageDialogFragment.SaveImageDialogListener;
import com.antew.redditinpictures.library.interfaces.SystemUiStateProvider;
import com.antew.redditinpictures.library.ui.base.BaseFragmentActivity;
import com.antew.redditinpictures.library.util.AndroidUtil;
import com.antew.redditinpictures.library.util.ImageDownloader;
import com.antew.redditinpictures.library.widget.CustomViewPager;
import com.antew.redditinpictures.pro.R;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public abstract class ImageViewerActivity extends BaseFragmentActivity implements SaveImageDialogListener, SystemUiStateProvider {

    /**
     * 8 is a great number! Not only is it divisible by 4, but it is also equivalent to 2 * 2 * 2 you just can't beat that!
     */
    private static final int    POST_LOAD_OFFSET = 8;
    private static final String IMAGE_CACHE_DIR  = "images";

    /**
     * The Adapter for the ViewPager
     */
    protected FragmentStatePagerAdapter mAdapter;

    /**
     * The ViewPager which holds the fragments
     */
    protected CustomViewPager mPager;
    /**
     * This BroadcastReceiver handles toggling between fullscreen/windowed mode
     */
    private BroadcastReceiver mToggleFullscreenReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isSystemUiVisible = intent.getBooleanExtra(Constants.Extra.EXTRA_IS_SYSTEM_UI_VISIBLE, false);
            if (mPager != null) {
                if (isSystemUiVisible) {
                    goFullscreen();
                } else {
                    exitFullscreen();
                }
            }
        }
    };
    /**
     * The lock menu item, so we can change it between a unlocked icon and a highlighted locked
     * icon.
     */
    protected MenuItem lockViewPagerItem;
    /**
     * The images for the adapter
     */
    protected List<? extends Parcelable> mImages = null;
    /**
     * The 'crouton' TextView for displaying messages to the user.
     */
    protected TextView       mCrouton;
    /**
     * The calculated height of the Action Bar
     */
    protected int            mActionBarHeight;
    /**
     * The wrapper view
     */
    protected RelativeLayout mWrapper;

    @Inject
    public ImageDownloader mImageDownloader;

    /**
     * Whether swiping on the ViewPager is enabled
     */
    private boolean mSwipingEnabled = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_detail_pager);

        // Set up activity to go full screen
        getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);

        mCrouton = (TextView) findViewById(R.id.crouton);
        mWrapper = (RelativeLayout) findViewById(R.id.wrapper);

        registerLocalBroadcastReceivers();
        getExtras();
        initializeAdapter();
        initializeActionBar();
        initializeViewPager();

        invalidateOptionsMenu();
    }

    /**
     * Register BroadcastReceivers with the LocalBroadcastManager
     */
    private void registerLocalBroadcastReceivers() {
        LocalBroadcastManager.getInstance(this)
                             .registerReceiver(mToggleFullscreenReceiver,
                                               new IntentFilter(Constants.Broadcast.BROADCAST_TOGGLE_FULLSCREEN));
    }

    /**
     * Get the extras from the intent and do whatever necessary
     */
    public abstract void getExtras();

    /**
     * Initialize the Adapter and ViewPager for the ViewPager
     */
    public void initializeAdapter() {
        mAdapter = getPagerAdapter();
        mPager = (CustomViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.setPageMargin((int) getResources().getDimension(R.dimen.image_detail_pager_margin));
        mPager.setPageMarginDrawable(R.drawable.background_holo_dark);
        mPager.setOffscreenPageLimit(2);
        mPager.setOnPageChangeListener(getViewPagerOnPageChangeListener());
    }

    /**
     * Enable some additional newer visibility and ActionBar features to create a more
     * immersive photo viewing experience.
     * <p/>
     * Initialize the mActionBarHeight variable.
     */
    private void initializeActionBar() {
        final ActionBar actionBar = getSupportActionBar();

        // Hide title text and set home as up
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.reddit_in_pictures);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Calculate ActionBar height
        TypedValue tv = new TypedValue();
        if (getTheme() != null && getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
            mActionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initializeViewPager() {
        moveViewPagerToSelectedIndex();
        // Hide and show the ActionBar as the visibility changes
        if (AndroidUtil.hasHoneycomb()) {
            mPager.setOnSystemUiVisibilityChangeListener(getOnSystemUiVisibilityChangeListener());
        }
    }

    /**
     * Get the adapter for the ViewPager. It is expected that it will return a subclass of
     * FragmentStatePagerAdapter
     *
     * @return The adapter for the ViewPager
     */
    public abstract FragmentStatePagerAdapter getPagerAdapter();

    /**
     * Get the page change listener for the ViewPager. By default it changes between
     * fullscreen/windowed mode
     *
     * @return
     */
    protected OnPageChangeListener getViewPagerOnPageChangeListener() {
        OnPageChangeListener viewPagerOnPageChangeListener = new OnPageChangeListener() {

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {}

            @Override
            public void onPageSelected(int position) {
                updateDisplay(position);

                if (position >= (mAdapter.getCount() - POST_LOAD_OFFSET)) {
                    reachedCloseToLastPage();
                }
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {}
        };

        return viewPagerOnPageChangeListener;
    }

    /**
     * Set the current item based on the extra passed in to this activity
     */
    private void moveViewPagerToSelectedIndex() {
        final int extraCurrentItem = getIntent().getIntExtra(Constants.Extra.EXTRA_IMAGE, -1);
        if (extraCurrentItem != -1) {
            mPager.setCurrentItem(extraCurrentItem);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public OnSystemUiVisibilityChangeListener getOnSystemUiVisibilityChangeListener() {
        return new OnSystemUiVisibilityChangeListener() {

            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
                    getSupportActionBar().hide();
                } else {
                    getSupportActionBar().show();
                }
            }
        };
    }

    /**
     * Update the display when the user switches pages in the ViewPager. See
     * {@link ImageViewerActivity#getViewPagerOnPageChangeListener()}
     *
     * @param position
     */
    protected abstract void updateDisplay(int position);

    /**
     * Called upon reaching the last page present in the ViewPager
     */
    public abstract void reachedCloseToLastPage();

    @Override
    public void onResume() {
        super.onResume();
        setSwipingState(mSwipingEnabled, false);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mToggleFullscreenReceiver);
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(Constants.Extra.EXTRA_ENTRIES, (ArrayList<? extends Parcelable>) mImages);
        outState.putBoolean(Constants.Extra.EXTRA_IS_SWIPING_ENABLED, mPager.isSwipingEnabled());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(Constants.Extra.EXTRA_ENTRIES)) {
            mImages = savedInstanceState.getParcelableArrayList(Constants.Extra.EXTRA_ENTRIES);
        }

        if (savedInstanceState.containsKey(Constants.Extra.EXTRA_IS_SWIPING_ENABLED)) {
            mSwipingEnabled = savedInstanceState.getBoolean(Constants.Extra.EXTRA_IS_SWIPING_ENABLED);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getSupportMenuInflater().inflate(R.menu.image_view_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // We save the icon for locking the view pager so that we can reference
        // it when we receive a broadcast message to toggle the ViewPager lock state
        lockViewPagerItem = menu.findItem(R.id.lock_viewpager);

        // Update the icon depending on whether swiping is enabled or disabled
        if (mSwipingEnabled) {
            lockViewPagerItem.setTitle(R.string.disable_swiping);
            lockViewPagerItem.setIcon(R.drawable.ic_action_lock_open_dark);
        } else {
            lockViewPagerItem.setTitle(R.string.enable_swiping);
            lockViewPagerItem.setIcon(R.drawable.ic_action_lock_closed_dark);
        }
        return true;
    }

    /**
     * Handler for when the user selects an item from the ActionBar.
     * <p>
     * The default functionality implements:<br>
     * - Toggling the swipe lock on the ViewPager via toggleViewPagerLock()<br>
     * - Sharing the post via the Android ACTION_SEND intent, the URL shared is provided by
     * subclasses via {@link #getUrlForSharing()}<br>
     * - Viewing the post in a Web browser (the URL is provided by subclasses from
     * {@link #getPostUri()} <br>
     * - Displaying a dialog to get the filename to use when saving an image, subclasses will
     * implement {@link #onFinishSaveImageDialog(String)}
     * </p>
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                EasyTracker.getInstance(this)
                           .send(
                               MapBuilder.createEvent(Constants.Analytics.Category.ACTION_BAR_ACTION, Constants.Analytics.Action.HOME, null,
                                                      null).build()
                                );
                finish();
                return true;
            case R.id.lock_viewpager:
                if (mPager.isSwipingEnabled()) {
                    EasyTracker.getInstance(this)
                               .send(MapBuilder.createEvent(Constants.Analytics.Category.ACTION_BAR_ACTION,
                                                            Constants.Analytics.Action.TOGGLE_SWIPING, Constants.Analytics.Label.DISABLED,
                                                            null).build());
                } else {
                    EasyTracker.getInstance(this)
                               .send(MapBuilder.createEvent(Constants.Analytics.Category.ACTION_BAR_ACTION,
                                                            Constants.Analytics.Action.TOGGLE_SWIPING, Constants.Analytics.Label.ENABLED,
                                                            null).build());
                }

                // Lock or unlock swiping in the ViewPager
                setSwipingState(!mPager.isSwipingEnabled(), true);
                return true;
            case R.id.share_post:
                EasyTracker.getInstance(this)
                           .send(
                               MapBuilder.createEvent(Constants.Analytics.Category.ACTION_BAR_ACTION, Constants.Analytics.Action.SHARE_POST,
                                                      getSubreddit(), null).build()
                                );
                String subject = getString(R.string.check_out_this_image);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                intent.putExtra(Intent.EXTRA_TEXT, subject + " " + getUrlForSharing());
                startActivity(Intent.createChooser(intent, getString(R.string.share_using_)));
                return true;
            case R.id.view_post:
                EasyTracker.getInstance(this)
                           .send(MapBuilder.createEvent(Constants.Analytics.Category.ACTION_BAR_ACTION,
                                                        Constants.Analytics.Action.OPEN_POST_EXTERNAL, getSubreddit(), null).build());
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, getPostUri());
                startActivity(browserIntent);
                return true;
            case R.id.save_post:
                EasyTracker.getInstance(this)
                           .send(
                               MapBuilder.createEvent(Constants.Analytics.Category.ACTION_BAR_ACTION, Constants.Analytics.Action.SAVE_POST,
                                                      getSubreddit(), null).build()
                                );
                handleSaveImage();
                return true;
            case R.id.report_image:
                EasyTracker.getInstance(this)
                           .send(MapBuilder.createEvent(Constants.Analytics.Category.ACTION_BAR_ACTION,
                                                        Constants.Analytics.Action.REPORT_POST, getSubreddit(), null).build()
                                );
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        reportCurrentItem();
                    }
                }).start();
                Toast.makeText(this, R.string.image_display_issue_reported, Toast.LENGTH_LONG).show();
                return true;
            default:
                return false;
        }
    }

    /**
     * Set whether swiping is enabled on the ViewPager.
     *
     * @param swipingEnabled
     *     Whether swiping should be enabled
     * @param showMessageToUser
     *     Whether to display a message to the user, set this to true if the user took direct action to change the state.
     */
    private void setSwipingState(boolean swipingEnabled, boolean showMessageToUser) {
        if (mAdapter != null && mPager != null) {
            mSwipingEnabled = swipingEnabled;
            mPager.setSwipingEnabled(mSwipingEnabled);
            if (showMessageToUser) {
                mCrouton.setText(mSwipingEnabled ? getString(R.string.swiping_enabled) : getString(R.string.swiping_disabled));
                FadeInThenOut.fadeInThenOut(mCrouton, 1500);
            }

            invalidateOptionsMenu();
        }
    }

    /**
     * Get the JSON representation of the current image/post in the ViewPager to report an error.
     *
     * @return The JSON representation of the currently viewed object.
     */
    protected abstract void reportCurrentItem();

    /**
     * Retrieve the current subreddit.
     * @return the current subreddit
     */
    public abstract String getSubreddit();

    /**
     * Get the URL of the current image in the ViewPager. Used in
     * {@link ImageViewerActivity#onOptionsItemSelected(MenuItem)}
     *
     * @return The URL of the current image in the ViewPager.
     */
    public abstract String getUrlForSharing();

    /**
     * Get the Uri for the page of the current post in the ViewPager.
     *
     * @return The Uri for the page
     */
    protected abstract Uri getPostUri();

    /**
     * Subclasses can choose how to handle the click of the 'Save' icon in the Action Bar.
     * The default action is to pop up a dialog prompting for a filename to save the image as
     *
     * @see ImageViewerActivity#getFilenameForSave()
     * @see SaveImageDialogFragment
     */
    public void handleSaveImage() {
        SaveImageDialogFragment saveImageDialog = SaveImageDialogFragment.newInstance(getFilenameForSave());
        saveImageDialog.show(getSupportFragmentManager(), Constants.Dialog.DIALOG_GET_FILENAME);
    }

    /**
     * Get the initial value for the filename prompt, by default it is an empty string
     *
     * @return The initial filename
     */
    public String getFilenameForSave() {
        return "";
    }

    /**
     * This method is expected to perform the actual saving of the image with the filename that was
     * returned.
     *
     * @param filename
     *     The filename that should be used when saving the image (without the extension)
     */
    @Override
    public abstract void onFinishSaveImageDialog(String filename);

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void goFullscreen() {
        EasyTracker.getInstance(this)
                   .send(MapBuilder.createEvent(Constants.Analytics.Category.UI_ACTION, Constants.Analytics.Action.TOGGLE_DETAILS,
                                                Constants.Analytics.Label.GO_FULLSCREEN, null).build());
        if (AndroidUtil.hasHoneycomb()) {
            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        } else {
            getSupportActionBar().hide();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void exitFullscreen() {
        EasyTracker.getInstance(this)
                   .send(MapBuilder.createEvent(Constants.Analytics.Category.UI_ACTION, Constants.Analytics.Action.TOGGLE_DETAILS,
                                                Constants.Analytics.Label.EXIT_FULLSCREEN, null).build());
        if (AndroidUtil.hasHoneycomb()) {
            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        } else {
            getSupportActionBar().show();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean isSystemUiVisible() {
        if (AndroidUtil.hasHoneycomb()) {
            final int vis = mPager.getSystemUiVisibility();
            if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
                return false;
            }
        } else {
            return getSupportActionBar().isShowing();
        }

        return true;
    }
}
