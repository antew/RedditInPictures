package com.antew.redditinpictures.library.json;

import com.antew.redditinpictures.library.enums.Vote;
import com.antew.redditinpictures.library.gson.VoteAdapter;
import com.antew.redditinpictures.library.logging.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class JsonDeserializer {
    public static final String TAG = JsonDeserializer.class.getSimpleName();
    private static Gson gson;
    
    public static <T> T deserialize(String json, Class<T> clazz) {
        try {
            return getGson().fromJson(json, clazz);
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "deserialize - Error parsing JSON!", e);
        } catch (IllegalStateException e) {
            Log.e(TAG, "deserialize - Error parsing JSON!", e);
        }
        
        return null;
    }
    
    public static Gson getGson () {
        if (gson == null) {
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(Vote.class, new VoteAdapter());
            builder.serializeNulls();
            gson = builder.create();
        }
        
        return gson;
    }
}
