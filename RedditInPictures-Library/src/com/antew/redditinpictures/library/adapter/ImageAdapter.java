package com.antew.redditinpictures.library.adapter;

import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.reddit.RedditApi.PostData;
import com.antew.redditinpictures.library.utils.ImageFetcher;

public class ImageAdapter extends BaseAdapter {
    public static final String    TAG         = "ImageAdapter";
    private final Context         mContext;
    private int                   mItemHeight = 0;
    private int                   mNumColumns = 0;
    private GridView.LayoutParams mImageViewLayoutParams;
    private List<PostData>        mImages;
    private ImageFetcher          mImageFetcher;

    public ImageAdapter(Context context, ImageFetcher imageFetcher, List<PostData> urls) {
        super();
        mContext = context;
        mImages = urls;
        mImageFetcher = imageFetcher;
        mImageViewLayoutParams = new GridView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    public PostData getPostData(int position) {
        return mImages.get(position);
    }

    public List<PostData> getPostData() {
        return mImages;
    }

    @Override
    public int getCount() {
        if (mImages == null)
            return 0;

        return mImages.size();
    }

    @Override
    public Object getItem(int position) {
        if (mImages == null)
            return null;

        return mImages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public void addItems(List<PostData> images) {
        mImages.addAll(images);
        notifyDataSetChanged();
    }

    public void clear() {
        mImages.clear();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public void removeNsfwImages() {
        Iterator<PostData> it = mImages.iterator();
        while (it.hasNext()) {
            PostData post = (PostData) it.next();
            if (post.isOver_18())
                it.remove();
        }
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        Log.i(TAG, "getView position = " + position);
        ViewHolder viewHolder = null;
        if (convertView == null) {
            // if it's not recycled, instantiate and initialize
            convertView = new ImageView(mContext);
            Log.i("getView", "NOT Recycling imageView");
            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) convertView;
            viewHolder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            viewHolder.imageView.setLayoutParams(mImageViewLayoutParams);
            convertView.setTag(viewHolder);
        } else {
        }
        if (convertView != null && convertView instanceof ImageView) {
            Log.i("getView", "Recycling imageView");
            // Otherwise re-use the converted view
            viewHolder = (ViewHolder) convertView.getTag();
        } else {

        }

        // Check the height matches our calculated column width
        if (viewHolder.imageView.getLayoutParams().height != mItemHeight) {
            viewHolder.imageView.setLayoutParams(mImageViewLayoutParams);
        }

        // Finally load the image asynchronously into the ImageView, this also takes care of
        // setting a placeholder image while the background thread runs
        mImageFetcher.loadImage(mImages.get(position).getUrl(), viewHolder.imageView, null, null);
        return convertView;
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

    public void setNumColumns(int numColumns) {
        mNumColumns = numColumns;
    }

    public int getNumColumns() {
        return mNumColumns;
    }
    
    static class ViewHolder {
        ImageView imageView;
    }
}
