package com.antew.redditinpictures.library.ui;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.antew.redditinpictures.library.adapter.SubredditMenuDrawerCursorAdapter;
import com.antew.redditinpictures.library.dialog.LoginDialogFragment;
import com.antew.redditinpictures.library.dialog.LoginDialogFragment.LoginDialogListener;
import com.antew.redditinpictures.library.dialog.LogoutDialogFragment;
import com.antew.redditinpictures.library.dialog.LogoutDialogFragment.LogoutDialogListener;
import com.antew.redditinpictures.library.enums.Age;
import com.antew.redditinpictures.library.enums.Category;
import com.antew.redditinpictures.library.interfaces.RedditDataProvider;
import com.antew.redditinpictures.library.interfaces.ScrollPosReadable;
import com.antew.redditinpictures.library.listener.OnSubredditActionListener;
import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.preferences.RedditInPicturesPreferences;
import com.antew.redditinpictures.library.preferences.SharedPreferencesHelper;
import com.antew.redditinpictures.library.reddit.LoginData;
import com.antew.redditinpictures.library.reddit.RedditLoginInformation;
import com.antew.redditinpictures.library.reddit.RedditUrl;
import com.antew.redditinpictures.library.reddit.SubredditData;
import com.antew.redditinpictures.library.reddit.json.MySubredditsResponse;
import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.library.ui.base.BaseFragmentActivity;
import com.antew.redditinpictures.library.utils.Consts;
import com.antew.redditinpictures.library.utils.Ln;
import com.antew.redditinpictures.library.utils.SafeAsyncTask;
import com.antew.redditinpictures.library.utils.Strings;
import com.antew.redditinpictures.library.utils.SubredditUtils;
import com.antew.redditinpictures.library.utils.Util;
import com.antew.redditinpictures.pro.BuildConfig;
import com.antew.redditinpictures.pro.R;
import com.antew.redditinpictures.sqlite.RedditContract;
import com.antew.redditinpictures.sqlite.RedditDatabase;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import java.util.ArrayList;
import net.simonvt.menudrawer.MenuDrawer;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

