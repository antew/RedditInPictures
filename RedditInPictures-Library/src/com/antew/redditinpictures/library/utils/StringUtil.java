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
    
    /**
     * Sanitizes the name as a valid fat32 filename. For simplicity, fat32
     * filename characters may be any combination of letters, digits, or
     * characters with code point values greater than 127. Replaces the invalid
     * characters with "_" and collapses multiple "_" together.
     *
     * This is based on code from the MyTracks application <a href="http://code.google.com/p/mytracks/">on Google Code</a>
     * @param name name
     */
    public static String sanitizeFileName(String name) {
      StringBuffer buffer = new StringBuffer(name.length());
      for (int i = 0; i < name.length(); i++) {
        int codePoint = name.codePointAt(i);
        char character = name.charAt(i);
        if (Character.isLetterOrDigit(character) || isSpecialFat32(character)) {
          buffer.appendCodePoint(codePoint);
        } else {
          buffer.append("_");
        }
      }
      
      String result = buffer.toString().replaceAll(" ", "_").replaceAll(",", "").replaceAll("\"", "").replaceAll("_+", "_");

      if (result.length() > 127)
          result = result.substring(0, 127);
      
      if (result.startsWith("_"))
          result = result.substring(1);
      
      if (result.endsWith("_"))
          result = result.substring(0, result.length() - 1);
      
      return result;
    }
    
    /**
     * Returns true if it is a special FAT32 character.
     * 
     * This is from the MyTracks application <a href="http://code.google.com/p/mytracks/">on Google Code</a>
     * @param character the character
     */
    private static boolean isSpecialFat32(char character) {
      switch (character) {
        case '$':
        case '%':
        case '\'':
        case '-':
        case '_':
        case '@':
        case '~':
        case '`':
        case '!':
        case '(':
        case ')':
        case '{':
        case '}':
        case '^':
        case '#':
        case '&':
        case '+':
        case ',':
        case ';':
        case '=':
        case '[':
        case ']':
        case ' ':
          return true;
        default:
          return false;
      }
    }
    
}
