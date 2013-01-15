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

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import com.androidquery.callback.AjaxStatus;
import com.antew.redditinpictures.library.R;
import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.preferences.SharedPreferencesHelper;
import com.antew.redditinpictures.library.reddit.MySubreddits;
import com.antew.redditinpictures.library.reddit.MySubreddits.SubredditData;
import com.antew.redditinpictures.library.reddit.RedditApiManager;
import com.antew.redditinpictures.library.reddit.RedditUrl;
import com.antew.redditinpictures.library.reddit.SubscribeAction;
import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.library.utils.Consts;
import com.antew.redditinpictures.library.utils.StringUtil;
import com.google.gson.Gson;

@TargetApi(11)
public class SubredditManagerApi11Plus extends SubredditManager {

    public static final String   TAG                = SubredditManagerApi11Plus.class.getSimpleName();
    private ArrayAdapter<String> mAdapter;
    private String               mSelectedSubreddit = RedditUrl.REDDIT_FRONTPAGE;
    private MenuItem             mResetToDefaultSubreddits;
    private MenuItem             mResyncWithReddit;
    private SubscribeAction      mAction;

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra(Consts.EXTRA_SELECTED_SUBREDDIT))
            mSelectedSubreddit = getIntent().getStringExtra(Consts.EXTRA_SELECTED_SUBREDDIT);

        List<String> subReddits = SharedPreferencesHelper.loadArray(PREFS_NAME, ARRAY_NAME, SubredditManagerApi11Plus.this);

        if (subReddits.size() == 0) {
            String[] reddits = getResources().getStringArray(R.array.default_reddits);
            for (int i = 0; i < reddits.length; i++) {
                subReddits.add(reddits[i]);
            }
        }

        Collections.sort(subReddits, StringUtil.getCaseInsensitiveComparator());

        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, subReddits);

        setListAdapter(mAdapter);

        final ListView listView = getListView();
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new ModeCallback());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    };

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.i("onPrepareOptionsMenu", "called");
        mResetToDefaultSubreddits = menu.findItem(R.id.reset_subreddits);
        mResyncWithReddit = menu.findItem(R.id.resync_subreddits);

        if (RedditApiManager.isLoggedIn()) {
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
        i.putExtra(Consts.EXTRA_NEWLY_SELECTED_SUBREDDIT, mAdapter.getItem(position));
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
    public void resyncSubredditsWithReddit() {
        RedditService.getMySubreddits(this);
    }

    @Override
    public void mySubredditsCallback(String url, String json, AjaxStatus status) {
        if (status.getCode() == HttpURLConnection.HTTP_OK) {
            Gson gson = new Gson();
            MySubreddits mMySubreddits = gson.fromJson(json, MySubreddits.class);
            Log.i("MyRedditsJson", json);

            List<String> subReddits = new ArrayList<String>();

            for (MySubreddits.Children c : mMySubreddits.getData().getChildren()) {
                SubredditData data = c.getData();
                subReddits.add(data.getDisplay_name());
                Log.i("Subscribed Subreddits", data.getDisplay_name());
            }

            SharedPreferencesHelper.saveArray(subReddits, SubredditManager.PREFS_NAME, SubredditManager.ARRAY_NAME, SubredditManagerApi11Plus.this);
            Collections.sort(subReddits, StringUtil.getCaseInsensitiveComparator());

            mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, subReddits);
            setListAdapter(mAdapter);
        } else {
            Log.e("MySubreddits", "Something went wrong on mySubreddits! status = " + status.getCode() + " | json = " + json == null ? "null" : json);

        }
    }

    @Override
    public void resetToDefaultSubreddits() {
        List<String> subReddits = new ArrayList<String>();

        String[] reddits = getResources().getStringArray(R.array.default_reddits);
        for (int i = 0; i < reddits.length; i++) {
            subReddits.add(reddits[i]);
        }

        Collections.sort(subReddits, StringUtil.getCaseInsensitiveComparator());

        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, subReddits);
        setListAdapter(mAdapter);
        SharedPreferencesHelper.saveArray(getReddits(), PREFS_NAME, ARRAY_NAME, SubredditManagerApi11Plus.this);
    }

    @Override
    public List<String> getReddits() {
        List<String> returnList = new ArrayList<String>();
        for (int i = 0; i < mAdapter.getCount(); i++) {
            returnList.add(mAdapter.getItem(i));
            Log.i("getReddits", "" + mAdapter.getItem(i));
        }

        Collections.sort(returnList, StringUtil.getCaseInsensitiveComparator());
        return returnList;
    }

    @Override
    public void createAddSubredditAlertDialog() {//@formatter:off
        if (isFinishing())
            return;
        
        final EditText input = new EditText(SubredditManagerApi11Plus.this);
        
        new AlertDialog.Builder(SubredditManagerApi11Plus.this)
                       .setTitle(R.string.add_subreddit)
                       .setMessage(R.string.enter_the_subreddit)
                       .setView(input)
                       .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String value = input.getText().toString();
                                
                                if (value.length() > 0) {
                                    mAdapter.add(value);
                                    mAdapter.sort(StringUtil.getCaseInsensitiveComparator());
                                    mAdapter.notifyDataSetChanged();
                                    
                                    if (RedditApiManager.isLoggedIn()) {
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

        private Object mDelete;

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

                if (checkedItems == null)
                    return true;

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
                    mAdapter.remove(subreddit);
                    if (RedditApiManager.isLoggedIn()) {
                        RedditService.subscribe(SubredditManagerApi11Plus.this, subreddit);
                        
                    }
                }

                mAdapter.notifyDataSetChanged();

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
                    mode.setSubtitle("One item selected");
                    break;
                default:
                    mode.setSubtitle("" + checkedCount + " items selected");
                    break;
            }

        }
    }

    @Override
    public void onBackPressed() {

        Intent i = new Intent();
        i.putExtra(Consts.EXTRA_SELECTED_SUBREDDIT, mSelectedSubreddit);
        setResult(RESULT_OK, i);
        super.onBackPressed();
    }
}
