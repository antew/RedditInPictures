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
package com.antew.redditinpictures.library.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import com.antew.redditinpictures.library.model.ImageSize;
import com.antew.redditinpictures.library.network.SynchronousNetworkApi;
import com.antew.redditinpictures.library.util.Ln;

public class GifView extends View {
    private Movie mMovie;
    private long  mMovieStart;
    private float mScale;

    public GifView(Context context) {
        super(context);
        setFocusable(true);
    }

    public GifView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
    }

    public void loadGif(String url) {
        Ln.i("Before trimming URL = %s", url);
        if (url.startsWith(ImageSize.ORIGINAL.name())) {
            url = url.replace(ImageSize.ORIGINAL.name(), "");
        }
        if (url.startsWith(ImageSize.SMALL_SQUARE.name())) {
            url = url.replace(ImageSize.SMALL_SQUARE.name(), "");
        }

        Ln.i("After trimming URL = %s", url);
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
            Ln.d("real time :: %d", relTime);
            Ln.i("Height = %d Width = %d", getMeasuredHeight(), getMeasuredWidth());
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
