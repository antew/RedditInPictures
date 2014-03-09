package com.antew.redditinpictures.library;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;

import com.antew.redditinpictures.Modules;

import dagger.ObjectGraph;

public class RedditInPicturesApplication extends Application {
    private static RedditInPicturesApplication instance;
    private ObjectGraph applicationGraph;

    /**
     * Create main application
     */
    public RedditInPicturesApplication() {
    }

    /**
     * Create main application
     *
     * @param context
     */
    public RedditInPicturesApplication(final Context context) {
        this();
        attachBaseContext(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        applicationGraph = ObjectGraph.create(Modules.get(this));
    }

    public ObjectGraph getApplicationGraph() {
        return applicationGraph;
    }

    /**
     * Create main application
     *
     * @param instrumentation
     */
    public RedditInPicturesApplication(final Instrumentation instrumentation) {
        this();
        attachBaseContext(instrumentation.getTargetContext());
    }

    public static RedditInPicturesApplication getInstance() {
        return instance;
    }
}
