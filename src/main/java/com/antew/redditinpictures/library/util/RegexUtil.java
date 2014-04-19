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
