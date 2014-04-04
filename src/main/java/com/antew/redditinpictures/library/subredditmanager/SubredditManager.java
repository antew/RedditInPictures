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

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseBooleanArray;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.preferences.SharedPreferencesHelper;
import com.antew.redditinpictures.library.reddit.RedditLoginInformation;
import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.utils.StringUtil;
import com.antew.redditinpictures.pro.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Activity for managing subreddits shown in the spinner.
 */
public class SubredditManager extends SherlockListActivity {
    public static final String TAG        = SubredditManager.class.getSimpleName();
    //@formatter:off
    private BroadcastReceiver   mMySubreddits = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received mySubreddits callback");
            ArrayList<String> subreddits = intent.getStringArrayListExtra(Constants.EXTRA_MY_SUBREDDITS);

            if (subreddits != null && subreddits.size() > 0) {
                setListAdapter(subreddits);
            } else {
                StringBuilder errorMessage = new StringBuilder("Something went wrong on 'my subreddits' callback");
                if (subreddits == null)
                    errorMessage.append(", subreddits object was null");
                else
                    errorMessage.append(", subreddits object had " + subreddits.size() + " items.");

                Log.e("MySubreddits", errorMessage.toString());
            }
        }
    };
    public static final String PREFS_NAME = "reddit_in_pictures";
    public static final String ARRAY_NAME = "subreddits";
    private ActionMode           mMode;
    private ArrayAdapter<String> mAdapter;
    private String mSelectedSubreddit = Constants.REDDIT_FRONTPAGE;
    private MenuItem mResetToDefaultSubreddits;
    private MenuItem mResyncWithReddit;

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra(Constants.EXTRA_SELECTED_SUBREDDIT)) {
            mSelectedSubreddit = getIntent().getStringExtra(Constants.EXTRA_SELECTED_SUBREDDIT);
        }

        List<String> subreddits = SharedPreferencesHelper.loadArray(PREFS_NAME, ARRAY_NAME, SubredditManager.this);

        if (subreddits.size() == 0) {
            String[] reddits = getResources().getStringArray(R.array.default_subreddits);
            for (int i = 0; i < reddits.length; i++) {
                subreddits.add(reddits[i]);
            }
        }

        Collections.sort(subreddits, StringUtil.getCaseInsensitiveComparator());

        setListAdapter(subreddits);

        final ListView listView = getListView();
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setItemsCanFocus(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMySubreddits, new IntentFilter(Constants.BROADCAST_MY_SUBREDDITS));
    }
    //@formatter:on

    public void setListAdapter(List<String> subreddits) {
        mAdapter = new ArrayAdapter<String>(this, getListLayoutId(), subreddits);
        setListAdapter(mAdapter);
    }

    public int getListLayoutId() {
        return android.R.layout.simple_list_item_multiple_choice;
    }

    @Override
    public void onBackPressed() {

        Intent i = new Intent();
        i.putExtra(Constants.EXTRA_SELECTED_SUBREDDIT, mSelectedSubreddit);
        setResult(RESULT_OK, i);
        super.onBackPressed();
    }

    public void clearActionMode() {
        if (mMode != null) {
            mMode.finish();
        }
    }

    @Override
    protected void onPause() {
        SharedPreferencesHelper.saveArray(getReddits(), PREFS_NAME, ARRAY_NAME, SubredditManager.this);
        LocalBroadcastManager.getInstance(SubredditManager.this).unregisterReceiver(mMySubreddits);
        super.onPause();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.add) {
            createAddSubredditAlertDialog();
        } else if (itemId == android.R.id.home) {
            Intent i = new Intent();
            i.putExtra(Constants.EXTRA_SELECTED_SUBREDDIT, mSelectedSubreddit);
            setResult(RESULT_OK, i);
            finish();
        } else if (itemId == R.id.resync_subreddits) {
            resyncSubredditsWithReddit();
        } else if (itemId == R.id.delete_subreddits) {
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
                mAdapter.remove(subreddit);
                if (RedditLoginInformation.isLoggedIn()) {
                    RedditService.unsubscribe(SubredditManager.this, subreddit);
                }
            }
            mAdapter.notifyDataSetChanged();
        } else if (itemId == R.id.reset_subreddits) {
            createResetSubredditsAlertDialog();
        }
        ;

        return true;
    }

    public void createAddSubredditAlertDialog() {//@formatter:off
        final EditText input = new EditText(SubredditManager.this);

        new AlertDialog.Builder(SubredditManager.this)
                       .setTitle(R.string.add_subreddit)
                       .setMessage(R.string.enter_the_subreddit)
                       .setView(input)
                       .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String subreddit = input.getText().toString();

                                if (subreddit.length() > 0) {
                                    mAdapter.add(subreddit);
                                    mAdapter.sort(StringUtil.getCaseInsensitiveComparator());
                                    mAdapter.notifyDataSetChanged();
                                }

                                if (RedditLoginInformation.isLoggedIn()) {
                                    RedditService.subscribe(SubredditManager.this, subreddit);
                                }
                            }
                       }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Do nothing.
                            }
                       }).show();

    } //@formatter:on

    public void resyncSubredditsWithReddit() {
        RedditService.getMySubreddits(SubredditManager.this);
    }

    private void createResetSubredditsAlertDialog() {
        new AlertDialog.Builder(SubredditManager.this).setTitle(R.string.reset)
                                                      .setMessage(R.string.reset_to_default_subreddits)
                                                      .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                                          public void onClick(DialogInterface dialog, int whichButton) {
                                                              resetToDefaultSubreddits();
                                                          }
                                                      })
                                                      .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                                          public void onClick(DialogInterface dialog, int whichButton) {
                                                              // Do nothing.
                                                          }
                                                      })
                                                      .show();
    }

    public void resetToDefaultSubreddits() {
        List<String> subreddits = new ArrayList<String>();

        String[] reddits = getResources().getStringArray(R.array.default_subreddits);
        for (int i = 0; i < reddits.length; i++) {
            subreddits.add(reddits[i]);
        }

        Collections.sort(subreddits, StringUtil.getCaseInsensitiveComparator());

        setListAdapter(subreddits);
        SharedPreferencesHelper.saveArray(getReddits(), PREFS_NAME, ARRAY_NAME, SubredditManager.this);
    }

    public List<String> getReddits() {
        List<String> returnList = new ArrayList<String>();
        for (int i = 0; i < mAdapter.getCount(); i++) {
            returnList.add(mAdapter.getItem(i));
        }

        Collections.sort(returnList, StringUtil.getCaseInsensitiveComparator());
        return returnList;
    }

    public ArrayAdapter<String> getAdapter() {
        return (ArrayAdapter<String>) getListAdapter();
    }
}
