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
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.antew.redditinpictures.library.R;
import com.antew.redditinpictures.library.enums.ImageType;
import com.antew.redditinpictures.library.image.Image;
import com.antew.redditinpictures.library.image.ImgurAlbumType;
import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.reddit.PostData;
import com.antew.redditinpictures.library.utils.Consts;

/**
 * This fragment will populate the children of the ViewPager from {@link ImageDetailActivity}.
 */
public class ImageDetailFragment extends ImageViewerFragment {
    public static final String    TAG               = ImageDetailFragment.class.getSimpleName();

    /**
     * Factory method to generate a new instance of the fragment given an image number.
     * 
     * @param postData
     *            The post to load
     * @return A new instance of ImageDetailFragment with imageNum extras
     */
    public static ImageDetailFragment newInstance(PostData image) {
        final ImageDetailFragment f = new ImageDetailFragment();

        final Bundle args = new Bundle();
        args.putParcelable(IMAGE_DATA_EXTRA, image);
        f.setArguments(args);

        return f;
    }

    /**
     * Empty constructor as per the Fragment documentation
     */
    public ImageDetailFragment() {}

    /**
     * Populate image using a url from extras, use the convenience factory method
     * {@link ImageDetailFragment#newInstance(String)} to create this fragment.
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
    protected void resolveImage() {
        mResolveImageTask = new ResolveImageTask();
        mResolveImageTask.execute(mImage.getUrl());
    }

    public void populatePostData(View v) {
        String separator = " " + "\u2022" + " ";
        TextView postTitle = (TextView) v.findViewById(R.id.post_title);
        TextView postInformation = (TextView) v.findViewById(R.id.post_information);
        
        mVotes.setText("" + mImage.getScore());

        String titleText = mImage.getTitle() + " <font color='#BEBEBE'>(" + mImage.getDomain() + ")</font>";
        postTitle.setText(Html.fromHtml(titleText));

        postInformation.setText(mImage.getSubreddit() + separator + mImage.getNum_comments() + " comments" + separator + mImage.getAuthor());
    }

    @Override
    public void loadImage(Image image) {
        super.loadImage(image);

        if (image == null) {
            Log.e(TAG, "Recieved null ImageContainer in loadImage(ImageContainer image)");
            return;
        }
        
        if (ImageType.IMGUR_ALBUM.equals(image.getImageType())) {
            mBtnViewGallery.setVisibility(View.VISIBLE);
            mBtnViewGallery.setOnClickListener(getViewGalleryOnClickListener());

            mAlbum = ((ImgurAlbumType) image).getAlbum();
        }
    }

    private OnClickListener getViewGalleryOnClickListener() {
        return new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.i(TAG, "View Gallery");
                Intent intent = new Intent(getActivity(), getImgurAlbumActivity());
                intent.putExtra(ImgurAlbumActivity.EXTRA_ALBUM, mAlbum);
                startActivity(intent);
            }
        };
    }
    
    public Class<? extends ImgurAlbumActivity> getImgurAlbumActivity() {
        return ImgurAlbumActivity.class;
    }

    @Override
    protected boolean shouldShowPostInformation() {
        return true;
    }
}
