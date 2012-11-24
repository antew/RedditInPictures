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

import com.antew.redditinpictures.library.R;
import com.antew.redditinpictures.library.ui.About;
import com.antew.redditinpictures.library.utils.Consts;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class RedditInPicturesPreferencesFragment extends PreferenceActivity {
    private boolean showNsfwImagesNewValue;
    private boolean showNsfwImagesOldValue;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra(Consts.EXTRA_SHOW_NSFW_IMAGES)) {
            showNsfwImagesOldValue = getIntent().getBooleanExtra(Consts.EXTRA_SHOW_NSFW_IMAGES, false);
            showNsfwImagesNewValue = showNsfwImagesOldValue;
        }
        getFragmentManager().beginTransaction().replace(android.R.id.content, getPrefsFragment()).commit();
    }

    public Fragment getPrefsFragment() {
        return new PrefsFragment();
    }

    @Override
    public void onBackPressed() {
        showNsfwImagesNewValue = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SharedPreferencesHelper.USE_HOLO_BACKGROUND, false);
        Intent i = new Intent();
        i.putExtra(Consts.EXTRA_SHOW_NSFW_IMAGES_CHANGED, showNsfwImagesNewValue != showNsfwImagesOldValue);
        setResult(RESULT_OK, i);
        super.onBackPressed();
    }

    /**
     * This fragment shows the preferences for the first header.
     */
    public static class PrefsFragment extends PreferenceFragment {
        protected static final String TAG = "PrefsFragment";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);
            addPreferencesFromResource(R.xml.preferences);

            getPreferenceManager().findPreference(SharedPreferencesHelper.ABOUT).setOnPreferenceClickListener(new OnPreferenceClickListener() {
                
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), About.class));
                    return true;
                }
            });
        }

    }

}