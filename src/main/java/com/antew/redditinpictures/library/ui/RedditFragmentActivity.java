package com.antew.redditinpictures.library.ui;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;
import butterknife.InjectView;
import butterknife.Optional;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.antew.redditinpictures.library.dialog.LoginDialogFragment;
import com.antew.redditinpictures.library.dialog.LogoutDialogFragment;
import com.antew.redditinpictures.library.enums.Age;
import com.antew.redditinpictures.library.enums.Category;
import com.antew.redditinpictures.library.event.LoadSubredditEvent;
import com.antew.redditinpictures.library.event.ProgressChangedEvent;
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
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.squareup.otto.Subscribe;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class RedditFragmentActivity extends BaseFragmentActivityWithMenu {
    public static final int SETTINGS_REQUEST = 20;
    private ViewType mActiveViewType = ViewType.LIST;
    private String mSelectedSubreddit = RedditUrl.REDDIT_FRONTPAGE;
    private Category mCategory = Category.HOT;
    private Age mAge = Age.TODAY;

    private enum ViewType {LIST, GRID}

    @InjectView(R.id.top_progressbar)
    protected SmoothProgressBar mProgressBar;

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
                    RedditSort.SortCriteria sortCriteria = RedditSort.get(item.getItemId());
                    mAge = sortCriteria.getAge();
                    mCategory = sortCriteria.getCategory();
                    loadSubreddit(mSelectedSubreddit);
                    return true;
                } else {
                    Ln.e("Unable to get sorting criteria for menu item id: "
                        + item.getItemId()
                        + ", unable to load subreddit");

                    // Fallback to the normal Hot category.
                    RedditSort.SortCriteria sortCriteria = RedditSort.get(R.id.category_hot);
                    if (sortCriteria != null) {
                        mAge = sortCriteria.getAge();
                        mCategory = sortCriteria.getCategory();
                        loadSubreddit(mSelectedSubreddit);
                        return true;
                    }
                }
                return super.onOptionsItemSelected(item);
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

    @Subscribe
    public void progressChanged(ProgressChangedEvent event) {
        if (event != null) {
            ViewPropertyAnimator.animate(mProgressBar)
                .setDuration(500)
                .alpha(event.isInProgress() ? 100 : 0);
        } else {
            ViewPropertyAnimator.animate(mProgressBar)
                .setDuration(500)
                .alpha(0);
        }
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.main, menu);

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
                break;
            case GRID:
                activeViewMenuItem.setIcon(R.drawable.ic_action_list_2_dark);
                break;
            default:
                break;
        }

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
