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
package com.antew.redditinpictures.library.preferences;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import com.antew.redditinpictures.library.ui.About;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.pro.R;

/**
 * Preferences for Honeycomb and later, this can be subclassed to add additional Preferences. For an
 * example see RedditInPicturesPreferencesFreeFragment in the RedditInPictures-Free project
 *
 * @author Antew
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class RedditInPicturesPreferencesFragment extends PreferenceActivity {
    private boolean showNsfwImagesNewValue;
    private boolean showNsfwImagesOldValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra(Constants.EXTRA_SHOW_NSFW_IMAGES)) {
            showNsfwImagesOldValue = getIntent().getBooleanExtra(Constants.EXTRA_SHOW_NSFW_IMAGES, false);
            showNsfwImagesNewValue = showNsfwImagesOldValue;
        }
        getFragmentManager().beginTransaction().replace(android.R.id.content, getPrefsFragment()).commit();
    }

    /**
     * This is overridden by subclasses to instantiate the correct Fragment
     *
     * @return
     */
    public Fragment getPrefsFragment() {
        return new PrefsFragment();
    }

    /**
     * Pass back whether the NSFW images preference changed
     */
    @Override
    public void onBackPressed() {
        showNsfwImagesNewValue = PreferenceManager.getDefaultSharedPreferences(this)
                                                  .getBoolean(getString(R.string.pref_show_nsfw_images), false);
        Intent i = new Intent();
        i.putExtra(Constants.EXTRA_SHOW_NSFW_IMAGES_CHANGED, showNsfwImagesNewValue != showNsfwImagesOldValue);
        setResult(RESULT_OK, i);
        super.onBackPressed();
    }

    public static class PrefsFragment extends PreferenceFragment {
        protected static final String TAG = "PrefsFragment";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);
            addPreferencesFromResource(R.xml.preferences);

            getPreferenceManager().findPreference(getString(R.string.pref_about))
                                  .setOnPreferenceClickListener(new OnPreferenceClickListener() {

                                      @Override
                                      public boolean onPreferenceClick(Preference preference) {
                                          startActivity(new Intent(getActivity(), About.class));
                                          return true;
                                      }
                                  });
        }
    }
}