package com.antew.redditinpictures.library.ui;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.androidquery.callback.AjaxStatus;
import com.antew.redditinpictures.library.BuildConfig;
import com.antew.redditinpictures.library.R;
import com.antew.redditinpictures.library.adapter.SubredditMenuAdapter;
import com.antew.redditinpictures.library.dialog.LoginDialogFragment;
import com.antew.redditinpictures.library.dialog.LoginDialogFragment.LoginDialogListener;
import com.antew.redditinpictures.library.dialog.LogoutDialogFragment;
import com.antew.redditinpictures.library.dialog.LogoutDialogFragment.LogoutDialogListener;
import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.preferences.RedditInPicturesPreferences;
import com.antew.redditinpictures.library.preferences.RedditInPicturesPreferencesFragment;
import com.antew.redditinpictures.library.preferences.SharedPreferencesHelper;
import com.antew.redditinpictures.library.reddit.MySubreddits;
import com.antew.redditinpictures.library.reddit.MySubreddits.SubredditData;
import com.antew.redditinpictures.library.reddit.RedditApi;
import com.antew.redditinpictures.library.reddit.RedditApi.Children;
import com.antew.redditinpictures.library.reddit.RedditApi.PostData;
import com.antew.redditinpictures.library.reddit.RedditApiManager;
import com.antew.redditinpictures.library.reddit.RedditLoginResponse;
import com.antew.redditinpictures.library.reddit.RedditUrl;
import com.antew.redditinpictures.library.reddit.RedditUrl.Age;
import com.antew.redditinpictures.library.reddit.RedditUrl.Category;
import com.antew.redditinpictures.library.subredditmanager.SubredditManager;
import com.antew.redditinpictures.library.subredditmanager.SubredditManagerApi11Plus;
import com.antew.redditinpictures.library.ui.ImageGridFragment.LoadMoreImages;
import com.antew.redditinpictures.library.utils.Consts;
import com.antew.redditinpictures.library.utils.ImageUtil;
import com.antew.redditinpictures.library.utils.StringUtil;
import com.antew.redditinpictures.library.utils.Util;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Simple FragmentActivity to hold the main {@link ImageGridFragment} and not much else.
 */
