/*
 * Copyright (C) 2014 Antew
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.antew.redditinpictures.library.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.widget.Toast;
import com.antew.redditinpictures.library.adapter.ImgurAlbumPagerAdapter;
import com.antew.redditinpictures.library.event.DownloadImageCompleteEvent;
import com.antew.redditinpictures.library.imgur.ImgurAlbumApi.Album;
import com.antew.redditinpictures.library.imgur.ImgurImageApi;
import com.antew.redditinpictures.library.imgur.ImgurImageApi.ImgurImage;
import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.library.util.Ln;
import com.antew.redditinpictures.library.util.StringUtil;
import com.antew.redditinpictures.library.util.Strings;
import com.antew.redditinpictures.pro.R;
import com.squareup.otto.Subscribe;
import java.util.List;

public class ImgurAlbumActivity extends ImageViewerActivity {
    public static final String TAG         = "ImgurAlbumActivity";
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
        super.getExtras();
        if (getIntent().hasExtra(EXTRA_ALBUM)) {
            mAlbum = getIntent().getParcelableExtra(EXTRA_ALBUM);
            mImages = mAlbum.getImages();
        }
    }

    @Override
    public FragmentStatePagerAdapter getPagerAdapter() {
        return new ImgurAlbumPagerAdapter(getFragmentManager(), getImages());
    }

    @SuppressWarnings("unchecked")
    protected List<ImgurImageApi.Image> getImages() {
        return (List<ImgurImageApi.Image>) mImages;
    }

    @Override
    protected void updateDisplay(int position) {
        getActionBar().setTitle(++position + "/" + getAdapter().getCount() + " - " + getString(R.string.reddit_in_pictures));
    }

    /**
     * Called upon reaching the last page present in the ViewPager
     */
    @Override
    public void reachedCloseToLastPage() {
        //Do nothing.
    }

    /**
     * Get the JSON representation of the current image/post in the ViewPager to report an error.
     *
     * @return The JSON representation of the currently viewed object.
     */
    @Override
    protected void reportCurrentItem() {
        RedditService.reportImage(this, getAdapter().getImage(mPager.getCurrentItem()));
    }

    @Override
    public String getSubreddit() {
        return "ImgurAlbum";
    }

    @Override
    public String getUrlForSharing() {
        ImgurImageApi.Image image = getAdapter().getImage(mPager.getCurrentItem());
        return image.getLink();
    }

    @Override
    protected Uri getPostUri() {
        ImgurImageApi.Image image = getAdapter().getImage(mPager.getCurrentItem());
        return Uri.parse(image.getLink());
    }

    @Override
    public String getFilenameForSave() {
        String name = super.getFilenameForSave();
        if (getAdapter() != null && mPager != null) {
            ImgurImageApi.Image p = getAdapter().getImage(mPager.getCurrentItem());
            name = p.getId();
            if (!p.getTitle().equals("")) {
                name = StringUtil.sanitizeFileName(p.getTitle());
            } else if (Strings.notEmpty(p.getDescription())) {
                name = StringUtil.sanitizeFileName(p.getDescription());
            }
        }

        return name;
    }

    @Override
    public void onFinishSaveImageDialog(String filename) {
        ImgurImageApi.Image image = getAdapter().getImage(mPager.getCurrentItem());
        mImageDownloader.downloadImage(image.getLink(), filename);
    }

    @Subscribe
    public void onDownloadImageComplete(DownloadImageCompleteEvent event) {
        Ln.i("DownloadImageComplete - filename was: " + event.getFilename());
        Toast.makeText(this, "Post saved as " + event.getFilename(), Toast.LENGTH_SHORT).show();
    }

    private ImgurAlbumPagerAdapter getAdapter() {
        return (ImgurAlbumPagerAdapter) mAdapter;
    }
}
