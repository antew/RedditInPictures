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
package com.antew.redditinpictures.library.imgur;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;
import com.antew.redditinpictures.library.image.Image;
import com.antew.redditinpictures.library.image.ImageResolver;
import com.antew.redditinpictures.library.image.ImgurAlbumType;
import com.antew.redditinpictures.library.util.Ln;
import com.antew.redditinpictures.library.util.SafeAsyncTask;
import com.antew.redditinpictures.pro.R;
import com.squareup.picasso.Picasso;
import java.lang.ref.WeakReference;

public class ResolveAlbumCoverWorkerTask extends SafeAsyncTask<String> {
    private static final String mImgurImagePrefix = "http://i.imgur.com/";
    // The S before the extension gets us a small image.
    private static final String mImgurImageSuffix = "s.jpg";

    private String    mImageUrl;
    private ImageView mImageView;
    private Context   mContext;

    public ResolveAlbumCoverWorkerTask(String mImageUrl, ImageView mImageView, Context mContext) {
        super();
        this.mImageUrl = mImageUrl;
        this.mImageView = mImageView;
        this.mContext = mContext;
    }

    public static boolean cancelPotentialDownload(String url, ImageView imageView) {
        ResolveAlbumCoverWorkerTask albumCoverResolverWorkerTask = getAlbumCoverResolverTask(imageView);

        if (albumCoverResolverWorkerTask != null) {
            String bitmapUrl = albumCoverResolverWorkerTask.mImageUrl;
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                albumCoverResolverWorkerTask.cancel(true);
            } else {
                // The same URL is already being downloaded.
                return false;
            }
        }
        return true;
    }

    /**
     * @throws Exception,
     *     captured on passed to onException() if present.
     */
    @Override
    protected void onPreExecute() throws Exception {
        super.onPreExecute();
        Picasso.with(mContext).load(R.drawable.empty_photo).into(mImageView);
    }

    /**
     * @param imageUrl
     *     the result of {@link #call()}
     *
     * @throws Exception,
     *     captured on passed to onException() if present.
     */
    @Override
    protected void onSuccess(String imageUrl) throws Exception {
        if (mImageView != null) {
            ResolveAlbumCoverWorkerTask albumCoverResolverWorkerTask = getAlbumCoverResolverTask(mImageView);
            if (this == albumCoverResolverWorkerTask) {
                try {
                    Picasso.with(mContext)
                           .load(Uri.parse(imageUrl))
                           .placeholder(R.drawable.loading_spinner_48)
                           .error(R.drawable.error_photo)
                           .into(mImageView);
                } catch (Exception e) {
                    Ln.e(e, "Failed to load image");
                    Picasso.with(mContext)
                           .load(R.drawable.error_photo)
                           .into(mImageView);
                }
            }
        }
    }

    public static ResolveAlbumCoverWorkerTask getAlbumCoverResolverTask(ImageView imageView) {
        if (imageView != null) {
            Object tag = imageView.getTag();
            if (tag instanceof LoadingTaskHolder) {
                LoadingTaskHolder loadingTaskHolder = (LoadingTaskHolder) tag;
                return loadingTaskHolder.getAlbumCoverResolverTask();
            }
        }
        return null;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     *
     * @throws Exception
     *     if unable to compute a result
     */
    @Override
    public String call() throws Exception {
        Ln.d("Resolving url: %s", mImageUrl);
        Image imageAlbum = ImageResolver.resolve(mImageUrl);
        if (imageAlbum instanceof ImgurAlbumType) {
            ImgurAlbumType imgurAlbumType = (ImgurAlbumType) imageAlbum;
            if (imgurAlbumType != null) {
                return mImgurImagePrefix + imgurAlbumType.getAlbum().getCover() + mImgurImageSuffix;
            }
        }
        return null;
    }

    public static class LoadingTaskHolder {
        private final WeakReference<ResolveAlbumCoverWorkerTask> mAlbumCoverResolverWorkerTask;

        public LoadingTaskHolder(ResolveAlbumCoverWorkerTask bitmapDownloaderTask) {
            mAlbumCoverResolverWorkerTask = new WeakReference<ResolveAlbumCoverWorkerTask>(bitmapDownloaderTask);
        }

        public ResolveAlbumCoverWorkerTask getAlbumCoverResolverTask() {
            return mAlbumCoverResolverWorkerTask.get();
        }
    }
}

