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
package com.antew.redditinpictures.library.textcolor;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Replacer {
    public enum TextColor {
        BLUE(0xFF0C5DA5), YELLOW(0xFFFFC200), ORANGE(0xFFBF5D30);

        private int mColor;

        TextColor(int color) {
            mColor = color;
        }

        int getColor() {
            return mColor;
        }
    }

    public static CharSequence replace(CharSequence source, String regex, TextColor color) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(source);
        return doReplace(source, matcher, color);
    }

    private static CharSequence doReplace(CharSequence mSource, Matcher mMatcher, TextColor mColor) {

        int mAppendPosition = 0;
        SpannableStringBuilder buffer = new SpannableStringBuilder();
        while (mMatcher.find()) {
            buffer.append(mSource.subSequence(mAppendPosition, mMatcher.start()));

            SpannableString text = new SpannableString(mMatcher.group());
            text.setSpan(new ForegroundColorSpan(mColor.getColor()), 0, text.length(), 0);
            buffer.append(text);

            mAppendPosition = mMatcher.end();
        }
        buffer.append(mSource.subSequence(mAppendPosition, mSource.length()));

        return buffer;
    }

}
