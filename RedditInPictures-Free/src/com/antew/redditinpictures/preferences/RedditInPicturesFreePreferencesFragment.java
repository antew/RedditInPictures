package com.antew.redditinpictures.preferences;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;

import com.antew.redditinpictures.R;
import com.antew.redditinpictures.library.preferences.RedditInPicturesPreferencesFragment;
import com.antew.redditinpictures.library.preferences.SharedPreferencesHelper;
import com.antew.redditinpictures.library.ui.About;
import com.antew.redditinpictures.util.ConstsFree;

// This really only requires API 11, the Lint check for setOnPreferenceChangeListener seems to be incorrect and reports that it requires API level 14
// http://developer.android.com/reference/android/preference/Preference.html#setOnPreferenceChangeListener(android.preference.Preference.OnPreferenceChangeListener)
@TargetApi(14)
public class RedditInPicturesFreePreferencesFragment extends RedditInPicturesPreferencesFragment {
    public Fragment getPrefsFragment() {
        return new PrefsFragment();
    }
    
    public static class PrefsFragment extends com.antew.redditinpictures.library.preferences.RedditInPicturesPreferencesFragment.PrefsFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences_free, false);
            addPreferencesFromResource(R.xml.preferences_free);

            final CheckBoxPreference adsPreference = (CheckBoxPreference) getPreferenceManager().findPreference(SharedPreferencesHelperFree.DISABLE_ADS);
            adsPreference.setOnPreferenceChangeListener(getAdsPreferenceOnChangeListener());
            
            getPreferenceManager().findPreference(SharedPreferencesHelperFree.UPGRADE).setOnPreferenceClickListener(new OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(ConstsFree.MARKET_INTENT + ConstsFree.PRO_VERSION_PACKAGE));
                    startActivity(intent);
                    return true;
                }
            });
        }

        private OnPreferenceChangeListener getAdsPreferenceOnChangeListener() {
            return new OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if ((Boolean) newValue == true) {//@formatter:off
                        new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.disable_ads)
                        .setMessage(R.string.in_exchange_for_disabling_ads)
                        .setPositiveButton(R.string.ive_rated_it, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                
                            }
                        }).setNegativeButton(R.string.go_to_market, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(ConstsFree.MARKET_INTENT + getActivity().getPackageName()));
                                startActivity(intent);
                            }
                        }).show();                
                    }
                    
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    Editor editor = sp.edit();
                    editor.putBoolean(SharedPreferencesHelperFree.DISABLE_ADS, (Boolean) newValue);
                    editor.commit();
                    return true;
                }
            };
        }
    }

}