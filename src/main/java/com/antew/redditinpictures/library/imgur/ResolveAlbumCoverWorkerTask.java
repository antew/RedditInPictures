package com.antew.redditinpictures.library.imgur;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import com.antew.redditinpictures.library.image.Image;
import com.antew.redditinpictures.library.image.ImageResolver;
import com.antew.redditinpictures.library.image.ImgurAlbumType;
import com.antew.redditinpictures.library.utils.Ln;
import com.antew.redditinpictures.library.utils.SafeAsyncTask;
import com.antew.redditinpictures.pro.R;
import com.squareup.picasso.Picasso;
import java.lang.ref.WeakReference;

public class ResolveAlbumCoverWorkerTask extends SafeAsyncTask<Image> {
    private static final String mImgurImagePrefix = "http://i.imgur.com/";
    private static final String mImgurImageSuffix = ".jpg";

    private String mImageUrl;
    private ImageView mImageView;
    private Context mContext;

    public ResolveAlbumCoverWorkerTask(String mImageUrl, ImageView mImageView, Context mContext) {
        super();
        this.mImageUrl = mImageUrl;
        this.mImageView = mImageView;
        this.mContext = mContext;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override public Image call() throws Exception {
        Ln.d("Resolving url: %s", mImageUrl);
        return ImageResolver.resolve(mImageUrl);
    }

    /**
     * @param image the result of {@link #call()}
     * @throws Exception, captured on passed to onException() if present.
     */
    @Override protected void onSuccess(Image image) throws Exception {
        if (mImageView != null) {
            ResolveAlbumCoverWorkerTask albumCoverResolverWorkerTask = getAlbumCoverResolverTask(mImageView);
            if (this == albumCoverResolverWorkerTask) {
                if (image instanceof ImgurAlbumType) {
                    ImgurAlbumApi.Album album = ((ImgurAlbumType) image).getAlbum();
                    Picasso.with(mContext)
                        .load(mImgurImagePrefix + album.getCover() + mImgurImageSuffix)
                        .placeholder(R.drawable.loading_spinner_48)
                        .error(R.drawable.empty_photo)
                        .into(mImageView);
                }
            }
        }
    }

    public static class LoadingDrawable extends ColorDrawable {
        private final WeakReference<ResolveAlbumCoverWorkerTask> mAlbumCoverResolverWorkerTask;

        public LoadingDrawable(ResolveAlbumCoverWorkerTask bitmapDownloaderTask) {
            super(Color.BLACK);
            mAlbumCoverResolverWorkerTask =
                new WeakReference<ResolveAlbumCoverWorkerTask>(bitmapDownloaderTask);
        }

        public ResolveAlbumCoverWorkerTask getAlbumCoverResolverTask() {
            return mAlbumCoverResolverWorkerTask.get();
        }
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

    public static ResolveAlbumCoverWorkerTask getAlbumCoverResolverTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof LoadingDrawable) {
                LoadingDrawable loadingDrawable = (LoadingDrawable)drawable;
                return loadingDrawable.getAlbumCoverResolverTask();
            }
        }
        return null;
    }
}

