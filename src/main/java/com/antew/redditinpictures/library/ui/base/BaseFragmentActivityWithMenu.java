package com.antew.redditinpictures.library.ui.base;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.actionbarsherlock.view.MenuItem;
import com.antew.redditinpictures.library.adapter.SubredditMenuDrawerCursorAdapter;
import com.antew.redditinpictures.library.dialog.LoginDialogFragment;
import com.antew.redditinpictures.library.dialog.SetDefaultSubredditsDialogFragment;
import com.antew.redditinpictures.library.enums.Age;
import com.antew.redditinpictures.library.enums.Category;
import com.antew.redditinpictures.library.event.LoadSubredditEvent;
import com.antew.redditinpictures.library.listener.OnSubredditActionListener;
import com.antew.redditinpictures.library.preferences.SharedPreferencesHelper;
import com.antew.redditinpictures.library.reddit.RedditLoginInformation;
import com.antew.redditinpictures.library.reddit.SubredditData;
import com.antew.redditinpictures.library.reddit.json.MySubredditsResponse;
import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.library.utils.Constants;
import com.antew.redditinpictures.library.utils.Ln;
import com.antew.redditinpictures.library.utils.Strings;
import com.antew.redditinpictures.library.utils.SubredditUtils;
import com.antew.redditinpictures.library.utils.Util;
import com.antew.redditinpictures.pro.R;
import com.antew.redditinpictures.sqlite.RedditContract;
import java.util.ArrayList;
import net.simonvt.menudrawer.MenuDrawer;

