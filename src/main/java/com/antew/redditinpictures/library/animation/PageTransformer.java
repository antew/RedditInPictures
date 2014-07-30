package com.antew.redditinpictures.library.animation;

import android.support.v4.view.ViewPager;
import android.view.View;

import com.antew.redditinpictures.library.util.Ln;

public class PageTransformer implements ViewPager.PageTransformer {

    private int[] ids;
    private float speed = 0.60f;

    public PageTransformer(int... ids) {
        this.ids = ids;
    }

    @Override
    public void transformPage(View view, float position) {
        // Only animate the nearest neighbors to the current page
        if (position < -1 || position > 1) {
            return;
        }

        // Loop through the input view ids and fine the first visible
        // view, that is what we will apply the animation to
        View transformedView = null;
        for (int viewId : ids) {
            transformedView = view.findViewById(viewId);
            if (transformedView != null && transformedView.getVisibility() == View.VISIBLE) {
                break;
            }
        }

        // Can't animate a view that doesn't exist
        if (transformedView == null) {
            return;
        }

        transformedView.setTranslationX(-1 * position * transformedView.getWidth() * speed);
    }

}