package com.antew.redditinpictures.library.utils;

import java.util.Comparator;
import java.util.Scanner;

public class StringUtil {
    public static Comparator<String> getCaseInsensitiveComparator() {
        return new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareToIgnoreCase(rhs);
            }
        };
    }
    
    public static String convertStreamToString(java.io.InputStream is) {
        Scanner scanner = new Scanner(is, "UTF-8").useDelimiter("\\A");
        if (scanner.hasNext())
            return scanner.next();
        
        return "";
    }
}
