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
