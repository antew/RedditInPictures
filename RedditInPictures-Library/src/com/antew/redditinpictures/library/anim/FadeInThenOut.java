package com.antew.redditinpictures.library.anim;

import android.view.View;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

public class FadeInThenOut {

    private static void fade(final View view, final int visibility, int duration) {
        //@formatter:off
        ViewPropertyAnimator.animate(view)
                            .alpha(0)
                            .setStartDelay(250)
                            .setDuration(duration / 2)
                            .setListener(new Animator.AnimatorListener() {
                                @Override public void onAnimationStart(Animator animator) {}
                                @Override public void onAnimationCancel(Animator animator) {}
                                @Override public void onAnimationRepeat(Animator animator) {}
                    
                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    view.setVisibility(visibility);
                                }
                            });
        //@formatter:on
    }

    public static void fadeInThenOut(final View view, final int duration) {
        //@formatter:off
        ViewHelper.setAlpha(view, 0);
        view.setVisibility(View.VISIBLE);
        ViewPropertyAnimator.animate(view)
                            .alpha(1f)
                            .setDuration(duration / 2)
                            .setListener(new AnimatorListener() {
                                @Override public void onAnimationStart(Animator animation) {}
                                @Override public void onAnimationRepeat(Animator animation) {}
                                @Override public void onAnimationCancel(Animator animation) {}
                                
                                @Override 
                                public void onAnimationEnd(Animator animation) {
                                    fade(view, View.GONE, duration);
                                }
                            });
        //@formatter:on
    }
}