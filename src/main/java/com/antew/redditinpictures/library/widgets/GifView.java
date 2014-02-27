package com.antew.redditinpictures.library.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.util.AttributeSet;
import android.view.View;

import com.antew.redditinpictures.library.enums.ImageSize;
import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.network.SynchronousNetworkApi;
import com.antew.redditinpictures.library.utils.AsyncTask;

public class GifView extends View {
    public static final String TAG                     = GifView.class.getSimpleName();
    private Movie              mMovie;
    private long               mMovieStart;
    private float              mScale;

    public GifView(Context context) {
        super(context);
        setFocusable(true);

    }

    public GifView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);

    }


    public void loadGif(String url) {
        Log.i(TAG, "Before trimming URL = " + url);
        if (url.startsWith(ImageSize.ORIGINAL.name()))
            url = url.replace(ImageSize.ORIGINAL.name(), "");
        if (url.startsWith(ImageSize.SMALL_SQUARE.name()))
            url = url.replace(ImageSize.SMALL_SQUARE.name(), "");

        Log.i(TAG, "After trimming URL = " + url);
        new DownloadImageTask().execute(url);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        long now = android.os.SystemClock.uptimeMillis();
        if (mMovieStart == 0) { // first time
            mMovieStart = now;
        }
        if (mMovie != null) {
            int dur = mMovie.duration();
            if (dur == 0) {
                dur = 3000;
            }
            int relTime = (int) ((now - mMovieStart) % dur);
            Log.d("", "real time :: " + relTime);
            Log.i(TAG, "Height = " + getMeasuredHeight() + " Width = " + getMeasuredWidth());
            mMovie.setTime(relTime);
            mMovie.draw(canvas, getWidth() - mMovie.width(), getHeight() - mMovie.height());
            invalidate();
        }
    }
    
    class DownloadImageTask extends AsyncTask<String, Void, byte[]> {
        private String data;

        public DownloadImageTask() {}

        /**
         * Background processing.
         */
        @Override
        protected byte[] doInBackground(String... params) {
            data = params[0];
            byte[] gif = SynchronousNetworkApi.downloadUrlToByteArray(data);
            return gif;
        }

        /**
         * Once we're resolved the URL, pass it to our load function to download/display it
         */
        @Override
        protected void onPostExecute(byte[] result) {
            mMovie = Movie.decodeByteArray(result, 0, result.length);
            invalidate();

        }

    }
}
