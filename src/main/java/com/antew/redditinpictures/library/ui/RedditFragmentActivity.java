package com.antew.redditinpictures.library.ui;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.antew.redditinpictures.library.enums.Age;
import com.antew.redditinpictures.library.enums.Category;
import com.antew.redditinpictures.library.event.LoadSubredditEvent;
import com.antew.redditinpictures.library.reddit.RedditUrl;
import com.antew.redditinpictures.library.ui.base.BaseFragmentActivityWithMenu;
import com.antew.redditinpictures.library.utils.Consts;
import com.antew.redditinpictures.library.utils.Util;
import com.antew.redditinpictures.pro.R;
import com.squareup.otto.Subscribe;

public class RedditFragmentActivity extends BaseFragmentActivityWithMenu {
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

    private void initializeActiveView() {
        switch (mActiveViewType) {
            case GRID:
                break;
            case LIST:
                Fragment listFragment = getNewImageListFragment();
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction trans = fm.beginTransaction();
                trans.replace(R.id.content_fragment, listFragment);
                trans.commit();
                break;
        }
    }

    private void changeViewType(ViewType newViewType) {
        /*
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
        */
    }

    public Fragment getImageGridFragment() {
        ImageGridFragment fragment =
            (ImageGridFragment) getSupportFragmentManager().findFragmentByTag(
                ImageGridFragment.TAG);
        if (fragment == null) {
            fragment = getNewImageGridFragment();
        }

        return fragment;
    }

    public Fragment getImageListFragment() {
        Fragment fragment =
            (ImageListFragment) getSupportFragmentManager().findFragmentByTag(
                ImageListFragment.TAG);
        if (fragment == null) {
            fragment = getNewImageListFragment();
        }

        return fragment;
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    public ImageGridFragment getNewImageGridFragment() {
        return new ImageGridFragment();
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
        Fragment f = RedditImageListFragment.newInstance(mSelectedSubreddit, mCategory, mAge);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction trans = fm.beginTransaction();
        trans.replace(R.id.content_fragment, f);
        trans.addToBackStack(subreddit + category.toString() + age.toString());
        trans.commit();
    }
}
