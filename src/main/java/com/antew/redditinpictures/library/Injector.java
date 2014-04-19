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

import dagger.ObjectGraph;

public final class Injector {

    private static ObjectGraph objectGraph = null;

    public static void init(final Object rootModule) {

        if (objectGraph == null) {
            objectGraph = ObjectGraph.create(rootModule);
        } else {
            objectGraph = objectGraph.plus(rootModule);
        }

        // Inject statics
        objectGraph.injectStatics();

    }

    public static void init(final Object rootModule, final Object target) {
        init(rootModule);
        inject(target);
    }

    public static void inject(final Object target) {
        objectGraph.inject(target);
    }

    public static <T> T resolve(Class<T> type) {
        return objectGraph.get(type);
    }
}
