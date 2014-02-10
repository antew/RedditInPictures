package com.antew.redditinpictures.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.actionbarsherlock.view.MenuItem;
import com.antew.redditinpictures.adapter.CursorPagerAdapterFree;
import com.antew.redditinpictures.dialog.UpdateToFullVersionDialogFragment;
import com.antew.redditinpictures.dialog.UpdateToFullVersionDialogFragment.UpdateToFullVersionDialogListener;
import com.antew.redditinpictures.library.ui.ImageDetailActivity;
import com.antew.redditinpictures.library.utils.Util;
import com.antew.redditinpictures.util.ConstsFree;

public class ImageDetailActivityFree extends ImageDetailActivity implements UpdateToFullVersionDialogListener {
    public static final String TAG = ImageDetailActivityFree.class.getSimpleName();

    @Override
    public void handleVote(MenuItem item) {
        showUpgradeDialog();
    }
    
    private void showUpgradeDialog() {
        DialogFragment upgrade = UpdateToFullVersionDialogFragment.newInstance();
        upgrade.show(getSupportFragmentManager(), ConstsFree.DIALOG_UPGRADE);
    }
    
    @Override
    public void handleSaveImage() {
        showUpgradeDialog();
    }

    @Override
    public void onFinishUpgradeDialog() {
        if (!Util.isUserAMonkey()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(ConstsFree.MARKET_INTENT + ConstsFree.PRO_VERSION_PACKAGE));
            startActivity(intent);        
        }
    }

    @Override
    public FragmentStatePagerAdapter getPagerAdapter() {
        return new CursorPagerAdapterFree(getSupportFragmentManager(), null);
    }
}
