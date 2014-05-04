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
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import butterknife.InjectView;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.database.RedditContract;
import com.antew.redditinpictures.library.dialog.LoginDialogFragment;
import com.antew.redditinpictures.library.dialog.LogoutDialogFragment;
import com.antew.redditinpictures.library.dialog.SaveImageDialogFragment;
import com.antew.redditinpictures.library.event.DownloadImageCompleteEvent;
import com.antew.redditinpictures.library.event.ForcePostRefreshEvent;
import com.antew.redditinpictures.library.event.LoadSubredditEvent;
import com.antew.redditinpictures.library.event.RequestCompletedEvent;
import com.antew.redditinpictures.library.event.RequestInProgressEvent;
import com.antew.redditinpictures.library.event.SaveImageEvent;
import com.antew.redditinpictures.library.model.Age;
import com.antew.redditinpictures.library.model.Category;
import com.antew.redditinpictures.library.model.reddit.LoginData;
import com.antew.redditinpictures.library.model.reddit.PostData;
import com.antew.redditinpictures.library.model.reddit.RedditLoginInformation;
import com.antew.redditinpictures.library.model.reddit.RedditSort;
import com.antew.redditinpictures.library.preferences.RedditInPicturesPreferences;
import com.antew.redditinpictures.library.preferences.SharedPreferencesHelper;
import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.library.ui.base.BaseFragmentActivityWithMenu;
import com.antew.redditinpictures.library.util.BundleUtil;
import com.antew.redditinpictures.library.util.ImageDownloader;
import com.antew.redditinpictures.library.util.Ln;
import com.antew.redditinpictures.library.util.RedditUtil;
import com.antew.redditinpictures.library.util.StringUtil;
import com.antew.redditinpictures.library.util.Strings;
import com.antew.redditinpictures.library.util.SubredditUtil;
import com.antew.redditinpictures.pro.R;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.squareup.otto.Subscribe;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import javax.inject.Inject;

