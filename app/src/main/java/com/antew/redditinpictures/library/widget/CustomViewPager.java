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
package com.antew.redditinpictures.library.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.antew.redditinpictures.pro.R;

public class CustomViewPager extends ViewPager {
    private boolean mEnabled;

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPageMargin((int) getResources().getDimension(R.dimen.pager_margin));
        this.mEnabled = true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mEnabled) {
            try {
                return super.onInterceptTouchEvent(event);
            } catch (final Exception e) {
                // This is wrapped in a try/catch because this would error when more than 2 fingers were down at the same time

                // java.lang.IllegalArgumentException: pointerIndex out of range
                // at android.view.MotionEvent.nativeGetAxisValue(Native Method)
                // at android.view.MotionEvent.getX(MotionEvent.java:1981)
                // at android.support.v4.view.MotionEventCompatEclair.getX(MotionEventCompatEclair.java:32)
                // at android.support.v4.view.MotionEventCompat$EclairMotionEventVersionImpl.getX(MotionEventCompat.java:86)
                // at android.support.v4.view.MotionEventCompat.getX(MotionEventCompat.java:184)
                // at android.support.v4.view.ViewPager.onInterceptTouchEvent(ViewPager.java:1339)

            }
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mEnabled) {
            return super.onTouchEvent(event);
        }

        return false;
    }

    public void toggleSwipingEnabled() {
        mEnabled = !mEnabled;
        this.requestDisallowInterceptTouchEvent(mEnabled);
    }

    public boolean isSwipingEnabled() {
        return mEnabled;
    }
}
