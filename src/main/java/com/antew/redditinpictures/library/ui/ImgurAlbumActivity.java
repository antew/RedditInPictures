package com.antew.redditinpictures.library.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.adapter.ImgurAlbumPagerAdapter;
import com.antew.redditinpictures.library.imgur.ImgurAlbumApi.Album;
import com.antew.redditinpictures.library.imgur.ImgurImageApi.ImgurImage;
import com.antew.redditinpictures.library.utils.StringUtil;
import com.antew.redditinpictures.pro.R;
import java.util.List;

public class ImgurAlbumActivity extends ImageViewerActivity {
    public static final String TAG = "ImgurAlbumActivity";
    public static final String EXTRA_ALBUM = "Album";
    private Album mAlbum;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Put the current page / total pages text in the ActionBar 
        updateDisplay(mPager.getCurrentItem());
    }

    @Override
    public void getExtras() {
        if (getIntent().hasExtra(EXTRA_ALBUM)) {
            mAlbum = getIntent().getParcelableExtra(EXTRA_ALBUM);
            mImages = mAlbum.getImages();
        }
    }

    @Override
    public FragmentStatePagerAdapter getPagerAdapter() {
        return new ImgurAlbumPagerAdapter(getSupportFragmentManager(), getImages());
    }

    @SuppressWarnings("unchecked")
    protected List<ImgurImage> getImages() {
        return (List<ImgurImage>) mImages;
    }

    @Override
    protected void updateDisplay(int position) {
        getSupportActionBar().setTitle(++position + "/" + getAdapter().getCount() + " - " + getString(R.string.reddit_in_pictures));
    }

    @Override
    public String getUrlForSharing() {
        ImgurImage image = getAdapter().getImage(mPager.getCurrentItem());
        return image.getLinks().getImgur_page();
    }

    @Override
    protected Uri getPostUri() {
        ImgurImage image = getAdapter().getImage(mPager.getCurrentItem());
        return Uri.parse(image.getLinks().getImgur_page());
    }

    @Override
    public String getFilenameForSave() {
        String name = super.getFilenameForSave();
        if (getAdapter() != null && mPager != null) {
            ImgurImage p = getAdapter().getImage(mPager.getCurrentItem());
            name = p.getImage().getHash();
            if (!p.getImage().getTitle().equals("")) {
                name = StringUtil.sanitizeFileName(p.getImage().getTitle());
            } else if (!p.getImage().getCaption().equals("")) {
                name = StringUtil.sanitizeFileName(p.getImage().getCaption());
            }
        }

        return name;
    }

    @Override
    public void onFinishSaveImageDialog(String filename) {
        ImgurImage p = getAdapter().getImage(mPager.getCurrentItem());
        Intent intent = new Intent(Constants.BROADCAST_DOWNLOAD_IMAGE);
        intent.putExtra(Constants.EXTRA_IMAGE_HASH, p.getImage().getHash());
        intent.putExtra(Constants.EXTRA_FILENAME, filename);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private ImgurAlbumPagerAdapter getAdapter() {
        return (ImgurAlbumPagerAdapter) mAdapter;
    }
}
