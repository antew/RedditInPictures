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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.ActionMode;
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
import com.antew.redditinpictures.library.utils.Consts;
import com.antew.redditinpictures.library.utils.StringUtil;
import com.google.gson.Gson;

/**
 * 
 * Activity for managing subreddits shown in the spinner.
 * 
 * Setting target API to 11 here because Lint incorrectly identifies ListView.setChoiceMode as only available in API 11+, but according to the docs it is available in API 1
 * See: http://developer.android.com/reference/android/widget/AbsListView.html#setChoiceMode(int)                                                                           
 * The same goes for getCheckedItemPositions() - http://developer.android.com/reference/android/widget/AbsListView.html#getCheckedItemPositions()                           
 * and setItemChecked - http://developer.android.com/reference/android/widget/AbsListView.html#setItemChecked(int, boolean)                                                 
 * 
 * @author Henry Pate
 *
 */
public class SubredditManager extends SherlockListActivity {
    public static final String   PREFS_NAME               = "reddit_in_pictures";
    public static final String   ARRAY_NAME               = "subreddits";
    private ActionMode           mMode;
    private ArrayAdapter<String> mAdapter;
    private String               mSelectedSubreddit       = RedditUrl.REDDIT_FRONTPAGE;
    private MenuItem             mResetToDefaultSubreddits;
    private MenuItem             mResyncWithReddit;
    
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra(Consts.EXTRA_SELECTED_SUBREDDIT))
            mSelectedSubreddit = getIntent().getStringExtra(Consts.EXTRA_SELECTED_SUBREDDIT);

        List<String> subReddits = SharedPreferencesHelper.loadArray(PREFS_NAME, ARRAY_NAME, SubredditManager.this);

        if (subReddits.size() == 0) {
            String[] reddits = getResources().getStringArray(R.array.default_reddits);
            for (int i = 0; i < reddits.length; i++) {
                subReddits.add(reddits[i]);
            }
        }

        Collections.sort(subReddits, StringUtil.getCaseInsensitiveComparator());

        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, subReddits);
        setListAdapter(mAdapter);

        final ListView listView = getListView();
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setItemsCanFocus(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    };
    
    public void resyncSubredditsWithReddit()
    {
        RedditApiManager.getMySubreddits("mySubredditsCallback", SubredditManager.this);
    }
   
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
            
            SharedPreferencesHelper.saveArray(subReddits, SubredditManager.PREFS_NAME, SubredditManager.ARRAY_NAME, SubredditManager.this);
            Collections.sort(subReddits, StringUtil.getCaseInsensitiveComparator());
            
            mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, subReddits);
            setListAdapter(mAdapter);
        } else {
            Log.e("MySubreddits", "Something went wrong on mySubreddits! status = " + status.getCode() + " | json = " + json == null ? "null" : json);
            
        }
    }   
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.edit_subreddits, menu);
        return true;
    }
    
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.add) {
            createAddSubredditAlertDialog();
        } else if (itemId == android.R.id.home) {
            Intent i = new Intent();
            i.putExtra(Consts.EXTRA_SELECTED_SUBREDDIT, mSelectedSubreddit);
            setResult(RESULT_OK, i);
            finish();
        } else if (itemId == R.id.resync_subreddits) {
            resyncSubredditsWithReddit();
        } else if (itemId == R.id.delete_subreddits) {
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
            for (String s : itemsToRemove)
            {
                mAdapter.remove(s);
                if (RedditApiManager.isLoggedIn())
                {
                    RedditApiManager.subscribe(s, SubscribeAction.UNSUBSCRIBE, getApplicationContext());
                }                    
            }
            mAdapter.notifyDataSetChanged();
        } else if (itemId == R.id.reset_subreddits) {
            createResetSubredditsAlertDialog();
        }
        ;

        return true;
    }

    private void createResetSubredditsAlertDialog() {
        new AlertDialog.Builder(SubredditManager.this).setTitle("Reset").setMessage("Reset to default subreddits?")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        resetToDefaultSubreddits();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();
    }

    public void createAddSubredditAlertDialog() {//@formatter:off
        final EditText input = new EditText(SubredditManager.this);
        
        new AlertDialog.Builder(SubredditManager.this)
                       .setTitle("Add Subreddit")
                       .setMessage("Enter the subreddit")
                       .setView(input)
                       .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                String value = input.getText().toString();
                                
                                if (value.length() > 0)
                                {
                                    mAdapter.add(value);
                                    mAdapter.sort(StringUtil.getCaseInsensitiveComparator());
                                    mAdapter.notifyDataSetChanged();
                                }
                                
                                if (RedditApiManager.isLoggedIn())
                                {
                                    RedditApiManager.subscribe(value, SubscribeAction.SUBSCRIBE, getApplicationContext());
                                }
                            }
                       }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                // Do nothing.
                            }
                       }).show();

    } //@formatter:off
    
    public void resetToDefaultSubreddits()
    {
        List<String> subReddits = new ArrayList<String>();
        
        String[] reddits = getResources().getStringArray(R.array.default_reddits);
        for (int i = 0; i < reddits.length; i++)
        {
            subReddits.add(reddits[i]);
        }
        
        Collections.sort(subReddits, StringUtil.getCaseInsensitiveComparator());
        
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, subReddits);
        setListAdapter(mAdapter);
        SharedPreferencesHelper.saveArray(getReddits(), PREFS_NAME, ARRAY_NAME, SubredditManager.this);
        
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);
    }

    public List<String> getReddits()
    {
        List<String> returnList = new ArrayList<String>();
        for (int i = 0; i < mAdapter.getCount(); i++)
        {
            returnList.add(mAdapter.getItem(i));
        }

        Collections.sort(returnList, StringUtil.getCaseInsensitiveComparator());
        return returnList;
    }

    public void clearActionMode()
    {
        if (mMode != null)
            mMode.finish();
    }
    
    @Override
    public void onBackPressed()
    {

        Intent i = new Intent();
        i.putExtra(Consts.EXTRA_SELECTED_SUBREDDIT, mSelectedSubreddit);
        setResult(RESULT_OK, i);
        super.onBackPressed();
    }

    @Override
    protected void onPause()
    {
        SharedPreferencesHelper.saveArray(getReddits(), PREFS_NAME, ARRAY_NAME, SubredditManager.this);
        super.onPause();
    }

}
