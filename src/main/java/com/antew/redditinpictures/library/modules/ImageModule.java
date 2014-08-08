package com.antew.redditinpictures.library.modules;

import android.app.Application;

import com.android.animation.ArgbEvaluator;
import com.antew.redditinpictures.library.util.Ln;
import com.bumptech.glide.Glide;
import com.nhaarman.listviewanimations.swinginadapters.prepared.AlphaInAnimationAdapter;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(library = true, complete = false)
public class ImageModule {

    @Provides
    @Singleton
    Picasso providePicasso(Application app, OkHttpClient client) {
        return new Picasso.Builder(app)
                .downloader(new OkHttpDownloader(client))
                .listener((picasso, uri, e) -> Ln.e(e, "Failed to load image: %s", uri))
                .build();
    }

    @Provides
    @Singleton
    Glide provideGlide(Application app) {
        return Glide.get(app);
    }

    @Provides
    @Singleton
    ArgbEvaluator provideArgbEvaluator() {
        return new ArgbEvaluator();
    }

}
