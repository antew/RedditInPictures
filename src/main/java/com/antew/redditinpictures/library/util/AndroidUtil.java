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

import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import com.antew.redditinpictures.library.ui.ImageDetailActivity;
import com.antew.redditinpictures.library.ui.ImgurAlbumActivity;
import com.antew.redditinpictures.library.ui.RedditFragmentActivity;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;

public class AndroidUtil {
    public static final String TAG = AndroidUtil.class.getSimpleName();

    private AndroidUtil() {
    }

    ;

    public static boolean isUserAMonkey() {
        boolean isUserAMonkey = false;
        try {
            Class<?> pointClass = Class.forName("android.app.ActivityManager");
            Method monkey = pointClass.getMethod("isUserAMonkey");
            // no exception, so new method is available, just use it
            isUserAMonkey = (Boolean) monkey.invoke(pointClass);
        } catch (Exception e) {
            // Do nothing
        }
        return isUserAMonkey;
    }

    public static int dpToPx(Context context, int dp) {
        return (int) (dp * (context.getResources().getDisplayMetrics().densityDpi / 160f) + 0.5f);
    }

    public static int pxToDp(Context context, int px) {
        return (int) (px / (context.getResources().getDisplayMetrics().densityDpi / 160f) + 0.5f);
    }

    public static void enableStrictMode() {
        StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog();
        StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder().detectAll().penaltyLog();

        threadPolicyBuilder.penaltyFlashScreen();
        vmPolicyBuilder.setClassInstanceLimit(RedditFragmentActivity.class, 1)
                       .setClassInstanceLimit(ImageDetailActivity.class, 1)
                       .setClassInstanceLimit(ImgurAlbumActivity.class, 1);

        StrictMode.setThreadPolicy(threadPolicyBuilder.build());
        StrictMode.setVmPolicy(vmPolicyBuilder.build());
    }

    public static boolean hasIcsMr1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static byte parcelBoolean(boolean val) {
        return (byte) (val ? 1 : 0);
    }

    public static boolean isSplitActionBar(Context context) {
        //TODO: Fix this.
        return true;
        //return ResourcesCompat.getResources_getBoolean(context, R.bool.abs__split_action_bar_is_narrow);
    }

    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }
}
