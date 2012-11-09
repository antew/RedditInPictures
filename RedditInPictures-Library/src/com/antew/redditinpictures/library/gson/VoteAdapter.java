package com.antew.redditinpictures.library.gson;

import java.io.IOException;

import com.antew.redditinpictures.library.reddit.RedditApiManager.Vote;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class VoteAdapter extends TypeAdapter<Vote>  {

    @Override
    public Vote read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return Vote.NEUTRAL;
        }
        
        boolean b = reader.nextBoolean();
        if (b)
            return Vote.UP;
        else
            return Vote.DOWN;
    }

    @Override
    public void write(JsonWriter writer, Vote vote) throws IOException {
        if (vote == null || vote.equals(Vote.NEUTRAL)) {
            writer.nullValue();
        }
        else if (vote.equals(Vote.UP))
            writer.value(true);
        else
            writer.value(false);
    }

}