public class BaseFragmentActivityWithMenu extends BaseFragmentActivity
    implements LoaderManager.LoaderCallbacks<Cursor>,
    SetDefaultSubredditsDialogFragment.SetDefaultSubredditsDialogListener {
    protected MenuDrawer mMenuDrawer;
    protected SubredditMenuDrawerCursorAdapter mSubredditAdapter;
    protected ArrayAdapter<String> mSubredditSearchResultsAdapter;
    @InjectView(R.id.et_subreddit_filter)
    protected AutoCompleteTextView mSubredditFilter;
    @InjectView(R.id.btn_search)
    protected ImageButton mSubredditSearch;
    @InjectView(R.id.lv_subreddits)
    protected ListView mSubredditList;
    @InjectView(R.id.ib_add)
    protected ImageButton mAddSubreddit;
    @InjectView(R.id.ib_sort)
    protected ImageButton mSortSubreddits;
    @InjectView(R.id.ib_refresh)
    protected ImageButton mRefreshSubreddits;

    protected String mSelectedSubreddit = Constants.REDDIT_FRONTPAGE;
    protected Category mCategory = Category.HOT;
    protected Age mAge = Age.TODAY;

    private OnSubredditActionListener mSubredditActionListener = new OnSubredditActionListener() {

        @Override
        public void onAction(SubredditData subredditData, SubredditAction action) {
            switch (action) {
                case View:
                    mMenuDrawer.closeMenu(true);
                    loadSubreddit(subredditData.getDisplay_name());
                    break;
                case Subscribe:
                    subscribeToSubreddit(subredditData.getName());
                    break;
                case Unsubscribe:
                    unsubscribeToSubreddit(subredditData.getName());
                    break;
                case Delete:
                    deleteSubreddit(subredditData.getName());
                    break;
            }
        }
    };

    private View.OnClickListener mSubredditSearchListener = new View.OnClickListener() {
        @Override public void onClick(View v) {
            if (mSubredditFilter != null) {
                searchForSubreddits(mSubredditFilter.getText().toString());
            }
        }
    };

    private View.OnClickListener mAddSubredditListener = new View.OnClickListener() {
        @Override public void onClick(View v) {
            // Show dialog to search for a subreddit and add one.
        }
    };

    private View.OnClickListener mSortSubredditsListener = new View.OnClickListener() {
        @Override public void onClick(View v) {
            // Switch between alpha/usage sorting.
        }
    };

    private View.OnClickListener mRefreshSubredditsListener = new View.OnClickListener() {
        @Override public void onClick(View v) {
            // Pulldown subreddits if logged in. If not logged in, confirm that they want to reset to default subreddits.
            handleRefreshSubreddits();
        }
    };

    private TextView.OnEditorActionListener mSubredditSearchEditorActionListener =
        new TextView.OnEditorActionListener() {

            /**
             * Called when an action is being performed.
             *
             * @param v The view that was clicked.
             * @param actionId Identifier of the action.  This will be either the
             * identifier you supplied, or {@link android.view.inputmethod.EditorInfo#IME_NULL
             * EditorInfo.IME_NULL} if being called due to the enter key
             * being pressed.
             * @param event If triggered by an enter key, this is the event;
             * otherwise, this is null.
             * @return Return true if you have consumed the action, else false.
             */
            @Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH && mSubredditFilter != null) {
                    searchForSubreddits(mSubredditFilter.getText().toString());
                }
                return false;
            }
        };

    private AdapterView.OnItemClickListener mSubredditSearchResponseListener =
        new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mSubredditSearchResultsAdapter != null) {
                    addSubreddit(mSubredditSearchResultsAdapter.getItem(position));
                }

                mMenuDrawer.setActiveView(view, position);
                mSubredditAdapter.setActivePosition(position);
                closeMenuDrawerIfNeeded();
            }
        };

    private TextWatcher mSubredditFilterWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            return;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s == null) {
                return;
            }

            filterSubreddits(s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {
            return;
        }
    };

    private AdapterView.OnItemClickListener mSubredditClickListener =
        new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                String subredditName = cursor.getString(
                    cursor.getColumnIndex(RedditContract.SubredditColumns.DISPLAY_NAME));
                int priority =
                    cursor.getInt(cursor.getColumnIndex(RedditContract.SubredditColumns.PRIORITY));

                // TODO: Make this less hacky...
                // Load the actual frontpage of reddit if selected
                if (priority == MySubredditsResponse.DefaultSubreddit.FRONTPAGE.getPriority()) {
                    subredditName = Constants.REDDIT_FRONTPAGE;
                }

                mMenuDrawer.setActiveView(view, position);
                mSubredditAdapter.setActivePosition(position);
                mMenuDrawer.closeMenu(true);
                loadSubreddit(subredditName);
            }
        };

    private BroadcastReceiver mSubredditsSearch = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(Constants.EXTRA_SUBREDDIT_NAMES)) {
                Ln.d("Got Back Subreddit Search Result");
                final ArrayList<String> subredditNames =
                    intent.getStringArrayListExtra(Constants.EXTRA_SUBREDDIT_NAMES);

                if (subredditNames != null) {
                    handleSubredditSearchResults(subredditNames);
                }
            }
        }
    };

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mMenuDrawer != null) {
                    mMenuDrawer.toggleMenu();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupMenuDrawer() {
        initializeSubredditFilter();
        initializeSubredditList();
        initializeLoaders();
        initializeReceivers();
        initializeSubredditMenu();
    }

    private void initializeMenuDrawer() {
        mMenuDrawer = MenuDrawer.attach(this, MenuDrawer.Type.OVERLAY);
        mMenuDrawer.setMenuView(R.layout.subreddit_menudrawer);
        mMenuDrawer.setSlideDrawable(R.drawable.ic_drawer);
        mMenuDrawer.setMenuSize(Util.dpToPx(this, 260));
        mMenuDrawer.setDrawerIndicatorEnabled(true);
    }

    private void initializeSubredditFilter() {
        if (mSubredditFilter != null) {
            mSubredditFilter.addTextChangedListener(mSubredditFilterWatcher);
            mSubredditFilter.setImeActionLabel(getString(R.string.go), KeyEvent.KEYCODE_ENTER);
            mSubredditFilter.setOnEditorActionListener(mSubredditSearchEditorActionListener);
        }

        if (mSubredditSearch != null) {
            mSubredditSearch.setOnClickListener(mSubredditSearchListener);
        }
    }

    private void initializeSubredditList() {
        mSubredditAdapter = getSubredditMenuAdapter();
        if (mSubredditList != null) {
            mSubredditList.setAdapter(mSubredditAdapter);
            mSubredditList.setOnItemClickListener(mSubredditClickListener);
        }
    }

    private void initializeSubredditMenu() {
        if (mAddSubreddit != null) {
            mAddSubreddit.setOnClickListener(mAddSubredditListener);
        }

        if (mSortSubreddits != null) {
            mSortSubreddits.setOnClickListener(mSortSubredditsListener);
        }

        if (mRefreshSubreddits != null) {
            mRefreshSubreddits.setOnClickListener(mRefreshSubredditsListener);
        }
    }

    private void handleRefreshSubreddits() {
        if (RedditLoginInformation.isLoggedIn()) {
            // Since the user is logged in we can just run the task to update their subreddits.
            SubredditUtils.SetDefaultSubredditsTask defaultSubredditsTask =
                new SubredditUtils.SetDefaultSubredditsTask(this, true);
            defaultSubredditsTask.execute();
        } else {
            // If they aren't logged in, we want to make sure that they understand this will set the subreddits back to default.
            SetDefaultSubredditsDialogFragment fragment = SetDefaultSubredditsDialogFragment.newInstance();
            fragment.show(getSupportFragmentManager(), Constants.Dialog.DIALOG_DEFAULT_SUBREDDITS);
        }
    }

    private void initializeLoaders() {
        LoaderManager loaderManager = getSupportLoaderManager();
        loaderManager.initLoader(Constants.LOADER_SUBREDDITS, null, this);
    }

    private void initializeReceivers() {
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mSubredditsSearch,
                new IntentFilter(Constants.BROADCAST_SUBREDDIT_SEARCH));
    }

    protected void addSubreddit(String subreddit) {
        if (Strings.notEmpty(subreddit)) {
            RedditService.aboutSubreddit(this, subreddit);
            loadSubreddit(subreddit);
        }
    }

    protected void forceRefreshCurrentSubreddit() {
        RedditService.forceRefreshSubreddit(this, mSelectedSubreddit, mAge, mCategory);
    }

    protected void loadSubreddit(String subreddit) {
        if (subreddit.equals("Frontpage")) {
            mSelectedSubreddit = Constants.REDDIT_FRONTPAGE;
        } else {
            mSelectedSubreddit = subreddit;
        }
        mBus.post(new LoadSubredditEvent(mSelectedSubreddit));
    }

    protected void filterSubreddits(String filterText) {
        LoaderManager loaderManager = getSupportLoaderManager();
        Bundle filterBundle = new Bundle();
        filterBundle.putString(Constants.EXTRA_QUERY, filterText);
        loaderManager.restartLoader(Constants.LOADER_SUBREDDITS, filterBundle, this);
    }

    protected void searchForSubreddits(String queryText) {
        if (Strings.notEmpty(queryText)) {
            RedditService.searchSubreddits(this, queryText,
                SharedPreferencesHelper.getShowNsfwImages(this));
        }
    }

    private void handleSubredditSearchResults(ArrayList<String> subredditNameList) {
        if (mSubredditFilter != null && subredditNameList != null && subredditNameList.size() > 0) {
            mSubredditSearchResultsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,
                    subredditNameList);
            mSubredditFilter.setAdapter(mSubredditSearchResultsAdapter);
            mSubredditFilter.showDropDown();
            mSubredditFilter.setOnItemClickListener(mSubredditSearchResponseListener);
        }
    }

    protected void subscribeToSubreddit(String subredditName) {
        if (!RedditLoginInformation.isLoggedIn()) {
            showLogin();
        } else {
            RedditService.subscribe(this, subredditName);
        }
    }

    protected void unsubscribeToSubreddit(String subredditName) {
        if (!RedditLoginInformation.isLoggedIn()) {
            showLogin();
        } else {
            RedditService.unsubscribe(this, subredditName);
        }
    }

    protected void showLogin() {
        // Only needs to be shown if they aren't currently logged in.
        if (!RedditLoginInformation.isLoggedIn()) {
            LoginDialogFragment loginFragment = LoginDialogFragment.newInstance();
            loginFragment.show(getSupportFragmentManager(), Constants.DIALOG_LOGIN);
        }
    }

    protected void closeMenuDrawerIfNeeded() {
        if (mMenuDrawer != null && mMenuDrawer.isMenuVisible()) {
            mMenuDrawer.closeMenu();
        }
    }

    protected void deleteSubreddit(String subredditName) {
        // If the user isn't logged in we don't care about subscribing/unsubscribing
        if (!RedditLoginInformation.isLoggedIn()) {
            RedditService.unsubscribe(this, subredditName);
        }
        ContentResolver resolver = getContentResolver();
        resolver.delete(RedditContract.Subreddits.CONTENT_URI,
            RedditContract.SubredditColumns.NAME + " = ?", new String[] { subredditName });
    }

    private SubredditMenuDrawerCursorAdapter getSubredditMenuAdapter() {
        if (mSubredditAdapter == null) {
            mSubredditAdapter =
                new SubredditMenuDrawerCursorAdapter(this, mSubredditActionListener);
        }

        return mSubredditAdapter;
    }

    @Override public void setContentView(int layoutResId) {
        initializeMenuDrawer();
        mMenuDrawer.setContentView(layoutResId);
        ButterKnife.inject(this);
        setupMenuDrawer();
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override public void onBackPressed() {
        // If the menu drawer is open, close it. Otherwise go about the normal business.
        if (mMenuDrawer != null && mMenuDrawer.isMenuVisible()) {
            mMenuDrawer.closeMenu();
            return;
        }
        super.onBackPressed();
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case Constants.LOADER_SUBREDDITS:
                String selection = null;
                String[] selectionArgs = null;

                if (args != null && args.containsKey(Constants.EXTRA_QUERY)) {
                    String query = args.getString(Constants.EXTRA_QUERY);

                    if (Strings.notEmpty(query)) {
                        selection = RedditContract.SubredditColumns.DISPLAY_NAME + " LIKE ?";
                        selectionArgs = new String[] { "%" + query + "%" };
                    }
                }
                return new CursorLoader(this, RedditContract.Subreddits.CONTENT_URI,
                    RedditContract.Subreddits.SUBREDDITS_PROJECTION, selection, selectionArgs,
                    RedditContract.Subreddits.DEFAULT_SORT);
        }

        return null;
    }

    /**
     * Called when a previously created loader has finished its load.  Note
     * that normally an application is <em>not</em> allowed to commit fragment
     * transactions while in this call, since it can happen after an
     * activity's state is saved.  See {@link android.support.v4.app.FragmentManager#beginTransaction()
     * FragmentManager.openTransaction()} for further discussion on this.
     *
     * <p>This function is guaranteed to be called prior to the release of
     * the last data that was supplied for this Loader.  At this point
     * you should remove all use of the old data (since it will be released
     * soon), but should not do your own release of the data since its Loader
     * owns it and will take care of that.  The Loader will take care of
     * management of its data so you don't have to.  In particular:
     *
     * <ul>
     * <li> <p>The Loader will monitor for changes to the data, and report
     * them to you through new calls here.  You should not monitor the
     * data yourself.  For example, if the data is a {@link android.database.Cursor}
     * and you place it in a {@link android.widget.CursorAdapter}, use
     * the {@link android.widget.CursorAdapter#CursorAdapter(android.content.Context,
     * android.database.Cursor, int)} constructor <em>without</em> passing
     * in either {@link android.widget.CursorAdapter#FLAG_AUTO_REQUERY}
     * or {@link android.widget.CursorAdapter#FLAG_REGISTER_CONTENT_OBSERVER}
     * (that is, use 0 for the flags argument).  This prevents the CursorAdapter
     * from doing its own observing of the Cursor, which is not needed since
     * when a change happens you will get a new Cursor throw another call
     * here.
     * <li> The Loader will release the data once it knows the application
     * is no longer using it.  For example, if the data is
     * a {@link android.database.Cursor} from a {@link android.content.CursorLoader},
     * you should not call close() on it yourself.  If the Cursor is being placed in a
     * {@link android.widget.CursorAdapter}, you should use the
     * {@link android.widget.CursorAdapter#swapCursor(android.database.Cursor)}
     * method so that the old Cursor is not closed.
     * </ul>
     *
     * @param loader The Loader that has finished.
     * @param data The data generated by the Loader.
     */
    @Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case Constants.LOADER_SUBREDDITS:
                mSubredditAdapter.swapCursor(data);
                break;
        }
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case Constants.LOADER_SUBREDDITS:
                mSubredditAdapter.swapCursor(null);
                break;
        }
    }

    @Override public void onSetDefaultSubreddits() {
        SubredditUtils.SetDefaultSubredditsTask defaultSubredditsTask =
            new SubredditUtils.SetDefaultSubredditsTask(this, true);
        defaultSubredditsTask.execute();
    }
}
