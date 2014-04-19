package com.antew.redditinpictures.library.util;

import android.text.Html;
import android.widget.TextView;
import com.commonsware.cwac.anddown.AndDown;

public class MarkdownUtil {
    public static void setMarkdownText(final TextView target, final String markdownText) {
        if (target == null) {
            return;
        }

        // Converting this to markdown could take an unknown amount of time, so let's load the text in first and then load the markdown in after it is processed.
        target.setText(markdownText);

        // No need to process it if there isn't anything.
        if (Strings.notEmpty(markdownText)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    AndDown markdownProcessor = new AndDown();
                    final String processedText = markdownProcessor.markdownToHtml(markdownText);
                    target.setText(Html.fromHtml(processedText));
                }
            }) {}.start();
        }
    }
}
