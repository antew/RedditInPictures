package com.antew.redditinpictures.library.animation;

import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * This is from Cyril Mottier's excellent article on the Prixing app, check it out
 * - http://cyrilmottier.com/2012/06/08/the-making-of-prixing-3-polishing-the-sliding-app-menu/
 */
public class PeekInterpolator implements Interpolator {
    private Interpolator mDecelerateInterpolator = new DecelerateInterpolator();
    private Interpolator mBounceInterpolator = new BounceInterpolator();

    @Override
    public float getInterpolation(float t) {
        // The purpose of this is pretty simple :
        // - mDecelerateInterpolator for t < 0.33
        // - mBounceInterpolator for t >= 0.33
        if (t < 0.33) {
            return mDecelerateInterpolator.getInterpolation(t / 0.33f);
        } else {
            return 1f - mBounceInterpolator.getInterpolation((t - 0.33f) / 0.67f);
        }
    }
}
