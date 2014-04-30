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
package com.antew.redditinpictures.util;

import android.content.Context;
import android.content.res.TypedArray;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.pro.R;

public class ConstsFree extends Constants {
    public static final String ADMOB_ID            = "a14fd4522953241";
    public static final String DIALOG_UPGRADE      = "Upgrade";
    public static final String MARKET_INTENT       = "market://details?id=";
    public static final String REMOVE_ADS          = "removeAds";
    public static final String PRO_VERSION_PACKAGE = "com.antew.redditinpictures.pro";
    private static Integer mActionBarSize;

    public static int getActionBarSize(Context context) {
        if (mActionBarSize == null) {
            mActionBarSize = context.getResources().getDimensionPixelSize(R.dimen.abs__action_bar_default_height);
        }
        return mActionBarSize;
    }
}
