package com.antew.redditinpictures.library.preferences;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.antew.redditinpictures.library.R;
import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.ui.About;
import com.antew.redditinpictures.library.utils.Consts;

public class RedditInPicturesPreferences extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener {
    public static final String TAG = RedditInPicturesPreferences.class.getSimpleName();
    CheckBoxPreference useMobileInterface;
    CheckBoxPreference showNsfwImages;
    private boolean showNsfwImagesOldValue;
    private boolean showNsfwImagesNewValue;
    private Preference useHoloBackground;

    /**
     * This uses the deprecated addPreferencesFromResource because fragment
     * preferences aren't part of the support library
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getIntent().hasExtra(Consts.EXTRA_SHOW_NSFW_IMAGES)) {
            Log.i(TAG, "Had EXTRA_SHOW_NSFW_IMAGES in intent, value was " + getIntent().getBooleanExtra(Consts.EXTRA_SHOW_NSFW_IMAGES, false));
            showNsfwImagesOldValue = getIntent().getBooleanExtra(Consts.EXTRA_SHOW_NSFW_IMAGES, false);
            showNsfwImagesNewValue = showNsfwImagesOldValue;
        }
        
        addPreferencesFromResource(R.xml.preferences);
        useMobileInterface = (CheckBoxPreference) getPreferenceScreen().findPreference(SharedPreferencesHelper.USE_MOBILE_INTERFACE);
        showNsfwImages = (CheckBoxPreference) getPreferenceScreen().findPreference(SharedPreferencesHelper.SHOW_NSFW_IMAGES);
        useHoloBackground = getPreferenceScreen().findPreference(SharedPreferencesHelper.USE_HOLO_BACKGROUND);
        
        getPreferenceScreen().findPreference(SharedPreferencesHelper.ABOUT).setOnPreferenceClickListener(new OnPreferenceClickListener() {
            
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(RedditInPicturesPreferences.this, About.class));
                return true;
            }
        });

    }

    @Override
    protected void onResume() {
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }
    
    @Override
    protected void onPause() {
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.i(TAG, "onSharedPreferencesChanged key = " + key);
        if (key.equals(SharedPreferencesHelper.USE_MOBILE_INTERFACE))
        {
            Log.i("Mobile Interface Changed to", "" + sharedPreferences.getBoolean(SharedPreferencesHelper.USE_MOBILE_INTERFACE, true));
        }
        else if (key.equals(SharedPreferencesHelper.SHOW_NSFW_IMAGES))
        {
            showNsfwImagesNewValue = sharedPreferences.getBoolean(SharedPreferencesHelper.SHOW_NSFW_IMAGES, false);
            Log.i("Show Nsfw Images Changed To", "" + sharedPreferences.getBoolean(SharedPreferencesHelper.SHOW_NSFW_IMAGES, false));
        }
        
    }
    
    @Override
    public void onBackPressed() {
        Intent i = new Intent();
        boolean result = showNsfwImagesNewValue != showNsfwImagesOldValue;
        Log.i(TAG, "onBackPressed Passing back " + Boolean.toString(result) + " to activity");
        i.putExtra(Consts.EXTRA_SHOW_NSFW_IMAGES_CHANGED, showNsfwImagesNewValue != showNsfwImagesOldValue);
        setResult(RESULT_OK, i);
        super.onBackPressed();
    }

}