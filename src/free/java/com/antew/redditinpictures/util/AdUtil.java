package com.antew.redditinpictures.util;

import com.google.ads.AdRequest;

public class AdUtil {
    
    public static AdRequest getAdRequest() {
        AdRequest adRequest = new AdRequest();
        adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
        adRequest.addTestDevice("F1B753A6DEEED0887495F26281CCDDA8");
        adRequest.addTestDevice("3F7DDA2355EFDDDC846D539EDEE73AFD");
        adRequest.addTestDevice("1B09EC5B49C938E43B2E18F10DBBB90B");
        adRequest.addTestDevice("EB9E2DFDF8AB7AAFB29C8E6F225F749C");
        adRequest.addTestDevice("F17DF10B08F6975718BB6A3218756F45");

        return adRequest;
    }
}
