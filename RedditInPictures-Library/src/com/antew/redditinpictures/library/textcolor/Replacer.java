package com.antew.redditinpictures.library.textcolor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

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
