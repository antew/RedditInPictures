package com.antew.redditinpictures.library.ui;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.RelativeLayout;
import android.widget.SpinnerAdapter;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.antew.redditinpictures.library.BuildConfig;
import com.antew.redditinpictures.library.R;
import com.antew.redditinpictures.library.adapter.SubredditMenuCursorAdapter;
import com.antew.redditinpictures.library.dialog.LoginDialogFragment;
import com.antew.redditinpictures.library.dialog.LoginDialogFragment.LoginDialogListener;
import com.antew.redditinpictures.library.dialog.LogoutDialogFragment;
import com.antew.redditinpictures.library.dialog.LogoutDialogFragment.LogoutDialogListener;
import com.antew.redditinpictures.library.enums.Age;
import com.antew.redditinpictures.library.enums.Category;
import com.antew.redditinpictures.library.interfaces.RedditDataProvider;
import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.preferences.RedditInPicturesPreferences;
import com.antew.redditinpictures.library.preferences.RedditInPicturesPreferencesFragment;
import com.antew.redditinpictures.library.preferences.SharedPreferencesHelper;
import com.antew.redditinpictures.library.reddit.LoginData;
import com.antew.redditinpictures.library.reddit.RedditLoginInformation;
import com.antew.redditinpictures.library.reddit.RedditUrl;
import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.library.subredditmanager.SubredditManager;
import com.antew.redditinpictures.library.subredditmanager.SubredditManagerApi11Plus;
import com.antew.redditinpictures.library.utils.Consts;
import com.antew.redditinpictures.library.utils.Util;
import com.antew.redditinpictures.sqlite.RedditContract;

import java.util.List;

