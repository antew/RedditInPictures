package com.antew.redditinpictures.util;

import android.content.Context;
import android.content.res.TypedArray;

import com.antew.redditinpictures.library.Constants;

public class ConstsFree extends Constants {
    public static final String ADMOB_ID            = "a14fd4522953241";
    public static final String DIALOG_UPGRADE      = "Upgrade";
    public static final String MARKET_INTENT       = "market://details?id=";
    public static final String REMOVE_ADS          = "removeAds";
    public static final String PRO_VERSION_PACKAGE = "com.antew.redditinpictures.pro";
    private static Integer mActionBarSize;

    public static int getActionBarSize(Context context) {
        if (mActionBarSize == null) {
            final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                    new int[] { android.R.attr.actionBarSize });
            mActionBarSize = (int) styledAttributes.getDimension(0, 0);
            styledAttributes.recycle();
        }
        return mActionBarSize;
    }
}
