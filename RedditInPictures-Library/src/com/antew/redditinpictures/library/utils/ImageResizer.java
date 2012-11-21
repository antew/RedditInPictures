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

package com.antew.redditinpictures.library.utils;

import java.io.FileDescriptor;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.antew.redditinpictures.library.logging.Log;

/**
 * A simple subclass of {@link ImageWorker} that resizes images from resources given a target width
 * and height. Useful for when the input images might be too large to simply load directly into
 * memory.
 */
public class ImageResizer extends ImageWorker {
    private static final String TAG       = "ImageResizer";
    protected int               mImageWidth;
    protected int               mImageHeight;
    protected static final int  MAX_TRIES = 5;

    /**
     * Initialize providing a single target image size (used for both width and height);
     * 
     * @param context
     * @param imageWidth
     * @param imageHeight
     */
    public ImageResizer(Context context, int imageWidth, int imageHeight) {
        super(context);
        setImageSize(imageWidth, imageHeight);
    }

    /**
     * Initialize providing a single target image size (used for both width and height);
     * 
     * @param context
     * @param imageSize
     */
    public ImageResizer(Context context, int imageSize) {
        super(context);
        setImageSize(imageSize);
    }

    /**
     * Set the target image width and height.
     * 
     * @param width
     * @param height
     */
    public void setImageSize(int width, int height) {
        mImageWidth = width;
        mImageHeight = height;
    }

    /**
     * Set the target image size (width and height will be the same).
     * 
     * @param size
     */
    public void setImageSize(int size) {
        setImageSize(size, size);
    }

    /**
     * The main processing method. This happens in a background task. In this case we are just
     * sampling down the bitmap and returning it from a resource.
     * 
     * @param resId
     * @return
     */
    private Bitmap processBitmap(int resId) {
        Log.d(TAG, "processBitmap - " + resId);
        return decodeSampledBitmapFromResource(mResources, resId, mImageWidth, mImageHeight);
    }

    @Override
    protected Bitmap processBitmap(Object data) {
        return processBitmap(Integer.parseInt(String.valueOf(data)));
    }

    /**
     * Decode and sample down a bitmap from resources to the requested width and height.
     * 
     * @param res
     *            The resources object containing the image data
     * @param resId
     *            The resource id of the image data
     * @param reqWidth
     *            The requested width of the resulting bitmap
     * @param reqHeight
     *            The requested height of the resulting bitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap b = null;
        int count = 0;
        while (b == null && count < MAX_TRIES) {
            try {
                b = BitmapFactory.decodeResource(res, resId, options);
            } catch (OutOfMemoryError e) {
                b = null;
                options.inSampleSize *= 2;
                count++;
                Log.e(TAG, "Out of memory decoding bitmap from Resource", e);
            }
        }
        return b;
    }

    /**
     * Decode and sample down a bitmap from a file to the requested width and height.
     * 
     * @param filename
     *            The full path of the file to decode
     * @param reqWidth
     *            The requested width of the resulting bitmap
     * @param reqHeight
     *            The requested height of the resulting bitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromFile(String filename, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap b = null;
        int count = 0;
        while (b == null && count < MAX_TRIES) {
            try {
                b = BitmapFactory.decodeFile(filename, options);
            } catch (OutOfMemoryError e) {
                b = null;
                options.inSampleSize *= 2;
                Log.e(TAG, "Out of memory decoding bitmap from File", e);
            }
        }
        return b;
    }

    /**
     * Decode and sample down a bitmap from a file input stream to the requested width and height.
     * 
     * @param fileDescriptor
     *            The file descriptor to read from
     * @param reqWidth
     *            The requested width of the resulting bitmap
     * @param reqHeight
     *            The requested height of the resulting bitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromDescriptor(FileDescriptor fileDescriptor, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap b = null;
        int count = 0;
        while (b == null && count < MAX_TRIES) {
            try {
                b = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
            } catch (OutOfMemoryError e) {
                options.inSampleSize *= 2;
                b = null;
                Log.e(TAG, "Out of memory decoding bitmap from Descriptor", e);
            }
        }

        return b;
    }

    /**
     * Calculate an inSampleSize for use in a {@link BitmapFactory.Options} object when decoding
     * bitmaps using the decode* methods from {@link BitmapFactory}. This implementation calculates
     * the closest inSampleSize that will result in the final decoded bitmap having a width and
     * height equal to or larger than the requested width and height. This implementation does not
     * ensure a power of 2 is returned for inSampleSize which can be faster when decoding but
     * results in a larger bitmap which isn't as useful for caching purposes.
     * 
     * @param options
     *            An options object with out* params already populated (run through a decode* method
     *            with inJustDecodeBounds==true
     * @param reqWidth
     *            The requested width of the resulting bitmap
     * @param reqHeight
     *            The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    @TargetApi(14)
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger
            // inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down
            // further.
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }

        //@formatter:off
        // We want to support hardware accelerated views, but we're constrained by the OpenGL maximum texture size
        // The android framework team added getMaximumBitmapWidth() and getMaximumBitmapHeight() in API Level 14
        // so we take advantage of that here if necessary
        /**
         * Commenting this out for now as getMaximumBitmapHeight just returns 32766 in 4.1.1 :-\
         * http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/4.1.1_r1/android/graphics/Canvas.java#Canvas.getMaximumBitmapHeight%28%29
         * 
        if (Util.hasIcs()) {
            Canvas canvas = new Canvas();
            boolean resize = true;
            int maxHeight = canvas.getMaximumBitmapHeight();
            int maxWidth = canvas.getMaximumBitmapWidth();
            if (maxHeight == 32766)
                maxHeight = 2048;
            
            if (maxWidth == 32766)
                maxWidth = 2048;
            
            Log.i("MaximumBitmapHeight", "" + maxHeight);
            Log.i("MaximumBitmapWidth", "" + maxWidth);
            
            while (resize) {
                int newHeight = height / inSampleSize;
                int newWidth = width / inSampleSize;
                Log.i("newHeight", "" + newHeight);
                Log.i("newWidth", "" + newWidth);
                if (newHeight > maxHeight || newWidth > maxWidth) {
                    inSampleSize++;
                } else {
                    resize = false;
                }
            }
            
        }
         */
        //@formatter:on

        return inSampleSize;
    }
}