public class RedditFragmentActivity extends BaseFragmentActivityWithMenu
    implements LoginDialogFragment.LoginDialogListener, LogoutDialogFragment.LogoutDialogListener, LoaderManager.LoaderCallbacks<Cursor>,
               SaveImageDialogFragment.SaveImageDialogListener {
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

    @Inject
    protected ImageDownloader mImageDownloader;
    private   PostData        mPostData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMenuDrawerContentView(R.layout.reddit_fragment_activity);
        restoreInstanceState(savedInstanceState);
        initializeActiveView();
        initializeLoaders();

        new SubredditUtil.SetDefaultSubredditsTask(this).execute();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(Constants.Extra.EXTRA_ACTIVE_VIEW, mActiveViewType.toString());
        outState.putString(Constants.Extra.EXTRA_SUBREDDIT, mSelectedSubreddit);
        outState.putString(Constants.Extra.EXTRA_CATEGORY, Strings.toString(mCategory));
        outState.putString(Constants.Extra.EXTRA_AGE, Strings.toString(mAge));
        super.onSaveInstanceState(outState);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mActiveViewType = ViewType.valueOf(
                BundleUtil.getString(savedInstanceState, Constants.Extra.EXTRA_ACTIVE_VIEW, ViewType.LIST.toString()));
            mSelectedSubreddit = BundleUtil.getString(savedInstanceState, Constants.Extra.EXTRA_SUBREDDIT,
                                                      Constants.Reddit.REDDIT_FRONTPAGE);
            mCategory = Category.fromString(
                BundleUtil.getString(savedInstanceState, Constants.Extra.EXTRA_CATEGORY, Category.HOT.getName()));
            mAge = Age.fromString(BundleUtil.getString(savedInstanceState, Constants.Extra.EXTRA_AGE, Age.TODAY.getAge()));
        }
    }

    private void initializeActiveView() {
        switch (mActiveViewType) {
            case GRID:
                EasyTracker.getInstance(this)
                           .send(MapBuilder.createEvent(Constants.Analytics.Category.INITIALIZE, Constants.Analytics.Action.IMAGE_VIEW,
                                                        Constants.Analytics.Label.GRID, null).build());
                FragmentTransaction gridTrans = getFragmentManager().beginTransaction();
                gridTrans.replace(R.id.content_fragment, getNewImageGridFragment());
                gridTrans.commit();
                break;
            case LIST:
                EasyTracker.getInstance(this)
                           .send(MapBuilder.createEvent(Constants.Analytics.Category.INITIALIZE, Constants.Analytics.Action.IMAGE_VIEW,
                                                        Constants.Analytics.Label.LIST, null).build());
                FragmentTransaction listTrans = getFragmentManager().beginTransaction();
                listTrans.replace(R.id.content_fragment, getNewImageListFragment());
                listTrans.commit();
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceivers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceivers();
    }

    private void unregisterReceivers() {
        if (mLoginComplete != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mLoginComplete);
        }
    }

    private void registerReceivers() {
        LocalBroadcastManager.getInstance(this)
                             .registerReceiver(mLoginComplete, new IntentFilter(Constants.Broadcast.BROADCAST_LOGIN_COMPLETE));
    }

    private void initializeLoaders() {
        getLoaderManager().initLoader(Constants.Loader.LOADER_LOGIN, null, this);
    }

    @Override
    protected void subscribeToSubreddit(String subredditName) {
        if (!RedditLoginInformation.isLoggedIn()) {
            showLogin();
        } else {
            RedditService.subscribe(this, subredditName);
        }
    }

    @Override
    protected void unsubscribeToSubreddit(String subredditName) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        item.setChecked(true);

        switch (item.getItemId()) {
            case R.id.change_view:
                switch (mActiveViewType) {
                    case LIST:
                        EasyTracker.getInstance(this)
                                   .send(MapBuilder.createEvent(Constants.Analytics.Category.ACTION_BAR_ACTION,
                                                                Constants.Analytics.Action.CHANGE_VIEW, Constants.Analytics.Label.GRID,
                                                                null).build());
                        break;
                    case GRID:
                        EasyTracker.getInstance(this)
                                   .send(MapBuilder.createEvent(Constants.Analytics.Category.ACTION_BAR_ACTION,
                                                                Constants.Analytics.Action.CHANGE_VIEW, Constants.Analytics.Label.LIST,
                                                                null).build());
                        break;
                }
                changeViewType(mActiveViewType == ViewType.LIST ? ViewType.GRID : ViewType.LIST);
                return true;
            case R.id.settings:
                EasyTracker.getInstance(this)
                           .send(MapBuilder.createEvent(Constants.Analytics.Category.ACTION_BAR_ACTION,
                                                        Constants.Analytics.Action.OPEN_SETTINGS, null, null).build());
                startPreferences();
                return true;
            case R.id.refresh_all:
                EasyTracker.getInstance(this)
                           .send(MapBuilder.createEvent(Constants.Analytics.Category.ACTION_BAR_ACTION,
                                                        Constants.Analytics.Action.REFRESH_POSTS, null, null).build());
                // Notify our image fragment(s) that they need
                // to remove references to the now stale data
                mBus.post(new ForcePostRefreshEvent());
                produceRequestInProgressEvent();
                forceRefreshCurrentSubreddit();
                return true;
            case R.id.login:
                if (RedditLoginInformation.isLoggedIn()) {
                    EasyTracker.getInstance(this)
                               .send(
                                   MapBuilder.createEvent(Constants.Analytics.Category.ACTION_BAR_ACTION, Constants.Analytics.Action.LOGIN,
                                                          Constants.Analytics.Label.LOGGED_IN, null).build()
                                    );
                } else {
                    EasyTracker.getInstance(this)
                               .send(
                                   MapBuilder.createEvent(Constants.Analytics.Category.ACTION_BAR_ACTION, Constants.Analytics.Action.LOGIN,
                                                          Constants.Analytics.Label.NOT_LOGGED_IN, null).build()
                                    );
                }
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
                    produceRequestInProgressEvent();
                    RedditSort.SortCriteria sortCriteria = RedditSort.get(item.getItemId());
                    EasyTracker.getInstance(this)
                               .send(MapBuilder.createEvent(Constants.Analytics.Category.ACTION_BAR_ACTION,
                                                            Constants.Analytics.Action.OPEN_SUBREDDIT, mSelectedSubreddit
                                                                                                       + "/"
                                                                                                       + Strings.toString(
                                       sortCriteria.getCategory())
                                                                                                       + "/"
                                                                                                       + Strings.toString(
                                       sortCriteria.getAge()), null
                                                           ).build());
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
                    FragmentTransaction gridTrans = getFragmentManager().beginTransaction();
                    gridTrans.replace(R.id.content_fragment, getNewImageGridFragment());
                    gridTrans.commit();
                    invalidateOptionsMenu();
                    break;
                case LIST:
                    FragmentTransaction listTrans = getFragmentManager().beginTransaction();
                    listTrans.replace(R.id.content_fragment, getNewImageListFragment());
                    listTrans.commit();
                    invalidateOptionsMenu();
                    break;
            }
        }
    }

    public void startPreferences() {
        Intent intent = new Intent(this, getPreferencesClass());
        intent.putExtra(Constants.Extra.EXTRA_SHOW_NSFW_IMAGES, SharedPreferencesHelper.getShowNsfwImages(this));
        startActivityForResult(intent, SETTINGS_REQUEST);
    }

    @Subscribe
    public void requestInProgress(RequestInProgressEvent event) {
        ViewPropertyAnimator.animate(mProgressBar).setDuration(500).alpha(100);
    }

    public void handleLoginAndLogout() {
        if (!RedditLoginInformation.isLoggedIn()) {
            LoginDialogFragment loginFragment = LoginDialogFragment.newInstance();
            loginFragment.show(getFragmentManager(), Constants.Dialog.DIALOG_LOGIN);
        } else {
            DialogFragment logoutFragment = LogoutDialogFragment.newInstance(RedditLoginInformation.getUsername());
            logoutFragment.show(getFragmentManager(), Constants.Dialog.DIALOG_LOGOUT);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle paramBundle) {
        switch (id) {
            case Constants.Loader.LOADER_LOGIN:
                return new CursorLoader(this, RedditContract.Login.CONTENT_URI, null, null, null, RedditContract.Login.DEFAULT_SORT);
            default:
                return super.onCreateLoader(id, paramBundle);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case Constants.Loader.LOADER_LOGIN:
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        String username = cursor.getString(cursor.getColumnIndex(RedditContract.Login.USERNAME));
                        String cookie = cursor.getString(cursor.getColumnIndex(RedditContract.Login.COOKIE));
                        String modhash = cursor.getString(cursor.getColumnIndex(RedditContract.Login.MODHASH));

                        LoginData data = new LoginData(username, modhash, cookie);
                        if (!data.equals(RedditLoginInformation.getLoginData())) {
                            RedditLoginInformation.setLoginData(data);
                        }

                        invalidateOptionsMenu();
                        new SubredditUtil.SetDefaultSubredditsTask(this).execute();
                        forceRefreshCurrentSubreddit();
                    }
                }
                break;
            default:
                super.onLoadFinished(loader, cursor);
                break;
        }
    }

    @Override
    protected void loadSubredditFromMenu(String subreddit) {
        loadSubreddit(subreddit, mCategory, mAge);
    }

    @Subscribe
    public void requestCompleted(RequestCompletedEvent event) {
        ViewPropertyAnimator.animate(mProgressBar).setDuration(500).alpha(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
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
        produceRequestInProgressEvent();
        RedditService.login(this, username, password);
        invalidateOptionsMenu();
    }

    @Override
    public void onFinishLogoutDialog() {
        produceRequestInProgressEvent();
        // Clear out the login data, Reddit API doesn't incorporate sessions into how it works so simply clearing out the cached data does the trick.
        RedditLoginInformation.setLoginData(null);

        // This should really be done async, but we store a maximum of one row
        // on the table and it is reasonably fast as is, around 50ms in my tests.
        getContentResolver().delete(RedditContract.Login.CONTENT_URI, null, null);
        new SubredditUtil.SetDefaultSubredditsTask(this, true).execute();
        invalidateOptionsMenu();
        forceRefreshCurrentSubreddit();
    }

    private void handleLoginComplete(Intent intent) {
        boolean successful = intent.getBooleanExtra(Constants.Extra.EXTRA_SUCCESS, false);
        if (!successful) {
            String username = intent.getStringExtra(Constants.Extra.EXTRA_USERNAME);
            if (Strings.notEmpty(username)) {
                LoginDialogFragment loginFragment = LoginDialogFragment.newInstance(username);
                loginFragment.show(getFragmentManager(), Constants.Dialog.DIALOG_LOGIN);
            }

            String errorMessage = intent.getStringExtra(Constants.Extra.EXTRA_ERROR_MESSAGE);
            Toast.makeText(this, getString(R.string.error) + errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    protected void produceRequestCompletedEvent() {
        mBus.post(new RequestCompletedEvent());
    }

    protected void produceRequestInProgressEvent() {
        mBus.post(new RequestInProgressEvent());
    }

    @Subscribe
    public void onLoadSubredditEvent(LoadSubredditEvent event) {
        if (event != null) {
            mSelectedSubreddit = event.getSubreddit();
            loadSubreddit(event.getSubreddit(), event.getCategory(), event.getAge());
        }
    }

    private void loadSubreddit(String subreddit, Category category, Age age) {
        if (subreddit.equals(Constants.Reddit.REDDIT_FRONTPAGE) || subreddit.equals(Constants.Reddit.REDDIT_FRONTPAGE_DISPLAY_NAME)) {
            mSelectedSubreddit = Constants.Reddit.REDDIT_FRONTPAGE;
        } else {
            mSelectedSubreddit = subreddit;
        }

        mCategory = category;
        mAge = age;

        switch (mActiveViewType) {
            case GRID:
                FragmentTransaction gridTrans = getFragmentManager().beginTransaction();
                gridTrans.replace(R.id.content_fragment, getNewImageGridFragment(mSelectedSubreddit, mCategory, mAge));
                gridTrans.addToBackStack(null);
                gridTrans.commit();
                break;
            case LIST:
                FragmentTransaction listTrans = getFragmentManager().beginTransaction();
                listTrans.replace(R.id.content_fragment, getNewImageListFragment(mSelectedSubreddit, mCategory, mAge));
                listTrans.addToBackStack(null);
                listTrans.commit();
                break;
        }

        setActionBarTitle(mSelectedSubreddit, RedditUtil.getSortDisplayString(mCategory, mAge));
    }

    @Subscribe
    public void onSaveImageEvent(SaveImageEvent event) {
        mPostData = event.getPostData();
        SaveImageDialogFragment saveImageDialog = SaveImageDialogFragment.newInstance(StringUtil.sanitizeFileName(mPostData.getTitle()));
        saveImageDialog.show(getFragmentManager(), Constants.Dialog.DIALOG_GET_FILENAME);
    }

    @Subscribe
    public void onDownloadImageComplete(DownloadImageCompleteEvent event) {
        mPostData = null;
        Ln.i("DownloadImageComplete - filename was: " + event.getFilename());
        Toast.makeText(this, "Image saved as " + event.getFilename(), Toast.LENGTH_SHORT).show();
    }

    public Class<? extends PreferenceActivity> getPreferencesClass() {
        return RedditInPicturesPreferences.class;
    }

    @Override
    public void onFinishSaveImageDialog(String filename) {
        mImageDownloader.downloadImage(mPostData.getUrl(), filename);
    }

    private enum ViewType {LIST, GRID}
}