public class ImageGridActivity extends BaseFragmentActivity
    implements LoginDialogListener, LogoutDialogListener, RedditDataProvider,
    LoaderManager.LoaderCallbacks<Cursor>, ScrollPosReadable {
    public static final int EDIT_SUBREDDITS_REQUEST = 10;
    public static final int SETTINGS_REQUEST = 20;

    private static final String TAG = "ImageGridActivity";

    protected boolean mShowNsfwImages;
    protected Age mAge = Age.TODAY;
    protected Category mCategory = Category.HOT;
    protected RelativeLayout mLayoutWrapper;

    private ProgressDialog mProgressDialog;
    private MenuItem mLoginMenuItem;
    private String mUsername;
    private MenuDrawer mSubredditDrawer;
    private String mSelectedSubreddit;
    private SubredditMenuDrawerCursorAdapter mSubredditAdapter;
    private ViewType mActiveViewType = ViewType.LIST;
    private int mFirstVisiblePos = 0;
    private boolean mLoginLoaderHasRun = true;

    @InjectView(R.id.top_progressbar)
    protected SmoothProgressBar mProgressBar;

    private enum ViewType {LIST, GRID }

    private OnSubredditActionListener mSubredditActionListener = new OnSubredditActionListener() {

        @Override
        public void onAction(SubredditData subredditData, SubredditAction action) {
            switch (action) {
                case View:
                    if (subredditData.getDisplay_name().equals("Frontpage")) {
                        mSelectedSubreddit = RedditUrl.REDDIT_FRONTPAGE;
                    } else {
                        mSelectedSubreddit = subredditData.getDisplay_name();
                    }
                    mSubredditDrawer.closeMenu(true);
                    loadSubreddit(mSelectedSubreddit);
                    break;
                case Subscribe:
                    if (!RedditLoginInformation.isLoggedIn()) {
                        handleLoginAndLogout();
                    } else {
                        RedditService.subscribe(ImageGridActivity.this, subredditData.getName());
                    }
                    break;
                case Unsubscribe:
                    if (!RedditLoginInformation.isLoggedIn()) {
                        handleLoginAndLogout();
                    } else {
                        RedditService.unsubscribe(ImageGridActivity.this, subredditData.getName());
                    }
                    break;
                case Delete:
                    // If the user isn't logged in we don't care about subscribing/unsubscribing
                    if (!RedditLoginInformation.isLoggedIn()) {
                        RedditService.unsubscribe(ImageGridActivity.this, subredditData.getName());
                    }
                    ContentResolver resolver = ImageGridActivity.this.getContentResolver();
                    resolver.delete(RedditContract.Subreddits.CONTENT_URI,
                        RedditContract.SubredditColumns.NAME + " = ?",
                        new String[] { subredditData.getName() });
                    break;
            }
        }
    };

    private BroadcastReceiver mSubredditsSearch = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(Consts.EXTRA_SUBREDDIT_NAMES)) {
                Ln.d("Got Back Subreddit Search Result");
                final ArrayList<String> subredditNames =
                    intent.getStringArrayListExtra(Consts.EXTRA_SUBREDDIT_NAMES);
                AutoCompleteTextView mSubredditFilter =
                    (AutoCompleteTextView) findViewById(R.id.et_subreddit_filter);
                if (mSubredditFilter != null) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(ImageGridActivity.this,
                        android.R.layout.simple_dropdown_item_1line, subredditNames);
                    mSubredditFilter.setAdapter(adapter);
                    mSubredditFilter.showDropDown();

                    mSubredditFilter.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
                            mSelectedSubreddit = subredditNames.get(position);
                            RedditService.aboutSubreddit(ImageGridActivity.this,
                                mSelectedSubreddit);

                            mSubredditDrawer.setActiveView(view, position);
                            mSubredditAdapter.setActivePosition(position);
                            mSubredditDrawer.closeMenu(true);
                            loadSubreddit(mSelectedSubreddit);
                        }
                    });

                    mSubredditFilter.setImeActionLabel(getString(R.string.go),
                        KeyEvent.KEYCODE_ENTER);
                    mSubredditFilter.setOnEditorActionListener(
                        new TextView.OnEditorActionListener() {
                            @Override
                            public boolean onEditorAction(TextView v, int actionId,
                                KeyEvent event) {
                                Ln.d("Action: %d Event: %s", actionId, event);
                                return true;
                            }
                        });
                }
            }
        }
    };

    //@formatter:off
    private BroadcastReceiver mMySubreddits = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received mySubreddits callback");
            hideProgressDialog();
        }
    };

    //@formatter:off
    private BroadcastReceiver mLoginComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Login request complete");
            hideProgressDialog();
            boolean successful = intent.getBooleanExtra(Consts.EXTRA_SUCCESS, false);
            if (!successful) {
                String errorMessage = intent.getStringExtra(Consts.EXTRA_ERROR_MESSAGE);
                Toast.makeText(ImageGridActivity.this, getString(R.string.error) + errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    };
    //@formatter:on

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeActionBar();

        LayoutInflater li = LayoutInflater.from(this);
        View contentView = li.inflate(R.layout.image_grid_activity, null);
        ButterKnife.inject(this, contentView);

        mSubredditDrawer = MenuDrawer.attach(this, MenuDrawer.Type.OVERLAY);
        mSubredditDrawer.setContentView(contentView);
        mSubredditDrawer.setMenuView(R.layout.subreddit_menudrawer);
        mSubredditDrawer.setSlideDrawable(R.drawable.ic_drawer);
        mSubredditDrawer.setMenuSize(Util.dpToPx(this, 260));
        mSubredditDrawer.setDrawerIndicatorEnabled(true);

        mSubredditAdapter = getSubredditMenuAdapter();
        ListView mSubredditList = (ListView) findViewById(R.id.lv_subreddits);
        mSubredditList.setAdapter(mSubredditAdapter);
        mSubredditList.setOnItemClickListener(mSubredditClickListener);

        final AutoCompleteTextView mSubredditFilter = (AutoCompleteTextView) findViewById(R.id.et_subreddit_filter);
        mSubredditFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                return;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s == null) {
                    return;
                }

                LoaderManager loaderManager = getSupportLoaderManager();
                Bundle filterBundle = new Bundle();
                filterBundle.putString(Consts.EXTRA_QUERY, s.toString());
                loaderManager.restartLoader(Consts.LOADER_SUBREDDITS, filterBundle, ImageGridActivity.this);
            }

            @Override
            public void afterTextChanged(Editable s) {
                return;
            }
        });

        ImageButton subredditSearch = (ImageButton) findViewById(R.id.btn_search);
        subredditSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RedditService.searchSubreddits(ImageGridActivity.this,
                    mSubredditFilter.getText().toString(),
                    SharedPreferencesHelper.getShowNsfwImages(ImageGridActivity.this));
            }
        });

        SetDefaultSubredditsTask defaultSubredditsTask = new SetDefaultSubredditsTask();
        defaultSubredditsTask.execute();

        loadSharedPreferences();

        // Whether we are in grid or list view
        if (savedInstanceState != null && savedInstanceState.containsKey(Consts.ACTIVE_VIEW)) {
            mActiveViewType = ViewType.valueOf(savedInstanceState.getString(Consts.ACTIVE_VIEW));
        }
        changeActiveViewType(mActiveViewType);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMySubreddits, new IntentFilter(Consts.BROADCAST_MY_SUBREDDITS));
        LocalBroadcastManager.getInstance(this).registerReceiver(mLoginComplete, new IntentFilter(Consts.BROADCAST_LOGIN_COMPLETE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mSubredditsSearch, new IntentFilter(Consts.BROADCAST_SUBREDDIT_SEARCH));
        initializeLoaders();
    }

    private void initializeLoaders() {
        LoaderManager loaderManager = getSupportLoaderManager();
        loaderManager.initLoader(Consts.LOADER_LOGIN, null, this);
        loaderManager.initLoader(Consts.LOADER_SUBREDDITS, null, this);
    }

    private void initializeActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
    }

    private void loadSharedPreferences() {
        mShowNsfwImages = SharedPreferencesHelper.getShowNsfwImages(ImageGridActivity.this);
        mAge = SharedPreferencesHelper.getAge(ImageGridActivity.this);
        mCategory = SharedPreferencesHelper.getCategory(ImageGridActivity.this);
    }

    public Fragment getImageGridFragment() {
        ImageGridFragment fragment = (ImageGridFragment) getSupportFragmentManager().findFragmentByTag(ImageGridFragment.TAG);
        if (fragment == null) {
            fragment = getNewImageGridFragment();
        }

        return fragment;
    }

    public Fragment getImageListFragment() {
        ImageListFragment fragment = (ImageListFragment) getSupportFragmentManager().findFragmentByTag(ImageListFragment.TAG);
        if (fragment == null) {
            fragment = getNewImageListFragment();
        }

        return fragment;
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMySubreddits);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLoginComplete);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mSubredditsSearch);
        super.onPause();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the user is logged in, update the Logout menu item to "Log out <username>"
        if (RedditLoginInformation.isLoggedIn()) {
            mLoginMenuItem.setTitle(getString(R.string.log_out_) + RedditLoginInformation.getUsername());
            mLoginMenuItem.setIcon(R.drawable.ic_action_exit_dark);
        } else {
            mLoginMenuItem.setTitle(R.string.log_on);
            mLoginMenuItem.setIcon(R.drawable.ic_action_key_dark);
        }

        return true;
    }

    private SubredditMenuDrawerCursorAdapter getSubredditMenuAdapter() {
        mSubredditAdapter = new SubredditMenuDrawerCursorAdapter(ImageGridActivity.this, mSubredditActionListener);

        return mSubredditAdapter;
    }

    public ImageGridFragment getNewImageGridFragment() {
        return new ImageGridFragment();
    }

    public ImageListFragment getNewImageListFragment() {
        return new ImageListFragment();
    }

    private AdapterView.OnItemClickListener mSubredditClickListener =
        new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                mSelectedSubreddit = cursor.getString(
                    cursor.getColumnIndex(RedditContract.SubredditColumns.DISPLAY_NAME));
                int priority =
                    cursor.getInt(cursor.getColumnIndex(RedditContract.SubredditColumns.PRIORITY));

                // TODO: Make this less hacky...
                // Load the actual frontpage of reddit if selected
                if (priority == MySubredditsResponse.DefaultSubreddit.FRONTPAGE.getPriority()) {
                    mSelectedSubreddit = RedditUrl.REDDIT_FRONTPAGE;
                }

                mSubredditDrawer.setActiveView(view, position);
                mSubredditAdapter.setActivePosition(position);
                mSubredditDrawer.closeMenu(true);
                loadSubreddit(mSelectedSubreddit);
            }
        };

    private void loadSubreddit(String subredditName) {
        setRequestInProgress(true);
        String title = subredditName;
        // If we don't have a subreddit, default to the Frontpage also.
        if (Strings.isEmpty(subredditName) || subredditName.equals(RedditUrl.REDDIT_FRONTPAGE)) {
            title = "Frontpage";
        }

        Intent intent = new Intent(Consts.BROADCAST_SUBREDDIT_SELECTED);
        getSupportActionBar().setTitle(title);
        intent.putExtra(Consts.EXTRA_SELECTED_SUBREDDIT, subredditName);
        intent.putExtra(Consts.EXTRA_AGE, mAge.name());
        intent.putExtra(Consts.EXTRA_CATEGORY, mCategory.name());
        LocalBroadcastManager.getInstance(ImageGridActivity.this).sendBroadcast(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //        outState.putInt(Consts.EXTRA_NAV_POSITION, getSupportActionBar().getSelectedNavigationIndex());
        outState.putString(Consts.EXTRA_SELECTED_SUBREDDIT, getSubreddit());
        outState.putString(Consts.ACTIVE_VIEW, mActiveViewType.name());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.main, menu);

        mLoginMenuItem = menu.findItem(R.id.login);
        MenuItem item;
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
            case HOT:    menu.findItem(R.id.category_hot).setChecked(true)   ; break;
            case NEW:    menu.findItem(R.id.category_new).setChecked(true)   ; break;
            case RISING: menu.findItem(R.id.category_rising).setChecked(true); break;
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

        MenuItem mActiveViewMenuItem = menu.findItem(R.id.change_view);
        switch (mActiveViewType) {
            case LIST:
                mActiveViewMenuItem.setIcon(R.drawable.ic_action_tiles_small_dark);
                break;

            case GRID:
                mActiveViewMenuItem.setIcon(R.drawable.ic_action_list_2_dark);
                break;
        }
        return true;
    }

    public void startPreferences() {
        Intent intent = new Intent(ImageGridActivity.this, getPreferencesClass());
        intent.putExtra(Consts.EXTRA_SHOW_NSFW_IMAGES, mShowNsfwImages);
        startActivityForResult(intent, SETTINGS_REQUEST);
    }

    public Class<? extends PreferenceActivity> getPreferencesClass() {
        return RedditInPicturesPreferences.class;
    }

    /**
     * Change the current view type to the input viewtype
     *
     * @param newViewType {@link ViewType} to switch to.
     */
    private void changeActiveViewType(ViewType newViewType) {
        FragmentManager fm = getSupportFragmentManager();
        final FragmentTransaction ft = fm.beginTransaction();
        String oldFragmentTag = null;
        String newFragmentTag = null;
        Fragment newFragment = null;

        switch (newViewType) {
            case GRID:
                mActiveViewType = ViewType.GRID;
                oldFragmentTag = ImageListFragment.TAG;
                newFragmentTag = ImageGridFragment.TAG;
                newFragment = getImageGridFragment();
                break;
            case LIST:
                mActiveViewType = ViewType.LIST;
                oldFragmentTag = ImageGridFragment.TAG;
                newFragmentTag = ImageListFragment.TAG;
                newFragment = getImageListFragment();
                break;
        }

        Fragment oldFragment = fm.findFragmentByTag(oldFragmentTag);
        if (oldFragment != null) {
            // Setting the first visible item in the
            // ImageGridFragment and ImageListFragment
            // is dependent on the fragment being
            // hidden so that the first visible position
            // can be saved and picked up by the next
            // image viewing fragment
            ft.hide(oldFragment);
        }

        if (newFragment.isAdded()) {
            ft.show(newFragment);
        } else {
            ft.add(mSubredditDrawer.getContentContainer().getId(), newFragment, newFragmentTag);
        }
        ft.commit();
        invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        item.setChecked(true);
        boolean loadFromUrl = false;

        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            mSubredditDrawer.toggleMenu();
        } else if (itemId == R.id.change_view) {
            ViewType newViewType = mActiveViewType == ViewType.LIST ? ViewType.GRID : ViewType.LIST;
            changeActiveViewType(newViewType);
        } else if (itemId == R.id.settings) {
            startPreferences();
        } else if (itemId == R.id.refresh_all) {
            loadSubreddit(mSelectedSubreddit);
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
            Log.i(TAG, "onOptionsItemSelected, loadFromUrl = true, calling loadSubreddit()");
            loadSubreddit(mSelectedSubreddit);
        }

        return true;
    }

    public void handleLoginAndLogout() {
        if (!RedditLoginInformation.isLoggedIn()) {
            LoginDialogFragment loginFragment = LoginDialogFragment.newInstance();
            loginFragment.show(getSupportFragmentManager(), Consts.DIALOG_LOGIN);
        } else {
            DialogFragment logoutFragment = LogoutDialogFragment.newInstance(mUsername);
            logoutFragment.show(getSupportFragmentManager(), Consts.DIALOG_LOGOUT);
        }
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        // If the menu drawer is open it, close it. Otherwise go about the normal business.
        if (mSubredditDrawer != null && mSubredditDrawer.isMenuVisible()) {
            mSubredditDrawer.closeMenu();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_SUBREDDITS_REQUEST && resultCode == RESULT_OK) {
            // TODO: Make sure the menudrawer updates the list of subreddits

            // If the user tapped an item in the subreddit list, select that subreddit and load the
            // new images
            if (data.hasExtra(Consts.EXTRA_NEWLY_SELECTED_SUBREDDIT)) {
                boolean mFirstCall = false;
                int pos = getSubredditPosition(
                    data.getStringExtra(Consts.EXTRA_NEWLY_SELECTED_SUBREDDIT));
                //                onNavigationItemSelected(pos, 0);
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
                // onNavigationItemSelected(Consts.POSITION_FRONTPAGE, 0);
            }
        } else if (requestCode == SETTINGS_REQUEST && resultCode == RESULT_OK) {
            if (data.getBooleanExtra(Consts.EXTRA_SHOW_NSFW_IMAGES_CHANGED, false)) {
                mShowNsfwImages = SharedPreferencesHelper.getShowNsfwImages(this);

                // If we're removing NSFW images we can modify the adapter in place, otherwise we
                // need to refresh
                if (mShowNsfwImages) {
                    //                    onNavigationItemSelected(getSupportActionBar().getSelectedNavigationIndex(), 0);
                } else {
                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Consts.BROADCAST_REMOVE_NSFW_IMAGES));
                }
            }
        }
    }

    /**
     * Return the position of the subreddit in the navigation spinner, or -1 if the subreddit is
     * not
     * found.
     *
     * @param subredditName Name of the subreddit to find
     * @return The position of the subreddit in the spinner, or -1 if the subreddit is not found
     */
    public int getSubredditPosition(String subredditName) {
        //        for (int i = 0; i < mSpinnerAdapter.getCount(); i++) {
        //            String curReddit = (String) mSpinnerAdapter.getItem(i);
        //            if (curReddit.equalsIgnoreCase(subredditName)) {
        //                return i;
        //            }
        //        }

        return -1;
    }

    public void selectSubredditInNavigation(String subredditName) {
        int position = getSubredditPosition(subredditName);
        if (position == -1) {
            if (BuildConfig.DEBUG) {
                throw new ArrayIndexOutOfBoundsException(
                    "Subreddit \"" + subredditName + "\" does not exist in the navigation!");
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
        if (mProgressDialog != null && mProgressDialog.isShowing()) mProgressDialog.dismiss();
    }

    private void setRequestInProgress(boolean requestInProgress) {
        animate(mProgressBar).setDuration(500).alpha(requestInProgress ? 100 : 0);
    }

    @Override
    public void onFinishLoginDialog(String username, String password) {
        mUsername = username;
        showProgressDialog(getString(R.string.log_on), getString(R.string.logging_on));
        RedditService.login(this, username, password);
    }

    @Override
    public void onFinishLogoutDialog() {
        // Clear out the login data, Reddit API doesn't incorporate sessions into how it works so simply clearing out the cached data does the trick.
        RedditLoginInformation.setLoginData(null);
        SetDefaultSubredditsTask defaultSubredditsTask = new SetDefaultSubredditsTask(true);
        defaultSubredditsTask.execute();
        loadSubreddit(RedditUrl.REDDIT_FRONTPAGE);
        invalidateOptionsMenu();
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
        return mSelectedSubreddit;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle paramBundle) {
        Log.i(TAG, "onCreateLoader");
        switch (id) {
            case Consts.LOADER_LOGIN:
                return new CursorLoader(this, RedditContract.Login.CONTENT_URI, null, null, null, RedditContract.Login.DEFAULT_SORT);
            case Consts.LOADER_SUBREDDITS:
                String selection = null;
                String[] selectionArgs = null;

                if (paramBundle != null && paramBundle.containsKey(Consts.EXTRA_QUERY)) {
                    String query = paramBundle.getString(Consts.EXTRA_QUERY);

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

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case Consts.LOADER_LOGIN:
                if (cursor != null) {
                    Ln.i(TAG, "onLoadFinished LOADER_LOGIN, " + cursor.getCount() + " rows");
                    if (cursor.moveToFirst()) {
                        mUsername = cursor.getString(cursor.getColumnIndex(RedditContract.Login.USERNAME));
                        String cookie = cursor.getString(cursor.getColumnIndex(RedditContract.Login.COOKIE));
                        String modhash = cursor.getString(cursor.getColumnIndex(RedditContract.Login.MODHASH));
                        Log.i(TAG, "Username = " + mUsername);
                        Log.i(TAG, "Cookie = " + cookie);
                        Log.i(TAG, "Modhash = " + modhash);

                        LoginData data = new LoginData(mUsername, modhash, cookie);
                        if (!data.equals(RedditLoginInformation.getLoginData())) {
                            RedditLoginInformation.setLoginData(data);

                            if (mLoginLoaderHasRun) {
                                loadSubreddit(RedditUrl.REDDIT_FRONTPAGE);
                                mLoginLoaderHasRun = false;
                            }
                        }

                        hideProgressDialog();
                        invalidateOptionsMenu();

                        SetDefaultSubredditsTask defaultSubredditsTask = new SetDefaultSubredditsTask();
                        defaultSubredditsTask.execute();
                    }
                }
                break;

            case Consts.LOADER_SUBREDDITS:
                setRequestInProgress(false);
                mSubredditAdapter.swapCursor(cursor);
                if (cursor != null) {
                    Log.i(TAG, "onLoadFinished LOADER_SUBREDDITS, " + cursor.getCount() + " rows");
                    hideProgressDialog();
                    invalidateOptionsMenu();
                }

                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case Consts.LOADER_SUBREDDITS:
                mSubredditAdapter.swapCursor(null);
                break;
        }
    }

    class SetDefaultSubredditsTask extends SafeAsyncTask<Void> {
        boolean forceDefaults = false;

        public SetDefaultSubredditsTask() {
        }

        public SetDefaultSubredditsTask(boolean forceDefaults) {
            this.forceDefaults = forceDefaults;
        }

        @Override
        public Void call() throws Exception {
            // If the user is logged in, we just want to update to what they have set.
            if (RedditLoginInformation.isLoggedIn()) {
                RedditService.getMySubreddits(ImageGridActivity.this);
            } else {
                RedditDatabase mDatabaseHelper = new RedditDatabase(ImageGridActivity.this);
                SQLiteDatabase mDatabase = mDatabaseHelper.getWritableDatabase();

                // Using a separate variable here since I want to consolidate operations and not overwrite the control variable possibly causing more problems.
                boolean terminateSubreddits = forceDefaults;

                // If we aren't terminating them by default, check to see if they have none. If so we want to set it to the defaults.
                if (!terminateSubreddits) {
                    // See how many Subreddits are in the database. Only needed if not forcing defaults.
                    long numSubreddits =
                        DatabaseUtils.queryNumEntries(mDatabase, RedditDatabase.Tables.SUBREDDITS);
                    Ln.d("Number of Subreddits is: %d", numSubreddits);
                    mDatabase.close();

                    // Set the indicator to cause the subreddits to be overwritten if we have no records.
                    if (numSubreddits == 0) {
                        terminateSubreddits = true;
                    }
                }

                // If we either don't have any subreddits or we want to force them to defaults.
                if (terminateSubreddits) {
                    SubredditUtils.setDefaultSubreddits(ImageGridActivity.this);
                }
            }
            return null;
        }
    }

    @Override
    public int getFirstVisiblePosition() {
        return mFirstVisiblePos;
    }

    @Override
    public void setFirstVisiblePosition(int firstVisiblePosition) {
        mFirstVisiblePos = firstVisiblePosition;
    }
}