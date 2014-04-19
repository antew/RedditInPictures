/*
 * Copyright (C) 2014 Antew
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
package com.antew.redditinpictures.ui;

import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceActivity;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;
import com.antew.redditinpictures.dialog.UpdateToFullVersionDialogFragment;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.dialog.SaveImageDialogFragment;
import com.antew.redditinpictures.library.event.DownloadImageCompleteEvent;
import com.antew.redditinpictures.library.event.SaveImageEvent;
import com.antew.redditinpictures.library.model.Age;
import com.antew.redditinpictures.library.model.Category;
import com.antew.redditinpictures.library.event.LoadSubredditEvent;
import com.antew.redditinpictures.library.event.RequestCompletedEvent;
import com.antew.redditinpictures.library.event.RequestInProgressEvent;
import com.antew.redditinpictures.library.ui.RedditFragmentActivity;
import com.antew.redditinpictures.library.util.AndroidUtil;
import com.antew.redditinpictures.library.util.Ln;
import com.antew.redditinpictures.library.util.StringUtil;
import com.antew.redditinpictures.preferences.RedditInPicturesFreePreferences;
import com.antew.redditinpictures.preferences.SharedPreferencesHelperFree;
import com.antew.redditinpictures.util.ConstsFree;
import com.squareup.otto.Subscribe;

public class RedditFragmentActivityFree extends RedditFragmentActivity
    implements UpdateToFullVersionDialogFragment.UpdateToFullVersionDialogListener {
    @Override
    public Fragment getNewImageGridFragment(String subreddit, Category category, Age age) {
        return RedditImageGridFragmentFree.newInstance(subreddit, category, age);
    }

    @Override
    public Fragment getNewImageListFragment(String subreddit, Category category, Age age) {
        return RedditImageListFragmentFree.newInstance(subreddit, category, age);
    }

    @Override @Subscribe
    public void requestInProgress(RequestInProgressEvent event) {
        super.requestInProgress(event);
    }

    @Override @Subscribe
    public void requestCompleted(RequestCompletedEvent event) {
        super.requestCompleted(event);
    }

    @Override
    @Subscribe
    public void onLoadSubredditEvent(LoadSubredditEvent event) {
        super.onLoadSubredditEvent(event);
    }

    @Subscribe
    public void onSaveImageEvent(SaveImageEvent event) {
        super.onSaveImageEvent(event);
    }

    @Subscribe public void onDownloadImageComplete(DownloadImageCompleteEvent event) {
        super.onDownloadImageComplete(event);
    }

    @Override
    public Class<? extends PreferenceActivity> getPreferencesClass() {
        return RedditInPicturesFreePreferences.class;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SETTINGS_REQUEST && resultCode == RESULT_OK) {
            if (SharedPreferencesHelperFree.getDisableAds(this)) {
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ConstsFree.REMOVE_ADS));
            }
        }
    }

    @Override
    public void handleLoginAndLogout() {
        DialogFragment upgrade = UpdateToFullVersionDialogFragment.newInstance();
        upgrade.show(getSupportFragmentManager(), ConstsFree.DIALOG_UPGRADE);
    }

    @Override protected void subscribeToSubreddit(String subredditName) {
        handleLoginAndLogout();
    }

    @Override protected void unsubscribeToSubreddit(String subredditName) {
        handleLoginAndLogout();
    }

    @Override
    public void onFinishUpgradeDialog() {
        if (!AndroidUtil.isUserAMonkey()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(ConstsFree.MARKET_INTENT + ConstsFree.PRO_VERSION_PACKAGE));
            startActivity(intent);
        }
    }
}