public abstract class ImageGridActivity extends SherlockFragmentActivity implements OnNavigationListener, LoadMoreImages,
        LoginDialogListener, LogoutDialogListener {
    private static final String TAG                     = "ImageGridActivity";
    public static final int     POSTS_TO_FETCH          = 50;
    protected boolean           mShowNsfwImages;
    protected Age               mAge                    = Age.TODAY;
    protected Category          mCategory               = Category.HOT;
    public static final int     EDIT_SUBREDDITS_REQUEST = 10;
    public static final int     SETTINGS_REQUEST        = 20;
    private String              mSubreddit;
    private SpinnerAdapter      mSpinnerAdapter;
    private RedditLoginResponse mRedditLoginResponse;
    private boolean             mReplaceAdapter;
    private boolean             mRequestInProgress;
    private RedditApi           mRedditApi;
    private int                 mNsfwBlockedCount;
    private boolean             mFirstCall              = true;
    private int                 mNavPosition;
    private ProgressDialog      mProgressDialog;
    private MenuItem            mLoginMenuItem;
    private TextView            mErrorMessage;
    private List<PostData>      mEntries;
    protected RelativeLayout    mLayoutWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            Util.enableStrictMode();
        }
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.image_grid_activity);

        mErrorMessage = (TextView) findViewById(R.id.error_message);
        mLayoutWrapper = (RelativeLayout) findViewById(R.id.image_grid_wrapper);
        mShowNsfwImages = SharedPreferencesHelper.getShowNsfwImages(this);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setListNavigationCallbacks(getListNavigationSpinner(), this);

        if (savedInstanceState != null) {
            getSupportActionBar().setSelectedNavigationItem(savedInstanceState.getInt(Consts.EXTRA_NAV_POSITION));
        }

        if (getSupportFragmentManager().findFragmentByTag(ImageGridFragment.TAG) == null) {
            Log.i(TAG, "onCreate, fragment was null, populating from spinner");
            populateViewPagerFromSpinner(0);
        } else {
            Log.i(TAG, "onCreate, fragment already existed, doing nothing");
        }

        String loginJson = SharedPreferencesHelper.getLoginJson(ImageGridActivity.this);
        if (!loginJson.equals("")) {
            String username = SharedPreferencesHelper.getUsername(ImageGridActivity.this);
            String modHash = SharedPreferencesHelper.getModHash(ImageGridActivity.this);
            String cookie = SharedPreferencesHelper.getCookie(ImageGridActivity.this);
            RedditApiManager.parseRedditLoginResponse(username, modHash, cookie, loginJson);
            RedditApiManager.setIsLoggedIn(true);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mLoginMenuItem = menu.findItem(R.id.login);

        // If the user is logged in, update the Logout menu item to "Log out <username>"
        if (RedditApiManager.isLoggedIn()) {
            mLoginMenuItem.setTitle(getString(R.string.log_out_) + RedditApiManager.getUsername());
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
        List<String> subReddits = SharedPreferencesHelper.loadArray(SubredditManager.PREFS_NAME, SubredditManager.ARRAY_NAME,
                ImageGridActivity.this);
        subReddits = addHeaderSubreddits(subReddits);

        if (!(subReddits.size() > 2)) {
            subReddits = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.default_reddits)));
            subReddits = addHeaderSubreddits(subReddits);
        }

        mSpinnerAdapter = new SubredditMenuAdapter(ImageGridActivity.this, subReddits, mAge, mCategory);

        return mSpinnerAdapter;
    }

    private void populateViewPagerFromSpinner(int position) {
        Log.i(TAG, "In populateViewPagerFromSpinner mFirstCall = " + mFirstCall);
        if (mFirstCall) {
            mFirstCall = false;
            return;
        }

        ((SubredditMenuAdapter) mSpinnerAdapter).notifyDataSetChanged(mCategory, mAge);
        getSupportActionBar().setSelectedNavigationItem(position);

        mReplaceAdapter = true;
        mSubreddit = mSpinnerAdapter.getItem(position).toString();
        //@formatter:off
        RedditUrl url = new RedditUrl.Builder(position == 0 ? RedditUrl.REDDIT_FRONTPAGE : mSubreddit)
                                     .age(mAge)
                                     .category(mCategory)
                                     .count(POSTS_TO_FETCH)
                                     .isLoggedIn(mRedditLoginResponse != null)
                                     .build();
        //@formatter:on
        populateViewPagerFromUrl(url.getUrl());
    }

    public void populateViewPagerFromUrl(String url) {
        Log.i(TAG, "In populateViewPagerFromUrl - URL = " + url);
        setSupportProgressBarIndeterminateVisibility(true);
        mErrorMessage.setVisibility(View.GONE);
        mRequestInProgress = true;
        RedditApiManager.makeRequest(url, "subredditDataCallback", this);
    }

    public ImageGridFragment getImageGridFragment(List<PostData> entries) {
        ImageGridFragment frag = getImageGridFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ImageGridFragment.ENTRIES, (ArrayList<PostData>) entries);
        frag.setArguments(args);

        return frag;
    }

    public abstract ImageGridFragment getImageGridFragment();

    private void requestComplete() {
        mRequestInProgress = false;
        setSupportProgressBarIndeterminateVisibility(false);
    }

    public void subredditDataCallback(String url, String json, AjaxStatus status) {
        requestComplete();

        if (status.getCode() == HttpURLConnection.HTTP_OK) {
            RedditApi redditApi = null;

            try {
                redditApi = RedditApi.getGson().fromJson(json, RedditApi.class);
                mEntries = filterPosts(redditApi);
            } catch (JsonSyntaxException e) {
                Log.e(TAG, "subredditDataCallback - JsonSyntaxException while parsing json!", e);
                Toast.makeText(ImageGridActivity.this, getString(R.string.error_parsing_reddit_data), Toast.LENGTH_SHORT).show();
                return;
            } catch (IllegalStateException e) {
                Log.e(TAG, "subredditDataCallback - IllegalStateException while parsing json!", e);
                Toast.makeText(ImageGridActivity.this, getString(R.string.error_parsing_reddit_data), Toast.LENGTH_SHORT).show();
                return;
            }

            if (mReplaceAdapter || getSupportFragmentManager().findFragmentByTag(ImageGridFragment.TAG) == null) {
                mRedditApi = redditApi;
                final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(android.R.id.content, getImageGridFragment(mEntries), ImageGridFragment.TAG);
                ft.commit();
            } else {
                mRedditApi.getData().addChildren(filterChildren(redditApi));
                mRedditApi.getData().setAfter(redditApi.getData().getAfter());
                mRedditApi.getData().setBefore(redditApi.getData().getBefore());

                ImageGridFragment frag = (ImageGridFragment) getSupportFragmentManager().findFragmentByTag(ImageGridFragment.TAG);
                frag.addImages(mEntries);
            }
            // refreshText(mNsfwBlockedCount);

        } else {
            mErrorMessage.setText(R.string.make_sure_you_have_an_internet_connection);
            mErrorMessage.setVisibility(View.VISIBLE);
            Log.e(TAG, "populateViewPagerFromUrl - Error connecting to Reddit, response code was " + status.getCode());
        }
    }

    /**
     * This method removes unusable posts from the Reddit API data.
     * 
     * @param ra
     *            The API data returned from reddit (e.g. from http://www.reddit.com/.json).
     * @return The filtered list of posts after removing non-images, unsupported images, and NSFW
     *         entries. Whether NSFW images are retained can be controlled via settings.
     */
    public List<PostData> filterPosts(RedditApi ra) {
        List<PostData> entries = new ArrayList<PostData>();
        mNsfwBlockedCount = 0;

        boolean showNsfwImages = SharedPreferencesHelper.getShowNsfwImages(ImageGridActivity.this);
        for (Children c : ra.getData().getChildren()) {
            PostData pd = c.getData();
            if (ImageUtil.isSupportedUrl(pd.getUrl())) {
                if (!pd.isOver_18() || showNsfwImages)
                    entries.add(pd);
                else
                    mNsfwBlockedCount++;

            }
        }

        return entries;
    }

    public List<Children> filterChildren(RedditApi ra) {
        List<Children> entries = new ArrayList<Children>();
        mNsfwBlockedCount = 0;

        boolean showNsfwImages = SharedPreferencesHelper.getShowNsfwImages(ImageGridActivity.this);
        for (Children c : ra.getData().getChildren()) {
            PostData pd = c.getData();
            if (ImageUtil.isSupportedUrl(pd.getUrl())) {
                if (!pd.isOver_18() || showNsfwImages)
                    entries.add(c);
                else
                    mNsfwBlockedCount++;

            }
        }

        return entries;
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        populateViewPagerFromSpinner(itemPosition);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(Consts.EXTRA_NAV_POSITION, getSupportActionBar().getSelectedNavigationIndex());
        outState.putString(Consts.EXTRA_SELECTED_SUBREDDIT,
                (String) mSpinnerAdapter.getItem(getSupportActionBar().getSelectedNavigationIndex()));
        outState.putParcelable(Consts.EXTRA_REDDIT_API, mRedditApi);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(Consts.EXTRA_SELECTED_SUBREDDIT))
            mSubreddit = savedInstanceState.getString(Consts.EXTRA_SELECTED_SUBREDDIT);

        if (savedInstanceState.containsKey(Consts.EXTRA_NAV_POSITION)) {
            mNavPosition = savedInstanceState.getInt(Consts.EXTRA_NAV_POSITION);
        }

        if (savedInstanceState.containsKey(Consts.EXTRA_REDDIT_API))
            mRedditApi = savedInstanceState.getParcelable(Consts.EXTRA_REDDIT_API);
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
        Intent intent = null;
        if (Util.hasHoneycomb())
            intent = new Intent(ImageGridActivity.this, SubredditManagerApi11Plus.class);
        else
            intent = new Intent(ImageGridActivity.this, SubredditManager.class);

        int index = getSupportActionBar().getSelectedNavigationIndex();
        intent.putExtra(Consts.EXTRA_SELECTED_SUBREDDIT, (String) mSpinnerAdapter.getItem(index));
        startActivityForResult(intent, EDIT_SUBREDDITS_REQUEST);
    }

    public abstract void startPreferences();

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
            populateViewPagerFromSpinner(getSupportActionBar().getSelectedNavigationIndex());
        } else if (itemId == R.id.login) {
            if (!RedditApiManager.isLoggedIn()) {
                LoginDialogFragment loginFragment = LoginDialogFragment.newInstance();
                loginFragment.show(getSupportFragmentManager(), Consts.DIALOG_LOGIN);
            } else {
                DialogFragment logoutFragment = LogoutDialogFragment.newInstance();
                logoutFragment.show(getSupportFragmentManager(), Consts.DIALOG_LOGOUT);
            }
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
            Log.i(TAG, "onOptionsItemSelected, loadFromUrl = true, calling populateViewPagerFromSpinner()");
            populateViewPagerFromSpinner(getSupportActionBar().getSelectedNavigationIndex());
        }

        return true;
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
                populateViewPagerFromSpinner(pos);
                return;
            }
            // If the user didn't choose a subreddit (meaning we are returned the subreddit they
            // were previously viewing), select it in the list, if it doesn't exist any longer we
            // default to the Front page.
            int selectedSubredditPos = getSubredditPosition(data.getStringExtra(Consts.EXTRA_SELECTED_SUBREDDIT));
            if (selectedSubredditPos >= 0)
                getSupportActionBar().setSelectedNavigationItem(selectedSubredditPos);
            else
                populateViewPagerFromSpinner(Consts.POSITION_FRONTPAGE);

        } else if (requestCode == SETTINGS_REQUEST && resultCode == RESULT_OK) {
            if (data.getBooleanExtra(Consts.EXTRA_SHOW_NSFW_IMAGES_CHANGED, false)) {
                mShowNsfwImages = SharedPreferencesHelper.getShowNsfwImages(this);

                // If we're removing NSFW images we can modify the adapter in place, otherwise we
                // need to refresh
                if (mShowNsfwImages) {
                    populateViewPagerFromSpinner(getSupportActionBar().getSelectedNavigationIndex());
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

    @Override
    public void loadMoreImages() {

        //@formatter:off
        String after = mRedditApi != null 
                    && mRedditApi.getData() != null 
                    && mRedditApi.getData().getAfter() != null ? mRedditApi.getData().getAfter() 
                                                               : null;
        
        // If a request isn't currently in progress and reddit indicated there were more images to load make the request
        if (!mRequestInProgress && after != null) {
            RedditUrl.Builder builder = new RedditUrl.Builder(getSupportActionBar().getSelectedNavigationIndex() == 0 ? RedditUrl.REDDIT_FRONTPAGE : mSubreddit)
                                                     .age(mAge)
                                                     .category(mCategory)
                                                     .count(POSTS_TO_FETCH)
                                                     .isLoggedIn(mRedditLoginResponse != null)
                                                     .after(after);

            mReplaceAdapter = false;
            Log.i(TAG, "loadMoreImages() calling populateViewPagerFromUrl()");
            populateViewPagerFromUrl(builder.build().getUrl());
                    //@formatter:on
        } else {
            Log.i(TAG, "loadMoreImages() - Request already in progress, ignoring");
        }

    }

    private void showLoginProgressDialog() {
        mProgressDialog = ProgressDialog.show(ImageGridActivity.this, getString(R.string.log_on), getString(R.string.logging_on), true,
                false);
    }

    public void loginCallback(String url, String json, AjaxStatus status) {
        if (status.getCode() == HttpURLConnection.HTTP_OK && status.getCookies().size() > 0) {
            Log.i("JSON", json);
            Gson gson = new Gson();

            mRedditLoginResponse = gson.fromJson(json, RedditLoginResponse.class);

            if (mRedditLoginResponse.getLoginResponse().getErrors().size() > 0) {
                hideProgressDialog();
                RedditApiManager.setIsLoggedIn(false);
                showLoginError(mRedditLoginResponse);
            } else {
                RedditApiManager.setIsLoggedIn(true);
                RedditApiManager.getMySubreddits("mySubredditsCallback", ImageGridActivity.this);
                invalidateOptionsMenu();
            }

        } else {
            Log.e("Login", "Something went wrong on login! status = " + status.getCode() + " | json = " + json == null ? "null" : json);
        }
    }

    private void showLoginError(RedditLoginResponse rlp) {
        String errorText = "";
        for (String[] error : rlp.getLoginResponse().getErrors())
            errorText += error[1] + " ";

        Toast.makeText(ImageGridActivity.this, getString(R.string.error) + errorText, Toast.LENGTH_SHORT).show();
    }

    public void mySubredditsCallback(String url, String json, AjaxStatus status) {
        if (status.getCode() == HttpURLConnection.HTTP_OK) {
            Gson gson = new Gson();
            MySubreddits mySubreddits = gson.fromJson(json, MySubreddits.class);
            Log.i("MyRedditsJson", json);

            List<String> subReddits = new ArrayList<String>();

            for (MySubreddits.Children c : mySubreddits.getData().getChildren()) {
                SubredditData data = c.getData();
                subReddits.add(data.getDisplay_name());
                Log.i("Subscribed Subreddits", data.getDisplay_name());
            }

            Collections.sort(subReddits, StringUtil.getCaseInsensitiveComparator());
            SharedPreferencesHelper.saveArray(subReddits, SubredditManager.PREFS_NAME, SubredditManager.ARRAY_NAME, ImageGridActivity.this);

            getSupportActionBar().setListNavigationCallbacks(getListNavigationSpinner(), ImageGridActivity.this);
        } else {
            Toast.makeText(ImageGridActivity.this, "Error retrieving subscribed subreddits.", Toast.LENGTH_SHORT).show();
            Log.e("MySubreddits",
                    "Something went wrong on mySubreddits! status = " + status.getCode() + " | json = " + json == null ? "null" : json);

        }

        hideProgressDialog();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }

    @Override
    public void onFinishLoginDialog(String username, String password) {
        showLoginProgressDialog();
        RedditApiManager.login(username, password, ImageGridActivity.this, "loginCallback");
    }

    @Override
    public void onFinishLogoutDialog() {
        RedditApiManager.logout(ImageGridActivity.this);
        invalidateOptionsMenu();
        getSupportActionBar().setListNavigationCallbacks(getListNavigationSpinner(), this);
        populateViewPagerFromSpinner(0);
    }

}
