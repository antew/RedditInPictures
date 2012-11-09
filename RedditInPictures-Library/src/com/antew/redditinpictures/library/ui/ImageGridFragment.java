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

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;

import com.actionbarsherlock.app.SherlockFragment;
import com.antew.redditinpictures.library.BuildConfig;
import com.antew.redditinpictures.library.R;
import com.antew.redditinpictures.library.R.id;
import com.antew.redditinpictures.library.adapter.ImageAdapter;
import com.antew.redditinpictures.library.imgur.ImgurThumbnailFetcher;
import com.antew.redditinpictures.library.reddit.RedditApi.PostData;
import com.antew.redditinpictures.library.utils.Consts;
import com.antew.redditinpictures.library.utils.ImageCache.ImageCacheParams;
import com.antew.redditinpictures.library.utils.ImageFetcher;
import com.antew.redditinpictures.library.utils.Util;

/**
 * The main fragment that powers the ImageGridActivity screen. Fairly straight forward GridView
 * implementation with the key addition being the ImageWorker class w/ImageCache to load children
 * asynchronously, keeping the UI nice and smooth and caching thumbnails for quick retrieval. The
 * cache is retained over configuration changes like orientation change so the images are populated
 * quickly if, for example, the user rotates the device.
 */
public abstract class ImageGridFragment extends SherlockFragment implements AdapterView.OnItemClickListener {
    public static final String  TAG             = "ImageGridFragment";
    private static final String IMAGE_CACHE_DIR = "thumbs";
    public static final String  ENTRIES         = "Entries";
    private int                 mImageThumbSize;
    private int                 mImageThumbSpacing;
    protected ImageAdapter      mAdapter;
    private ImageFetcher        mImageFetcher;
    private LoadMoreImages      mLoadMoreImages;

    public interface LoadMoreImages {
        public void loadMoreImages();
    }

    /**
     * Empty constructor as per the Fragment documentation
     */
    public ImageGridFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);

        List<PostData> images = new ArrayList<PostData>();
        if (getArguments() != null) {
            images = getArguments().getParcelableArrayList(ENTRIES);
        }

        ImageCacheParams cacheParams = new ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);

        // Set memory cache to 25% of mem class
        cacheParams.setMemCacheSizePercent(getActivity(), 0.25f);

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        mImageFetcher = new ImgurThumbnailFetcher(getActivity(), mImageThumbSize);
        mImageFetcher.setLoadingImage(R.drawable.empty_photo);
        mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);

        mAdapter = new ImageAdapter(getActivity(), mImageFetcher, images);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mRemoveNsfwImages,
                new IntentFilter(Consts.BROADCAST_REMOVE_NSFW_IMAGES));

        try {
            mLoadMoreImages = (LoadMoreImages) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement LoadMoreImages interface");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.image_grid_fragment, container, false);
        final GridView gridView = (GridView) v.findViewById(R.id.gridView);
        View emptyView = v.findViewById(R.id.empty_grid_view);
        setUpGridView(gridView, emptyView);
        return v;
    }

    public void setUpGridView(GridView gridView, View emptyView) {
        gridView.setAdapter(mAdapter);
        gridView.setEmptyView(emptyView);
        gridView.setOnItemClickListener(this);
        gridView.setOnScrollListener(getGridViewOnScrollListener(gridView));
        gridView.getViewTreeObserver().addOnGlobalLayoutListener(getGridGlobalLayoutListener(gridView));
    }

    private AbsListView.OnScrollListener getGridViewOnScrollListener(final GridView gridView) {
        return new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                // Pause fetcher to ensure smoother scrolling when flinging
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    mImageFetcher.setPauseWork(true);
                } else {
                    mImageFetcher.setPauseWork(false);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // if we're at the bottom of the listview, load more data

                if (totalItemCount > 0 && ((firstVisibleItem + visibleItemCount) >= totalItemCount) && mLoadMoreImages != null)
                    mLoadMoreImages.loadMoreImages();
            }
        };
    }

    /**
     * This listener is used to get the final width of the GridView and then calculate the number of
     * columns and the width of each column. The width of each column is variable as the GridView
     * has stretchMode=columnWidth. The column width is used to set the height of each view so we
     * get nice square thumbnails.
     * 
     * @param gridView
     * @return The global layout listener
     */
    private ViewTreeObserver.OnGlobalLayoutListener getGridGlobalLayoutListener(final GridView gridView) {
        return new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mAdapter.getNumColumns() == 0) {
                    final int numColumns = (int) Math.floor(gridView.getWidth() / (mImageThumbSize + mImageThumbSpacing));
                    if (numColumns > 0) {
                        final int columnWidth = (gridView.getWidth() / numColumns) - mImageThumbSpacing;
                        mAdapter.setNumColumns(numColumns);
                        mAdapter.setItemHeight(columnWidth);
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "onCreateView - numColumns set to " + numColumns);
                        }
                    }
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        mImageFetcher.setExitTasksEarly(true);
        mImageFetcher.flushCache();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mImageFetcher.closeCache();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

        final Intent i = new Intent(getActivity(), getImageDetailActivityClass());
        i.putExtra(ImageDetailActivity.EXTRA_IMAGE, (int) id);
        Bundle b = new Bundle();
        b.putParcelableArrayList(ImageDetailActivity.EXTRA_ENTRIES, (ArrayList<PostData>) mAdapter.getPostData());
        i.putExtras(b);

        if (Util.hasJellyBean()) {
            ActivityOptions options = ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight());
            getActivity().startActivity(i, options.toBundle());
        } else {
            startActivity(i);
        }
    }

    public Class<? extends ImageDetailActivity> getImageDetailActivityClass() {
        return ImageDetailActivity.class;
    }

    public void addImages(List<PostData> images) {
        mAdapter.addItems(images);
        mAdapter.notifyDataSetChanged();
    }

    public void clearAdapter() {
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();
    }

    /**
     * This BroadcastReceiver handles updating the score when a vote is cast or changed
     */
    private BroadcastReceiver mRemoveNsfwImages = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            mAdapter.removeNsfwImages();
            mAdapter.notifyDataSetChanged();
        }
    };
}
