package com.antew.redditinpictures.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;

import com.antew.redditinpictures.dialog.UpdateToFullVersionDialogFragment;
import com.antew.redditinpictures.dialog.UpdateToFullVersionDialogFragment.UpdateToFullVersionDialogListener;
import com.antew.redditinpictures.library.ui.ImageGridActivity;
import com.antew.redditinpictures.library.ui.ImageGridFragment;
import com.antew.redditinpictures.library.utils.Util;
import com.antew.redditinpictures.preferences.RedditInPicturesFreePreferences;
import com.antew.redditinpictures.preferences.RedditInPicturesFreePreferencesFragment;
import com.antew.redditinpictures.preferences.SharedPreferencesHelperFree;
import com.antew.redditinpictures.util.ConstsFree;

public class ImageGridActivityFree extends ImageGridActivity implements UpdateToFullVersionDialogListener {
    public static final String TAG = ImageGridActivityFree.class.getSimpleName();
    
    @Override
    public ImageGridFragment getImageGridFragment() {
        return new ImageGridFragmentFree();
    }

    @Override
    public void startPreferences() {
        if (Util.hasHoneycomb()) {
            Intent intent = new Intent(ImageGridActivityFree.this, RedditInPicturesFreePreferencesFragment.class);
            intent.putExtra(ConstsFree.EXTRA_SHOW_NSFW_IMAGES, mShowNsfwImages);
            startActivityForResult(intent, SETTINGS_REQUEST);
        } else {
            Intent intent = new Intent(ImageGridActivityFree.this, RedditInPicturesFreePreferences.class);
            intent.putExtra(ConstsFree.EXTRA_SHOW_NSFW_IMAGES, mShowNsfwImages);
            startActivityForResult(intent, SETTINGS_REQUEST);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SETTINGS_REQUEST && resultCode == RESULT_OK) {
            if (SharedPreferencesHelperFree.getDisableAds(this))
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ConstsFree.REMOVE_ADS));
        }
    }

    @Override
    public void handleLoginAndLogout() {
        DialogFragment upgrade = UpdateToFullVersionDialogFragment.newInstance();
        upgrade.show(getSupportFragmentManager(), ConstsFree.DIALOG_UPGRADE);
    }
    
    @Override
    public void onFinishUpgradeDialog() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(ConstsFree.MARKET_INTENT + ConstsFree.PRO_VERSION_PACKAGE));
        startActivity(intent);        
    }

}
