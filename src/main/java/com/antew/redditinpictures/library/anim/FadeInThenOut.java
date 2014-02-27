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
package com.antew.redditinpictures.library.anim;

import android.view.View;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

/**
 * Utility class to simplify fading views in and out with NineOldAndroids
 * @author Antew
 *
 */
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