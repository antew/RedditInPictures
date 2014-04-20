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
package com.antew.redditinpictures.library.util;

import java.util.HashMap;
import java.util.regex.Pattern;

public class RegexPatternCache {
    private RegexPatternCache() {}

    ;

    public static boolean containsKey(String patternString) {
        return getInstance().containsKey(patternString);
    }

    private static HashMap<String, Pattern> getInstance() {
        return RegexPatternCacheHolder.INSTANCE;
    }

    public static Pattern getPattern(String patternString) {
        if (getInstance().containsKey(patternString)) {
            return getInstance().get(patternString);
        }

        return null;
    }

    public static void addPattern(String patternString, Pattern compiledPattern) {
        getInstance().put(patternString, compiledPattern);
    }

    private static class RegexPatternCacheHolder {
        public static final HashMap<String, Pattern> INSTANCE = new HashMap<String, Pattern>();
    }
}
