/*
 * Copyright (C) 2012 Antew | antewcode@gmail.com
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
package com.antew.redditinpictures.library.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.GridView;
import android.widget.ImageView;

import com.antew.redditinpictures.library.R;
import com.antew.redditinpictures.library.ui.ImageGridFragment;
import com.antew.redditinpictures.library.utils.ImageFetcher;
import com.antew.redditinpictures.sqlite.RedditContract;
import com.squareup.picasso.Picasso;

/**
 * This is used as the backing adapter for the {@link GridView} in {@link ImageGridFragment}
 * 
 * @author Antew
 * 
 */
public class ImageCursorAdapter extends CursorAdapter {
    public static final String    TAG         = ImageCursorAdapter.class.getSimpleName();
    private int                   mItemHeight = 0;
    private int                   mNumColumns = 0;
    private GridView.LayoutParams mImageViewLayoutParams;
    private ImageFetcher          mImageFetcher;
    private Cursor                mCursor;

    /**
     * 
     * @param context
     *            The context
     * @param imageFetcher
     *            The image fetcher (currently using a {@link com.antew.redditinpictures.library.imgur.SizeAwareImageFetcher}
     * @param cursor
     *            Cursor to a database containing PostData information
     */
    public ImageCursorAdapter(Context context, ImageFetcher imageFetcher, Cursor cursor) {
        super(context, cursor, 0);
        mContext = context;
        mImageFetcher = imageFetcher;
        mCursor = cursor;
        mImageViewLayoutParams = new GridView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

    }

    @Override
    public Object getItem(int position) {
        mCursor.moveToPosition(position);
        return mCursor;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Sets the item height. Useful for when we know the column width so the height can be set to
     * match.
     * 
     * @param height
     */
    public void setItemHeight(int height) {
        if (height == mItemHeight) {
            return;
        }
        mItemHeight = height;
        mImageViewLayoutParams = new GridView.LayoutParams(LayoutParams.MATCH_PARENT, mItemHeight);
        mImageFetcher.setImageSize(height);
        notifyDataSetChanged();
    }

    /**
     * Sets the number of columns, this is currently used in the {@link OnGlobalLayoutListener} in
     * {@link ImageGridFragment}
     * 
     * @param numColumns
     */
    public void setNumColumns(int numColumns) {
        mNumColumns = numColumns;
    }

    public int getNumColumns() {
        return mNumColumns;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView imageView = (ImageView) view;
        // Check the height matches our calculated column width
        if (imageView.getLayoutParams().height != mItemHeight) {
            imageView.setLayoutParams(mImageViewLayoutParams);
        }

        String url = cursor.getString(cursor.getColumnIndex(RedditContract.PostColumns.URL));
        String thumbnail = cursor.getString(cursor.getColumnIndex(RedditContract.PostColumns.THUMBNAIL));
        // If we have a thumbnail from Reddit use that, otherwise use the full URL
        // Reddit will send 'default' for one of the default alien icons, which we want to avoid using
        if (!thumbnail.trim().equals("") && !thumbnail.equals("default")) {
            url = thumbnail;
        }
        Picasso.with(mContext).load(url).placeholder(R.drawable.empty_photo).into(imageView);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(mImageViewLayoutParams);
        return imageView;

    }

}
