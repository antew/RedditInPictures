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
