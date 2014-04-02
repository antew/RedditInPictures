/*
 * Copyright (C) 2012 Antew | antewcode@gmail.com
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
package com.antew.redditinpictures.library.subredditmanager;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.preferences.SharedPreferencesHelper;
import com.antew.redditinpictures.library.reddit.RedditLoginInformation;
import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.library.utils.Constants;
import com.antew.redditinpictures.library.utils.StringUtil;
import com.antew.redditinpictures.pro.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@TargetApi(11)
public class SubredditManagerApi11Plus extends SubredditManager {

    public static final String   TAG                = SubredditManagerApi11Plus.class.getSimpleName();
    private String               mSelectedSubreddit = Constants.REDDIT_FRONTPAGE;
    private MenuItem             mResetToDefaultSubreddits;
    private MenuItem             mResyncWithReddit;

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().hasExtra(Constants.EXTRA_SELECTED_SUBREDDIT)) {
            mSelectedSubreddit = getIntent().getStringExtra(Constants.EXTRA_SELECTED_SUBREDDIT);
        }

        setListAdapter(getSubredditsFromSharedPreferences());

        final ListView listView = getListView();
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new ModeCallback());
    };

    /**
     * Get a list of subreddits in alphabetical order
     *
     * @return Returns a list of saved subreddits in alphabetical order, or the default list of subreddits if none have been saved
     */
    public List<String> getSubredditsFromSharedPreferences() {

        List<String> subreddits = SharedPreferencesHelper.loadArray(PREFS_NAME, ARRAY_NAME, SubredditManagerApi11Plus.this);

        if (subreddits.size() == 0) {
            subreddits = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.default_subreddits)));
        }

        Collections.sort(subreddits, StringUtil.getCaseInsensitiveComparator());

        return subreddits;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.i("onPrepareOptionsMenu", "called");
        mResetToDefaultSubreddits = menu.findItem(R.id.reset_subreddits);
        mResyncWithReddit = menu.findItem(R.id.resync_subreddits);

        if (RedditLoginInformation.isLoggedIn()) {
            mResetToDefaultSubreddits.setEnabled(false);
            mResyncWithReddit.setEnabled(true);
            mResetToDefaultSubreddits.setVisible(false);
            mResyncWithReddit.setVisible(true);
        } else {
            mResetToDefaultSubreddits.setEnabled(true);
            mResyncWithReddit.setEnabled(false);
            mResetToDefaultSubreddits.setVisible(true);
            mResyncWithReddit.setVisible(false);
        }

        return true;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent();
        i.putExtra(Constants.EXTRA_NEWLY_SELECTED_SUBREDDIT, getAdapter().getItem(position));
        setResult(RESULT_OK, i);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.edit_subreddits_api_11_plus, menu);
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getSupportActionBar().setSubtitle(R.string.long_press_to_start_selection);
    }

    @Override
    public int getListLayoutId() {
        return android.R.layout.simple_list_item_activated_1;
    }

    @Override
    public void resetToDefaultSubreddits() {
        List<String> subreddits = new ArrayList<String>();

        String[] reddits = getResources().getStringArray(R.array.default_subreddits);
        for (int i = 0; i < reddits.length; i++) {
            subreddits.add(reddits[i]);
        }

        Collections.sort(subreddits, StringUtil.getCaseInsensitiveComparator());

        setListAdapter(subreddits);
        SharedPreferencesHelper.saveArray(getReddits(), PREFS_NAME, ARRAY_NAME, SubredditManagerApi11Plus.this);
    }

    @Override
    public List<String> getReddits() {
        List<String> returnList = new ArrayList<String>();
        ArrayAdapter<String> adapter = getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            returnList.add(adapter.getItem(i));
            Log.i("getReddits", "" + adapter.getItem(i));
        }

        Collections.sort(returnList, StringUtil.getCaseInsensitiveComparator());
        return returnList;
    }

    @Override
    public void createAddSubredditAlertDialog() {
        if (isFinishing()) {
            return;
        }

        final EditText input = new EditText(SubredditManagerApi11Plus.this);

        //@formatter:off
        new AlertDialog.Builder(SubredditManagerApi11Plus.this)
                       .setTitle(R.string.add_subreddit)
                       .setMessage(R.string.enter_the_subreddit)
                       .setView(input)
                       .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ArrayAdapter<String> adapter = getAdapter();
                                String value = input.getText().toString();
                                
                                if (value.length() > 0) {
                                    adapter.add(value);
                                    adapter.sort(StringUtil.getCaseInsensitiveComparator());
                                    adapter.notifyDataSetChanged();
                                    
                                    if (RedditLoginInformation.isLoggedIn()) {
                                        RedditService.subscribe(getApplicationContext(), value);
                                    }
                                }
                            }
                       }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Do nothing.
                            }
                       }).show();

    } //@formatter:off

    private class ModeCallback implements ListView.MultiChoiceModeListener
    {

        private android.view.MenuItem mDelete;

        @Override
        public boolean onCreateActionMode(android.view.ActionMode mode, android.view.Menu menu)
        {
            mode.setTitle(R.string.select_items);
            
            android.view.MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.action_menu, menu);
            mDelete = menu.findItem(R.id.menu_delete);
            
            return true;
        }

        @Override
        public boolean onPrepareActionMode(android.view.ActionMode mode, android.view.Menu menu) {
            return true;
        }

        @Override
        public boolean onActionItemClicked(android.view.ActionMode mode, android.view.MenuItem item) {
            if (item.getItemId() == R.id.menu_delete) {
                final ListView listView = getListView();
                final SparseBooleanArray checkedItems = listView.getCheckedItemPositions();

                if (checkedItems == null) {
                    return true;
                }

                List<String> itemsToRemove = new ArrayList<String>();
                final int checkedItemsCount = checkedItems.size();
                for (int j = 0; j < checkedItemsCount; j++) {
                    if (checkedItems.valueAt(j)) {
                        final int position = checkedItems.keyAt(j);
                        final String currentItem = (String) listView.getItemAtPosition(position);
                        itemsToRemove.add(currentItem);
                        listView.setItemChecked(position, false);

                    }
                }

                for (String subreddit : itemsToRemove) {
                    ArrayAdapter<String> adapter = getAdapter();
                    adapter.remove(subreddit);
                    if (RedditLoginInformation.isLoggedIn()) {
                        RedditService.subscribe(SubredditManagerApi11Plus.this, subreddit);
                        
                    }
                }

                getAdapter().notifyDataSetChanged();

            }
            return true;
        }

        @Override
        public void onDestroyActionMode(android.view.ActionMode mode) {

        }

        @Override
        public void onItemCheckedStateChanged(android.view.ActionMode mode, int position, long id, boolean checked) {
            final int checkedCount = getListView().getCheckedItemCount();
            switch (checkedCount) {
                case 0:
                    mode.setSubtitle(null);
                    break;
                case 1:
                    mode.setSubtitle(R.string.one_item_selected);
                    break;
                default:
                    mode.setSubtitle(checkedCount + getString(R.string.items_selected));
                    break;
            }

        }
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent();
        i.putExtra(Constants.EXTRA_SELECTED_SUBREDDIT, mSelectedSubreddit);
        setResult(RESULT_OK, i);
        super.onBackPressed();
    }
}
