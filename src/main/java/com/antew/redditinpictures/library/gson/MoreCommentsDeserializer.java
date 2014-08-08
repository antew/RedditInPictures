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
package com.antew.redditinpictures.library.gson;

import com.antew.redditinpictures.library.model.reddit.Child;
import com.antew.redditinpictures.library.model.reddit.MoreComments;
import com.google.common.reflect.TypeToken;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.List;

public class MoreCommentsDeserializer implements JsonDeserializer<MoreComments> {

    @Override
    public MoreComments deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonArray listofThings = ((JsonObject) element).getAsJsonObject("json").getAsJsonObject("data").getAsJsonArray("things");
        Type childType = new TypeToken<List<Child>>(){}.getType();
        return new MoreComments(context.deserialize(listofThings, childType));
    }
}