public class ImageGridActivity extends SherlockFragmentActivity implements OnNavigationListener, LoginDialogListener, LogoutDialogListener, RedditDataProvider,
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG                     = "ImageGridActivity";
    protected boolean           mShowNsfwImages;
    protected Age               mAge                    = Age.TODAY;
    protected Category          mCategory               = Category.HOT;
    public static final int     EDIT_SUBREDDITS_REQUEST = 10;
    public static final int     SETTINGS_REQUEST        = 20;
    private SpinnerAdapter      mSpinnerAdapter;
    private boolean             mFirstCall              = true;
    private ProgressDialog      mProgressDialog;
    private MenuItem            mLoginMenuItem;
    protected RelativeLayout    mLayoutWrapper;
    private String              mUsername;

    //@formatter:off
    private BroadcastReceiver   mMySubreddits = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received mySubreddits callback");
            getSupportActionBar().setListNavigationCallbacks(getListNavigationSpinner(), ImageGridActivity.this);
            hideProgressDialog();
        }
    };
    //@formatter:on

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            Util.enableStrictMode();
        }
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_grid_activity);
        initializeActionBar();

        if (savedInstanceState != null) {
            getSupportActionBar().setSelectedNavigationItem(savedInstanceState.getInt(Consts.EXTRA_NAV_POSITION));
        }

        loadSharedPreferences();
        initializeImageGridFragment();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMySubreddits, new IntentFilter(Consts.BROADCAST_MY_SUBREDDITS));
        
        initializeLoaders();
    }

    private void initializeLoaders() {
        LoaderManager loaderManager = getSupportLoaderManager();
        loaderManager.initLoader(Consts.LOADER_LOGIN, null, this);
        loaderManager.initLoader(Consts.LOADER_SUBREDDITS, null, this);
    }

    private void initializeActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setListNavigationCallbacks(getListNavigationSpinner(), this);
    }

    private void loadSharedPreferences() {
        mLayoutWrapper = (RelativeLayout) findViewById(R.id.image_grid_wrapper);
        mShowNsfwImages = SharedPreferencesHelper.getShowNsfwImages(ImageGridActivity.this);
        mAge = SharedPreferencesHelper.getAge(ImageGridActivity.this);
        mCategory = SharedPreferencesHelper.getCategory(ImageGridActivity.this);

        if (SharedPreferencesHelper.getUseHoloBackground(ImageGridActivity.this)) {
            getWindow().setBackgroundDrawableResource(R.drawable.background_holo_dark);
        }
    }

    private void initializeImageGridFragment() {
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, getImageGridFragment(), ImageGridFragment.TAG).commit();
    }

    public Fragment getImageGridFragment() {
        ImageGridFragment fragment = (ImageGridFragment) getSupportFragmentManager().findFragmentByTag(ImageGridFragment.TAG);
        if (fragment == null) {
            fragment = getNewImageGridFragment();
        }

        return fragment;
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMySubreddits);
        super.onPause();
    }

    /**
     * On some version of android the indeterminate progress bar will show when the feature is
     * requested, so we make sure it is hidden here
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setSupportProgressBarIndeterminateVisibility(false);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mLoginMenuItem = menu.findItem(R.id.login);

        // If the user is logged in, update the Logout menu item to "Log out <username>"
        if (RedditLoginInformation.isLoggedIn()) {
            mLoginMenuItem.setTitle(getString(R.string.log_out_) + RedditLoginInformation.getUsername());
            mLoginMenuItem.setIcon(R.drawable.ic_action_logout);
        } else {
            mLoginMenuItem.setTitle(R.string.log_on);
            mLoginMenuItem.setIcon(R.drawable.ic_action_login);
        }

        return true;
    }

    private List<String> addHeaderSubreddits(List<String> subReddits) {
        subReddits.add(0, getString(R.string.frontpage));
        subReddits.add(1, getString(R.string.all));
        return subReddits;
    }

    private SpinnerAdapter getListNavigationSpinner() {
//        List<String> subReddits = SharedPreferencesHelper.loadArray(SubredditManager.PREFS_NAME, SubredditManager.ARRAY_NAME, ImageGridActivity.this);
//        if (!(subReddits.size() > 2)) {
//            subReddits = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.default_reddits)));
//        }
//
//        Collections.sort(subReddits, StringUtil.getCaseInsensitiveComparator());
//        subReddits = addHeaderSubreddits(subReddits);

        mSpinnerAdapter = new SubredditMenuCursorAdapter(ImageGridActivity.this, null, mAge, mCategory);

        return mSpinnerAdapter;
    }

    public ImageGridFragment getNewImageGridFragment() {
        return new ImageGridFragment();
    }

    public String getSubredditName(int position) {
        return position == Consts.POSITION_FRONTPAGE ? RedditUrl.REDDIT_FRONTPAGE : mSpinnerAdapter.getItem(position).toString();
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        if (mFirstCall) {
            mFirstCall = false;
            return true;
        }

        Intent intent = new Intent(Consts.BROADCAST_SUBSCRIBE);
        intent.putExtra(Consts.EXTRA_SELECTED_SUBREDDIT, getSubredditName(itemPosition));
        intent.putExtra(Consts.EXTRA_AGE, mAge.name());
        intent.putExtra(Consts.EXTRA_CATEGORY, mCategory.name());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(Consts.EXTRA_NAV_POSITION, getSupportActionBar().getSelectedNavigationIndex());
        outState.putString(Consts.EXTRA_SELECTED_SUBREDDIT, getSubreddit());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.main, menu);

        MenuItem item = null;
        //@formatter:off
        // Put a checkmark by the currently selected Category + Age combination
        switch (mCategory) {
            case CONTROVERSIAL:
                switch (mAge)
                {
                    case ALL_TIME:  item = menu.findItem(R.id.category_controversial_all_time); item.setChecked(true); break;
                    case THIS_HOUR: item = menu.findItem(R.id.category_controversial_hour)    ; item.setChecked(true); break;
                    case THIS_MONTH:item = menu.findItem(R.id.category_controversial_month)   ; item.setChecked(true); break;
                    case THIS_WEEK: item = menu.findItem(R.id.category_controversial_week)    ; item.setChecked(true); break;
                    case THIS_YEAR: item = menu.findItem(R.id.category_controversial_year)    ; item.setChecked(true); break;
                    case TODAY:     item = menu.findItem(R.id.category_controversial_today)   ; item.setChecked(true); break;
                }
                break;
            case HOT: 
                menu.findItem(R.id.category_hot).setChecked(true); 
                break;
            case NEW:
                menu.findItem(R.id.category_new).setChecked(true); 
                break;
            case RISING:
                menu.findItem(R.id.category_rising).setChecked(true);
                break;
            case TOP:
                switch (mAge)
                {
                    case ALL_TIME:  item = menu.findItem(R.id.category_top_all_time); item.setChecked(true); break;
                    case THIS_HOUR: item = menu.findItem(R.id.category_top_hour)    ; item.setChecked(true); break;
                    case THIS_MONTH:item = menu.findItem(R.id.category_top_month)   ; item.setChecked(true); break;
                    case THIS_WEEK: item = menu.findItem(R.id.category_top_week)    ; item.setChecked(true); break;
                    case THIS_YEAR: item = menu.findItem(R.id.category_top_year)    ; item.setChecked(true); break;
                    case TODAY:     item = menu.findItem(R.id.category_top_today)   ; item.setChecked(true); break;
                }
                break;
            default:
                mCategory = Category.HOT;
                mAge = Age.TODAY;
                menu.findItem(R.id.category_hot).setChecked(true);
                break;
        } 
        //@formatter:on

        return true;
    }

    private void editSubreddits() {
        Intent intent = new Intent(ImageGridActivity.this, getEditSubredditsClass());
        intent.putExtra(Consts.EXTRA_SELECTED_SUBREDDIT, getSubreddit());
        startActivityForResult(intent, EDIT_SUBREDDITS_REQUEST);
    }

    public void startPreferences() {
        Intent intent = new Intent(ImageGridActivity.this, getPreferencesClass());
        intent.putExtra(Consts.EXTRA_SHOW_NSFW_IMAGES, mShowNsfwImages);
        startActivityForResult(intent, SETTINGS_REQUEST);
    }

    public Class<? extends SubredditManager> getEditSubredditsClass() {
        if (Util.hasHoneycomb())
            return SubredditManagerApi11Plus.class;
        else
            return SubredditManager.class;
    }

    public Class<? extends PreferenceActivity> getPreferencesClass() {
        if (Util.hasHoneycomb())
            return RedditInPicturesPreferencesFragment.class;
        else
            return RedditInPicturesPreferences.class;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        item.setChecked(true);
        boolean loadFromUrl = false;

        int itemId = item.getItemId();
        if (itemId == R.id.edit_subreddits) {
            editSubreddits();
        } else if (itemId == R.id.settings) {
            startPreferences();
        } else if (itemId == R.id.refresh_all) {
            onNavigationItemSelected(getSupportActionBar().getSelectedNavigationIndex(), 0);
        } else if (itemId == R.id.login) {
            handleLoginAndLogout();
        }
        //@formatter:off
        else if (itemId == R.id.category_hot)                    { mCategory = Category.HOT;                                   loadFromUrl = true; }
        else if (itemId == R.id.category_new)                    { mCategory = Category.NEW;                                   loadFromUrl = true; }
        else if (itemId == R.id.category_rising)                 { mCategory = Category.RISING;                                loadFromUrl = true; } 
        else if (itemId == R.id.category_top_hour)               { mCategory = Category.TOP;            mAge = Age.THIS_HOUR ; loadFromUrl = true; }
        else if (itemId == R.id.category_top_today)              { mCategory = Category.TOP;            mAge = Age.TODAY     ; loadFromUrl = true; }
        else if (itemId == R.id.category_top_week)               { mCategory = Category.TOP;            mAge = Age.THIS_WEEK ; loadFromUrl = true; }
        else if (itemId == R.id.category_top_month)              { mCategory = Category.TOP;            mAge = Age.THIS_MONTH; loadFromUrl = true; }
        else if (itemId == R.id.category_top_year)               { mCategory = Category.TOP;            mAge = Age.THIS_YEAR ; loadFromUrl = true; }
        else if (itemId == R.id.category_top_all_time)           { mCategory = Category.TOP;            mAge = Age.ALL_TIME  ; loadFromUrl = true; }
        else if (itemId == R.id.category_controversial_hour)     { mCategory = Category.CONTROVERSIAL;  mAge = Age.THIS_HOUR ; loadFromUrl = true; }
        else if (itemId == R.id.category_controversial_today)    { mCategory = Category.CONTROVERSIAL;  mAge = Age.TODAY     ; loadFromUrl = true; }
        else if (itemId == R.id.category_controversial_week)     { mCategory = Category.CONTROVERSIAL;  mAge = Age.THIS_WEEK ; loadFromUrl = true; }
        else if (itemId == R.id.category_controversial_month)    { mCategory = Category.CONTROVERSIAL;  mAge = Age.THIS_MONTH; loadFromUrl = true; }
        else if (itemId == R.id.category_controversial_year)     { mCategory = Category.CONTROVERSIAL;  mAge = Age.THIS_YEAR ; loadFromUrl = true; }
        else if (itemId == R.id.category_controversial_all_time) { mCategory = Category.CONTROVERSIAL;  mAge = Age.ALL_TIME  ; loadFromUrl = true; } 
        // @formatter:on
        if (loadFromUrl) {
            SharedPreferencesHelper.saveCategorySelectionLoginInformation(mAge, mCategory, ImageGridActivity.this);
            getSubredditAdapter().notifyDataSetChanged(mCategory, mAge);
            Log.i(TAG, "onOptionsItemSelected, loadFromUrl = true, calling populateViewPagerFromSpinner()");
            onNavigationItemSelected(getSupportActionBar().getSelectedNavigationIndex(), 0);
        }

        return true;
    }

    public void handleLoginAndLogout() {
        if (!RedditLoginInformation.isLoggedIn()) {
            LoginDialogFragment loginFragment = LoginDialogFragment.newInstance();
            loginFragment.show(getSupportFragmentManager(), Consts.DIALOG_LOGIN);
        } else {
            DialogFragment logoutFragment = LogoutDialogFragment.newInstance();
            logoutFragment.show(getSupportFragmentManager(), Consts.DIALOG_LOGOUT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_SUBREDDITS_REQUEST && resultCode == RESULT_OK) {
            // Update the spinner with the subreddits
            getSupportActionBar().setListNavigationCallbacks(getListNavigationSpinner(), ImageGridActivity.this);

            // If the user tapped an item in the subreddit list, select that subreddit and load the
            // new images
            if (data.hasExtra(Consts.EXTRA_NEWLY_SELECTED_SUBREDDIT)) {
                mFirstCall = false;
                int pos = getSubredditPosition(data.getStringExtra(Consts.EXTRA_NEWLY_SELECTED_SUBREDDIT));
                onNavigationItemSelected(pos, 0);
                return;
            }
            // If the user didn't choose a subreddit (meaning we are returned the subreddit they
            // were previously viewing), select it in the list, if it doesn't exist any longer we
            // default to the Front page.
            String subredditName = data.getStringExtra(Consts.EXTRA_SELECTED_SUBREDDIT);
            if (subredditExistsInNavigation(subredditName)) {
                selectSubredditInNavigation(subredditName);
            } else {
                // Select the Reddit Front Page
                onNavigationItemSelected(Consts.POSITION_FRONTPAGE, 0);
            }

        } else if (requestCode == SETTINGS_REQUEST && resultCode == RESULT_OK) {
            if (data.getBooleanExtra(Consts.EXTRA_SHOW_NSFW_IMAGES_CHANGED, false)) {
                mShowNsfwImages = SharedPreferencesHelper.getShowNsfwImages(this);

                // If we're removing NSFW images we can modify the adapter in place, otherwise we
                // need to refresh
                if (mShowNsfwImages) {
                    onNavigationItemSelected(getSupportActionBar().getSelectedNavigationIndex(), 0);
                } else {
                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Consts.BROADCAST_REMOVE_NSFW_IMAGES));
                }

            }

        }
    }

    /**
     * Return the position of the subreddit in the navigation spinner, or -1 if the subreddit is not
     * found.
     * 
     * @param subredditName
     *            Name of the subreddit to find
     * @return The position of the subreddit in the spinner, or -1 if the subreddit is not found
     */
    public int getSubredditPosition(String subredditName) {
        for (int i = 0; i < mSpinnerAdapter.getCount(); i++) {
            String curReddit = (String) mSpinnerAdapter.getItem(i);
            if (curReddit.equalsIgnoreCase(subredditName)) {
                return i;
            }
        }

        return -1;
    }

    public void selectSubredditInNavigation(String subredditName) {
        int position = getSubredditPosition(subredditName);
        if (position == -1) {
            if (BuildConfig.DEBUG) {
                throw new ArrayIndexOutOfBoundsException("Subreddit \"" + subredditName + "\" does not exist in the navigation!");
            } else {
                Log.e(TAG, "Subreddit \"" + subredditName + "\" does not exist in the navigation!");
            }
        } else {
            getSupportActionBar().setSelectedNavigationItem(position);
        }
    }

    public boolean subredditExistsInNavigation(String subredditName) {
        return getSubredditPosition(subredditName) >= 0;
    }

    private void showProgressDialog(String title, String message) {
        mProgressDialog = ProgressDialog.show(ImageGridActivity.this, title, message, true, false);
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }

    @Override
    public void onFinishLoginDialog(String username, String password) {
        mUsername = username;
        showProgressDialog(getString(R.string.log_on), getString(R.string.logging_on));
        RedditService.login(this, username, password);

    }

    @Override
    public void onFinishLogoutDialog() {
        int rowsDeleted = getContentResolver().delete(RedditContract.Login.CONTENT_URI, null, null);
        Log.i(TAG, "rows deleted = " + rowsDeleted);

        invalidateOptionsMenu();
        getSupportActionBar().setListNavigationCallbacks(getListNavigationSpinner(), this);
        onNavigationItemSelected(getSupportActionBar().getSelectedNavigationIndex(), 0);
    }

    @Override
    public Age getAge() {
        return mAge;
    }

    @Override
    public Category getCategory() {
        return mCategory;
    }

    @Override
    public String getSubreddit() {
        return getSubredditName(getSupportActionBar().getSelectedNavigationIndex());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle paramBundle) {
        Log.i(TAG, "onCreateLoader");
        switch (id) {
            case Consts.LOADER_LOGIN:
                return new CursorLoader(this, RedditContract.Login.CONTENT_URI, null, null, null, RedditContract.Login.DEFAULT_SORT);
                
            case Consts.LOADER_SUBREDDITS:
                return new CursorLoader(this, RedditContract.Subreddits.CONTENT_URI, RedditContract.Subreddits.SUBREDDITS_PROJECTION, null, null, RedditContract.Subreddits.DEFAULT_SORT);
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case Consts.LOADER_LOGIN:
                if (cursor != null) {
                    Log.i(TAG, "onLoadFinished LOADER_LOGIN, " + cursor.getCount() + " rows");
                    if (cursor.moveToFirst()) {
                        mUsername = cursor.getString(cursor.getColumnIndex(RedditContract.Login.USERNAME));
                        String cookie = cursor.getString(cursor.getColumnIndex(RedditContract.Login.COOKIE));
                        String modhash = cursor.getString(cursor.getColumnIndex(RedditContract.Login.MODHASH));
                        Log.i(TAG, "Username = " + mUsername);
                        Log.i(TAG, "Cookie = " + cookie);
                        Log.i(TAG, "Modhash = " + modhash);

                        LoginData data = new LoginData(mUsername, modhash, cookie);
                        RedditLoginInformation.setLoginData(data);

                        hideProgressDialog();
                        invalidateOptionsMenu();
//                    showProgressDialog(getString(R.string.loading), getString(R.string.retrieving_subscribed_subreddits));
//                    RedditService.getMySubreddits(this);
                    }
                }
                break;
                
            case Consts.LOADER_SUBREDDITS:
                if (cursor != null) {
                    Log.i(TAG, "onLoadFinished LOADER_SUBREDDITS, " + cursor.getCount() + " rows");
                    if (cursor.moveToFirst()) {
                        getSubredditAdapter().swapCursor(cursor);
                        hideProgressDialog();
                        invalidateOptionsMenu();
                    }
                }

                
                break;
        }
        

    }

    @Override
    public void onLoaderReset(Loader<Cursor> paramLoader) {
        if (getSubredditAdapter() != null)
            getSubredditAdapter().swapCursor(null);
    }
    
    public SubredditMenuCursorAdapter getSubredditAdapter() {
        return (SubredditMenuCursorAdapter) mSpinnerAdapter;
    }

}
