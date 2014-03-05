package com.antew.redditinpictures.library;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;

public class RedditInPicturesApplication extends Application {
    private static RedditInPicturesApplication instance;

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

        // Perform injection
        Injector.init(getRootModule(), this);

    }

    private Object getRootModule() {
        return new RootModule();
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
