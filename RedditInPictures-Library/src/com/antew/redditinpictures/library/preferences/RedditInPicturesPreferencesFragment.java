package com.antew.redditinpictures.library.preferences;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.antew.redditinpictures.library.R;
import com.antew.redditinpictures.library.utils.Consts;

// This really only requires API 11, the Lint check for setOnPreferenceChangeListener seems to be incorrect and reports that it requires API level 14
// http://developer.android.com/reference/android/preference/Preference.html#setOnPreferenceChangeListener(android.preference.Preference.OnPreferenceChangeListener)
@TargetApi(14)
public class RedditInPicturesPreferencesFragment extends PreferenceActivity {
    private boolean showNsfwImagesNewValue;
    private boolean showNsfwImagesOldValue;

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
        Intent i = new Intent();
        i.putExtra(Consts.EXTRA_SHOW_NSFW_IMAGES_CHANGED, showNsfwImagesNewValue != showNsfwImagesOldValue);
        setResult(RESULT_OK, i);
        super.onBackPressed();
    }

    /**
     * This fragment shows the preferences for the first header.
     */
    public static class PrefsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);
            addPreferencesFromResource(R.xml.preferences);

            final CheckBoxPreference useMobileInterface = (CheckBoxPreference) getPreferenceManager().findPreference(
                    SharedPreferencesHelper.USE_MOBILE_INTERFACE);
            final CheckBoxPreference showNsfwImages = (CheckBoxPreference) getPreferenceManager().findPreference(
                    SharedPreferencesHelper.SHOW_NSFW_IMAGES);

            useMobileInterface.setOnPreferenceChangeListener(getMobileInterfaceOnChangeListener());

            showNsfwImages.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    Editor editor = sp.edit();
                    editor.putBoolean(SharedPreferencesHelper.SHOW_NSFW_IMAGES, (Boolean) newValue);
                    editor.commit();
                    ((RedditInPicturesPreferencesFragment) getActivity()).showNsfwImagesNewValue = (Boolean) newValue;
                    return true;
                }
            });
        }

        private OnPreferenceChangeListener getMobileInterfaceOnChangeListener() {
            return new OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    Editor editor = sp.edit();
                    editor.putBoolean(SharedPreferencesHelper.USE_MOBILE_INTERFACE, (Boolean) newValue);
                    editor.commit();

                    return true;

                }
            };
        }

    }

}