package com.antew.redditinpictures.ui;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.actionbarsherlock.view.Menu;
import com.antew.redditinpictures.library.R;
import com.antew.redditinpictures.library.ui.ImageGridFragment;
import com.antew.redditinpictures.library.utils.Util;
import com.antew.redditinpictures.preferences.RedditInPicturesFreePreferences;
import com.antew.redditinpictures.preferences.RedditInPicturesFreePreferencesFragment;
import com.antew.redditinpictures.preferences.SharedPreferencesHelperFree;
import com.antew.redditinpictures.util.Consts;

public class ImageGridActivityFree extends com.antew.redditinpictures.library.ui.ImageGridActivity {
    public static final String TAG = ImageGridActivityFree.class.getSimpleName();
    
    @Override
    public ImageGridFragment getImageGridFragment() {
        return new ImageGridFragmentFree();
    }

    @Override
    public void startPreferences() {
        if (Util.hasHoneycomb()) {
            Intent intent = new Intent(ImageGridActivityFree.this, RedditInPicturesFreePreferencesFragment.class);
            intent.putExtra(Consts.EXTRA_SHOW_NSFW_IMAGES, mShowNsfwImages);
            startActivityForResult(intent, SETTINGS_REQUEST);
        } else {
            Intent intent = new Intent(ImageGridActivityFree.this, RedditInPicturesFreePreferences.class);
            intent.putExtra(Consts.EXTRA_SHOW_NSFW_IMAGES, mShowNsfwImages);
            startActivityForResult(intent, SETTINGS_REQUEST);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SETTINGS_REQUEST && resultCode == RESULT_OK) {
            if (SharedPreferencesHelperFree.getDisableAds(this))
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Consts.REMOVE_ADS));
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        
        menu.removeItem(R.id.login);
        
        return true;
    }
}
