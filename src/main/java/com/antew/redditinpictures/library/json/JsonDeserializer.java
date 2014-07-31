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
package com.antew.redditinpictures.library.json;

import com.antew.redditinpictures.library.gson.RedditApiDeserializer;
import com.antew.redditinpictures.library.gson.VoteAdapter;
import com.antew.redditinpictures.library.model.Vote;
import com.antew.redditinpictures.library.model.reddit.Child;
import com.antew.redditinpictures.library.model.reddit.Comment;
import com.antew.redditinpictures.library.model.reddit.JsonRedditApi;
import com.antew.redditinpictures.library.model.reddit.MoreChild;
import com.antew.redditinpictures.library.model.reddit.PostChild;
import com.antew.redditinpictures.library.model.reddit.RedditApi;
import com.antew.redditinpictures.library.util.Ln;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

import java.io.Reader;

public class JsonDeserializer {
    private static Gson gson;

    public static <T> T deserialize(Reader json, Class<T> clazz) {
        try {
            return getGson().fromJson(json, clazz);
        } catch (JsonSyntaxException e) {
            Ln.e(e, "deserialize - Error parsing JSON!");
        } catch (IllegalStateException e) {
            Ln.e(e, "deserialize - Error parsing JSON!");
        }

        return null;
    }

    public static <T> T deserialize(String json, Class<T> clazz) {
        try {
            return getGson().fromJson(json, clazz);
        } catch (JsonSyntaxException e) {
            Ln.e(e, "deserialize - Error parsing JSON!");
        } catch (IllegalStateException e) {
            Ln.e(e, "deserialize - Error parsing JSON!");
        }

        return null;
    }

    public static Gson getGson() {
        RuntimeTypeAdapterFactory<Child> adapter = RuntimeTypeAdapterFactory.of(Child.class, "kind")
                .registerSubtype(PostChild.class, "t3")
                .registerSubtype(Comment.class, "t1")
                .registerSubtype(MoreChild.class, "more");

        if (gson == null) {
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(Vote.class, new VoteAdapter());
            builder.registerTypeAdapter(RedditApi.class, new RedditApiDeserializer());
            builder.registerTypeAdapterFactory(adapter);
            builder.serializeNulls();
            gson = builder.create();
        }

        return gson;
    }
}
