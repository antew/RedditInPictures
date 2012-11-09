package com.antew.redditinpictures.ui;

import android.support.v4.app.FragmentStatePagerAdapter;

import com.actionbarsherlock.view.Menu;
import com.antew.redditinpictures.adapter.ImgurAlbumPagerAdapterFree;
import com.antew.redditinpictures.library.R;
import com.antew.redditinpictures.library.adapter.ImgurAlbumPagerAdapter;
import com.antew.redditinpictures.library.ui.ImgurAlbumActivity;

public class ImgurAlbumActivityFree extends ImgurAlbumActivity {

    public FragmentStatePagerAdapter getPagerAdapter() {
        return new ImgurAlbumPagerAdapterFree(getSupportFragmentManager(), getImages());   
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.removeItem(R.id.save_post);
        
        return true;
    }
}
