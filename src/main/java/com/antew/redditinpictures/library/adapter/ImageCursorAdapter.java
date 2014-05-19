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
package com.antew.redditinpictures.library.adapter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.antew.redditinpictures.library.database.RedditContract;
import com.antew.redditinpictures.library.imgur.ResolveAlbumCoverWorkerTask;
import com.antew.redditinpictures.library.util.Ln;
import com.antew.redditinpictures.library.util.Strings;
import com.antew.redditinpictures.pro.R;
import com.squareup.picasso.Picasso;
import java.util.regex.Pattern;

/**
 * This is used as the backing adapter for the {@link GridView}
 *
 * @author Antew
 */
public class ImageCursorAdapter extends CursorAdapter {
    public static final String TAG         = ImageCursorAdapter.class.getSimpleName();
    private             int    mItemHeight = 0;
    private             int    mNumColumns = 0;
    private Context mContext;
    private GridView.LayoutParams mImageViewLayoutParams;
    private Pattern mImgurNonAlbumPattern = Pattern.compile("^https?://imgur.com/[^/]*$");
    private Pattern mImgurAlbumPattern    = Pattern.compile("^https?://imgur.com/a/.*$");

    /**
     * @param context
     *     The context
     */
    public ImageCursorAdapter(Context context) {
        super(context, null, 0);
        mContext = context;
        mImageViewLayoutParams = new GridView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    public Object getItem(int position) {
        getCursor().moveToPosition(position);
        return getCursor();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(mImageViewLayoutParams);
        return imageView;
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
        String domain = cursor.getString(cursor.getColumnIndex(RedditContract.PostColumns.DOMAIN));

        // If we have a thumbnail from Reddit use that, otherwise use the full URL
        // Reddit will send 'default' for one of the default alien icons, which we want to avoid using
        if (Strings.notEmpty(thumbnail) && !thumbnail.equals("default")) {
            url = thumbnail;
        } else {
            // If the url is not pointing directly to the image. (Normally at i.imgur.com not imgur.com)
            if (domain.equals("imgur.com")) {
                // If the url is not an album but is just using a shortlink to an image append .jpg to the end and hope for the best.
                if (mImgurNonAlbumPattern.matcher(url).matches()) {
                    // The S before the extension gets us a small image.
                    url += "s.jpg";
                    Ln.d("Updating Url To: %s", url);
                } else if (mImgurAlbumPattern.matcher(url).matches()) {
                    if (ResolveAlbumCoverWorkerTask.cancelPotentialDownload(url, imageView)) {
                        ResolveAlbumCoverWorkerTask task = new ResolveAlbumCoverWorkerTask(url, imageView, mContext);
                        ResolveAlbumCoverWorkerTask.LoadingTaskHolder loadingTaskHolder = new ResolveAlbumCoverWorkerTask.LoadingTaskHolder(
                            task);
                        imageView.setTag(loadingTaskHolder);
                        task.execute();
                    }
                    //Since this is an album, we don't want it to be attempted to be loaded.
                    url = null;
                }
            }
        }

        if (Strings.notEmpty(url)) {
            try {
                Picasso.with(mContext)
                       .load(Uri.parse(url))
                       .placeholder(R.drawable.empty_photo)
                       .error(R.drawable.error_photo)
                       .fit()
                       .centerCrop()
                       .into(imageView);
            } catch (Exception e) {
                Ln.e(e, "Failed to load image");
                Picasso.with(mContext)
                       .load(R.drawable.error_photo)
                       .fit()
                       .centerCrop()
                       .into(imageView);
            }
        }
    }

    /**
     * Sets the item height. Useful for when we know the column width so the height can be set to
     * match.
     *
     * @param height
     *     The height to use for the grid items
     */
    public void setItemHeight(int height) {
        if (height == mItemHeight) {
            return;
        }
        mItemHeight = height;
        mImageViewLayoutParams = new GridView.LayoutParams(LayoutParams.MATCH_PARENT, mItemHeight);
        notifyDataSetChanged();
    }

    public int getNumColumns() {
        return mNumColumns;
    }

    /**
     * Sets the number of columns, this is currently used in the {@link OnGlobalLayoutListener}
     *
     * @param numColumns
     *     The number of columns in the GridView
     */
    public void setNumColumns(int numColumns) {
        mNumColumns = numColumns;
    }
}
