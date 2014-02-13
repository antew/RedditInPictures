package com.antew.redditinpictures.library.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {
    
    public static String getMatch(String patternString, String url) {
        Pattern compiledPattern = null;
        String hash = null;
        
        if (RegexPatternCache.containsKey(patternString)) {
            compiledPattern = RegexPatternCache.getPattern(patternString);
        } else {
            compiledPattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
            RegexPatternCache.addPattern(patternString, compiledPattern);
        }
            
        Matcher m = compiledPattern.matcher(url);
        if (m.find()) {
            hash = m.group(1);
        }
        
        return hash;
    }
    
}
