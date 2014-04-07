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

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import com.antew.redditinpictures.library.imgur.ImgurImageApi.Image;
import com.antew.redditinpictures.library.imgur.ImgurImageApi.ImgurImage;
import com.antew.redditinpictures.library.utils.ImageUtil;
import com.antew.redditinpictures.library.utils.Ln;
import com.squareup.picasso.Picasso;

/**
 * This fragment will populate the children of the ViewPager from {@link ImgurAlbumActivity}.
 */
public class ImgurAlbumFragment extends ImageViewerFragment {
    public static final String TAG = "ImgurAlbumFragment";
    private ImgurImage mImage;

    /**
     * Empty constructor as per the Fragment documentation
     */
    public ImgurAlbumFragment() {
    }

    /**
     * Factory method to generate a new instance of the fragment given an {@link ImgurImage}
     *
     * @return A new instance of ImageDetailFragment with imageNum extras
     */
    public static ImgurAlbumFragment newInstance(ImgurImage image) {
        final ImgurAlbumFragment f = new ImgurAlbumFragment();
        final Bundle args = new Bundle();
        args.putParcelable(IMAGE_DATA_EXTRA, image);
        f.setArguments(args);

        return f;
    }

    @Override
    public void loadExtras() {
        mImage = getArguments() != null ? (ImgurImage) getArguments().getParcelable(IMAGE_DATA_EXTRA) : null;
    }

    @Override
    protected boolean shouldShowPostInformation() {
        return hasTitle() || hasCaption();
    }

    @Override
    public void populatePostData(View v) {
        // Normally has the details of the reddit post (e.g.
        // "Android * 120 Comments * monkeyonatypewriter")
        mPostInformation.setTextSize(14);

        // Hide the number of votes
        mPostVotes.setVisibility(View.GONE);

        Image image = mImage.getImage();
        boolean hasTitle = hasTitle();
        boolean hasCaption = hasCaption();

        if (hasTitle || hasCaption) {
            if (hasTitle) {
                Ln.i("Title - %s", image.getTitle());
                mPostTitle.setText(Html.fromHtml(image.getTitle()));
            } else {
                mPostTitle.setVisibility(View.GONE);
            }

            if (hasCaption) {
                Ln.i("Caption - %s", image.getCaption());
                mPostInformation.setText(Html.fromHtml(image.getCaption()));
            } else {
                mPostInformation.setVisibility(View.GONE);
            }
        } else {
            v.setVisibility(View.GONE);
            v.invalidate();
        }
    }

    @Override
    protected void resolveImage() {
        String imageUrl = mImage.getLinks().getOriginal();
        if (ImageUtil.isGif(imageUrl)) {
            super.loadGifInWebView(imageUrl);
        } else {
            Picasso.with(getActivity()).load(imageUrl).into(mImageView);
        }
    }

    private boolean hasTitle() {
        return mImage.getImage().getTitle() != null && !mImage.getImage().getTitle().trim().equals("");
    }

    private boolean hasCaption() {
        return mImage.getImage().getCaption() != null && !mImage.getImage().getCaption().trim().equals("");
    }
}
