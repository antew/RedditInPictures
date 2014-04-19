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
package com.antew.redditinpictures;

import com.antew.redditinpictures.library.RedditInPicturesApplication;
import com.antew.redditinpictures.library.util.Ln;

public class Modules {

    public static Object get(RedditInPicturesApplication app) {
        Ln.e("Called free 'Modules' class");
        return new ApplicationModuleFree();
    }
}
