package com.antew.redditinpictures.library.util;

import android.view.View;

public class ViewUtil {
    public static void toggleVisibility(View view) {
        switch (view.getVisibility()) {
            case View.VISIBLE:
                view.setVisibility(View.GONE);
                break;
            // Fall through
            case View.INVISIBLE:
            case View.GONE:
                view.setVisibility(View.VISIBLE);
                break;
        }
    }
}
