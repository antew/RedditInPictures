package com.antew.redditinpictures.library.modules;

import android.app.Application;

import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.json.JsonDeserializer;
import com.antew.redditinpictures.library.service.AcceptJsonInterceptor;
import com.antew.redditinpictures.library.service.RedditServiceRetrofit;
import com.antew.redditinpictures.library.util.Ln;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.io.IOException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.Endpoint;
import retrofit.Endpoints;
import retrofit.RestAdapter;
import retrofit.client.Client;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

@Module(library = true, complete = false)
public class NetworkModule {
    private static final String REDDIT_URL = "http://www.reddit.com";
    private static final int DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50MB
    private Application application = null;

    public NetworkModule() {}

    public NetworkModule(Application application) {
        this.application = application;
    }
    @Provides
    @Singleton
    Endpoint provideImgurEndpoint() {
        return Endpoints.newFixedEndpoint(REDDIT_URL);
    }

    @Provides
    @Singleton
    Client provideOkHttpClient(OkHttpClient client) {
        return new OkClient(client);
    }

    @Provides
    @Singleton
    RestAdapter provideRestAdapter(Endpoint endpoint, Client client) {
        return new RestAdapter.Builder()
                .setClient(client)
                .setEndpoint(endpoint)
                .setRequestInterceptor(new AcceptJsonInterceptor())
                .setConverter(new GsonConverter(JsonDeserializer.getGson()))
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();
    }

    @Provides
    @Singleton
    RedditServiceRetrofit provideRedditService(RestAdapter restAdapter) {
        return restAdapter.create(RedditServiceRetrofit.class);
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

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(Application app) {
        return createOkHttpClient(app);
    }
}
