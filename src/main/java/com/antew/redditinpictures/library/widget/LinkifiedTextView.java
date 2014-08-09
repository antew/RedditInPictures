package com.antew.redditinpictures.library.widget;

import android.content.Context;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * This a slightly modified version of Michael Evan's post on how he linkified text in a ListView
 * while maintaining the ability for the user to click a full row as well, which was in turn inspired by
 * weakwire on a StackOverflow post.
 *
 * @see <a href="http://www.michaelevans.org/blog/2013/03/29/clickable-links-in-android-listviews">michaelevans.org</a>
 * @see <a href="http://stackoverflow.com/questions/7236840/android-textview-linkify-intercepts-with-parent-view-gestures/7327332#7327332">weakwire's post on StackOverflow</a>
 */
public class LinkifiedTextView extends TextView {
    public LinkifiedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        TextView widget = this;
        Object text = widget.getText();
        if (text instanceof Spanned) {
            Spanned buffer = (Spanned) text;

            int action = event.getAction();

            if (action == MotionEvent.ACTION_UP
                    || action == MotionEvent.ACTION_DOWN) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();

                x += widget.getScrollX();
                y += widget.getScrollY();

                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);

                ClickableSpan[] link = buffer.getSpans(off, off,
                        ClickableSpan.class);

                if (link.length != 0) {
                    if (action == MotionEvent.ACTION_UP) {
                        link[0].onClick(widget);
                    } else if (action == MotionEvent.ACTION_DOWN) {
                        Selection.setSelection(new SpannableString(buffer),
                                buffer.getSpanStart(link[0]),
                                buffer.getSpanEnd(link[0]));
                    }
                    return true;
                }
            }

        }

        return false;
    }
}
