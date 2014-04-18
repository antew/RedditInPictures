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
        Picasso.with(mContext).load(R.drawable.empty_photo).error(R.drawable.error_photo).into(mImageView);
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
                Picasso.with(mContext)
                       .load(Uri.parse(imageUrl))
                       .placeholder(R.drawable.loading_spinner_48)
                       .error(R.drawable.empty_photo)
                       .into(mImageView);
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
            return mImgurImagePrefix + ((ImgurAlbumType) imageAlbum).getAlbum().getCover() + mImgurImageSuffix;
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

