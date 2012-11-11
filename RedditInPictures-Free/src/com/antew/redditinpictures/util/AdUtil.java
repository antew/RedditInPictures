package com.antew.redditinpictures.util;

import com.google.ads.AdRequest;

public class AdUtil {
    
    public static AdRequest getAdRequest() {
        AdRequest adRequest = new AdRequest();
        adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
        adRequest.addTestDevice("F1B753A6DEEED0887495F26281CCDDA8");
        adRequest.addTestDevice("3F7DDA2355EFDDDC846D539EDEE73AFD");
        return adRequest;
    }
}
