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
import com.antew.redditinpictures.library.widget.CustomViewPager;
import com.antew.redditinpictures.pro.R;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import java.util.ArrayList;
import java.util.List;

;

public abstract class ImageViewerActivity extends BaseFragmentActivity implements SaveImageDialogListener, SystemUiStateProvider {

    private static final String IMAGE_CACHE_DIR = "images";

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
        if (getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
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
     * fullscreen/windowed mode depending on the value of {@link ImageViewerActivity#mIsFullscreen}
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

                if (position == (mAdapter.getCount() - 1)) {
                    reachedLastPage();
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
    public void reachedLastPage() {
        // Do nothing
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mToggleFullscreenReceiver);
    }

    /**
     * Get the image size to use for scaling.
     *
     * @return The requested image width/height
     */
    private int getImageWidthForResizing() {
        // Fetch screen height and width, to use as our max size when loading images as this activity runs full screen
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int height = displayMetrics.heightPixels;
        final int width = displayMetrics.widthPixels;

        // For this sample we'll use half of the longest width to resize our images. As the
        // image scaling ensures the image is larger than this, we should be left with a
        // resolution that is appropriate for both portrait and landscape. For best image quality
        // we shouldn't divide by 2, but this will use more memory and require a larger memory
        // cache.
        final int longest = (height > width ? height : width) / 2;

        return longest;
    }

    /**
     * Fix for bug where orientation change on 2.x would cause the indeterminate progress bar to
     * show
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setSupportProgressBarIndeterminateVisibility(false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(Constants.Extra.EXTRA_ENTRIES, (ArrayList<? extends Parcelable>) mImages);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(Constants.Extra.EXTRA_ENTRIES)) {
            mImages = savedInstanceState.getParcelableArrayList(Constants.Extra.EXTRA_ENTRIES);
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

        return true;
    }

    /**
     * Handler for when the user selects an item from the ActionBar.
     * <p>
     * The default functionality implements:<br>
     * - Toggling the swipe lock on the ViewPager via {@link #toggleViewPagerLock()}<br>
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
                toggleViewPagerLock();
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
            case R.id.refresh:
                EasyTracker.getInstance(this)
                           .send(MapBuilder.createEvent(Constants.Analytics.Category.ACTION_BAR_ACTION,
                                                        Constants.Analytics.Action.REFRESH_POST, getSubreddit(), null).build());
                refreshCurentImage();
                return true;
            default:
                return false;
        }
    }

    /**
     * Toggle whether swiping is enabled in the ViewPager.
     */
    public void toggleViewPagerLock() {
        if (mAdapter != null && mPager != null) {
            mPager.toggleSwipingEnabled();
            lockViewPagerItem.setIcon(
                mPager.isSwipingEnabled() ? R.drawable.ic_action_lock_open_dark : R.drawable.ic_action_lock_closed_dark);
            mCrouton.setText(mPager.isSwipingEnabled() ? getString(R.string.swiping_enabled) : getString(R.string.swiping_disabled));
            FadeInThenOut.fadeInThenOut(mCrouton, 1500);
        }
    }

    public abstract String getSubreddit();

    /**
     * Get the URL of the current image in the ViewPager. Used in
     * {@link ImageViewerActivity#onOptionsItemSelected(MenuItem)}
     *
     * @return The URL of the current image in the ViewPager.
     */
    public abstract String getUrlForSharing();

    /**
     * Get the currently displayed image fragment and cause it to refresh the currently displayed contents.
     */
    protected abstract void refreshCurentImage();

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
