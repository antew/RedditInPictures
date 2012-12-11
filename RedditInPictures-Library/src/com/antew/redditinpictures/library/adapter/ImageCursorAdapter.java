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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.GridView;
import android.widget.ImageView;

import com.antew.redditinpictures.library.imgur.ImgurThumbnailFetcher;
import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.ui.ImageGridFragment;
import com.antew.redditinpictures.library.utils.ImageFetcher;
import com.antew.redditinpictures.sqlite.RedditContract;

/**
 * This is used as the backing adapter for the {@link GridView} in {@link ImageGridFragment}
 * @author Antew
 *
 */
public class ImageCursorAdapter extends CursorAdapter {
    public static final String    TAG         = "ImageAdapter";
    private final Context         mContext;
    private int                   mItemHeight = 0;
    private int                   mNumColumns = 0;
    private GridView.LayoutParams mImageViewLayoutParams;
    private ImageFetcher          mImageFetcher;
    private LayoutInflater mLayoutInflater;
    private Cursor mCursor;

    /**
     * 
     * @param context The context
     * @param imageFetcher The image fetcher (currently using a {@link ImgurThumbnailFetcher}
     * @param cursor Cursor to a database containing PostData information
     */
    public ImageCursorAdapter(Context context, ImageFetcher imageFetcher, Cursor cursor) {
        super(context, cursor, 0);
        mContext = context;
        mImageFetcher = imageFetcher;
        mCursor = cursor;
        mLayoutInflater = LayoutInflater.from(context);
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

//    /**
//     * Add {@link PostData} objects to the Adapter, also calls {@link BaseAdapter#notifyDataSetChanged()}
//     * @param images The list of posts to add to the Adapter
//     */
//    public void addItems(List<PostData> images) {
//        mImages.addAll(images);
//        notifyDataSetChanged();
//    }
//
//    /**
//     * Clear the list backing the Adapter
//     */
//    public void clear() {
//        mImages.clear();
//        notifyDataSetChanged();
//    }
//
//    /**
//     * Removes all NSFW images from the Adapter and then calls {@link BaseAdapter#notifyDataSetChanged()}
//     */
//    public void removeNsfwImages() {
//        Iterator<PostData> it = mImages.iterator();
//        while (it.hasNext()) {
//            PostData post = (PostData) it.next();
//            if (post.isOver_18())
//                it.remove();
//        }
//        notifyDataSetChanged();
//    }

//    @Override
//    public View getView(int position, View convertView, ViewGroup container) {
//        ImageView imageView = null;
//        if (convertView == null) {
//            // if it's not recycled, instantiate and initialize
//            imageView = new ImageView(mContext);
//            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            imageView.setLayoutParams(mImageViewLayoutParams);
//        } else {
//            // Otherwise re-use the converted view
//            imageView = (ImageView) convertView;
//        }
//
//        // Check the height matches our calculated column width
//        if (imageView.getLayoutParams().height != mItemHeight) {
//            imageView.setLayoutParams(mImageViewLayoutParams);
//        }
//
//        // If we have a thumbnail from Reddit use that, otherwise use the full URL
//        String url = mImages.get(position).getUrl();
//        if (!mImages.get(position).getThumbnail().trim().equals("")) {
////            url = mImages.get(position).getThumbnail();
////            Log.i(TAG, "loading pos = " + position + " from the reddit thumbnail! " + url);
//        }
//        mImageFetcher.loadImage(url, imageView, null, null);
//        return imageView;
//    }

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
     * Sets the number of columns, this is currently used in the {@link OnGlobalLayoutListener} in {@link ImageGridFragment}
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
        if (!thumbnail.trim().equals("")) {
            url = thumbnail;
            Log.i(TAG, "loading pos = " + cursor.getPosition() + " from the reddit thumbnail! " + url);
        }
        mImageFetcher.loadImage(url, imageView, null, null);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(mImageViewLayoutParams);
        return imageView;
        
    }
    
}
