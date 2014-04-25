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
package com.antew.redditinpictures.library.service;

import android.content.Context;
import com.antew.redditinpictures.library.model.gfycat.GfycatImage;
import com.antew.redditinpictures.library.util.AndroidUtil;
import com.antew.redditinpictures.library.util.Ln;
import com.antew.redditinpictures.library.util.Strings;
import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GfycatService {

    public static GfycatImage convertGif(Context context, String imageUrl) throws MalformedURLException {
        OkHttpClient client = new OkHttpClient();
        URL url = new URL("http://upload.gfycat.com/transcode/redditinpics1?fetchUrl=" + imageUrl);
        HttpURLConnection connection = client.open(url);
        InputStream in = null;
        BufferedReader reader = null;
        try {
            in = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(in));

            Gson gson = new Gson();
            GfycatImage image = gson.fromJson(reader, GfycatImage.class);
            if (Strings.notEmpty(image.getError())) {
                Ln.e("Failed to get Image %s", image.getError());
                return null;
            }
            return image;
        } catch (IOException e) {
            Ln.e(e);
        } finally {
            AndroidUtil.closeQuietly(in);
            AndroidUtil.closeQuietly(reader);
        }
        return null;
    }
}
