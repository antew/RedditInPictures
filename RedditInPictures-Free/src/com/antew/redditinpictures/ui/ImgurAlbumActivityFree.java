package com.antew.redditinpictures.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.antew.redditinpictures.adapter.ImgurAlbumPagerAdapterFree;
import com.antew.redditinpictures.dialog.UpdateToFullVersionDialogFragment;
import com.antew.redditinpictures.dialog.UpdateToFullVersionDialogFragment.UpdateToFullVersionDialogListener;
import com.antew.redditinpictures.library.ui.ImgurAlbumActivity;
import com.antew.redditinpictures.util.ConstsFree;

public class ImgurAlbumActivityFree extends ImgurAlbumActivity implements UpdateToFullVersionDialogListener{

    public FragmentStatePagerAdapter getPagerAdapter() {
        return new ImgurAlbumPagerAdapterFree(getSupportFragmentManager(), getImages());   
    }
    
    @Override
    public void handleSaveImage() {
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
