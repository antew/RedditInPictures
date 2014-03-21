package com.antew.redditinpictures.library.ui;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.antew.redditinpictures.library.dialog.LoginDialogFragment;
import com.antew.redditinpictures.library.dialog.LogoutDialogFragment;
import com.antew.redditinpictures.library.enums.Age;
import com.antew.redditinpictures.library.enums.Category;
import com.antew.redditinpictures.library.event.LoadSubredditEvent;
import com.antew.redditinpictures.library.preferences.RedditInPicturesPreferences;
import com.antew.redditinpictures.library.preferences.SharedPreferencesHelper;
import com.antew.redditinpictures.library.reddit.RedditLoginInformation;
import com.antew.redditinpictures.library.reddit.RedditSort;
import com.antew.redditinpictures.library.reddit.RedditUrl;
import com.antew.redditinpictures.library.ui.base.BaseFragmentActivityWithMenu;
import com.antew.redditinpictures.library.utils.Consts;
import com.antew.redditinpictures.library.utils.Ln;
import com.antew.redditinpictures.library.utils.Strings;
import com.antew.redditinpictures.library.utils.Util;
import com.antew.redditinpictures.pro.R;
import com.squareup.otto.Subscribe;

public class RedditFragmentActivity extends BaseFragmentActivityWithMenu {
    public static final int SETTINGS_REQUEST = 20;
    private ViewType mActiveViewType = ViewType.LIST;
    private String mSelectedSubreddit = RedditUrl.REDDIT_FRONTPAGE;
    private Category mCategory = Category.HOT;
    private Age mAge = Age.TODAY;

    private enum ViewType {LIST, GRID}

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reddit_fragment_activity);
        restoreInstanceState(savedInstanceState);
        initializeActiveView();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (Util.hasHoneycombMR1()) {
                mActiveViewType = ViewType.valueOf(
                    savedInstanceState.getString(Consts.ACTIVE_VIEW, ViewType.LIST.toString()));
                mSelectedSubreddit = savedInstanceState.getString(Consts.EXTRA_SELECTED_SUBREDDIT,
                    RedditUrl.REDDIT_FRONTPAGE);
            } else {
                if (savedInstanceState.containsKey(Consts.ACTIVE_VIEW)) {
                    mActiveViewType =
                        ViewType.valueOf(savedInstanceState.getString(Consts.ACTIVE_VIEW));
                }

                if (savedInstanceState.containsKey(Consts.EXTRA_SELECTED_SUBREDDIT)) {
                    mSelectedSubreddit =
                        savedInstanceState.getString(Consts.EXTRA_SELECTED_SUBREDDIT);
                }
            }
        }
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.change_view:
                changeViewType(mActiveViewType == ViewType.LIST ? ViewType.GRID : ViewType.LIST);
                return true;
            case R.id.settings:
                startPreferences();
                return true;
            case R.id.refresh_all:
                loadSubreddit(mSelectedSubreddit);
                return true;
            case R.id.login:
                handleLoginAndLogout();
                return true;
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
                    RedditSort.SortCriteria sortCriteria = RedditSort.get(item.getItemId());
                    mAge = sortCriteria.getAge();
                    mCategory = sortCriteria.getCategory();
                    loadSubreddit(mSelectedSubreddit);
                } else {
                    Ln.e("Unable to get sorting criteria for menu item id: " + item.getItemId() + ", unable to load subreddit");
                }
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void handleLoginAndLogout() {
        if (!RedditLoginInformation.isLoggedIn()) {
            LoginDialogFragment loginFragment = LoginDialogFragment.newInstance();
            loginFragment.show(getSupportFragmentManager(), Consts.DIALOG_LOGIN);
        } else {
            DialogFragment logoutFragment =
                LogoutDialogFragment.newInstance(RedditLoginInformation.getUsername());
            logoutFragment.show(getSupportFragmentManager(), Consts.DIALOG_LOGOUT);
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

    private void changeViewType(ViewType viewType) {
        if (viewType != null) {
            mActiveViewType = viewType;
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
    }

    public void startPreferences() {
        Intent intent = new Intent(this, RedditInPicturesPreferences.class);
        intent.putExtra(Consts.EXTRA_SHOW_NSFW_IMAGES,
            SharedPreferencesHelper.getShowNsfwImages(this));
        startActivityForResult(intent, SETTINGS_REQUEST);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    public Fragment getNewImageGridFragment() {
        return RedditImageGridFragment.newInstance(mSelectedSubreddit, mCategory, mAge);
    }

    public Fragment getNewImageListFragment() {
        return RedditImageListFragment.newInstance(mSelectedSubreddit, mCategory, mAge);
    }

    @Subscribe
    public void onLoadSubredditEvent(LoadSubredditEvent event) {
        if (event != null) {
            mSelectedSubreddit = event.getSubreddit();
            loadSubreddit(mSelectedSubreddit, mCategory, mAge);
            Toast.makeText(this, "Loading: " + event.getSubreddit(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadSubreddit(String subreddit, Category category, Age age) {
        if (Strings.notEmpty(subreddit)) {
            mSelectedSubreddit = subreddit;
        }

        if (category != null) {
            mCategory = category;
        }

        if (age != null) {
            mAge = age;
        }

        switch (mActiveViewType) {
            case GRID:
                FragmentTransaction gridTrans = getSupportFragmentManager().beginTransaction();
                gridTrans.replace(R.id.content_fragment, getNewImageGridFragment());
                gridTrans.addToBackStack(null);
                gridTrans.commit();
                break;
            case LIST:
                FragmentTransaction listTrans = getSupportFragmentManager().beginTransaction();
                listTrans.replace(R.id.content_fragment, getNewImageListFragment());
                listTrans.addToBackStack(null);
                listTrans.commit();
                break;
        }

        setActionBarTitle(mSelectedSubreddit);
    }
}
