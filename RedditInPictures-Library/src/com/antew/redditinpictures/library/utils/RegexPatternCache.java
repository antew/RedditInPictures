package com.antew.redditinpictures.library.utils;

import java.util.HashMap;
import java.util.regex.Pattern;

public class RegexPatternCache {
    private RegexPatternCache() {};
    
    private static class RegexPatternCacheHolder {
        public static final HashMap<String, Pattern> INSTANCE = new HashMap<String, Pattern>();
    }
    
    private static HashMap<String, Pattern> getInstance() {
        return RegexPatternCacheHolder.INSTANCE;
    }
    
    public static boolean containsKey(String patternString) {
        return getInstance().containsKey(patternString);
    }
    
    public static Pattern getPattern(String patternString) {
        if (getInstance().containsKey(patternString))
            return getInstance().get(patternString);
        
        return null;
    }
    
    public static void addPattern(String patternString, Pattern compiledPattern) {
        getInstance().put(patternString, compiledPattern);
    }
}
