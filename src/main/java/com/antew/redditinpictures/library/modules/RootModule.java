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
package com.antew.redditinpictures.library.modules;

import android.accounts.AccountManager;
import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;

import com.antew.redditinpictures.library.util.Ln;
import com.squareup.okhttp.Cache;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.inputmethod.InputMethodManager;
import com.antew.redditinpictures.device.ScreenSize;
import com.antew.redditinpictures.library.RedditInPicturesApplication;
import com.antew.redditinpictures.library.annotation.ForApplication;
import com.antew.redditinpictures.library.service.ImgurAuthenticationInterceptor;
import com.antew.redditinpictures.library.service.ImgurServiceRetrofit;
import com.antew.redditinpictures.library.util.ImageDownloader;
import com.antew.redditinpictures.library.util.MainThreadBus;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.otto.Bus;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

import dagger.Module;
import dagger.Provides;
import retrofit.Endpoint;
import retrofit.Endpoints;
import retrofit.RestAdapter;
import retrofit.client.Client;
import retrofit.client.OkClient;

import javax.inject.Singleton;

/**
 * Module for all Android related provisions
 */
@Module(library = true)
public class RootModule {
    private static final String IMGUR_API_URL = "https://api.imgur.com/3/";
    private static final int DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50MB
    private final RedditInPicturesApplication application;

    public RootModule(RedditInPicturesApplication application) {
        this.application = application;
    }

    @Provides
    @Singleton
    Application provideApplication() { return application; }

    @Provides
    @Singleton
    @ForApplication
    Context provideApplicationContext() {
        return application.getApplicationContext();
    }

    @Provides
    @ForApplication
    SharedPreferences provideDefaultSharedPreferences(@ForApplication final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Provides
    @ForApplication
    PackageInfo providePackageInfo(@ForApplication Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Provides
    @ForApplication
    TelephonyManager provideTelephonyManager(@ForApplication Context context) {
        return getSystemService(context, Context.TELEPHONY_SERVICE);
    }

    @SuppressWarnings("unchecked")
    public <T> T getSystemService(Context context, String serviceConstant) {
        return (T) context.getSystemService(serviceConstant);
    }

    @Provides
    @ForApplication
    InputMethodManager provideInputMethodManager(@ForApplication Context context) {
        return (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Provides
    @ForApplication
    ApplicationInfo provideApplicationInfo(@ForApplication final Context context) {
        return context.getApplicationInfo();
    }

    @Provides
    @ForApplication
    AccountManager provideAccountManager(@ForApplication final Context context) {
        return AccountManager.get(context);
    }

    @Provides
    @ForApplication
    ClassLoader provideClassLoader(@ForApplication final Context context) {
        return context.getClassLoader();
    }

    @Provides
    @ForApplication
    NotificationManager provideNotificationManager(@ForApplication final Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Provides
    @ForApplication
    ConnectivityManager provideConnectivityManager(@ForApplication final Context context) {
        return (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Provides
    @Singleton
    Bus provideOttoBus() {
        return new MainThreadBus(new Bus());
    }

    @Provides
    @Singleton
    ScreenSize provideScreenSize(@ForApplication final Context context) {
        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return new ScreenSize(displayMetrics.widthPixels, displayMetrics.heightPixels);
    }

    @Provides
    @Singleton
    @ForApplication
    ImageDownloader provideImageDownloader(@ForApplication final Context context) {
        return new ImageDownloader(context);
    }

    @Provides
    @Singleton
    Endpoint provideImgurEndpoint() {
        return Endpoints.newFixedEndpoint(IMGUR_API_URL);
    }

    @Provides
    @Singleton
    Client provideOkHttpClient(OkHttpClient client) {
        return new OkClient(client);
    }

    @Provides
    @Singleton
    RestAdapter provideRestAdapter(Endpoint endpoint, Client client, ImgurAuthenticationInterceptor interceptor) {
        return new RestAdapter.Builder()
                              .setClient(client)
                              .setEndpoint(endpoint)
                              .setRequestInterceptor(interceptor)
                              .setLogLevel(RestAdapter.LogLevel.FULL)
                              .build();
    }

    @Provides
    @Singleton
    ImgurServiceRetrofit provideImgurService(RestAdapter restAdapter) {
        return restAdapter.create(ImgurServiceRetrofit.class);
    }

    static OkHttpClient createOkHttpClient(Application app) {
        OkHttpClient client = new OkHttpClient();

        // Install an HTTP cache in the application cache directory.
        try {
            File cacheDir = new File(app.getCacheDir(), "http");
            Cache cache = new Cache(cacheDir, DISK_CACHE_SIZE);
            client.setCache(cache);
        } catch (IOException e) {
            Ln.e(e, "Unable to install disk cache.");
        }

        return client;
    }

    @Provides @Singleton OkHttpClient provideOkHttpClient(Application app) {
        return createOkHttpClient(app);
    }

    @Provides @Singleton
    Picasso providePicasso(Application app, OkHttpClient client) {
        return new Picasso.Builder(app)
                          .downloader(new OkHttpDownloader(client))
                          .listener(new Picasso.Listener() {
                              @Override
                              public void onImageLoadFailed(Picasso picasso, Uri uri, Exception e) {
                                  Ln.e(e, "Failed to load image: %s", uri);
                              }
                          }).build();
    }
}
