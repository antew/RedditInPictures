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
