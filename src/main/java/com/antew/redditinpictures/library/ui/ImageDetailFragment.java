/*
 * Copyright (C) 2012 The Android Open Source Project
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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.model.ImageType;
import com.antew.redditinpictures.library.image.Image;
import com.antew.redditinpictures.library.image.ImgurAlbumType;
import com.antew.redditinpictures.library.image.ImgurGalleryType;
import com.antew.redditinpictures.library.model.reddit.PostData;
import com.antew.redditinpictures.library.util.Ln;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

/**
 * This fragment will populate the children of the ViewPager from {@link ImageDetailActivity}.
 */
public class ImageDetailFragment extends ImageViewerFragment {
    /**
     * Empty constructor as per the Fragment documentation
     */
    public ImageDetailFragment() {}

    /**
     * Factory method to generate a new instance of the fragment given an image number.
     *
     * @param image
     *     The post to load
     *
     * @return A new instance of ImageDetailFragment with imageNum extras
     */
    public static Fragment newInstance(PostData image) {
        final ImageDetailFragment fragment = new ImageDetailFragment();

        final Bundle args = new Bundle();
        args.putParcelable(IMAGE_DATA_EXTRA, image);
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Populate image using a url from extras, use the convenience factory method
     * {@link ImageDetailFragment#newInstance(com.antew.redditinpictures.library.model.reddit.PostData)} to create this fragment.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadExtras();
    }

    public void loadExtras() {
        mImage = getArguments() != null ? (PostData) getArguments().getParcelable(IMAGE_DATA_EXTRA) : null;
    }

    @Override
    protected boolean shouldShowPostInformation() {
        return true;
    }

    public void populatePostData(View v) {
        String separator = " " + "\u2022" + " ";
        mPostVotes.setText("" + mImage.getScore());
        String titleText = mImage.getTitle() + " <font color='#BEBEBE'>(" + mImage.getDomain() + ")</font>";
        mPostTitle.setText(Html.fromHtml(titleText));
        mPostInformation.setText(
            mImage.getSubreddit() + separator + mImage.getNum_comments() + " comments" + separator + mImage.getAuthor());
    }

    @Override
    protected void resolveImage() {
        mResolveImageTask = new ResolveImageTask();
        mResolveImageTask.execute(mImage.getUrl());
    }

    @Override
    public void loadImage(Image image) {
        super.loadImage(image);

        if (image == null) {
            Ln.e("Recieved null ImageContainer in loadImage(ImageContainer image)");
            return;
        }

        if (ImageType.IMGUR_ALBUM.equals(image.getImageType())) {
            mAlbum = ((ImgurAlbumType) image).getAlbum();
        } else if (ImageType.IMGUR_GALLERY.equals(image.getImageType())) {
            mAlbum = ((ImgurGalleryType) image).getAlbum();

            if (mAlbum == null) {
                super.loadImage(((ImgurGalleryType) image).getSingleImage());
            }
        }

        if (mAlbum != null && mAlbum.getImages().size() > 1) {
            mBtnViewGallery.setVisibility(View.VISIBLE);
            mBtnViewGallery.setOnClickListener(getViewGalleryOnClickListener());
        }
    }

    private OnClickListener getViewGalleryOnClickListener() {
        return new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Ln.i("View Gallery");
                EasyTracker.getInstance(getActivity())
                           .send(
                               MapBuilder.createEvent(Constants.Analytics.Category.UI_ACTION, Constants.Analytics.Action.OPEN_GALLERY,
                                                      Constants.Analytics.Label.IMGUR, (long) mAlbum.getImages().size()).build()
                                );
                Intent intent = new Intent(getActivity(), getImgurAlbumActivity());
                intent.putExtra(ImgurAlbumActivity.EXTRA_ALBUM, mAlbum);
                startActivity(intent);
            }
        };
    }

    public Class<? extends ImgurAlbumActivity> getImgurAlbumActivity() {
        return ImgurAlbumActivity.class;
    }
}
