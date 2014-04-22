package com.antew.redditinpictures.library.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.AttributeSet;
import com.antew.redditinpictures.library.network.SynchronousNetworkApi;
import com.antew.redditinpictures.library.util.Ln;
import com.antew.redditinpictures.library.util.SafeAsyncTask;
import com.antew.redditinpictures.library.util.Strings;
import uk.co.senab.photoview.PhotoView;

public class GifPhotoView extends PhotoView implements Runnable {
    private static final String TAG = "GifDecoderView";
    private GifDecoder gifDecoder;
    private Bitmap     tmpBitmap;
    private final Handler handler   = new Handler();
    private       boolean animating = false;
    private Thread animationThread;
    private final Runnable updateResults = new Runnable() {
        @Override
        public void run() {
            if (tmpBitmap != null && !tmpBitmap.isRecycled()) {
                setImageBitmap(tmpBitmap);
            }
        }
    };

    public GifPhotoView(Context context) {
        super(context);
    }

    public GifPhotoView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public GifPhotoView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
    }

    @Override public void run() {
        final int n = gifDecoder.getFrameCount();
        do {
            for (int i = 0; i < n; i++) {
                try {
                    tmpBitmap = gifDecoder.getNextFrame();
                    handler.post(updateResults);
                } catch (final ArrayIndexOutOfBoundsException e) {
                    Ln.e(TAG, e);
                } catch (final IllegalArgumentException e) {
                    Ln.e(TAG, e);
                }
                gifDecoder.advance();
                try {
                    Thread.sleep(gifDecoder.getNextDelay());
                } catch (final InterruptedException e) {
                    // suppress
                }
            }
        } while (animating);
    }

    public void setBytes(final byte[] bytes) {
        gifDecoder = new GifDecoder();
        try {
            gifDecoder.read(bytes);
        } catch (final OutOfMemoryError e) {
            gifDecoder = null;
            return;
        }

        if (canStart()) {
            animationThread = new Thread(this);
            animationThread.start();
        }
    }

    public void loadGif(String url) {
        new DownloadImageTask(url).execute();
    }

    public void startAnimation() {
        animating = true;

        if (canStart()) {
            animationThread = new Thread(this);
            animationThread.start();
        }
    }

    public void stopAnimation() {
        animating = false;

        if (animationThread != null) {
            animationThread.interrupt();
            animationThread = null;
        }
    }

    private boolean canStart() {
        return animating && gifDecoder != null && animationThread == null;
    }

    @Override protected void onDetachedFromWindow() {
        stopAnimation();
        super.onDetachedFromWindow();
    }

    private class DownloadImageTask extends SafeAsyncTask<byte[]> {
        String mUrl;

        public DownloadImageTask(String url) {
            mUrl = url;
        }

        @Override
        public byte[] call() throws Exception {
            if (Strings.isEmpty(mUrl)) {
                Ln.e("DownloadImageTask - Invalid Arguments, URL and filename must not be null or empty: "
                     + "filename = "
                     + ", mUrl = "
                     + Strings.toString(mUrl));

                return null;
            }

            return SynchronousNetworkApi.downloadUrlToByteArray(mUrl);
        }

        @Override protected void onSuccess(byte[] result) throws Exception {
            setBytes(result);
            startAnimation();
        }
    }
}
