package com.antew.redditinpictures.library;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import com.antew.redditinpictures.Modules;
import com.antew.redditinpictures.library.modules.RootModule;

public class RedditInPicturesApplication extends Application {
    private static RedditInPicturesApplication instance;

    /**
     * Create main application
     *
     * @param context
     */
    public RedditInPicturesApplication(final Context context) {
        this();
        attachBaseContext(context);
    }

    /**
     * Create main application
     */
    public RedditInPicturesApplication() {
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

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        Injector.init(new RootModule(this));
        Injector.init(Modules.get(this));
    }
}
