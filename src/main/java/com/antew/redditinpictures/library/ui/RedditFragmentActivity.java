package com.antew.redditinpictures.library.ui;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;
import butterknife.InjectView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.antew.redditinpictures.library.dialog.LoginDialogFragment;
import com.antew.redditinpictures.library.dialog.LogoutDialogFragment;
import com.antew.redditinpictures.library.enums.Age;
import com.antew.redditinpictures.library.enums.Category;
import com.antew.redditinpictures.library.event.LoadSubredditEvent;
import com.antew.redditinpictures.library.event.RequestCompletedEvent;
import com.antew.redditinpictures.library.event.RequestInProgressEvent;
import com.antew.redditinpictures.library.preferences.RedditInPicturesPreferences;
import com.antew.redditinpictures.library.preferences.SharedPreferencesHelper;
import com.antew.redditinpictures.library.reddit.LoginData;
import com.antew.redditinpictures.library.reddit.RedditLoginInformation;
import com.antew.redditinpictures.library.reddit.RedditSort;
import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.library.ui.base.BaseFragmentActivityWithMenu;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.utils.Ln;
import com.antew.redditinpictures.library.utils.RedditUtils;
import com.antew.redditinpictures.library.utils.SubredditUtils;
import com.antew.redditinpictures.library.utils.Util;
import com.antew.redditinpictures.pro.R;
import com.antew.redditinpictures.sqlite.RedditContract;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.squareup.otto.Subscribe;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class RedditFragmentActivity extends BaseFragmentActivityWithMenu
    implements LoginDialogFragment.LoginDialogListener, LogoutDialogFragment.LogoutDialogListener, LoaderManager.LoaderCallbacks<Cursor> {
    public static final int SETTINGS_REQUEST = 20;
    @InjectView(R.id.top_progressbar)
    protected SmoothProgressBar mProgressBar;
    private ViewType          mActiveViewType = ViewType.LIST;
    private BroadcastReceiver mLoginComplete  = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleLoginComplete(intent);
        }
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reddit_fragment_activity);
        restoreInstanceState(savedInstanceState);
        initializeActiveView();
        initalizeReceivers();
        initializeLoaders();

        new SubredditUtils.SetDefaultSubredditsTask(this).execute();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (Util.hasHoneycombMR1()) {
                mActiveViewType = ViewType.valueOf(savedInstanceState.getString(Constants.ACTIVE_VIEW, ViewType.LIST.toString()));
                mSelectedSubreddit = savedInstanceState.getString(Constants.EXTRA_SELECTED_SUBREDDIT, Constants.REDDIT_FRONTPAGE);
            } else {
                if (savedInstanceState.containsKey(Constants.ACTIVE_VIEW)) {
                    mActiveViewType = ViewType.valueOf(savedInstanceState.getString(Constants.ACTIVE_VIEW));
                }

                if (savedInstanceState.containsKey(Constants.EXTRA_SELECTED_SUBREDDIT)) {
                    mSelectedSubreddit = savedInstanceState.getString(Constants.EXTRA_SELECTED_SUBREDDIT);
                }
            }
        }
    }

    private void initializeActiveView() {
        switch (mActiveViewType) {
            case GRID:
                FragmentTransaction gridTrans = getSupportFragmentManager().beginTransaction();
                gridTrans.replace(R.id.content_fragment, getNewImageGridFragment());
                gridTrans.commit();
                break;
            case LIST:
                FragmentTransaction listTrans = getSupportFragmentManager().beginTransaction();
                listTrans.replace(R.id.content_fragment, getNewImageListFragment());
                listTrans.commit();
                break;
        }
    }

    private void initalizeReceivers() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mLoginComplete, new IntentFilter(Constants.BROADCAST_LOGIN_COMPLETE));
    }

    private void initializeLoaders() {
        LoaderManager loaderManager = getSupportLoaderManager();
        loaderManager.initLoader(Constants.LOADER_LOGIN, null, this);
    }

    @Override protected void subscribeToSubreddit(String subredditName) {
        if (!RedditLoginInformation.isLoggedIn()) {
            showLogin();
        } else {
            RedditService.subscribe(this, subredditName);
        }
    }

    @Override protected void unsubscribeToSubreddit(String subredditName) {
        if (!RedditLoginInformation.isLoggedIn()) {
            showLogin();
        } else {
            RedditService.unsubscribe(this, subredditName);
        }
    }

    public Fragment getNewImageGridFragment(String subreddit, Category category, Age age) {
        return RedditImageGridFragment.newInstance(subreddit, category, age);
    }

    public Fragment getNewImageGridFragment() {
        return getNewImageGridFragment(mSelectedSubreddit, mCategory, mAge);
    }

    public Fragment getNewImageListFragment(String subreddit, Category category, Age age) {
        return RedditImageListFragment.newInstance(subreddit, category, age);
    }

    public Fragment getNewImageListFragment() {
        return getNewImageListFragment(mSelectedSubreddit, mCategory, mAge);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        item.setChecked(true);

        switch (item.getItemId()) {
            case R.id.change_view:
                changeViewType(mActiveViewType == ViewType.LIST ? ViewType.GRID : ViewType.LIST);
                return true;
            case R.id.settings:
                startPreferences();
                return true;
            case R.id.refresh_all:
                requestInProgress(null);
                forceRefreshCurrentSubreddit();
                return true;
            case R.id.login:
                handleLoginAndLogout();
                return true;
            // fall through
            case R.id.category_hot:
            case R.id.category_new:
            case R.id.category_rising:
            case R.id.category_top_hour:
            case R.id.category_top_today:
            case R.id.category_top_week:
            case R.id.category_top_month:
            case R.id.category_top_year:
            case R.id.category_top_all_time:
            case R.id.category_controversial_hour:
            case R.id.category_controversial_today:
            case R.id.category_controversial_week:
            case R.id.category_controversial_month:
            case R.id.category_controversial_year:
            case R.id.category_controversial_all_time:
                if (RedditSort.contains(item.getItemId())) {
                    requestInProgress(null);
                    RedditSort.SortCriteria sortCriteria = RedditSort.get(item.getItemId());
                    loadSubreddit(mSelectedSubreddit, sortCriteria.getCategory(), sortCriteria.getAge());
                    return true;
                } else {
                    Ln.e("Unable to get sorting criteria for menu item id: " + item.getItemId() + ", unable to load subreddit");
                    return super.onOptionsItemSelected(item);
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void changeViewType(ViewType viewType) {
        if (viewType != null) {
            mActiveViewType = viewType;
            switch (mActiveViewType) {
                case GRID:
                    FragmentTransaction gridTrans = getSupportFragmentManager().beginTransaction();
                    gridTrans.replace(R.id.content_fragment, getNewImageGridFragment());
                    gridTrans.commit();
                    invalidateOptionsMenu();
                    break;
                case LIST:
                    FragmentTransaction listTrans = getSupportFragmentManager().beginTransaction();
                    listTrans.replace(R.id.content_fragment, getNewImageListFragment());
                    listTrans.commit();
                    invalidateOptionsMenu();
                    break;
            }
        }
    }

    public void startPreferences() {
        Intent intent = new Intent(this, getPreferencesClass());
        intent.putExtra(Constants.EXTRA_SHOW_NSFW_IMAGES, SharedPreferencesHelper.getShowNsfwImages(this));
        startActivityForResult(intent, SETTINGS_REQUEST);
    }

    @Subscribe
    public void requestInProgress(RequestInProgressEvent event) {
        ViewPropertyAnimator.animate(mProgressBar).setDuration(500).alpha(100);
    }

    public void handleLoginAndLogout() {
        if (!RedditLoginInformation.isLoggedIn()) {
            LoginDialogFragment loginFragment = LoginDialogFragment.newInstance();
            loginFragment.show(getSupportFragmentManager(), Constants.DIALOG_LOGIN);
        } else {
            DialogFragment logoutFragment = LogoutDialogFragment.newInstance(RedditLoginInformation.getUsername());
            logoutFragment.show(getSupportFragmentManager(), Constants.DIALOG_LOGOUT);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle paramBundle) {
        switch (id) {
            case Constants.LOADER_LOGIN:
                return new CursorLoader(this, RedditContract.Login.CONTENT_URI, null, null, null, RedditContract.Login.DEFAULT_SORT);
            default:
                return super.onCreateLoader(id, paramBundle);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case Constants.LOADER_LOGIN:
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        String username = cursor.getString(cursor.getColumnIndex(RedditContract.Login.USERNAME));
                        String cookie = cursor.getString(cursor.getColumnIndex(RedditContract.Login.COOKIE));
                        String modhash = cursor.getString(cursor.getColumnIndex(RedditContract.Login.MODHASH));

                        LoginData data = new LoginData(username, modhash, cookie);
                        if (!data.equals(RedditLoginInformation.getLoginData())) {
                            RedditLoginInformation.setLoginData(data);
                        }

                        requestCompleted(null);
                        invalidateOptionsMenu();

                        SubredditUtils.SetDefaultSubredditsTask defaultSubredditsTask = new SubredditUtils.SetDefaultSubredditsTask(this);
                        defaultSubredditsTask.execute();
                    }
                }
                break;
            default:
                super.onLoadFinished(loader, cursor);
                break;
        }
    }

    @Override protected void loadSubredditFromMenu(String subreddit) {
        loadSubreddit(subreddit, mCategory, mAge);
    }

    @Subscribe
    public void requestCompleted(RequestCompletedEvent event) {
        ViewPropertyAnimator.animate(mProgressBar).setDuration(500).alpha(0);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.main, menu);

        MenuItem item;
        // Put a checkmark by the currently selected Category + Age combination
        switch (mCategory) {
            case CONTROVERSIAL:
                switch (mAge) {
                    case ALL_TIME:
                        item = menu.findItem(R.id.category_controversial_all_time);
                        item.setChecked(true);
                        break;
                    case THIS_HOUR:
                        item = menu.findItem(R.id.category_controversial_hour);
                        item.setChecked(true);
                        break;
                    case THIS_MONTH:
                        item = menu.findItem(R.id.category_controversial_month);
                        item.setChecked(true);
                        break;
                    case THIS_WEEK:
                        item = menu.findItem(R.id.category_controversial_week);
                        item.setChecked(true);
                        break;
                    case THIS_YEAR:
                        item = menu.findItem(R.id.category_controversial_year);
                        item.setChecked(true);
                        break;
                    case TODAY:
                        item = menu.findItem(R.id.category_controversial_today);
                        item.setChecked(true);
                        break;
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
                switch (mAge) {
                    case ALL_TIME:
                        item = menu.findItem(R.id.category_top_all_time);
                        item.setChecked(true);
                        break;
                    case THIS_HOUR:
                        item = menu.findItem(R.id.category_top_hour);
                        item.setChecked(true);
                        break;
                    case THIS_MONTH:
                        item = menu.findItem(R.id.category_top_month);
                        item.setChecked(true);
                        break;
                    case THIS_WEEK:
                        item = menu.findItem(R.id.category_top_week);
                        item.setChecked(true);
                        break;
                    case THIS_YEAR:
                        item = menu.findItem(R.id.category_top_year);
                        item.setChecked(true);
                        break;
                    case TODAY:
                        item = menu.findItem(R.id.category_top_today);
                        item.setChecked(true);
                        break;
                }
                break;
            default:
                mCategory = Category.HOT;
                mAge = Age.TODAY;
                menu.findItem(R.id.category_hot).setChecked(true);
                break;
        }

        MenuItem loginMenuItem = menu.findItem(R.id.login);
        // If the user is logged in, update the Logout menu item to "Log out <username>"
        if (RedditLoginInformation.isLoggedIn()) {
            loginMenuItem.setTitle(getString(R.string.log_out_) + RedditLoginInformation.getUsername());
            loginMenuItem.setIcon(R.drawable.ic_action_exit_dark);
        } else {
            loginMenuItem.setTitle(R.string.log_on);
            loginMenuItem.setIcon(R.drawable.ic_action_key_dark);
        }

        MenuItem activeViewMenuItem = menu.findItem(R.id.change_view);
        switch (mActiveViewType) {
            case LIST:
                activeViewMenuItem.setIcon(R.drawable.ic_action_tiles_small_dark);
                activeViewMenuItem.setTitle(R.string.view_as_grid);
                break;
            case GRID:
                activeViewMenuItem.setIcon(R.drawable.ic_action_list_2_dark);
                activeViewMenuItem.setTitle(R.string.view_as_list);
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    public void onFinishLoginDialog(String username, String password) {
        requestInProgress(null);
        RedditService.login(this, username, password);
        invalidateOptionsMenu();
    }

    @Override
    public void onFinishLogoutDialog() {
        // Clear out the login data, Reddit API doesn't incorporate sessions into how it works so simply clearing out the cached data does the trick.
        RedditLoginInformation.setLoginData(null);

        // This should really be done async, but we store a maximum of one row
        // on the table and it is reasonably fast as is, around 50ms in my tests.
        getContentResolver().delete(RedditContract.Login.CONTENT_URI, null, null);
        SubredditUtils.SetDefaultSubredditsTask defaultSubredditsTask = new SubredditUtils.SetDefaultSubredditsTask(this, true);
        defaultSubredditsTask.execute();
        invalidateOptionsMenu();
    }

    private void handleLoginComplete(Intent intent) {
        requestCompleted(null);
        boolean successful = intent.getBooleanExtra(Constants.EXTRA_SUCCESS, false);
        if (!successful) {
            String errorMessage = intent.getStringExtra(Constants.EXTRA_ERROR_MESSAGE);
            Toast.makeText(this, getString(R.string.error) + errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe
    public void onLoadSubredditEvent(LoadSubredditEvent event) {
        if (event != null) {
            mSelectedSubreddit = event.getSubreddit();
            loadSubreddit(event.getSubreddit(), event.getCategory(), event.getAge());
        }
    }

    private void loadSubreddit(String subreddit, Category category, Age age) {
        if (subreddit.equals(Constants.REDDIT_FRONTPAGE) || subreddit.equals(Constants.REDDIT_FRONTPAGE_DISPLAY_NAME)) {
            mSelectedSubreddit = Constants.REDDIT_FRONTPAGE;
        } else {
            mSelectedSubreddit = subreddit;
        }

        mCategory = category;
        mAge = age;

        switch (mActiveViewType) {
            case GRID:
                FragmentTransaction gridTrans = getSupportFragmentManager().beginTransaction();
                gridTrans.replace(R.id.content_fragment, getNewImageGridFragment(mSelectedSubreddit, mCategory, mAge));
                gridTrans.addToBackStack(null);
                gridTrans.commit();
                break;
            case LIST:
                FragmentTransaction listTrans = getSupportFragmentManager().beginTransaction();
                listTrans.replace(R.id.content_fragment, getNewImageListFragment(mSelectedSubreddit, mCategory, mAge));
                listTrans.addToBackStack(null);
                listTrans.commit();
                break;
        }

        setActionBarTitle(mSelectedSubreddit, RedditUtils.getSortDisplayString(mCategory, mAge));
    }

    public Class<? extends PreferenceActivity> getPreferencesClass() {
        return RedditInPicturesPreferences.class;
    }

    private enum ViewType {LIST, GRID}
}
