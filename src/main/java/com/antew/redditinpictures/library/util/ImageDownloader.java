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
package com.antew.redditinpictures.library.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import com.antew.redditinpictures.library.annotation.ForApplication;
import com.antew.redditinpictures.library.event.DownloadImageCompleteEvent;
import com.antew.redditinpictures.library.image.ImageResolver;
import com.antew.redditinpictures.library.model.ImageSize;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import javax.inject.Inject;

public class ImageDownloader {
    @Inject
    public  Bus     mBus;
    private Context context;

    /**
     * Create an image downloader.  This is meant to
     * work with Application context!
     *
     * @param context
     */
    @Inject
    public ImageDownloader(@ForApplication Context context) {
        this.context = context;
    }

    public void downloadImage(String url, String filename) {
        new DownloadImageTask(context, url, filename).execute();
    }

    private class DownloadImageTask extends SafeAsyncTask<String> {
        Context mContext;
        String  mUrl;
        String  mFilename;

        public DownloadImageTask(Context context, String url, String filename) {
            mContext = context;
            mUrl = url;
            mFilename = filename;
        }

        @Override
        public String call() throws Exception {
            if (Strings.isEmpty(mUrl) || Strings.isEmpty(mFilename) || mContext == null) {
                Ln.e("DownloadImageTask - Invalid Arguments, URL and filename must not be null or empty: "
                     + "filename = "
                     + Strings.toString(mFilename)
                     + ", mUrl = "
                     + Strings.toString(mUrl));

                return null;
            }

            String resolvedUrl = mUrl;

            // If the image is already resolved, resolve it. (e.g. in the ListView)
            if (!ImageUtil.isSupportedImage(mUrl)) {
                resolvedUrl = ImageResolver.resolve(mUrl, ImageSize.ORIGINAL);
            }

            OutputStream outputStream = null;
            try {
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                if (ImageUtil.isJpeg(resolvedUrl)) {
                    mFilename += ".jpg";
                } else if (ImageUtil.isPng(resolvedUrl)) {
                    mFilename += ".png";
                } else if (ImageUtil.isGif(resolvedUrl)) {
                    mFilename += ".gif";
                } else if (ImageUtil.isWebp(resolvedUrl)) {
                    mFilename += ".webp";
                } else {
                    mFilename += ".jpg";
                }

                File file = new File(path, mFilename);

                // Make sure the Pictures directory exists.
                path.mkdirs();

                outputStream = new FileOutputStream(file);

                // Picasso doesn't handle Gifs properly and there isn't any way to write them from a Bitmap properly.
                if (ImageUtil.isGif(resolvedUrl)) {
                    OkHttpClient client = new OkHttpClient();
                    InputStream in = null;
                    try {
                        HttpURLConnection connection = client.open(new URL(resolvedUrl));
                        in = connection.getInputStream();

                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = in.read(buf)) != -1) {
                            outputStream.write(buf, 0, len);
                        }
                        outputStream.flush();
                        MediaScannerConnection.scanFile(mContext, new String[] { file.toString() }, null, null);
                    } catch (MalformedURLException e) {
                        Ln.e(e);
                    } catch (IOException e) {
                        Ln.e(e);
                    } finally {
                        AndroidUtil.closeQuietly(in);
                    }
                } else {
                    Bitmap image = Picasso.with(mContext).load(Uri.parse(resolvedUrl)).get();
                    if (image != null) {
                        if (ImageUtil.isJpeg(resolvedUrl)) {
                            image.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                        } else if (ImageUtil.isPng(resolvedUrl)) {
                            //PNG is loseless and ignores the quality setting.
                            image.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                        } else if (ImageUtil.isWebp(resolvedUrl)) {
                            image.compress(Bitmap.CompressFormat.WEBP, 90, outputStream);
                        } else if (ImageUtil.isBitmap(resolvedUrl)) {
                            image.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                        } else {
                            image.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                        }

                        outputStream.flush();
                        image.recycle();
                        Ln.d("Saved File to %s", file.getAbsolutePath());

                        // Tell the media scanner about the new file so that it is
                        // immediately available to the user.
                        MediaScannerConnection.scanFile(mContext, new String[] { file.toString() }, null, null);
                    }
                }
            } finally {
                AndroidUtil.closeQuietly(outputStream);
            }
            return mFilename.toString();
        }

        @Override
        protected void onSuccess(String result) throws Exception {
            mBus.post(new DownloadImageCompleteEvent(result));
        }
    }
}
