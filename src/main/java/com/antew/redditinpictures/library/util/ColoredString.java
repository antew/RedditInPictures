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

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColoredString {
    SpannableStringBuilder builder;

    public ColoredString() {
        builder = new SpannableStringBuilder();
    }

    public ColoredString(String text, int color) {
        builder = new SpannableStringBuilder(text);
        builder.setSpan(new ForegroundColorSpan(color), 0, text.length(), 0);
    }

    public ColoredString append(String text) {
        builder.append(text);
        return this;
    }

    public ColoredString append(String text, int color) {
        SpannableString s = new SpannableString(text);
        s.setSpan(new ForegroundColorSpan(color), 0, text.length(), 0);
        builder.append(s);
        return this;
    }

    public ColoredString append(String text, String regex, int color) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        builder.append(doReplace(text, matcher, color));
        return this;
    }

    public SpannableStringBuilder getText() {
        return builder;
    }

    private static CharSequence doReplace(CharSequence mSource, Matcher mMatcher, int color) {

        int mAppendPosition = 0;
        SpannableStringBuilder buffer = new SpannableStringBuilder();
        while (mMatcher.find()) {
            buffer.append(mSource.subSequence(mAppendPosition, mMatcher.start()));

            SpannableString text = new SpannableString(mMatcher.group());
            text.setSpan(new ForegroundColorSpan(color), 0, text.length(), 0);
            buffer.append(text);

            mAppendPosition = mMatcher.end();
        }
        buffer.append(mSource.subSequence(mAppendPosition, mSource.length()));

        return buffer;
    }

}
