package com.antew.redditinpictures.preferences;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;

import com.antew.redditinpictures.R;
import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.preferences.RedditInPicturesPreferences;
import com.antew.redditinpictures.library.utils.Util;
import com.antew.redditinpictures.util.ConstsFree;

public class RedditInPicturesFreePreferences extends RedditInPicturesPreferences {
    CheckBoxPreference adsPreference;

    /**
     * This uses the deprecated addPreferencesFromResource because fragment preferences aren't part
     * of the support library
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_free);
        adsPreference = (CheckBoxPreference) getPreferenceScreen().findPreference(SharedPreferencesHelperFree.DISABLE_ADS);

    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SharedPreferencesHelperFree.DISABLE_ADS)) {
            //@formatter:off
            if (sharedPreferences.getBoolean(key, false) == true && !isFinishing())
            {
                new AlertDialog.Builder(this)
                               .setTitle(R.string.disable_ads)
                               .setMessage(R.string.in_exchange_for_disabling_ads_dialog)
                               .setPositiveButton(R.string.i_ve_rated_it, new DialogInterface.OnClickListener() {
                                   public void onClick(DialogInterface dialog, int whichButton) {
                                       
                                   }
                               }).setNegativeButton(R.string.go_to_market, new DialogInterface.OnClickListener() {
                                   public void onClick(DialogInterface dialog, int whichButton) {
                                       if (!Util.isUserAMonkey()) {
                                           Intent intent = new Intent(Intent.ACTION_VIEW);
                                           intent.setData(Uri.parse(ConstsFree.MARKET_INTENT + getPackageName()));
                                           startActivity(intent);
                                       }
                                   }
                               }).show();             
            }
            //@formatter:on
            Log.d("Disable Ads Changed to", "" + sharedPreferences.getBoolean(SharedPreferencesHelperFree.DISABLE_ADS, false));
        }
    }

}
