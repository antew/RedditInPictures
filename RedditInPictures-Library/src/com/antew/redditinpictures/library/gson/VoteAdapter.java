/*
 * Copyright (C) 2012 Antew | antewcode@gmail.com
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

import com.antew.redditinpictures.library.enums.Vote;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * This is used to serialize/deserialize Votes on Reddit posts Reddit uses true for an upvote, false
 * for downvotes, and null for neutral votes
 * 
 * @author Antew
 * 
 */
public class VoteAdapter extends TypeAdapter<Vote> {

    @Override
    public Vote read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return Vote.NEUTRAL;
        }

        return reader.nextBoolean() ? Vote.UP : Vote.DOWN;
    }

    @Override
    public void write(JsonWriter writer, Vote vote) throws IOException {
        if (vote == null || vote.equals(Vote.NEUTRAL)) {
            writer.nullValue();
        } else if (vote.equals(Vote.UP))
            writer.value(true);
        else
            writer.value(false);
    }

}
