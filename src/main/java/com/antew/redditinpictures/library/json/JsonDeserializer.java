package com.antew.redditinpictures.library.json;

import com.antew.redditinpictures.library.enums.Vote;
import com.antew.redditinpictures.library.gson.VoteAdapter;
import com.antew.redditinpictures.library.utils.Ln;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
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
        if (gson == null) {
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(Vote.class, new VoteAdapter());
            builder.serializeNulls();
            gson = builder.create();
        }

        return gson;
    }
}
