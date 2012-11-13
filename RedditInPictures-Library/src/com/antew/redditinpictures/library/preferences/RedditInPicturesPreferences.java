package com.antew.redditinpictures.library.preferences;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.antew.redditinpictures.library.R;
import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.ui.About;
import com.antew.redditinpictures.library.utils.Consts;

public class RedditInPicturesPreferences extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener {
    CheckBoxPreference useMobileInterface;
    CheckBoxPreference showNsfwImages;
    SharedPreferences  sharedPreferences;
    private boolean showNsfwImagesOldValue;
    private boolean showNsfwImagesNewValue;

    /**
     * This uses the deprecated addPreferencesFromResource because fragment
     * preferences aren't part of the support library
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getIntent().hasExtra(Consts.EXTRA_SHOW_NSFW_IMAGES)) {
            showNsfwImagesOldValue = getIntent().getBooleanExtra(Consts.EXTRA_SHOW_NSFW_IMAGES, false);
            showNsfwImagesNewValue = showNsfwImagesOldValue;
        }
        
        addPreferencesFromResource(R.xml.preferences);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RedditInPicturesPreferences.this);

        useMobileInterface = (CheckBoxPreference) getPreferenceScreen().findPreference(SharedPreferencesHelper.USE_MOBILE_INTERFACE);
        showNsfwImages = (CheckBoxPreference) getPreferenceScreen().findPreference(SharedPreferencesHelper.SHOW_NSFW_IMAGES);
        
        getPreferenceScreen().findPreference("about").setOnPreferenceClickListener(new OnPreferenceClickListener() {
            
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(RedditInPicturesPreferences.this, About.class));
                return true;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SharedPreferencesHelper.USE_MOBILE_INTERFACE))
        {
            Log.d("Mobile Interface Changed to", "" + sharedPreferences.getBoolean(SharedPreferencesHelper.USE_MOBILE_INTERFACE, true));
        }
        else if (key.equals(SharedPreferencesHelper.SHOW_NSFW_IMAGES))
        {
            showNsfwImagesNewValue = sharedPreferences.getBoolean(SharedPreferencesHelper.SHOW_NSFW_IMAGES, true);
            Log.d("Show Nsfw Images Changed To", "" + sharedPreferences.getBoolean(SharedPreferencesHelper.SHOW_NSFW_IMAGES, true));
        }
        
    }
    
    @Override
    public void onBackPressed() {
        Intent i = new Intent();
        i.putExtra(Consts.EXTRA_SHOW_NSFW_IMAGES_CHANGED, showNsfwImagesNewValue != showNsfwImagesOldValue);
        setResult(RESULT_OK, i);
        super.onBackPressed();
    }

}